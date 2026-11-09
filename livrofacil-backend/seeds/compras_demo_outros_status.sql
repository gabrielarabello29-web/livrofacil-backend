-- LivroFácil: 20 pedidos adicionais para demonstrar status diferentes de ENTREGUE/FINALIZADO.
-- Pré-requisitos: executar livros_50_seed.sql e compras_demo_dashboard.sql; schema atualizado
-- pela aplicação. Usa clientes demo 01-20 e livros do catálogo já carregado.
-- Inclui todos os outros status do enum: PENDENTE, AGUARDANDO_PAGAMENTO, EM_CHECKOUT,
-- EM_PROCESSAMENTO, PAGAMENTO_APROVADO, EM_SEPARACAO, NA_TRANSPORTADORA,
-- EM_ROTA_DE_ENTREGA e CANCELADO. Não cria pedidos ENTREGUE ou FINALIZADO.
-- Pedidos nos status de checkout e EM_PROCESSAMENTO podem ser removidos pelo agendador
-- do backend após o tempo de expiração configurado (30 min por padrão).
-- Idempotente: cria carrinhos novos com token estável e só movimenta estoque para pedidos novos.
-- Dados são fictícios; cartões têm somente bandeira/últimos 4 dígitos, sem dados de cobrança reais.

BEGIN;

DROP TABLE IF EXISTS pg_temp.seed_status_compras;
DROP TABLE IF EXISTS pg_temp.seed_status_novos;

CREATE TEMPORARY TABLE seed_status_compras (
    numero smallint PRIMARY KEY,
    cliente_numero smallint NOT NULL,
    status varchar(30) NOT NULL,
    dias_atras integer NOT NULL,
    isbn varchar(20) NOT NULL,
    quantidade integer NOT NULL CHECK (quantidade > 0),
    parcelas integer NOT NULL CHECK (parcelas BETWEEN 1 AND 3),
    bandeira varchar(20) NOT NULL,
    ultimos_digitos varchar(4) NOT NULL
) ON COMMIT PRESERVE ROWS;

CREATE TEMPORARY TABLE seed_status_novos (
    numero smallint PRIMARY KEY,
    ped_id bigint UNIQUE
) ON COMMIT PRESERVE ROWS;

INSERT INTO seed_status_compras
(numero, cliente_numero, status, dias_atras, isbn, quantidade, parcelas, bandeira, ultimos_digitos)
VALUES
(1,  1, 'PENDENTE',             0, '9780451627766', 1, 1, 'VISA',       '4201'),
(2,  2, 'AGUARDANDO_PAGAMENTO', 0, '9780226458113', 1, 1, 'MASTERCARD', '5202'),
(3,  3, 'EM_CHECKOUT',          0, '9780965607414', 1, 1, 'VISA',       '4203'),
(4,  4, 'EM_PROCESSAMENTO',     1, '9780553026696', 1, 2, 'MASTERCARD', '5204'),
(5,  5, 'PAGAMENTO_APROVADO',   2, '9781857150988', 1, 1, 'VISA',       '4205'),
(6,  6, 'EM_SEPARACAO',         3, '9781587261411', 1, 2, 'MASTERCARD', '5206'),
(7,  7, 'NA_TRANSPORTADORA',   4, '9780195103090', 1, 1, 'VISA',       '4207'),
(8,  8, 'EM_ROTA_DE_ENTREGA',  5, '9788506004234', 1, 2, 'MASTERCARD', '5208'),
(9,  9, 'CANCELADO',            6, '9780811219495', 1, 1, 'VISA',       '4209'),
(10, 10, 'PENDENTE',             0, '9781541245259', 1, 1, 'MASTERCARD', '5210'),
(11, 11, 'AGUARDANDO_PAGAMENTO', 0, '9780307277923', 1, 1, 'VISA',       '4211'),
(12, 12, 'EM_CHECKOUT',          0, '9781848940994', 1, 1, 'MASTERCARD', '5212'),
(13, 13, 'EM_PROCESSAMENTO',     7, '9780671754013', 1, 3, 'VISA',       '4213'),
(14, 14, 'PAGAMENTO_APROVADO',   9, '9781906694661', 1, 2, 'MASTERCARD', '5214'),
(15, 15, 'EM_SEPARACAO',        11, '9780330284141', 1, 1, 'VISA',       '4215'),
(16, 16, 'NA_TRANSPORTADORA',  13, '9780448455976', 1, 2, 'MASTERCARD', '5216'),
(17, 17, 'EM_ROTA_DE_ENTREGA', 15, '9781663611499', 1, 1, 'VISA',       '4217'),
(18, 18, 'CANCELADO',           18, '9781400344161', 1, 1, 'MASTERCARD', '5218'),
(19, 19, 'PAGAMENTO_APROVADO',  20, '9781406844955', 1, 2, 'VISA',       '4219'),
(20, 20, 'EM_SEPARACAO',        22, '9780593303740', 1, 3, 'MASTERCARD', '5220');

