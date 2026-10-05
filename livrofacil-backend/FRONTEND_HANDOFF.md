# Handoff de integracao: LivroFacil Frontend

Este documento resume as alteracoes realizadas no backend e orienta a integracao do `livrofacil-frontend`. Foi escrito para servir como contexto da IA da equipe de frontend. Os contratos abaixo refletem o codigo atual do backend neste workspace; nao representam uma validacao contra o Supabase nem contra o frontend em execucao.

## Resumo

- O backend agora implementa solicitacao e administracao de trocas, com emissao de voucher apos o recebimento do item.
- O checkout agora expoe validacao de cupons e permite aplicar um cupom ou voucher a um pedido ja iniciado, recalculando o total no servidor. Validar/aplicar nao consome o cupom; uso promocional e registrado apenas quando a finalizacao do pedido tem sucesso.
- PedidoResponse agora inclui os pagamentos registrados, para a tela de pedidos administrativos.
- O cadastro/edicao de livros, o checkout de pedidos e a listagem administrativa ja existiam no backend. Os testes desses fluxos passaram; nao foi necessario criar outro CRUD de livros.
- O frontend ainda precisa integrar as novas chamadas e adequar os payloads/estado de pagamento indicados abaixo.

## Trocas

### Solicitacao

`POST /api/trocas`

Payload obrigatorio:

```json
{
  "clienteId": "UUID-do-cliente",
  "pedidoId": 123,
  "itemPedidoId": 456,
  "motivo": "Produto danificado",
  "detalhes": "Opcional"
}
```

Em vez de `itemPedidoId`, pode-se enviar `livroId`. O alias `produtoId` tambem e aceito para esse campo. Se o pedido possuir mais de uma linha do mesmo livro, envie `itemPedidoId` para identificar a linha sem ambiguidade.

Regras implementadas:

- `clienteId` e `pedidoId` sao obrigatorios. `clienteId` e UUID enviado como string JSON; `pedidoId`, `itemPedidoId` e `livroId` sao numeros.
- O pedido precisa pertencer ao cliente e estar com status `ENTREGUE`.
- O item precisa fazer parte daquele pedido.
- Um item nao pode ter outra troca `SOLICITADA`, `AUTORIZADA` ou `TROCADA`. Trocas `RECUSADA` podem ser solicitadas novamente.
- Sucesso retorna HTTP 201 e um objeto de troca.

### Administracao da troca

- `GET /api/trocas`: lista todas as trocas.
- `GET /api/trocas/cliente/{clienteId}`: lista trocas do cliente.
- `PATCH /api/trocas/{id}/autorizar`: somente troca `SOLICITADA`.
- `PATCH /api/trocas/{id}/recusar`, body `{"motivo":"Justificativa"}`: somente troca `SOLICITADA`; motivo obrigatorio.
- `PATCH /api/trocas/{id}/receber`, body opcional `{"retornarEstoque":true}`: somente troca `AUTORIZADA`.

Estados retornados: `SOLICITADA`, `AUTORIZADA`, `RECUSADA`, `TROCADA`.

Se `retornarEstoque` for `true`, a quantidade do item volta ao estoque e a quantidade vendida e decrementada. Se nao existir estoque para o livro, o recebimento falha em vez de emitir um voucher sem concluir a operacao.

### Voucher de troca

- Emitido quando a troca autorizada e recebida; nao e emitido na solicitacao nem na autorizacao.
- O objeto da troca inclui `voucherCodigo` e `voucherValor`; tambem inclui `voucherResgatadoEm`, inicialmente `null`.
- O codigo segue `TR-` + 16 caracteres hexadecimais em maiusculas. Exemplo de formato: `TR-1A2B3C4D5E6F7890`.
- O valor e o preco unitario historico da compra multiplicado pela quantidade da linha do pedido.
- O voucher pertence ao cliente e nao tem expiracao implementada. O saldo e reutilizavel em compras parciais; a validacao nao altera nem consome o voucher. Ele so e marcado como usado quando o saldo chega a zero.
- Aplicar em um pedido de outro cliente ou tentar reutilizar um voucher ja resgatado e rejeitado.
- O credito descontado e limitado ao subtotal do pedido. Na finalizacao, o backend subtrai do voucher apenas o valor efetivamente utilizado; saldo restante continua disponivel e `voucherResgatadoEm` so e preenchido quando chega a zero. `voucherValor` na resposta da troca representa o saldo atual.
- Nao existe endpoint exclusivo de busca de voucher; o codigo e retornado pela resposta da troca e validado pelo endpoint de cupons.

Formato resumido de resposta da troca:

