 Arquitetura — E-commerce de Livros

## 1. Visão geral

Projeto de um e-commerce de livros baseado nos requisitos fornecidos pelo professor.

### Tecnologias autorizadas

- **Web**
- **Back-end:** Java / Node.js / .NET
- **Front-end:** tecnologias que executam no navegador
- **Banco:** relacional
- **Arquitetura:** MVC
- **IA**

### Stack proposta

| Camada | Tecnologia |
|---|---|
| Front-end | React + Vite |
| Back-end | Java + Spring Boot |
| Banco de dados | PostgreSQL |
| Arquitetura | MVC |
| Comunicação | API REST / JSON |
| IA | API de IA generativa, como OpenAI |
| Autenticação | Planejada; ainda não implementada |

A escolha de Java + Spring Boot é adequada para concentrar as regras de negócio, disponibilizar a API REST e integrar o banco e a IA.

## Estado atual da implementação

O backend já está estruturado em módulos de domínio e segue a separação clássica de responsabilidades em camadas:

- `controller` para expor endpoints REST;
- `service` para regras de negócio;
- `repository` para acesso ao banco;
- `entity` para persistência JPA;
- `dto` para entrada e saída de dados;
- `exception` para padronização de erros;
- `config` para inicialização e suporte de infraestrutura.

A implementação atual do projeto evidencia que a arquitetura já está mais avançada do que uma estrutura inicial genérica. O código real implementa recursos concretos de cliente, livro, carrinho, compra e pagamento.

### Módulos já implementados no código

- `modulos.cliente`: `Cliente`, `Endereco`, `FormaPagamento`, `BandeiraPagamento`
- `modulos.livro`: `Livro`, `Autor`, `Categoria`, `Editora`, `Estoque`, `GrupoPrecificacao`, `Dimensao`
- `modulos.compra`: `Carrinho`, `ItemCarrinho`, `Pedido`, `ItemPedido`, `PagamentoPedido`, `Cupom`, `StatusPedido`
- `modulos.analise`: estrutura inicial de módulo de análise
- `modulos.estoque`: estrutura operacional do estoque
- `modulos.troca`: estrutura de trocas agrupada por domínio
- `modulos.venda`: estrutura de venda e fluxo comercial
- `ia`: módulo de integração com IA generativa
- `config`: `ClienteDataInitializer`, `EstoqueDataInitializer`, `CorsConfig`, `CheckoutSchemaMigration`
- `exception`: `GlobalExceptionHandler`, `RegraDeNegocioException`, `RecursoNaoEncontradoException`

### Entidades JPA implementadas no projeto

Abaixo estão as entidades efetivamente presentes hoje no código Java:

```text
Cliente
- cli_id (UUID)
- cli_numero_registro
- cli_nome
- cli_email
- cli_senha
- cli_cpf
- cli_telefone
- cli_data_cadastro
- cli_data_nascimento
- cli_genero
- cli_ativo
- cli_data_exclusao
- cli_dados_anonimizados
- cli_perfil

Endereco
- end_id
- end_tipo
- end_logradouro
- end_numero
- end_complemento
- end_bairro
- end_cidade
- end_estado
- end_cep
- end_principal
- cli_id (FK para Cliente)

FormaPagamento
- for_pag_id
- for_pag_nome_titular
- for_pag_tipo_cartao
- for_pag_ultimos_digitos
- for_pag_validade
- for_pag_bandeira
- for_pag_preferencial
- for_pag_ativo
- for_pag_criado_em
- cli_id (FK para Cliente)

BandeiraPagamento
- ban_pag_id
- ban_pag_nome
- ban_pag_disponivel

Livro
- liv_id
- liv_codigo
- liv_titulo
- liv_ano
- liv_edicao
- liv_isbn
- liv_numero_paginas
- liv_sinopse
- liv_imagem_url
- liv_codigo_barras
- liv_valor_venda
- liv_ativo
- aut_id (FK para Autor)
- edi_id (FK para Editora)
- grp_pre_id (FK para GrupoPrecificacao)
- liv_altura
- liv_largura
- liv_profundidade
- liv_peso

Autor
- aut_id
- aut_nome

Categoria
- cat_id
- cat_nome

Editora
- edi_id
- edi_nome

GrupoPrecificacao
- grp_pre_id
- grp_pre_nome
- grp_pre_percentual_margem

Estoque
- est_id
- liv_id (FK unica para Livro)
- est_quantidade_disponivel
- est_quantidade_bloqueada
- est_quantidade_vendida

Carrinho
- car_id
- car_token
- cli_id (FK opcional para Cliente)
- car_atualizado_em

ItemCarrinho
- ite_car_id
- car_id (FK para Carrinho)
- liv_id (FK para Livro)
- ite_car_quantidade

Pedido
- ped_id
- cli_id (FK para Cliente)
- car_id (FK para Carrinho)
- ped_checkout_chave
- ped_status
- ped_criado_em
- ped_atualizado_em
- ped_reserva_expira_em
- ped_subtotal
- ped_desconto
- ped_total
- ped_cupom
- ped_entrega_endereco
- ped_cobranca_endereco

ItemPedido
- ite_ped_id
- ped_id (FK para Pedido)
- liv_id (FK para Livro)
- ite_ped_titulo
- ite_ped_quantidade
- ite_ped_valor_unitario

PagamentoPedido
- pag_ped_id
- ped_id (FK para Pedido)
- for_pag_id (FK para FormaPagamento)
- pag_ped_valor
- pag_ped_parcelas

Cupom
- cup_id
- cup_codigo
- cup_percentual_desconto
- cup_ativo
```