DO $$
BEGIN
    IF (SELECT count(*) FROM seed_status_compras) <> 20 THEN
        RAISE EXCEPTION 'A carga precisa conter exatamente 20 pedidos.';
    END IF;

    IF (SELECT count(DISTINCT b.liv_isbn)
        FROM seed_status_compras s JOIN livro b ON b.liv_isbn = s.isbn) <> 20 THEN
        RAISE EXCEPTION 'Nem todos os 20 ISBNs estão no catálogo. Execute primeiro a carga de livros.';
    END IF;

    IF (SELECT count(DISTINCT c.cli_email)
        FROM seed_status_compras s
        JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test') <> 20 THEN
        RAISE EXCEPTION 'Não encontrei os 20 clientes demo. Execute primeiro compras_demo_dashboard.sql.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'pedido' AND column_name = 'ped_frete'
    ) OR NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'pedido' AND column_name = 'ped_entregue_em'
    ) THEN
        RAISE EXCEPTION 'Schema desatualizado. Inicie a aplicação uma vez para aplicar as migrations.';
    END IF;
END $$;

-- Cria cartões sintéticos adicionais somente se ainda não existirem para o cliente.
INSERT INTO forma_pagamento (
    cli_id, for_pag_nome_titular, for_pag_tipo_cartao, for_pag_ultimos_digitos,
    for_pag_validade, for_pag_bandeira, for_pag_preferencial, for_pag_ativo, for_pag_criado_em
)
SELECT c.cli_id, c.cli_nome, 'CREDITO', s.ultimos_digitos, '12/30', s.bandeira, 'N', 'S', CURRENT_TIMESTAMP
FROM seed_status_compras s
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
WHERE NOT EXISTS (
    SELECT 1 FROM forma_pagamento f
    WHERE f.cli_id = c.cli_id AND f.for_pag_tipo_cartao = 'CREDITO'
      AND f.for_pag_bandeira = s.bandeira AND f.for_pag_ultimos_digitos = s.ultimos_digitos
);

-- Carrinhos exclusivos para estes pedidos, sem depender de IDs fixos.
INSERT INTO carrinho (cli_id, car_token, car_atualizado_em)
SELECT c.cli_id, 'SEED-STATUS-' || lpad(s.numero::text, 2, '0'), CURRENT_TIMESTAMP
FROM seed_status_compras s
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
ON CONFLICT (car_token) DO NOTHING;

-- Manifesto dos pedidos ainda não criados; controla reexecução e movimentação de estoque.
INSERT INTO seed_status_novos (numero)
SELECT s.numero
FROM seed_status_compras s
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-STATUS-' || lpad(s.numero::text, 2, '0')
WHERE NOT EXISTS (
    SELECT 1 FROM pedido p WHERE p.cli_id = c.cli_id AND p.car_id = car.car_id
);

INSERT INTO pedido (
    cli_id, car_id, ped_checkout_chave, ped_status, ped_criado_em, ped_atualizado_em,
    ped_entregue_em, ped_reserva_expira_em, ped_subtotal, ped_desconto, ped_frete,
    ped_total, ped_cupom, ped_cobranca_endereco, ped_entrega_endereco
)
SELECT
    c.cli_id,
    car.car_id,
    CASE WHEN s.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT') THEN car.car_id ELSE NULL END,
    s.status,
    valores.criado_em,
    CASE WHEN s.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT', 'EM_PROCESSAMENTO')
         THEN CURRENT_TIMESTAMP ELSE valores.criado_em + interval '2 hours' END,
    NULL,
    CASE WHEN s.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT')
         THEN CURRENT_TIMESTAMP + interval '14 days' ELSE NULL END,
    valores.subtotal,
    0.00,
    frete.valor_frete,
    valores.subtotal + frete.valor_frete,
    NULL,
    endereco.texto,
    endereco.texto
FROM seed_status_compras s
JOIN seed_status_novos novo ON novo.numero = s.numero
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-STATUS-' || lpad(s.numero::text, 2, '0')
JOIN livro l ON l.liv_isbn = s.isbn
CROSS JOIN LATERAL (
    SELECT CURRENT_TIMESTAMP - (s.dias_atras * interval '1 day') AS criado_em,
           l.liv_valor_venda * s.quantidade AS subtotal
) valores
CROSS JOIN LATERAL (
    SELECT CASE
        WHEN valores.subtotal >= 150.00 THEN 0.00::numeric
        WHEN EXISTS (SELECT 1 FROM endereco e WHERE e.cli_id = c.cli_id AND e.end_estado = 'SP') THEN 19.90::numeric
        ELSE 29.90::numeric
    END AS valor_frete
) frete
CROSS JOIN LATERAL (
    SELECT e.end_logradouro || ', ' || e.end_numero || ' - ' || e.end_bairro || ', '
           || e.end_cidade || '/' || e.end_estado || ' - CEP ' || e.end_cep AS texto
    FROM endereco e
    WHERE e.cli_id = c.cli_id
    ORDER BY CASE WHEN e.end_principal = 'S' THEN 0 ELSE 1 END, e.end_id
    LIMIT 1
) endereco;

