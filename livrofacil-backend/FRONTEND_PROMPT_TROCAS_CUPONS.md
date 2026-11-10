# PROMPT FRONTEND — LIVROFÁCIL | IMPLEMENTAÇÃO COMPLETA DE TROCAS, DEVOLUÇÕES E CUPONS

Você é responsável por implementar as telas React para o fluxo de trocas, devoluções e cupons do LivroFácil. Este prompt descreve o backend atualmente implementado. Não invente endpoints, propriedades, estados ou autenticação.

## 1. Auditoria obrigatória antes de alterar

Analise o frontend existente antes de escrever código: rotas; páginas de cliente e administração; pedidos e detalhes de pedidos; checkout; clients/services HTTP; componentes compartilhados; estado global/hooks; tratamento de erros; perfil/identidade do cliente; padrões visuais; testes Cypress e configuração. Reaproveite arquitetura, estilos e componentes. Confirme como o cliente atual obtém seu `clienteId`: o backend ainda não fornece sessão/token autenticado.

Não faça alterações no backend. Não alegue que chamadas estão autenticadas: os endpoints atuais usam `clienteId` informado pela interface e as rotas administrativas não têm autorização técnica de perfil.

## 2. Regras funcionais implementadas

- `tipo`: `TROCA` ou `DEVOLUCAO`.
- `status`: `SOLICITADA`, `AGUARDANDO_RECEBIMENTO`, `RECEBIDA`, `CONCLUIDA`, `RECUSADA`; valores legados `AUTORIZADA` e `TROCADA` continuam possíveis em registros antigos.
- Uma solicitação só pode ser criada para pedido `ENTREGUE` que tenha `entregueEm` persistido. A data antiga não é retroativamente inventada; pedidos antigos sem a data devem mostrar que não são elegíveis e orientar a procurar suporte.
- O prazo é baseado na data local de entrega e permite solicitar até o fim do sétimo dia corrido, no fuso `America/Sao_Paulo`.
- `quantidade` pode ser parcial, mas precisa ser positiva, não exceder quantidade comprada, e as unidades não podem ser simultaneamente comprometidas em solicitações abertas/concluídas.
- Admin autoriza, recusando com motivo, ou confirma recebimento. A aprovação muda para `AGUARDANDO_RECEBIMENTO`, não emite crédito. O endpoint de recebimento conclui o fluxo, registra histórico, pode repor estoque e emite um voucher uma única vez.
- Reentrada ao estoque se aplica somente à quantidade recebida e é definida no recebimento.
- Crédito é calculado pelo preço histórico bruto unitário vezes a quantidade recebida, excluindo frete. **Limitação:** o pedido guarda desconto global, sem rateio por item; portanto o backend não calcula crédito líquido de desconto por produto. Não mostre o valor como “valor líquido pago”. É crédito em cupom, não estorno financeiro.
- No checkout há no máximo um cupom. Voucher abate somente `subtotal` de produtos, nunca frete. O saldo não usado de voucher de troca gera voucher residual na confirmação do checkout. Frete restante precisa ser pago por outro meio; se a transação falha, a transação do backend reverte o consumo e a emissão.
- Promoções e vouchers são os dois tipos de benefício reconhecidos. A interface não deve permitir combiná-los.
- Aplicar `clienteId` nos caminhos/queries exatamente como indicado abaixo, usando o usuário selecionado pela arquitetura já existente. Isso não é prova de titularidade no servidor: a API não tem principal autenticado atualmente.

## 3. Contratos HTTP reais

Base URL depende da configuração existente do frontend. Todos os endpoints abaixo não exigem bearer token no backend atual. Usar `Content-Type: application/json` nos requests JSON.

### Troca/devolução pelo cliente

**Criar solicitação** — `POST /api/trocas`, `201 Created`.

Request obrigatório:

```json
{
  "clienteId": "UUID do cliente",
  "pedidoId": 123,
  "itemPedidoId": 456,
  "tipo": "DEVOLUCAO",
  "quantidade": 1,
  "motivo": "Produto danificado",
  "detalhes": "Descrição opcional"
}
```

Em lugar de `itemPedidoId`, pode-se informar `livroId` (alias legado `produtoId`) para seleção. Prefira `itemPedidoId`, principalmente quando houver linhas distintas do mesmo livro. `tipo` e `quantidade` são obrigatórios; `motivo` não pode estar vazio; `pedidoId` positivo e `clienteId` UUID.

Response `TrocaResponse` contém `id`, `pedidoId`, `clienteId`, `clienteNome`, `pedidoStatus`, `entregueEm`, `itemPedidoId`, `livroId`, `titulo`, `quantidade` (quantidade total comprada), `quantidadeSolicitada`, `quantidadeRecebida`, `valorOriginal`, `motivo`, `detalhes`, `motivoRecusa`, `status`, `criadoEm`, `tipo`, `decididoEm`, `recebidoEm`, `creditoEm`, `retornarEstoque`, `voucherCodigo`, `voucherValor`, `voucherResgatadoEm`.