A estrutura atual mostra que o projeto não é apenas um esqueleto conceitual; ele já contém entidades concretas de domínio e regras de negócio aplicadas em camada de serviço.

---

# 2. Arquitetura geral

```text
                    ┌─────────────────────────┐
                    │       FRONT-END         │
                    │      React + Vite       │
                    │                         │
                    │  Páginas / Componentes  │
                    │  Chatbot / Gráficos     │
                    └────────────┬────────────┘
                                 │
                            HTTP / REST
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │        BACK-END         │
                    │      Java + Spring      │
                    │                         │
                    │          MVC            │
                    │                         │
                    │ Controller              │
                    │      ↓                  │
                    │ Service                │
                    │      ↓                  │
                    │ Repository             │
                    └───────┬─────────┬───────┘
                            │         │
                            ▼         ▼
                   ┌────────────┐ ┌──────────────┐
                   │ PostgreSQL │ │   IA         │
                   │            │ │ Generativa   │
                   │ Banco      │ │ Chatbot      │
                   │ relacional │ │ Recomendações│
                   └────────────┘ └──────────────┘
```

---

# 3. Arquitetura MVC

O projeto utilizará MVC no back-end.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controller

Responsável por:

- receber requisições HTTP;
- validar os dados de entrada;
- chamar os Services;
- devolver respostas HTTP.

Exemplo:

```text
POST /api/livros
GET /api/livros
PUT /api/livros/{id}
DELETE /api/livros/{id}
```

### Service

Responsável pelas regras de negócio.

Exemplos:

- validar estoque;
- calcular preço;
- validar cupons;
- alterar status da venda;
- processar trocas;
- gerar recomendações;
- conversar com a IA.

### Repository

Responsável pelo acesso ao banco de dados utilizando JPA/Spring Data.

---

# 4. Estrutura do Back-end

```text
livrofacil-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── livrofacil/
│   │   │           ├── LivrofacilBackendApplication.java
│   │   │           │
│   │   │           ├── config/
│   │   │           │   ├── ClienteDataInitializer.java
│   │   │           │   ├── CorsConfig.java
│   │   │           │   ├── CheckoutSchemaMigration.java
│   │   │           │   └── EstoqueDataInitializer.java
│   │   │           │
│   │   │           ├── exception/
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   ├── RecursoNaoEncontradoException.java
│   │   │           │   └── RegraDeNegocioException.java
│   │   │           │
│   │   │           ├── ia/
│   │   │           │   ├── controller/
│   │   │           │   ├── dto/
│   │   │           │   └── service/
│   │   │           │
│   │   │           └── modulos/
│   │   │               ├── analise/
│   │   │               │   ├── controller/
│   │   │               │   ├── dto/
│   │   │               │   └── service/
│   │   │               │
│   │   │               ├── carrinho/
│   │   │               │   ├── controller/
│   │   │               │   ├── dto/
│   │   │               │   ├── entity/
│   │   │               │   ├── repository/
│   │   │               │   └── service/
│   │   │               │
│   │   │               ├── cliente/
│   │   │               │   ├── controller/
│   │   │               │   ├── dto/
│   │   │               │   ├── entity/
│   │   │               │   ├── repository/
│   │   │               │   └── service/
│   │   │               │
│   │   │               ├── compra/
│   │   │               │   ├── controller/
│   │   │               │   ├── dto/
│   │   │               │   ├── entity/
│   │   │               │   ├── repository/
│   │   │               │   └── service/
│   │   │               │
│   │   │               ├── estoque/
│   │   │               │   ├── controller/
│   │   │               │   ├── dto/
│   │   │               │   ├── entity/
│   │   │               │   ├── repository/
│   │   │               │   └── service/
│   │   │               │
│   │   │               ├── livro/
│   │   │               │   ├── controller/
│   │   │               │   ├── dto/
│   │   │               │   ├── entity/
│   │   │               │   ├── repository/
│   │   │               │   └── service/
│   │   │               │
│   │   │               ├── troca/
│   │   │               │   ├── controller/
│   │   │               │   ├── dto/
│   │   │               │   ├── entity/
│   │   │               │   ├── repository/
│   │   │               │   └── service/
│   │   │               │
│   │   │               └── venda/
│   │   │                   ├── controller/
│   │   │                   ├── dto/
│   │   │                   ├── entity/
│   │   │                   ├── repository/
│   │   │                   └── service/
│   │   │
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   └── db/
│   │   │
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── livrofacil/
│   │                   ├── DtoAccessorsTest.java
│   │                   ├── ResponseDtoMappingTest.java
│   │                   ├── exception/
│   │                   ├── modulos/
│   │                   └── Service/
│   │
│   └── target/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── HELP.md
├── README.md
├── arquitetura_ecommerce_livros.md
├── estrutura.md
└── .gitignore
```

