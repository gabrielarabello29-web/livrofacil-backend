# Integração front-end / back-end: chat LivroFácil

Os endpoints abaixo usam o prefixo `/api`. IDs de cliente, sessão, mensagem e feedback são UUIDs; IDs de categorias e livros são numéricos.

> **Limitação de autenticação:** esta entrega não implementa autenticação de servidor. `clienteId` é um identificador recebido do chamador, não uma prova de identidade. O back-end verifica a existência do cliente e confere os vínculos entre cliente, sessão, resposta e feedback, mas não consegue confirmar que o chamador é aquele cliente nem validar seu perfil.

## Enviar mensagem

`POST /api/chat/mensagens`

```json
{
  "clienteId": "c1baea49-b8e8-4903-b272-2bd3264ca75a",
  "sessaoId": null,
  "mensagem": "Quero um romance com mistério"
}
```

Informe `sessaoId` como `null` para criar uma sessão ou como UUID para continuar uma sessão do mesmo cliente.

```json
{
  "sessaoId": "1807d74e-d19a-43d6-a66a-cfb62e1928e1",
  "mensagemId": "740693ee-8044-4a80-8c31-880f7c030708",
  "texto": "Estas opções combinam romance e mistério.",
  "livros": [
    {
      "livroId": 123,
      "titulo": "Título",
      "autor": "Nome do autor",
      "imagemUrl": "https://exemplo.com/capa.jpg",
      "preco": 39.9,
      "url": "/livros/123"
    }
  ]
}
```

A conversa é salva em `chat_sessao` e `chat_mensagem`; as respostas associam os livros válidos retornados pelo catálogo ativo e com estoque disponível. O contexto enviado ao Vertex AI contém a mensagem atual, até 20 mensagens da sessão, preferências salvas, títulos/autores/categorias de pedidos `ENTREGUE`/`FINALIZADO` e metadados do catálogo. O `clienteId` e os dados cadastrais do cliente não são incluídos nesse contexto; evite inserir dados pessoais ou credenciais nas mensagens. Se não houver preferências nem compras, o catálogo continua disponível e o assistente pode perguntar sobre interesses.

## Registrar ou atualizar feedback

`POST /api/chat/feedback`

```json
{
  "clienteId": "c1baea49-b8e8-4903-b272-2bd3264ca75a",
  "mensagemId": "740693ee-8044-4a80-8c31-880f7c030708",
  "avaliacao": "UTIL",
  "comentario": ""
}
```

`avaliacao` aceita `UTIL` ou `NAO_UTIL`. A restrição única por cliente/resposta atualiza um voto existente em vez de duplicá-lo; `criadoEm` permanece a data da criação original.

```json
{
  "feedbackId": "104f0bd4-b651-497c-b1ae-19bece941ad4",
  "mensagemId": "740693ee-8044-4a80-8c31-880f7c030708",
  "avaliacao": "UTIL",
  "criadoEm": "2026-11-19T12:00:00Z"
}
```

## Preferências literárias

`GET /api/clientes/{clienteId}/preferencias-literarias`

Quando ainda não existem preferências, retorna listas vazias e preços `null`.

`PUT /api/clientes/{clienteId}/preferencias-literarias`

```json
{
  "categoriaIds": [12, 34],
  "temasDeInteresse": ["mistério", "ficção histórica"],
  "precoMinimo": null,
  "precoMaximo": 80
}
```

O PUT retorna os mesmos campos e valores. Os IDs são conferidos em `categoria`; preços negativos, categorias inexistentes ou preço máximo inferior ao mínimo retornam `400`.

## Integração generativa e erros

O chat usa Gemini pelo Vertex AI e Application Default Credentials (ADC); a aplicação não utiliza chaves de API para essa integração.

Configure no ambiente do servidor:

- `GOOGLE_CLOUD_PROJECT`: ID do projeto com Vertex AI habilitado (obrigatório).
- `GOOGLE_CLOUD_LOCATION`: localização do Vertex AI (opcional; padrão `us`, multi-região dos EUA).
- `GEMINI_MODEL`: ID do modelo (opcional; padrão `gemini-3.5-flash-lite`).
- `GOOGLE_APPLICATION_CREDENTIALS`: caminho para credenciais locais de desenvolvimento, se ADC não estiver disponível por identidade gerenciada.

Para desenvolvimento local, crie/ative ADC e garanta que o principal tenha permissão de inferência no projeto. Para produção, prefira a identidade de serviço do ambiente ou Workload Identity; não salve arquivos JSON de credenciais no repositório nem os envie ao front-end.

Ative a API Vertex AI (`aiplatform.googleapis.com`) no projeto e conceda à identidade usada pela aplicação a função `roles/aiplatform.user` (ou uma função personalizada com a permissão de previsão necessária). O cliente usa `generateContent` no endpoint correspondente à localização configurada e solicita saída JSON via `generationConfig.responseMimeType`. O padrão `us` usa o endpoint multi-região dos EUA, compatível com `gemini-3.5-flash-lite`; `global` usa o endpoint global, `eu` o endpoint multi-região europeu e regiões padrão usam o host regional.

Projeto ou credenciais ADC ausentes retornam `503` no envelope padrão de erro. Falhas HTTP do Vertex AI ou respostas inválidas do Gemini retornam `502`. IDs inexistentes ou vínculos de outro cliente retornam `404`; validação de payload e categorias retorna `400`. A resposta de erro mantém os campos `status`, `erro`, `mensagem`, `erros`, `caminho` e `timestamp`.

O front-end pode consultar categorias em `GET /api/livros/opcoes/categorias`. A migração Flyway `V6__chat_recomendacao_personalizada.sql` cria as tabelas para sessão, mensagens, feedback e preferências.