```json
{
  "id": 7,
  "pedidoId": 123,
  "itemPedidoId": 456,
  "livroId": 89,
  "titulo": "Titulo do livro",
  "quantidade": 1,
  "valorOriginal": 49.90,
  "motivo": "Produto danificado",
  "detalhes": null,
  "motivoRecusa": null,
  "status": "TROCADA",
  "criadoEm": "...",
  "voucherCodigo": "TR-1A2B3C4D5E6F7890",
  "voucherValor": 49.90,
  "voucherResgatadoEm": null
}
```

## Cupons no checkout

### Validacao

`POST /api/cupons/validar`

```json
{"codigo":"DESC10"}
```

Cupom percentual valido:

```json
{
  "valido": true,
  "tipo": "PERCENTUAL",
  "valor": 10,
  "codigo": "DESC10",
  "mensagem": "Cupom valido"
}
```

Voucher valido usa `"tipo":"FIXO"` e `valor` monetario. Codigo invalido, inativo, voucher ja consumido ou voucher sem saldo retorna `valido: false`; validacao e somente leitura e nao marca o voucher como utilizado nem incrementa uso de cupom. O endpoint e `POST`, nao ha `GET /api/cupons` nem filtros de listagem de cupons implementados.

### Aplicacao

`POST /api/pedidos/{pedidoId}/cupons?clienteId={uuid}`

```json
{"codigo":"DESC10"}
```

O `clienteId` e obrigatorio na query e deve identificar o dono do pedido. O endpoint retorna o PedidoResponse atualizado, incluindo `subtotal`, `desconto`, `total` e `cupom`. A aplicacao exige que o pedido continue em checkout e que a reserva nao tenha expirado.

A validacao do codigo nao calcula o total. O total muda na aplicacao ao pedido, nao na validacao. Para cupom percentual, o desconto e calculado sobre o subtotal; para voucher fixo, usa-se o menor entre valor do voucher e subtotal.

### Pagamento apos desconto

O frontend deve atualizar a divisao dos pagamentos com o campo `total` retornado pela aplicacao do cupom. Na finalizacao, a soma dos pagamentos deve ser igual ao total atualizado. Se o voucher zerar o total, envie `"pagamentos":[]`; se houver saldo positivo, informe uma ou mais formas de pagamento, respeitando as validacoes existentes de cartao e parcelamento.

## Compra e painel administrativo

- `POST /api/pedidos/iniciar` persiste um pedido/reserva em checkout, com status inicial `EM_CHECKOUT`, itens, subtotal, desconto, total e reserva de estoque. A reserva expira conforme `livrofacil.checkout.expiracao-minutos` (padrao de 30 minutos).
- Status aceitos pelo dominio: `PENDENTE`, `AGUARDANDO_PAGAMENTO`, `EM_CHECKOUT`, `EM_PROCESSAMENTO`, `PAGAMENTO_APROVADO`, `EM_SEPARACAO`, `NA_TRANSPORTADORA`, `EM_ROTA_DE_ENTREGA`, `ENTREGUE`, `FINALIZADO`, `CANCELADO`.
- `PedidoResponse.rastreamento` retorna `etapa` (status atual) e `ultimaModificacao` (data/hora da ultima alteracao). Use esses campos nos detalhes para apresentar etapa atual e ultima atualizacao. Nao ha historico persistido de todas as transicoes.
- Finalizar com sucesso grava pagamentos, confirma a venda no estoque, esvazia o carrinho e retorna `EM_PROCESSAMENTO`.
- `GET /api/pedidos/admin` lista pedidos persistidos. `GET /api/pedidos/admin/{pedidoId}` retorna detalhe. A resposta do pedido inclui `id`, `clienteId`, `cliente`, `status`, datas, subtotal, desconto, total, cupom, enderecos, itens e pagamentos.
- Cada item inclui `id`, `pedidoId`, `livroId`, `quantidade` e `valorUnitario`.
- Cada pagamento inclui `id`, `formaPagamentoId`, `valor`, `parcelas`, `bandeira` e `ultimosDigitos`. Nao sao retornados titular nem validade do cartao.

## Escopo de devolucoes e trocas

O fluxo existente permite ao cliente solicitar troca de um item de pedido entregue, ao administrador listar e autorizar/recusar, e registrar o recebimento. No recebimento, pode opcionalmente retornar o item ao estoque e o backend emite voucher pelo valor historico do item. Estados: `SOLICITADA`, `AUTORIZADA`, `RECUSADA`, `TROCADA`. Nao estao implementados reembolso em dinheiro, envio automatico de substituto, etiqueta/logistica reversa ou prazo configuravel. A interface deve oferecer somente as operacoes disponiveis.