### Observação de arquitetura atual

A estrutura real do projeto não está mais apenas em um modelo conceitual de módulos; ela representa, na prática, uma organização em subdomínios com relacionamento direto entre entidades de negócio. Isso aparece claramente nos módulos de cliente, livro e compra.

A arquitetura já está seguindo um modelo de negócios que pode ser descrito como:

```text
Controller -> UseCase/Service -> Repository -> JPA Entity -> PostgreSQL
```

E, em alguns fluxos, como carrinho e pedido, há o relacionamento entre módulos e entidades de domínio, por exemplo:

```text
Cliente 1 ── N Endereco
Cliente 1 ── N FormaPagamento
Cliente 1 ── 1 Carrinho
Carrinho 1 ── N ItemCarrinho
Pedido N ── 1 Cliente
Pedido 1 ── N ItemPedido
Pedido 1 ── N PagamentoPedido
```

---

# 5. Módulos do sistema

## 5.1 Livros

Módulo responsável pelo cadastro, consulta e manutenção do catálogo de livros.

### Implementado no código

- `Livro` como entidade principal;
- `Autor`, `Editora`, `Categoria`, `GrupoPrecificacao` como dados auxiliares;
- `Dimensao` como valor embutido em `Livro`;
- `Estoque` com quantidade disponível, bloqueada e vendida;
- `LivroController` com endpoints REST para CRUD e busca;
- `CadastrarLivroUseCase` com regras de cadastro, consulta, ativação e atualização;
- DTOs específicos: `LivroRequest`, `LivroResponse`, `EstoqueRequest`, `EstoqueResponse`.

### Entidades e relacionamento do módulo de livro

```text
Autor      1 ── N Livro
Editora    1 ── N Livro
Categoria  N ── N Livro
Livro      1 ── 1 Estoque
GrupoPrecificacao 1 ── N Livro
Livro      possui Dimensao embutida
```

### Campos importantes do módulo de livro

- código único
- título
- ano
- edição
- ISBN
- número de páginas
- sinopse
- URL da imagem
- código de barras
- valor de venda
- status ativo/inativo
- autor
- editora
- grupo de precificação
- categorias
- dimensões

Esse módulo está bem alinhado com o requisito do e-commerce e já está materializado no código.

---

## 5.2 Clientes

Este módulo está implementado com estrutura concreta em controllers, services, entity e DTOs. O documento deve refletir os nomes reais existentes no projeto.

### Controllers reais do módulo cliente

```text
src/main/java/com/livrofacil/modulos/cliente/controller/
├── ClienteController.java
├── EnderecoController.java
├── FormaPagamentoController.java
└── BandeiraPagamentoController.java
```

### ClienteController

Base URL real:

```text
/api/clientes
```

Endpoints implementados:

```text
POST   /api/clientes
GET    /api/clientes
GET    /api/clientes/buscar
GET    /api/clientes/{id}
POST   /api/clientes/login
PUT    /api/clientes/{id}
PATCH  /api/clientes/{id}/senha
DELETE /api/clientes/{id}
```

Responsabilidades reais:

- cadastro do cliente;
- busca por filtros (nome, email, cpf, telefone, dataNascimento, gênero, endereço, cidade, estado, cep);
- autenticação de login simples por e-mail e senha;
- atualização de dados cadastrais;
- alteração de senha;
- inativação lógica do cliente.

### EnderecoController

Base URL real:

```text
/api/clientes/{clienteId}/enderecos
```

Endpoints implementados:

```text
POST   /api/clientes/{clienteId}/enderecos
GET    /api/clientes/{clienteId}/enderecos
GET    /api/clientes/{clienteId}/enderecos/{enderecoId}
PUT    /api/clientes/{clienteId}/enderecos/{enderecoId}
DELETE /api/clientes/{clienteId}/enderecos/{enderecoId}
```

Responsabilidade real:

- cadastrar, listar, buscar, atualizar e remover endereços vinculados ao cliente;
- marcar um endereço como principal;
- garantir que apenas um endereço principal seja ativo por cliente quando necessário.

### FormaPagamentoController

Base URL real:

```text
/api/clientes/{clienteId}/formas-pagamento
```

Endpoints implementados:

