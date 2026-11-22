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

## Exportar dataset de treinamento (`JSONL`)

`GET /api/chat/dataset.jsonl`

Gera e baixa 100 exemplos de treino em `livrofacil-dataset-treino.jsonl`.
`GET /api/chat/dataset/validation.jsonl` baixa 20 exemplos de validação em
`livrofacil-dataset-validacao.jsonl`. Ambos usam JSON Lines no formato aceito pelo ajuste
supervisionado Gemini no Vertex AI (`systemInstruction` e `contents`, com papéis `user`
e `model`). A entrada usa a estrutura de contexto do chat; a resposta é o objeto JSON
com `texto` e `livroIds`.

O arquivo de treino contém 15 guardrails e o de validação contém 3, totalizando 15% em
cada conjunto. Exemplos de feedback `UTIL` só são exportados quando a resposta possui
um snapshot válido do contexto enviado ao Gemini e os livros recomendados continuam no
catálogo ativo. Exemplos antigos sem snapshot são ignorados; o contexto não é reconstruído
com preferências ou compras atuais.

A exportação também inclui:

- respostas marcadas como `UTIL`;
- títulos e autores de pedidos concluídos (`ENTREGUE` ou `FINALIZADO`);
- recomendações, sinopses e dúvidas sobre obras disponíveis no catálogo;
- cenários de recomendação, sinopses, autoria e guardrail fora do escopo de livros.

Mensagens reais e snapshots podem conter dados pessoais no texto ou no histórico; os
arquivos precisam de revisão/anonimização manual antes de carregar em um bucket ou serviço
de treinamento.

Por padrão, as rotas exigem o cabeçalho `X-Dataset-Export-Token`. Configure
`DATASET_EXPORT_TOKEN` com um segredo forte. Para uma demonstração somente local, inicie
com o perfil Spring `demo`; esse perfil permite exportar sem token. Não use esse perfil
em servidor acessível pela internet e não inclua o token no frontend nem no repositório.
Sem token no perfil padrão, a exportação retorna `503`; com token ausente ou incorreto,
retorna `403`. Respostas `UTIL` antigas sem snapshot de contexto são excluídas do treino;
a migração V7 não preenche contexto retroativamente.

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

### Configurar variáveis no PowerShell (Windows)

As atribuições `$env:NOME = "valor"` valem apenas para a janela atual e para os processos iniciados por ela. Configure-as antes de iniciar o backend; ao fechar a janela, os valores deixam de existir. Substitua `SEU_PROJECT_ID` pelo ID do projeto Google Cloud:

```powershell
$env:GOOGLE_CLOUD_PROJECT = "SEU_PROJECT_ID"
$env:GOOGLE_CLOUD_LOCATION = "us"
$env:GEMINI_MODEL = "gemini-3.5-flash-lite"
Set-Location .\livrofacil-backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

O perfil `demo` permite baixar os datasets sem `DATASET_EXPORT_TOKEN`; use-o somente em desenvolvimento local. Fora desse perfil, defina `DATASET_EXPORT_TOKEN` no ambiente antes de iniciar a aplicação e envie o valor apenas no cabeçalho `X-Dataset-Export-Token`. Não coloque tokens, senhas ou arquivos de credenciais em comandos compartilhados, no Git ou no front-end. Reinicie o backend depois de alterar variáveis para que a aplicação receba os novos valores.

Para configurar um valor não secreto do projeto para futuras janelas do Windows, pode-se usar:

```powershell
[Environment]::SetEnvironmentVariable("GOOGLE_CLOUD_PROJECT", "SEU_PROJECT_ID", "User")
```

Essa configuração persistente só se aplica a novos processos; feche e abra um novo terminal antes de iniciar o backend.

Ative a API Vertex AI (`aiplatform.googleapis.com`) no projeto e conceda à identidade usada pela aplicação a função `roles/aiplatform.user` (ou uma função personalizada com a permissão de previsão necessária). O cliente usa `generateContent` no endpoint correspondente à localização configurada e solicita saída JSON via `generationConfig.responseMimeType`. O padrão `us` usa o endpoint multi-região dos EUA, compatível com `gemini-3.5-flash-lite`; `global` usa o endpoint global, `eu` o endpoint multi-região europeu e regiões padrão usam o host regional.

Projeto ou credenciais ADC ausentes retornam `503` no envelope padrão de erro. Falhas HTTP do Vertex AI ou respostas inválidas do Gemini retornam `502`. IDs inexistentes ou vínculos de outro cliente retornam `404`; validação de payload e categorias retorna `400`. A resposta de erro mantém os campos `status`, `erro`, `mensagem`, `erros`, `caminho` e `timestamp`.

O front-end pode consultar categorias em `GET /api/livros/opcoes/categorias`. A migração Flyway `V6__chat_recomendacao_personalizada.sql` cria as tabelas para sessão, mensagens, feedback e preferências.