**Listar solicitações do cliente** — `GET /api/trocas/cliente/{clienteId}`, `200`, array de `TrocaResponse`.

**Detalhe do cliente** — `GET /api/trocas/cliente/{clienteId}/{trocaId}`, `200`, `TrocaResponse`. A API verifica que a solicitação pertence ao `clienteId` fornecido.

**Histórico do cliente** — `GET /api/trocas/cliente/{clienteId}/{trocaId}/historico`, `200`, array:

```json
[{"id": 8, "trocaId": 123, "status": "AGUARDANDO_RECEBIMENTO", "observacao": "...", "criadoEm": "2026-10-19T15:10:00"}]
```

A observação da rejeição contém a justificativa. A trilha inclui solicitação, aprovação/rejeição, recebimento e conclusão/crédito.

### Cupons/vouchers do cliente

**Listar vouchers de troca** — `GET /api/trocas/cliente/{clienteId}/vouchers`, `200`, array de `VoucherTrocaResponse`.

**Detalhe do voucher** — `GET /api/trocas/cliente/{clienteId}/vouchers/{voucherId}`, `200`, `VoucherTrocaResponse`, com verificação do `clienteId` informado.

Campos: `id`, `codigo`, `valor` (saldo atual), `saldo` (mesmo saldo), `valorOriginal`, `tipo` (`TROCA`), `status` (`DISPONIVEL` ou `UTILIZADO`), `trocaId`, `voucherOrigemId`, `criadoEm`, `resgatadoEm`, `pedidoResgateId`. Voucher promocional não integra essa listagem; é um endpoint de vouchers de troca. Não existe expiração/cancelamento de voucher modelado atualmente.

### Checkout/cupom

**Validar código** — `POST /api/cupons/validar`, request `{ "codigo": "TR-..." }`, response `200` com `{ "valido": true, "tipo": "...", "valor": 80.00, "codigo": "...", "mensagem": "..." }`. O endpoint recebe apenas código, então a validação final de titularidade e valores acontece no pedido.

**Iniciar compra** — `POST /api/pedidos/iniciar` (já existente). Inclui `clienteId`, `carrinhoId`, `enderecoEntregaId`, `enderecoCobranca` e opcionalmente um único `cupom` string.

**Aplicar/trocar cupom no pedido** — `POST /api/pedidos/{pedidoId}/cupons?clienteId={uuid}`, body `{ "codigo": "..." }`, response `200` `PedidoResponse`. Não enviar uma lista/array de códigos.

**Finalizar** — `POST /api/pedidos/{pedidoId}/finalizar?clienteId={uuid}`, body existente do checkout com `carrinhoId` e `pagamentos` (lista de cartões). O backend valida total e pagamentos. Não remova o fluxo existente de vários cartões.

O pedido inclui `subtotal`, `desconto`, `frete`, `total`, `cupom`, `status`, `entregueEm`, `itens` e `voucherSaldoGerado` (quando houver saldo residual emitido na resposta da finalização). Mostrar produtos, desconto do cupom e frete separadamente. O valor da resposta é a fonte de verdade; não recalcule dinheiro no cliente.

### Administração — trocas/devoluções

**Fila com filtros opcionais** — `GET /api/trocas?status=SOLICITADA&tipo=DEVOLUCAO`, `200`, array. `status` e `tipo` podem ser omitidos.

**Detalhe** — `GET /api/trocas/{trocaId}`, `200`, `TrocaResponse`.

**Histórico** — `GET /api/trocas/{trocaId}/historico`, `200`, array `TrocaHistoricoResponse`.

**Autorizar** — `PATCH /api/trocas/{trocaId}/autorizar`, sem body, `200`, `TrocaResponse`. Novo status `AGUARDANDO_RECEBIMENTO`; não mostrar cupom nesse estágio.

**Rejeitar** — `PATCH /api/trocas/{trocaId}/recusar`, body `{ "motivo": "Justificativa obrigatória" }`, `200`, `TrocaResponse`.

**Confirmar recebimento e concluir** — `PATCH /api/trocas/{trocaId}/receber`, body:

```json
{"retornarEstoque": true, "quantidadeRecebida": 1}
```

`retornarEstoque` default `false`; se `quantidadeRecebida` for omitida, usa toda a quantidade aprovada. Se enviada, deve ser positiva e não maior que a solicitada. `200`, `TrocaResponse` com status `CONCLUIDA`, `creditoEm`, `voucherCodigo` e `voucherValor`. O cupom só é emitido aqui, nunca pelo frontend.

Essas rotas administrativas existem, mas o backend ainda não aplica autorização/perfil. Não descreva UI como protegida por controle servidor. Se o frontend existente esconder controles por perfil, isso é somente apresentação.