```text
POST   /api/clientes/{clienteId}/formas-pagamento
GET    /api/clientes/{clienteId}/formas-pagamento
GET    /api/clientes/{clienteId}/formas-pagamento/{formaPagamentoId}
PUT    /api/clientes/{clienteId}/formas-pagamento/{formaPagamentoId}
PATCH  /api/clientes/{clienteId}/formas-pagamento/{formaPagamentoId}/preferencial
DELETE /api/clientes/{clienteId}/formas-pagamento/{formaPagamentoId}
DELETE /api/clientes/{clienteId}/formas-pagamento/{formaPagamentoId}/excluir
PATCH  /api/clientes/{clienteId}/formas-pagamento/{formaPagamentoId}/reativar
```

Responsabilidade real:

- cadastro de cartão do cliente;
- listagem em ordem asc/desc;
- definição de cartão preferencial;
- inativação e reativação do cartão;
- exclusão definitiva quando não há histórico de uso.

### BandeiraPagamentoController

Base URL real:

```text
/api/pagamentos/bandeiras
```

Endpoints implementados:

```text
GET   /api/pagamentos/bandeiras
PATCH /api/pagamentos/bandeiras/{id}
```

Responsabilidade real:

- listar bandeiras disponíveis;
- ativar/desativar bandeira no sistema.

### Entidades do módulo cliente realmente existentes

```text
Cliente
Endereco
FormaPagamento
BandeiraPagamento
```

### Relacionamentos reais do módulo cliente

```text
Cliente 1 ── N Endereco
Cliente 1 ── N FormaPagamento
Cliente 1 ── 1 Carrinho
Cliente 1 ── N Pedido
```

### Regras de negócio implementadas no código cliente

- validação de e-mail, CPF e telefone únicos;
- senha com regra mínima de força;
- confirmação de senha;
- maioridade mínima;
- inativação lógica e anonimização posterior;
- atualização de senha separada do restante do cadastro;
- cartão preferencial por cliente;
- bandeira de cartão obrigatória e disponível;
- prevenção de exclusão de cartão em uso em histórico de pedidos.

### O que ainda não está implementado no módulo cliente

- autenticação com JWT/Spring Security;
- criptografia real de senha;
- cadastro de múltiplos perfis administrativos com autorização real;
- histórico de transações do cliente;
- ranking do cliente.

---

## 5.3 Carrinho

Módulo responsável por armazenar os itens selecionados pelo cliente antes da finalização da compra.

### Implementado no código

- `Carrinho` como entidade principal;
- `ItemCarrinho` como associação entre carrinho e livro;
- `CarrinhoController` para criar e consultar carrinho;
- `CarrinhoService` para adicionar, alterar e remover itens;
- `CriarCarrinhoRequest`, `ItemCarrinhoRequest`, `ItemCarrinhoResponse`.

### Relacionamentos reais

```text
Carrinho 1 ── N ItemCarrinho
Carrinho N ── 1 Cliente
ItemCarrinho N ── 1 Livro
```

### Regras implementadas no carrinho

- criação ou associação de carrinho por token ou cliente;
- adição de livro ao carrinho;
- validação de livro ativo antes da compra;
- atualização da quantidade por item;
- remoção de item do carrinho;
- controle de data de atualização do carrinho;
- verificação de pertencimento do carrinho ao cliente informado.

### O que ainda falta no fluxo de carrinho

- bloqueio de estoque em tempo real;
- expiração automática do carrinho;
- cálculo de frete;
- regras de cupom vinculadas ao carrinho;
- finalização integrada com fluxo de pagamento completo.

---

## 5.4 Compra / Pedido

Módulo responsável pelo fechamento do pedido e pelo fluxo de pagamento.

### Entidades implementadas

- `Pedido`
- `ItemPedido`
- `PagamentoPedido`
- `Cupom`
- `StatusPedido`

### Relacionamentos reais

```text
Cliente 1 ── N Pedido
Carrinho 1 ── 1 Pedido
Pedido 1 ── N ItemPedido
Pedido 1 ── N PagamentoPedido
Pedido N ── 1 FormaPagamento
```

### Status do pedido já definidos no código

```text
PENDENTE
AGUARDANDO_PAGAMENTO
EM_CHECKOUT
EM_PROCESSAMENTO
PAGAMENTO_APROVADO
EM_SEPARACAO
NA_TRANSPORTADORA
EM_ROTA_DE_ENTREGA
ENTREGUE
FINALIZADO
CANCELADO
```

### Implementado

- criação do pedido e associação ao cliente/carrinho;
- status do pedido;
- subtotal, desconto e total;
- armazenamento de endereço de entrega e cobrança;
- itens do pedido com valor unitário e quantidade;
- pagamento vinculado ao pedido;
- estrutura base de cupom e desconto;
- controller REST para pedido e carrinho.

### Ainda não implementado no fluxo de compra