## Livros

O CRUD ja existe. Criar com `POST /api/livros`, editar com `PUT /api/livros/{id}`. O cadastro recebe relacoes por IDs e requer capa como URL HTTP(S), nao upload.

Exemplo de payload:

```json
{
  "codigo": "LIVRO-001",
  "titulo": "Titulo",
  "ano": 2024,
  "edicao": 1,
  "isbn": "9780000000000",
  "numeroPaginas": 200,
  "sinopse": "Sinopse do livro",
  "imagemUrl": "https://exemplo.com/capa.jpg",
  "codigoBarras": "7890000000000",
  "valorVenda": 49.90,
  "ativo": true,
  "autorId": 1,
  "editoraId": 1,
  "grupoPrecificacaoId": 1,
  "categoriaIds": [1],
  "dimensao": {
    "altura": 20,
    "largura": 14,
    "profundidade": 2,
    "peso": 0.4
  }
}
```

Os campos textuais/codigos, inteiros positivos, IDs e dimensoes sao validados; codigo, ISBN e codigo de barras devem ser unicos. O livro precisa ter ao menos uma categoria. Opcoes estao em `GET /api/livros/opcoes/autores`, `/editoras`, `/categorias` e `/grupos-precificacao`. A resposta administrativa de livro inclui IDs e nomes das relacoes e o estoque. Atualizacao de estoque e separada em `PUT /api/livros/{id}/estoque`.

## Autenticacao, IDs e CORS

- Este backend nao exige token/cookie e nao implementa controle de acesso por perfil para endpoints administrativos. O frontend nao deve tratar a protecao visual de rota como seguranca do servidor.
- `clienteId` e UUID (string). IDs de pedido, carrinho, livro, item, endereco e forma de pagamento sao numericos.
- CORS permite `http://localhost:5173` em `/api/**`, metodos `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS` e headers `Content-Type`/`Accept`. `allowCredentials` e `false`; `Authorization` nao esta permitido na configuracao atual.
- Chamadas diretas para `http://localhost:8080/api` sao permitidas no ambiente local indicado; o proxy Vite `/api` tambem pode ser usado.

## Arquivos do backend relevantes

- Trocas: `src/main/java/com/livrofacil/modulos/troca/controller/TrocaController.java`, `service/TrocaService.java`, entidades `Troca` e `VoucherTroca`.
- Cupons e pedidos: `src/main/java/com/livrofacil/modulos/compra/controller/CupomController.java`, `service/CupomService.java`, `service/PedidoService.java` e `dto/PedidoResponse.java`.
- Livros: `src/main/java/com/livrofacil/modulos/livro/controller/LivroController.java`, `service/CadastrarLivroUseCase.java`, `dto/LivroRequest.java`.

## Validacao executada

Os testes de unidade direcionados e a suite sem o teste `@SpringBootTest` passaram: 160 testes, zero falhas/erros. O teste de contexto completo nao foi executado porque o datasource configurado aponta para banco remoto. A persistencia real das novas entidades e a integracao ponta a ponta precisam ser verificadas em ambiente de desenvolvimento apropriado.

Nesta atualizacao foram executados 28 testes direcionados de `PedidoServiceTest`, `CupomServiceTest` e `TrocaServiceTest`, todos passando. A suite completa e o teste de contexto Spring nao foram executados nesta alteracao.

## Prompt pronto para a IA do frontend

```text
Leia o arquivo FRONTEND_HANDOFF.md do backend como contrato atual. Inspecione o fluxo ativo do frontend, sem alterar fluxos legados. Integre: (1) solicitacao/listagem de troca do cliente e acoes administrativas de autorizar, recusar e receber; (2) exibicao de `voucherCodigo`, saldo atual em `voucherValor` e `voucherResgatadoEm`, considerando o voucher usado somente quando o saldo zera; (3) validacao e aplicacao de cupom/voucher no checkout, sem consumir ao validar/aplicar, enviando `clienteId` na query e recalculando pagamentos pelo `total` retornado; (4) nos detalhes do pedido, exiba rastreamento com `rastreamento.etapa` e `rastreamento.ultimaModificacao` (nao ha historico de eventos); (5) admin: pagamentos e dados de pedido retornados; (6) nao prometa reembolso em dinheiro nem envio automatico de substituto, pois nao existem na API. CRUD de livros ja existe; integre os endpoints descritos. Antes de editar, liste arquivos/rotas, incompatibilidades e plano curto; depois implemente o necessario, teste e reporte arquivos alterados, testes e pendencias. Nao invente GET /cupons, upload de capa, autenticacao por token ou validade de voucher.
```