UPDATE seed_status_novos novo
SET ped_id = p.ped_id
FROM seed_status_compras s
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-STATUS-' || lpad(s.numero::text, 2, '0')
JOIN pedido p ON p.cli_id = c.cli_id AND p.car_id = car.car_id
WHERE novo.numero = s.numero;

-- Verifica estoque disponível antes de registrar qualquer movimentação.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM seed_status_novos n
        JOIN seed_status_compras s ON s.numero = n.numero
        JOIN livro l ON l.liv_isbn = s.isbn
        JOIN estoque e ON e.liv_id = l.liv_id
        WHERE s.status IN (
            'PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT', 'EM_PROCESSAMENTO',
            'PAGAMENTO_APROVADO', 'EM_SEPARACAO', 'NA_TRANSPORTADORA', 'EM_ROTA_DE_ENTREGA'
        ) AND e.est_quantidade_disponivel < s.quantidade
    ) THEN
        RAISE EXCEPTION 'Estoque insuficiente para um ou mais pedidos demo.';
    END IF;
END $$;

-- Itens de pedido com preço/título atual do catálogo. Cada pedido usa um livro.
INSERT INTO item_pedido (ped_id, liv_id, ite_ped_titulo, ite_ped_quantidade, ite_ped_valor_unitario)
SELECT n.ped_id, l.liv_id, l.liv_titulo, s.quantidade, l.liv_valor_venda
FROM seed_status_novos n
JOIN seed_status_compras s ON s.numero = n.numero
JOIN livro l ON l.liv_isbn = s.isbn;

-- Itens do carrinho permanecem somente nos estados de checkout.
INSERT INTO item_carrinho (car_id, liv_id, ite_car_quantidade)
SELECT car.car_id, l.liv_id, s.quantidade
FROM seed_status_novos n
JOIN seed_status_compras s ON s.numero = n.numero
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-STATUS-' || lpad(s.numero::text, 2, '0')
JOIN livro l ON l.liv_isbn = s.isbn
WHERE s.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT')
ON CONFLICT (car_id, liv_id) DO NOTHING;

-- Pagamentos integrais para os pedidos com pagamento registrado/aprovado.
INSERT INTO pagamento_pedido (ped_id, for_pag_id, pag_ped_valor, pag_ped_parcelas)
SELECT n.ped_id, f.for_pag_id, p.ped_total, s.parcelas
FROM seed_status_novos n
JOIN seed_status_compras s ON s.numero = n.numero
JOIN pedido p ON p.ped_id = n.ped_id
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
JOIN forma_pagamento f ON f.cli_id = c.cli_id
                       AND f.for_pag_tipo_cartao = 'CREDITO'
                       AND f.for_pag_bandeira = s.bandeira
                       AND f.for_pag_ultimos_digitos = s.ultimos_digitos
WHERE s.status IN (
    'EM_PROCESSAMENTO', 'PAGAMENTO_APROVADO', 'EM_SEPARACAO',
    'NA_TRANSPORTADORA', 'EM_ROTA_DE_ENTREGA'
);

-- Checkout aberto reserva estoque; estados pagos/em andamento contam como vendidos.
UPDATE estoque e
SET est_quantidade_disponivel = e.est_quantidade_disponivel - s.quantidade,
    est_quantidade_bloqueada = e.est_quantidade_bloqueada +
        CASE WHEN s.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT') THEN s.quantidade ELSE 0 END,
    est_quantidade_vendida = e.est_quantidade_vendida +
        CASE WHEN s.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT', 'CANCELADO') THEN 0 ELSE s.quantidade END
FROM seed_status_novos n
JOIN seed_status_compras s ON s.numero = n.numero
JOIN livro l ON l.liv_isbn = s.isbn
WHERE e.liv_id = l.liv_id
  AND s.status <> 'CANCELADO';

COMMIT;

-- Resumo dos 20 pedidos desta carga por status (não inclui ENTREGUE/FINALIZADO).
SELECT s.status, count(*) AS quantidade
FROM seed_status_compras s
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-STATUS-' || lpad(s.numero::text, 2, '0')
JOIN pedido p ON p.cli_id = c.cli_id AND p.car_id = car.car_id
GROUP BY s.status
ORDER BY s.status;

-- Conferência dos pedidos, valores e clientes.
SELECT c.cli_nome AS cliente, p.ped_id AS pedido, p.ped_status AS status,
       p.ped_criado_em AS criado_em, l.liv_titulo AS livro,
       ip.ite_ped_quantidade AS quantidade, p.ped_total AS total
FROM seed_status_compras s
JOIN cliente c ON c.cli_email = 'comprador' || lpad(s.cliente_numero::text, 2, '0') || '@seed.livrofacil.test'
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-STATUS-' || lpad(s.numero::text, 2, '0')
JOIN pedido p ON p.cli_id = c.cli_id AND p.car_id = car.car_id
JOIN item_pedido ip ON ip.ped_id = p.ped_id
JOIN livro l ON l.liv_id = ip.liv_id
ORDER BY p.ped_criado_em DESC;