- validação real de pagamento junto a operadora;
- geração automática de nota fiscal;
- confirmação de pagamento e liberação de estoque real;
- integração com logística e rastreio de entrega;
- regra de devolução/troca completa.

---

## 5.5 Estoque

Módulo responsável por controlar a quantidade disponível do produto e o movimento de venda.

### Entidade principal implementada

- `Estoque`

### Campos da entidade

- id
- livro
- quantidadeDisponivel
- quantidadeBloqueada
- quantidadeVendida

### Relacionamento real

```text
Livro 1 ── 1 Estoque
```

### O que está em uso

- controle de quantidade disponível;
- quantidade bloqueada;
- quantidade vendida;
- associação direta com `Livro`.

### Ainda não implementado

- registro de entrada de estoque por fornecedor;
- histórico de movimentações;
- controle de custos por lote;
- baixa de estoque por movimentação real e auditoria.

---

## 5.6 Troca / devolução

Estrutura de domínio prevista, mas ainda não consolidada em fluxo real de negócio.

### O que existe no projeto

- módulo `troca` presente na estrutura do projeto;
- modelos organizados por domínio, mas sem implementação funcional completa em `controller`, `service` e `repository` verificáveis.

### Fluxo esperado

```text
Pedido entregue
   ↓
Solicitação de troca
   ↓
Validação da troca
   ↓
Autorização
   ↓
Recebimento do produto
   ↓
Atualização do status
```

Ainda está como evolução futura do sistema.

---

## 5.7 Análise / relatórios

Estrutura prevista para gerar relatórios administrativos.

### O que existe

- módulo `analise` com controller, dto e service;
- base para relatórios de vendas.

### Objetivo esperado

- consultar volume de vendas por período;
- agrupar por categoria e mês;
- alimentar dashboard administrativo.

### Ainda não implementado

- consulta real e agrupada por período e categoria;
- geração de gráfico ou painel analítico final.

---

## 5.8 IA Generativa

Módulo de integração com IA para comportamento do chatbot e recomendações.

### O que existe no código

- pacote `ia` com `controller`, `service` e `dto`;
- estrutura base para interação com modelo externo.

### Objetivo arquitetural

```text
React / Front-end
   ↓
REST API
   ↓
IA Controller
   ↓
IA Service
   ↓
OpenAI / modelo externo
```

### Ainda não implementado

- integração real com API da OpenAI;
- contexto do cliente e catálogo para recomendações;
- chat funcional completo;
- autenticação e contexto de usuário para IA.

---

# 6. Mapa de relacionamento entre entidades

A arquitetura atual do backend pode ser representada pelos principais vínculos abaixo:

```text
CLIENTE
  ├── ENDERECO (1:N)
  ├── FORMA_PAGAMENTO (1:N)
  ├── CARRINHO (1:1)
  └── PEDIDO (1:N)

LIVRO
  ├── AUTOR (N:1)
  ├── EDITORA (N:1)
  ├── GRUPO_PRECIFICACAO (N:1)
  ├── CATEGORIA (N:N)
  └── ESTOQUE (1:1)

CARRINHO
  └── ITEM_CARRINHO (1:N)

PEDIDO
  ├── ITEM_PEDIDO (1:N)
  ├── PAGAMENTO_PEDIDO (1:N)
  └── CUPOM (N:1 ou uso local)
```

Essa representação resume bem a modelagem de domínio que já está presente no código.

---

# 7. Camadas de arquitetura da aplicação

```text
FRONT-END (React)
        ↓
REST API
        ↓
CONTROLLER
        ↓
SERVICE / USE CASE
        ↓
REPOSITORY
        ↓
ENTITY / JPA
        ↓
POSTGRESQL
```

Esse fluxo está em linha com a implementação atual: o backend se comunica com o banco via JPA e expõe endpoints REST para o front-end.

---

# 8. Estado real da implementação

O projeto está em um estágio de evolução tecnológica e funcional interessante:

### Já implementado

- estrutura modular por domínio;
- entidades de cliente, livro, carrinho, pedido e pagamento;
- DTOs e controllers REST;
- regras de validação de dados;
- uso de JPA e Spring Data;
- tratamento de exceções centralizado;
- cenário básico de compra e produto.

### Ainda em desenvolvimento / não implementado

- autenticação e autorização real;
- segurança de senha em hash;
- integrações com gateway de pagamento;
- log de transações;
- dashboard analítico e relatórios fiscais;
- IA funcional com contexto real do cliente;
- fluxo completo de trocas e logística.

---

# 9. Conclusão da arquitetura atual

A arquitetura do backend de `livrofacil` já está organizada por módulos reais do negócio e não apenas por uma proposta genérica. O código representa uma base sólida para e-commerce de livros com foco em:

- cliente;
- catálogo de livros;
- carrinho;
- compra e pedido;
- estoque;
- formas de pagamento;
- integração de IA e análise.