### Atualização de status do pedido/entrega

`PATCH /api/pedidos/admin/{pedidoId}/status`, body `{ "status": "ENTREGUE" }`, `200` `PedidoResponse`. A primeira transição para `ENTREGUE` grava `entregueEm` no fuso da aplicação. Pedidos já `ENTREGUE` e sem timestamp não são corrigidos retroativamente e ficam inelegíveis.

## 4. Formato de erro

Usar a estrutura global existente, por exemplo:

```json
{
  "status": 409,
  "erro": "Conflict",
  "mensagem": "O prazo de 7 dias corridos para solicitar troca ou devolucao expirou",
  "erros": {},
  "caminho": "/api/trocas",
  "timestamp": "2026-10-19T15:10:00"
}
```

Validação bean normalmente retorna HTTP 400 com `erros` por campo. Recurso não encontrado: 404. Regra de negócio/transição/prazo: 409. Erro de parsing/parâmetro: 400. Tratar também respostas de servidor e falhas de rede sem perder o conteúdo da API.

## 5. Telas e arquivos a implementar (adaptar nomes à estrutura encontrada)

### Cliente

- **Meus pedidos/detalhe:** mostrar “Solicitar troca/devolução” quando status entregue, `entregueEm` existir e a data atual no fuso da aplicação não tiver passado de `entregueEm + 7 dias`. API continua sendo autoridade; bloquear pedidos sem timestamp antigo. Formulário seleciona linha do pedido, quantidade disponível, tipo, motivo e detalhes opcionais.
- **Minhas solicitações:** listar pelo endpoint cliente, filtro client-side por status/tipo se padrões atuais não tiverem filtros, detalhe e histórico, justificativa, dados de recebimento e crédito quando concluído.
- **Meus cupons:** listar vouchers com código, saldo/valor original, status, origem (`trocaId`, `voucherOrigemId`), emissão e pedido de resgate. Copiar código. Não inferir expiração.
- **Checkout:** um seletor único e exclusivo, remover o benefício atual antes de aplicar outro. Exibir subtotal elegível de produtos, desconto e frete. Explicar que voucher não paga frete e que eventual residual é emitido pelo servidor após a finalização bem-sucedida. Continuar exigindo pagamento por cartão do total ainda devido e manter cartões múltiplos.

### Administração

- Gestão com lista/filtros, cliente, pedido, entrega, tipo, item/quantidade, motivo, datas, status, justificativa e histórico.
- Permitir autorizar ou rejeitar apenas em `SOLICITADA`; justificativa obrigatória para rejeição.
- Em `AGUARDANDO_RECEBIMENTO`, informar quantidade efetivamente recebida e opção de retornar ao estoque. Não permitir receber mais do que solicitado.
- Após resposta `CONCLUIDA`, mostrar crédito/voucher. Não fabricar nem emitir cupom na interface.

### Integração

Criar/reutilizar service functions para os contratos acima; hooks para carregar, enviar, atualizar e invalidar listas/detalhes; tratar loading, empty, error e sucesso; evitar envio duplicado com estado pending; manter design e roteamento atuais. As URLs precisam ser construídas com o client/axios/fetch já adotado pelo projeto.

## 6. Testes frontend obrigatórios

Escrever Cypress para: botão elegível e indisponível (não entregue, sem `entregueEm`, fora dos sete dias); envio dentro do prazo; item/quantidade parcial e inválida; erro de prazo/API; duplicidade; listagem/detalhe/histórico; decisão admin e justificativa; recebimento parcial, estoque e exibição de voucher só após conclusão; um só cupom no checkout; frete sempre separado; saldo residual vindo da API; pagamento do frete e manutenção dos cartões; erro de titularidade/consumo e regressão do checkout.

Diferenciar testes Cypress com `cy.intercept`/mocks de testes e2e integrados ao backend real. Não dizer que um teste com mock valida banco, segurança ou transação.

## 7. Checklist de entrega

- [ ] Auditoria das rotas/pages/services/testes existentes concluída antes da edição.
- [ ] Cliente: elegibilidade, formulário, listagem, detalhe/histórico e cupons.
- [ ] Admin: listagem, filtros, aprovar, rejeitar, receber e consultar voucher.
- [ ] Checkout mantém pagamentos e permite no máximo um cupom.
- [ ] Frete é mostrado e cobrado separadamente do voucher.
- [ ] Nenhum crédito é criado na UI.
- [ ] Campos/estados/URLs são exatamente os descritos neste contrato.
- [ ] Cobertura de loading/empty/errors/permissão visual e regressões.
- [ ] Cypress mock e testes integrados são reportados separadamente.
- [ ] Limitações registradas: ausência de autorização backend; crédito não desconta descontos globais do pedido; pedidos legados sem entrega registrada são inelegíveis; vouchers atualmente sem expiração/cancelamento.