Em outras palavras, o sistema já saiu do estágio de rascunho conceitual e está seguindo uma modelagem de domínio concreta, porém com alguns pontos de extensão e segurança ainda pendentes.

---

# 10. Resumo executivo

O backend atual apresenta uma arquitetura típica de Spring Boot com separação por módulos e entidades. O que já existe no código é muito mais concreto do que o documento inicial indicava e está pronto para servir como base para apresentação acadêmica e evolução futura.

Responsável por:

- finalizar compra;
- calcular total;
- calcular frete;
- selecionar endereço;
- selecionar pagamento;
- validar pagamento;
- alterar status;
- despachar pedido;
- confirmar entrega.

### Status sugeridos

```text
EM_PROCESSAMENTO
APROVADA
REPROVADA
EM_TRANSPORTE
ENTREGUE
EM_TROCA
TROCA_AUTORIZADA
TROCADO
```

Fluxo:

```text
CARRINHO
    ↓
FINALIZAÇÃO
    ↓
EM_PROCESSAMENTO
    ↓
Validação do pagamento
    ├───────────────┐
    ↓               ↓
APROVADA        REPROVADA
    ↓
EM_TRANSPORTE
    ↓
ENTREGUE
    ↓
Possível troca
```

---

# 6. Estoque

O estoque deve controlar as entradas de produtos.

Uma entrada deve possuir:

- livro;
- quantidade;
- valor de custo;
- fornecedor;
- data de entrada.

Estrutura conceitual:

```text
LIVRO
  │
  └── ESTOQUE
        │
        └── ENTRADA_ESTOQUE
              ├── quantidade
              ├── valorCusto
              ├── fornecedor
              └── dataEntrada
```

O sistema também deve controlar:

```text
quantidade disponível
quantidade bloqueada
quantidade vendida
```

---

# 7. Trocas

Fluxo:

```text
ENTREGUE
   ↓
Cliente solicita troca
   ↓
EM_TROCA
   ↓
Administrador analisa
   ↓
TROCA_AUTORIZADA
   ↓
Recebimento do produto
   ↓
TROCADO
   ↓
Geração de cupom de troca
```

O item somente poderá entrar no fluxo de troca depois de uma compra entregue.

---

# 8. Análise gerencial

O administrador poderá analisar vendas por período e categoria.

Endpoint sugerido:

```text
GET /api/analises/vendas
```

Parâmetros:

```text
dataInicio
dataFim
categorias
```

Resposta:

```json
[
  {
    "mes": "2026-01",
    "categoria": "Romance",
    "valor": 12500.00
  },
  {
    "mes": "2026-01",
    "categoria": "Tecnologia",
    "valor": 8900.00
  }
]
```

O agrupamento deve ser mensal.

O front-end será responsável por apresentar o gráfico.

---

# 9. IA Generativa

A IA será integrada através de uma API de um modelo já existente.

Não será necessário desenvolver ou treinar um modelo de IA do zero.

## Arquitetura

```text
                    ┌──────────────┐
                    │    React     │
                    │              │
                    │   Chatbot    │
                    └──────┬───────┘
                           │
                      POST /api/chat
                           │
                           ▼
                  ┌─────────────────┐
                  │ ChatController  │
                  └────────┬────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │   ChatService   │
                  └──────┬─────┬────┘
                         │     │
                ┌────────┘     └─────────┐
                ▼                        ▼
        ┌──────────────┐          ┌─────────────┐
        │ PostgreSQL   │          │  OpenAI API │
        │              │          │             │
        │ Livros       │          │ IA          │
        │ Categorias   │          │ Generativa  │
        │ Compras      │          └─────────────┘
        │ Preferências │
        └──────────────┘
```

## Fluxo do chatbot

```text
Usuário
   ↓
React
   ↓
POST /api/chat
   ↓
ChatController
   ↓
ChatService
   ├── consulta dados do sistema
   └── envia contexto para IA
              ↓
          IA Generativa
              ↓
          resposta
              ↓
          React
```

## Comportamento da IA

A IA poderá receber instruções como:

```text
Você é a assistente virtual de uma loja online de livros.

Seu objetivo é:
- ajudar o cliente a encontrar livros;
- responder dúvidas;
- recomendar livros;
- auxiliar na busca;
- utilizar os dados fornecidos pelo sistema.

Regras:
- seja objetiva e amigável;
- não invente livros;
- não invente preços;
- não invente disponibilidade;
- utilize prioritariamente os dados do catálogo;
- quando não souber uma informação, informe que não encontrou essa informação.
```

Essas instruções são o comportamento do chatbot e podem ser ajustadas pelo sistema.

---

# 10. IA + Banco de Dados

A IA não deve ser responsável por armazenar os dados verdadeiros da loja.

O banco continua sendo a fonte dos dados.

Exemplo:

```text
Usuário:
"Quero um livro de ficção científica até R$50"

              ↓

Spring Boot

              ↓

PostgreSQL
"Quais livros atendem aos critérios?"

              ↓

Livros encontrados

              ↓

OpenAI
"Com base nesses livros, responda ao cliente..."

              ↓

Resposta
```

Dessa forma a IA não precisa inventar produtos.

---

# 11. Recomendação personalizada

Para clientes autenticados, o sistema pode enviar contexto adicional:

```text
Cliente:
123

Histórico:
- Ficção científica
- Fantasia
- Tecnologia

Compras anteriores:
- Livro A
- Livro B
- Livro C

Preferências:
- Ficção científica
- Livros de até R$80

Catálogo disponível :
- Livro X
- Livro Y
- Livro Z
```

A IA  utiliza  essas  informações para produzir recomendações.
---
---

# 12. Front-end

Estrutura :

```text
frontend/
└── src/
    ├── pages/
    │   ├── login/
    │   ├── livros/
    │   ├── cliente/
    │   ├── carrinho/
    │   ├── compras/
    │   ├── trocas/
    │   ├── estoque/
    │   ├── admin/
    │   ├── analise/
    │   └── chatbot/
    │
    ├── components/
    │   ├── Header/
    │   ├── Footer/
    │   ├── LivroCard/
    │   ├── CarrinhoItem/
    │   ├── Chatbot/
    │   └── Modal/
    │
    ├── services/
    │   ├── api.js
    │   ├── livroService.js
    │   ├── clienteService.js
    │   ├── carrinhoService.js
    │   ├── vendaService.js
    │   ├── estoqueService.js
    │   ├── trocaService.js
    │   ├── analiseService.js
    │   └── chatService.js
    │
    ├── context/
    │   ├── AuthContext.jsx
    │   └── CartContext.jsx
    │
    └── routes/
        └── AppRoutes.jsx
```

---

# 13. Comunicação Front-end → Back-end

O React não acessará o banco diretamente.

```text
ERRADO:

React → PostgreSQL


CORRETO:

React
  ↓
REST API
  ↓
Spring Boot
  ↓
Service
  ↓
Repository
  ↓
PostgreSQL
```

---

# 14. Segurança (planejada)

A autenticação ainda não foi implementada. Quando entrar no escopo do projeto, ficará no back-end.

```text
Login
   ↓
Spring Security
   ↓
Validação das credenciais
   ↓
Emissão de JWT
   ↓
Requisições autenticadas
```

Perfis sugeridos:

```text
ROLE_CLIENTE
ROLE_ADMIN
ROLE_GERENTE
```

O back-end deverá validar se o usuário possui permissão antes de executar operações administrativas.

---

# 15. Banco de dados — modelo inicial

```text
CLIENTE
   │
   ├── ENDERECO
   │
   └── CARTAO

LIVRO
   │
   ├── LIVRO_CATEGORIA ── CATEGORIA
   ├── AUTOR
   ├── EDITORA
   └── GRUPO_PRECIFICACAO

LIVRO
   │
   └── ENTRADA_ESTOQUE
          │
          └── FORNECEDOR

CLIENTE
   │
   └── CARRINHO
          │
          └── ITEM_CARRINHO
                   │
                   └── LIVRO

CLIENTE
   │
   └── VENDA
          │
          ├── ITEM_VENDA ─── LIVRO
          ├── ENDERECO
          ├── PAGAMENTO
          └── CUPOM

VENDA
   │
   └── TROCA
          │
          └── ITEM_TROCA
```

Tabelas auxiliares:

```text
USUARIO
PERFIL
LOG_TRANSACAO
CUPOM
NOTIFICACAO
```

---

# 16. API REST inicial

## Livros

```text
GET    /api/livros
GET    /api/livros/{id}
POST   /api/livros
PUT    /api/livros/{id}
PATCH  /api/livros/{id}/ativar
PATCH  /api/livros/{id}/inativar
```

## Clientes

A implementação real do módulo cliente possui os seguintes endpoints e estruturas em `controller` e `service`:

```text
POST   /api/clientes
GET    /api/clientes
GET    /api/clientes/buscar
GET    /api/clientes/{id}
POST   /api/clientes/login
PUT    /api/clientes/{id}
PATCH  /api/clientes/{id}/senha
DELETE /api/clientes/{id}
```

Os controllers reais do cliente são:

```text
ClienteController.java
EnderecoController.java
FormaPagamentoController.java
BandeiraPagamentoController.java
```

Esses endpoints refletem exatamente o que foi implementado no código e não o padrão genérico do documento inicial.

## Carrinho

```text
GET    /api/carrinho
POST   /api/carrinho/itens
PUT    /api/carrinho/itens/{id}
DELETE /api/carrinho/itens/{id}
```

## Vendas

```text
POST   /api/vendas
GET    /api/vendas
GET    /api/vendas/{id}
PATCH  /api/vendas/{id}/transporte
PATCH  /api/vendas/{id}/entregue
```

## Estoque

```text
POST   /api/estoque/entradas
GET    /api/estoque
```

## Trocas

```text
POST   /api/trocas
GET    /api/trocas
PATCH  /api/trocas/{id}/autorizar
PATCH  /api/trocas/{id}/receber
```

## Análise

```text
GET /api/analises/vendas
```

## IA

```text
POST /api/chat
```

---

# 17. Comunicação com a OpenAI

A chave da API deve ficar somente no back-end.

```text
React
  ↓
Spring Boot
  ↓
AIService
  ↓
OpenAI API
```

Nunca:

```text
React
  ↓
OpenAI
```

A chave pode ser configurada através de variável de ambiente:

```text
OPENAI_API_KEY=sua-chave
```

Durante o desenvolvimento, o projeto pode funcionar completamente localmente:

```text
React      → localhost:5173
Spring     → localhost:8080
PostgreSQL → localhost:5432
OpenAI     → API externa pela internet
```

O computador precisa apenas ter acesso à internet para realizar a chamada à API da OpenAI.

---

# 18. Requisitos não funcionais considerados

A arquitetura deverá considerar:

### Tempo de resposta

As consultas devem buscar atender ao limite de até 1 segundo definido pelo requisito.

### Log

Operações de escrita deverão registrar:

```text
data
hora
usuário responsável
dados alterados
```

Uma tabela possível:

```text
LOG_TRANSACAO
├── id
├── usuario_id
├── operacao
├── entidade
├── entidade_id
├── dados_anteriores
├── dados_novos
└── data_hora
```

### Senha

O cadastro de cliente atual é apenas demonstrativo e não possui senha. Regras de senha e armazenamento seguro serão documentados quando a autenticação for implementada.

---

# 19. Fluxo completo do sistema

```text
                         USUÁRIO
                            │
                            ▼
                     ┌────────────┐
                     │   REACT    │
                     └─────┬──────┘
                           │
                         REST
                           │
                           ▼
                  ┌─────────────────┐
                  │   SPRING BOOT   │
                  │                 │
                  │   CONTROLLER    │
                  │       ↓         │
                  │     SERVICE     │
                  │       ↓         │
                  │   REPOSITORY    │
                  └──────┬─────┬────┘
                         │     │
                         ▼     ▼
                  ┌─────────┐ ┌──────────┐
                  │PostgreSQL│ │ OpenAI  │
                  └─────────┘ └──────────┘
```

---

# 20. Princípios do projeto

1. O React é responsável pela interface.
2. O Spring Boot é responsável pelas regras de negócio.
3. O PostgreSQL é responsável pela persistência.
4. A comunicação entre front e back será feita por API REST.
5. O front-end nunca acessará diretamente o banco.
6. A API Key da IA ficará somente no back-end.
7. A IA será utilizada como serviço externo.
8. Os dados reais de livros, estoque, preços e vendas virão do banco.
9. A IA será responsável pela interpretação da linguagem e geração das respostas.
10. O projeto seguirá arquitetura MVC.
11. A autenticação e autorização serão controladas pelo back-end.
12. Os módulos serão separados por domínio de negócio.

---

# 21. Ordem recomendada de desenvolvimento

```text
1. Modelagem do banco
        ↓
2. Criar projeto Spring Boot
        ↓
3. Criar entidades
        ↓
4. Criar repositories
        ↓
5. Criar services
        ↓
6. Criar controllers
        ↓
7. Implementar clientes (CRUD demonstrativo concluído)
   ↓
8. Implementar autenticação
   ↓
9. Implementar livros
        ↓
10. Implementar estoque
        ↓
11. Implementar carrinho
        ↓
12. Implementar vendas
        ↓
13. Implementar trocas
        ↓
14. Implementar análise
        ↓
15. Implementar front-end
        ↓
16. Integrar IA
        ↓
17. Testes
        ↓
18. Deploy/apresentação
```

---

# 22. Resumo da solução

```text
                    E-COMMERCE DE LIVROS
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
       FRONT-END         BACK-END          BANCO
        React          Java/Spring       PostgreSQL
          │                 │
          │                 ├── Livros
          │                 ├── Clientes
          │                 ├── Carrinho
          │                 ├── Vendas
          │                 ├── Estoque
          │                 ├── Trocas
          │                 ├── Análise
          │                 └── IA
          │                       │
          │                       ▼
          │                 OpenAI API
          │
          └──────────── REST/JSON ─────────────┘
```

## Objetivo da arquitetura

Construir um e-commerce web monolítico em arquitetura MVC, com front-end separado do back-end, banco relacional para persistência e integração com IA generativa através de uma API externa. O chatbot deverá utilizar os dados fornecidos pelo sistema para auxiliar na busca e recomendação de livros, sem expor a chave da API no navegador.
