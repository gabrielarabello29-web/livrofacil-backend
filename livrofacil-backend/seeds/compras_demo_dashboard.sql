-- LivroFácil: clientes e compras fictícias para exercitar pedidos e dashboard.
-- Execute depois de livrofacil-backend/seeds/livros_50_seed.sql e com o schema atualizado
-- pelo menos uma vez pela inicialização da aplicação (migrations/ddl-auto).
-- Cria 24 clientes de teste (e-mail .test; sem CPF, telefone ou senha), endereços,
-- cartões fictícios (somente últimos 4 dígitos), carrinhos, pedidos, itens e pagamentos.
-- Status são valores válidos do enum StatusPedido. As datas são relativas à execução
-- para distribuir vendas recentes pelos últimos meses. Não usa estados transitórios de
-- checkout que o agendador do backend expira; estoque é movimentado uma vez por pedido novo.
-- Pode executar novamente na mesma sessão ou em outra sem duplicar os clientes/pedidos.
-- Não apaga dados permanentes. Não contém número de cartão completo nem código de segurança.

BEGIN;

DROP TABLE IF EXISTS pg_temp.seed_dash_compras;
DROP TABLE IF EXISTS pg_temp.seed_dash_clientes;
DROP TABLE IF EXISTS pg_temp.seed_dash_pedidos_criados;

CREATE TEMPORARY TABLE seed_dash_clientes (
    numero smallint PRIMARY KEY,
    nome varchar(100) NOT NULL,
    email varchar(50) NOT NULL UNIQUE,
    nascimento date NOT NULL,
    genero varchar(30) NOT NULL,
    logradouro varchar(150) NOT NULL,
    endereco_numero varchar(20) NOT NULL,
    bairro varchar(100) NOT NULL,
    cidade varchar(100) NOT NULL,
    estado varchar(2) NOT NULL,
    cep varchar(10) NOT NULL,
    bandeira varchar(20) NOT NULL,
    ultimos_digitos varchar(4) NOT NULL
) ON COMMIT PRESERVE ROWS;

CREATE TEMPORARY TABLE seed_dash_compras (
    numero smallint PRIMARY KEY,
    status varchar(30) NOT NULL,
    dias_atras integer NOT NULL,
    isbn_a varchar(20) NOT NULL,
    quantidade_a integer NOT NULL CHECK (quantidade_a > 0),
    isbn_b varchar(20),
    quantidade_b integer NOT NULL DEFAULT 0 CHECK (quantidade_b >= 0),
    parcelas integer NOT NULL CHECK (parcelas BETWEEN 1 AND 3)
) ON COMMIT PRESERVE ROWS;

CREATE TEMPORARY TABLE seed_dash_pedidos_criados (
    numero smallint PRIMARY KEY,
    ped_id bigint UNIQUE
) ON COMMIT PRESERVE ROWS;

INSERT INTO seed_dash_clientes
(numero, nome, email, nascimento, genero, logradouro, endereco_numero, bairro, cidade, estado, cep, bandeira, ultimos_digitos)
VALUES
(1, 'Ana Ribeiro', 'comprador01@seed.livrofacil.test', '1993-04-12', 'Feminino', 'Rua das Acácias', '101', 'Centro', 'São Paulo', 'SP', '01001-001', 'VISA', '4101'),
(2, 'Bruno Martins', 'comprador02@seed.livrofacil.test', '1988-09-23', 'Masculino', 'Avenida Central', '202', 'Jardim América', 'Rio de Janeiro', 'RJ', '20010-000', 'MASTERCARD', '5102'),
(3, 'Camila Souza', 'comprador03@seed.livrofacil.test', '1997-01-08', 'Feminino', 'Rua do Comércio', '303', 'Centro', 'Belo Horizonte', 'MG', '30110-000', 'ELO', '6303'),
(4, 'Daniel Costa', 'comprador04@seed.livrofacil.test', '1985-06-17', 'Masculino', 'Rua das Flores', '404', 'Pinheiros', 'São Paulo', 'SP', '05422-001', 'VISA', '4104'),
(5, 'Eduarda Lima', 'comprador05@seed.livrofacil.test', '1999-11-02', 'Feminino', 'Avenida Brasil', '505', 'Copacabana', 'Rio de Janeiro', 'RJ', '22040-001', 'MASTERCARD', '5105'),
(6, 'Felipe Alves', 'comprador06@seed.livrofacil.test', '1991-02-26', 'Masculino', 'Rua dos Ipês', '606', 'Savassi', 'Belo Horizonte', 'MG', '30140-071', 'ELO', '6306'),
(7, 'Gabriela Rocha', 'comprador07@seed.livrofacil.test', '1994-07-14', 'Feminino', 'Rua Augusta', '707', 'Consolação', 'São Paulo', 'SP', '01305-000', 'VISA', '4107'),
(8, 'Henrique Barros', 'comprador08@seed.livrofacil.test', '1987-03-30', 'Masculino', 'Rua da Praia', '808', 'Centro', 'Porto Alegre', 'RS', '90010-150', 'MASTERCARD', '5108'),
(9, 'Isabela Mendes', 'comprador09@seed.livrofacil.test', '2000-12-05', 'Feminino', 'Rua Sete de Setembro', '909', 'Centro', 'Curitiba', 'PR', '80020-100', 'ELO', '6309'),
(10, 'João Ferreira', 'comprador10@seed.livrofacil.test', '1990-10-19', 'Masculino', 'Avenida Afonso Pena', '110', 'Funcionários', 'Belo Horizonte', 'MG', '30130-003', 'VISA', '4110'),
(11, 'Karina Oliveira', 'comprador11@seed.livrofacil.test', '1996-05-21', 'Feminino', 'Rua XV de Novembro', '211', 'Centro', 'Curitiba', 'PR', '80020-310', 'MASTERCARD', '5111'),
(12, 'Lucas Pereira', 'comprador12@seed.livrofacil.test', '1984-08-11', 'Masculino', 'Rua Oscar Freire', '312', 'Jardins', 'São Paulo', 'SP', '01426-001', 'ELO', '6312'),
(13, 'Mariana Gomes', 'comprador13@seed.livrofacil.test', '1998-02-09', 'Feminino', 'Rua da Bahia', '413', 'Centro', 'Belo Horizonte', 'MG', '30160-011', 'VISA', '4113'),
(14, 'Nicolas Carvalho', 'comprador14@seed.livrofacil.test', '1992-11-27', 'Masculino', 'Rua Voluntários da Pátria', '514', 'Botafogo', 'Rio de Janeiro', 'RJ', '22270-010', 'MASTERCARD', '5114'),
(15, 'Olívia Santos', 'comprador15@seed.livrofacil.test', '1989-01-16', 'Feminino', 'Rua Padre Chagas', '615', 'Moinhos de Vento', 'Porto Alegre', 'RS', '90570-080', 'ELO', '6315'),
(16, 'Pedro Nascimento', 'comprador16@seed.livrofacil.test', '1995-09-04', 'Masculino', 'Rua Harmonia', '716', 'Vila Madalena', 'São Paulo', 'SP', '05435-000', 'VISA', '4116'),
(17, 'Queila Batista', 'comprador17@seed.livrofacil.test', '1986-04-28', 'Feminino', 'Avenida Paraná', '817', 'Centro', 'Curitiba', 'PR', '80010-000', 'MASTERCARD', '5117'),
(18, 'Rafael Dias', 'comprador18@seed.livrofacil.test', '1993-12-13', 'Masculino', 'Rua dos Andradas', '918', 'Centro Histórico', 'Porto Alegre', 'RS', '90020-005', 'ELO', '6318'),
(19, 'Sofia Teixeira', 'comprador19@seed.livrofacil.test', '2001-06-06', 'Feminino', 'Rua da Consolação', '119', 'Consolação', 'São Paulo', 'SP', '01301-000', 'VISA', '4119'),
(20, 'Tiago Moreira', 'comprador20@seed.livrofacil.test', '1983-03-18', 'Masculino', 'Rua Gonçalves Dias', '220', 'Lourdes', 'Belo Horizonte', 'MG', '30140-091', 'MASTERCARD', '5120'),
(21, 'Úrsula Freitas', 'comprador21@seed.livrofacil.test', '1997-10-25', 'Feminino', 'Rua das Laranjeiras', '321', 'Laranjeiras', 'Rio de Janeiro', 'RJ', '22240-003', 'ELO', '6321'),
(22, 'Vitor Azevedo', 'comprador22@seed.livrofacil.test', '1990-01-31', 'Masculino', 'Rua Marechal Deodoro', '422', 'Centro', 'Curitiba', 'PR', '80010-010', 'VISA', '4122'),
(23, 'William Cardoso', 'comprador23@seed.livrofacil.test', '1988-05-15', 'Masculino', 'Rua dos Pinheiros', '523', 'Pinheiros', 'São Paulo', 'SP', '05422-000', 'MASTERCARD', '5123'),
(24, 'Yasmin Araújo', 'comprador24@seed.livrofacil.test', '1999-08-07', 'Feminino', 'Avenida Ipiranga', '624', 'República', 'São Paulo', 'SP', '01046-010', 'ELO', '6324');

-- ISBNs referem-se ao catálogo criado pela carga de livros anterior.
INSERT INTO seed_dash_compras
(numero, status, dias_atras, isbn_a, quantidade_a, isbn_b, quantidade_b, parcelas)
VALUES
(1, 'FINALIZADO', 5,   '9788194215813', 2, '9780099419785', 1, 2),
(2, 'ENTREGUE', 12,    '9780099419785', 1, NULL, 0, 1),
(3, 'ENTREGUE', 21,    '9783730600009', 1, '9787511024831', 1, 3),
(4, 'NA_TRANSPORTADORA', 34, '9787511024831', 1, '9780582096707', 1, 2),
(5, 'EM_ROTA_DE_ENTREGA', 45, '9780582096707', 1, NULL, 0, 1),
(6, 'EM_SEPARACAO', 60, '9798542843742', 1, '9788415089452', 1, 2),
(7, 'PAGAMENTO_APROVADO', 75, '9788415089452', 1, NULL, 0, 1),
(8, 'PAGAMENTO_APROVADO', 2, '9783125777705', 1, NULL, 0, 1),
(9, 'EM_SEPARACAO', 3,    '9780008537722', 1, NULL, 0, 1),
(10, 'NA_TRANSPORTADORA', 4, '9781408855652', 1, NULL, 0, 1),
(11, 'CANCELADO', 30, '9780007323128', 1, NULL, 0, 1),
(12, 'FINALIZADO', 120, '9780441013593', 1, '9781663607980', 1, 3),
(13, 'FINALIZADO', 135, '9780006546061', 1, NULL, 0, 2),
(14, 'ENTREGUE', 150, '9786074536782', 1, '9781410427922', 1, 1),
(15, 'FINALIZADO', 165, '9781784874872', 1, NULL, 0, 2),
(16, 'ENTREGUE', 180, '9780735284296', 1, '9780553175219', 1, 3),
(17, 'FINALIZADO', 195, '9781408845820', 2, NULL, 0, 1),
(18, 'NA_TRANSPORTADORA', 210, '9781594489518', 1, NULL, 0, 2),
(19, 'FINALIZADO', 225, '9781946963147', 1, '9780061122415', 1, 2),
(20, 'ENTREGUE', 240, '9780063212220', 1, NULL, 0, 1),
(21, 'FINALIZADO', 255, '9781987146127', 1, NULL, 0, 2),
(22, 'CANCELADO', 270, '9780735211292', 1, NULL, 0, 1),
(23, 'FINALIZADO', 300, '9781785044229', 1, '9780804830805', 1, 3),
(24, 'FINALIZADO', 330, '9781410427922', 1, '9780394502946', 1, 2);

DO $$
BEGIN
    IF (SELECT count(*) FROM seed_dash_clientes) <> 24
       OR (SELECT count(*) FROM seed_dash_compras) <> 24 THEN
        RAISE EXCEPTION 'A carga de demonstração deve conter 24 clientes e 24 pedidos.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'pedido' AND column_name = 'ped_frete'
    ) OR NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'pedido' AND column_name = 'ped_entregue_em'
    ) THEN
        RAISE EXCEPTION 'Schema de compras desatualizado. Inicie a aplicação uma vez para aplicar as migrations antes desta carga.';
    END IF;

    IF (
        SELECT count(DISTINCT b.liv_isbn)
        FROM (
            SELECT isbn_a AS isbn FROM seed_dash_compras
            UNION
            SELECT isbn_b AS isbn FROM seed_dash_compras WHERE isbn_b IS NOT NULL
        ) referencias
        JOIN livro b ON b.liv_isbn = referencias.isbn
    ) <> (
        SELECT count(DISTINCT isbn)
        FROM (
            SELECT isbn_a AS isbn FROM seed_dash_compras
            UNION
            SELECT isbn_b AS isbn FROM seed_dash_compras WHERE isbn_b IS NOT NULL
        ) referencias
    ) THEN
        RAISE EXCEPTION 'Não encontrei todos os livros referenciados. Execute primeiro a carga de livros e confira os ISBNs.';
    END IF;
END $$;

-- Clientes sintéticos. IDs UUID derivados do e-mail tornam a carga repetível.
WITH faltantes AS (
    SELECT s.*,
           row_number() OVER (ORDER BY s.numero) AS sequencia
    FROM seed_dash_clientes s
    WHERE NOT EXISTS (SELECT 1 FROM cliente c WHERE c.cli_email = s.email)
), base AS (
    SELECT COALESCE(MAX(cli_numero_registro), 0) AS maior_registro FROM cliente
)
INSERT INTO cliente (
    cli_id, cli_numero_registro, cli_nome, cli_email, cli_senha, cli_cpf, cli_telefone,
    cli_data_cadastro, cli_data_nascimento, cli_genero, cli_ativo, cli_data_exclusao,
    cli_dados_anonimizados, cli_perfil
)
SELECT
    (substr(md5('livrofacil-dashboard:' || f.email), 1, 8) || '-' ||
     substr(md5('livrofacil-dashboard:' || f.email), 9, 4) || '-' ||
     substr(md5('livrofacil-dashboard:' || f.email), 13, 4) || '-' ||
     substr(md5('livrofacil-dashboard:' || f.email), 17, 4) || '-' ||
     substr(md5('livrofacil-dashboard:' || f.email), 21, 12))::uuid,
    base.maior_registro + f.sequencia,
    f.nome,
    f.email,
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - interval '500 days' + (f.numero * interval '1 day'),
    f.nascimento,
    f.genero,
    'S',
    NULL,
    FALSE,
    'CLIENTE'
FROM faltantes f
CROSS JOIN base
ON CONFLICT DO NOTHING;

-- Um endereço principal por cliente, sem depender de IDs sequenciais.
INSERT INTO endereco (
    cli_id, end_tipo, end_logradouro, end_numero, end_complemento,
    end_bairro, end_cidade, end_estado, end_cep, end_principal
)
SELECT c.cli_id, 'Residencial', s.logradouro, s.endereco_numero, 'Apto ' || s.numero,
       s.bairro, s.cidade, s.estado, s.cep, 'S'
FROM seed_dash_clientes s
JOIN cliente c ON c.cli_email = s.email
WHERE NOT EXISTS (
    SELECT 1 FROM endereco e
    WHERE e.cli_id = c.cli_id AND e.end_principal = 'S'
      AND e.end_logradouro = s.logradouro AND e.end_numero = s.endereco_numero
);

-- Cartões fictícios armazenam apenas bandeira e últimos quatro dígitos.
INSERT INTO forma_pagamento (
    cli_id, for_pag_nome_titular, for_pag_tipo_cartao, for_pag_ultimos_digitos,
    for_pag_validade, for_pag_bandeira, for_pag_preferencial, for_pag_ativo, for_pag_criado_em
)
SELECT c.cli_id, s.nome, 'CREDITO', s.ultimos_digitos, '12/30', s.bandeira, 'S', 'S', CURRENT_TIMESTAMP
FROM seed_dash_clientes s
JOIN cliente c ON c.cli_email = s.email
WHERE NOT EXISTS (
    SELECT 1 FROM forma_pagamento f
    WHERE f.cli_id = c.cli_id AND f.for_pag_tipo_cartao = 'CREDITO'
      AND f.for_pag_bandeira = s.bandeira AND f.for_pag_ultimos_digitos = s.ultimos_digitos
);

-- Carrinho estável por cliente; um carrinho representa no máximo um pedido desta carga.
INSERT INTO carrinho (cli_id, car_token, car_atualizado_em)
SELECT c.cli_id, 'SEED-DASH-' || lpad(s.numero::text, 2, '0'), CURRENT_TIMESTAMP
FROM seed_dash_clientes s
JOIN cliente c ON c.cli_email = s.email
ON CONFLICT (car_token) DO NOTHING;

-- Primeiro registra quais pedidos ainda não existem; isso controla a idempotência
-- e permite associar os IDs identity sem uma CTE de escrita aninhada.
INSERT INTO seed_dash_pedidos_criados (numero)
SELECT s.numero
FROM seed_dash_compras s
JOIN seed_dash_clientes sc ON sc.numero = s.numero
JOIN cliente c ON c.cli_email = sc.email
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-DASH-' || lpad(s.numero::text, 2, '0')
WHERE NOT EXISTS (
    SELECT 1 FROM pedido existente
    WHERE existente.cli_id = c.cli_id AND existente.car_id = car.car_id
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
    CASE WHEN s.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT')
         THEN CURRENT_TIMESTAMP ELSE valores.criado_em + interval '2 hours' END,
    CASE WHEN s.status IN ('ENTREGUE', 'FINALIZADO')
         THEN valores.criado_em + interval '5 days' ELSE NULL END,
    CASE WHEN s.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT')
         THEN CURRENT_TIMESTAMP + interval '14 days' ELSE NULL END,
    valores.subtotal,
    0.00,
    frete.valor_frete,
    valores.subtotal + frete.valor_frete,
    NULL,
    endereco.texto,
    endereco.texto
FROM seed_dash_compras s
JOIN seed_dash_pedidos_criados novo ON novo.numero = s.numero
JOIN seed_dash_clientes sc ON sc.numero = s.numero
JOIN cliente c ON c.cli_email = sc.email
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-DASH-' || lpad(s.numero::text, 2, '0')
JOIN livro livro_a ON livro_a.liv_isbn = s.isbn_a
LEFT JOIN livro livro_b ON livro_b.liv_isbn = s.isbn_b
CROSS JOIN LATERAL (
    SELECT CURRENT_TIMESTAMP - (s.dias_atras * interval '1 day')
                           - (s.numero * interval '5 minutes') AS criado_em,
           livro_a.liv_valor_venda * s.quantidade_a
             + COALESCE(livro_b.liv_valor_venda * s.quantidade_b, 0) AS subtotal
) valores
CROSS JOIN LATERAL (
    SELECT CASE
        WHEN valores.subtotal >= 150.00 THEN 0.00::numeric
        WHEN sc.estado = 'SP' THEN 19.90::numeric
        ELSE 29.90::numeric
    END AS valor_frete
) frete
CROSS JOIN LATERAL (
    SELECT sc.logradouro || ', ' || sc.endereco_numero || ' - ' || sc.bairro || ', '
           || sc.cidade || '/' || sc.estado || ' - CEP ' || sc.cep AS texto
) endereco;

UPDATE seed_dash_pedidos_criados novo
SET ped_id = p.ped_id
FROM seed_dash_compras s
JOIN seed_dash_clientes sc ON sc.numero = s.numero
JOIN cliente c ON c.cli_email = sc.email
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-DASH-' || lpad(s.numero::text, 2, '0')
JOIN pedido p ON p.cli_id = c.cli_id AND p.car_id = car.car_id
WHERE novo.numero = s.numero;

-- Aborta antes de movimentar estoque caso os pedidos novos excedam disponibilidade.
DO $$
BEGIN
    IF EXISTS (
        WITH itens AS (
            SELECT s.status, l.liv_id, s.quantidade_a AS quantidade
            FROM seed_dash_pedidos_criados n
            JOIN seed_dash_compras s ON s.numero = n.numero
            JOIN livro l ON l.liv_isbn = s.isbn_a
            UNION ALL
            SELECT s.status, l.liv_id, s.quantidade_b AS quantidade
            FROM seed_dash_pedidos_criados n
            JOIN seed_dash_compras s ON s.numero = n.numero
            JOIN livro l ON l.liv_isbn = s.isbn_b
            WHERE s.isbn_b IS NOT NULL
        ), demanda AS (
            SELECT liv_id,
                   COALESCE(sum(quantidade) FILTER (WHERE status IN (
                       'PAGAMENTO_APROVADO', 'EM_SEPARACAO', 'NA_TRANSPORTADORA',
                       'EM_ROTA_DE_ENTREGA', 'ENTREGUE', 'FINALIZADO'
                   )), 0) AS vendidos,
                   COALESCE(sum(quantidade) FILTER (WHERE status IN (
                       'PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT'
                   )), 0) AS reservados
            FROM itens
            GROUP BY liv_id
        )
        SELECT 1
        FROM demanda d
        JOIN estoque e ON e.liv_id = d.liv_id
        WHERE e.est_quantidade_disponivel < d.vendidos + d.reservados
    ) THEN
        RAISE EXCEPTION 'Estoque insuficiente para os pedidos de demonstração novos.';
    END IF;
END $$;

-- Itens de pedido mantêm título e preço históricos da edição vendida.
INSERT INTO item_pedido (ped_id, liv_id, ite_ped_titulo, ite_ped_quantidade, ite_ped_valor_unitario)
SELECT n.ped_id, l.liv_id, l.liv_titulo, s.quantidade_a, l.liv_valor_venda
FROM seed_dash_pedidos_criados n
JOIN seed_dash_compras s ON s.numero = n.numero
JOIN livro l ON l.liv_isbn = s.isbn_a
UNION ALL
SELECT n.ped_id, l.liv_id, l.liv_titulo, s.quantidade_b, l.liv_valor_venda
FROM seed_dash_pedidos_criados n
JOIN seed_dash_compras s ON s.numero = n.numero
JOIN livro l ON l.liv_isbn = s.isbn_b
WHERE s.isbn_b IS NOT NULL;

-- Registra um pagamento integral nos pedidos já aprovados/em atendimento.
INSERT INTO pagamento_pedido (ped_id, for_pag_id, pag_ped_valor, pag_ped_parcelas)
SELECT n.ped_id, f.for_pag_id, p.ped_total, s.parcelas
FROM seed_dash_pedidos_criados n
JOIN seed_dash_compras s ON s.numero = n.numero
JOIN pedido p ON p.ped_id = n.ped_id
JOIN seed_dash_clientes sc ON sc.numero = s.numero
JOIN cliente c ON c.cli_email = sc.email
JOIN forma_pagamento f ON f.cli_id = c.cli_id
                       AND f.for_pag_tipo_cartao = 'CREDITO'
                       AND f.for_pag_bandeira = sc.bandeira
                       AND f.for_pag_ultimos_digitos = sc.ultimos_digitos
WHERE s.status IN (
    'PAGAMENTO_APROVADO', 'EM_SEPARACAO', 'NA_TRANSPORTADORA',
    'EM_ROTA_DE_ENTREGA', 'ENTREGUE', 'FINALIZADO'
);

-- Mantém estoque coerente com a criação de cada pedido, sem repetir o movimento em reexecuções.
WITH itens AS (
    SELECT s.status, l.liv_id, s.quantidade_a AS quantidade
    FROM seed_dash_pedidos_criados n
    JOIN seed_dash_compras s ON s.numero = n.numero
    JOIN livro l ON l.liv_isbn = s.isbn_a
    UNION ALL
    SELECT s.status, l.liv_id, s.quantidade_b AS quantidade
    FROM seed_dash_pedidos_criados n
    JOIN seed_dash_compras s ON s.numero = n.numero
    JOIN livro l ON l.liv_isbn = s.isbn_b
    WHERE s.isbn_b IS NOT NULL
), demanda AS (
    SELECT liv_id,
           COALESCE(sum(quantidade) FILTER (WHERE status IN (
               'PAGAMENTO_APROVADO', 'EM_SEPARACAO', 'NA_TRANSPORTADORA',
               'EM_ROTA_DE_ENTREGA', 'ENTREGUE', 'FINALIZADO'
           )), 0) AS vendidos,
           COALESCE(sum(quantidade) FILTER (WHERE status IN (
               'PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT'
           )), 0) AS reservados
    FROM itens
    GROUP BY liv_id
)
UPDATE estoque e
SET est_quantidade_disponivel = e.est_quantidade_disponivel - d.vendidos - d.reservados,
    est_quantidade_bloqueada = e.est_quantidade_bloqueada + d.reservados,
    est_quantidade_vendida = e.est_quantidade_vendida + d.vendidos
FROM demanda d
WHERE e.liv_id = d.liv_id;

COMMIT;

-- Validação: 24 clientes e seus pedidos, com itens, pagamentos e status.
SELECT
    c.cli_nome AS cliente,
    c.cli_email AS email_teste,
    p.ped_id AS pedido,
    p.ped_status AS status,
    p.ped_criado_em AS criado_em,
    string_agg(ip.ite_ped_titulo || ' x' || ip.ite_ped_quantidade, ', ' ORDER BY ip.ite_ped_titulo) AS itens,
    p.ped_subtotal AS subtotal,
    p.ped_frete AS frete,
    p.ped_total AS total,
    count(DISTINCT pg.pag_ped_id) AS pagamentos
FROM seed_dash_compras s
JOIN seed_dash_clientes sc ON sc.numero = s.numero
JOIN cliente c ON c.cli_email = sc.email
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-DASH-' || lpad(s.numero::text, 2, '0')
JOIN pedido p ON p.cli_id = c.cli_id AND p.car_id = car.car_id
LEFT JOIN item_pedido ip ON ip.ped_id = p.ped_id
LEFT JOIN pagamento_pedido pg ON pg.ped_id = p.ped_id
GROUP BY c.cli_nome, c.cli_email, p.ped_id
ORDER BY p.ped_criado_em DESC;

-- Resumo por status para conferir a variedade no dashboard.
SELECT p.ped_status, count(*) AS quantidade
FROM seed_dash_compras s
JOIN seed_dash_clientes sc ON sc.numero = s.numero
JOIN cliente c ON c.cli_email = sc.email
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-DASH-' || lpad(s.numero::text, 2, '0')
JOIN pedido p ON p.cli_id = c.cli_id AND p.car_id = car.car_id
GROUP BY p.ped_status
ORDER BY p.ped_status;

-- Série mensal de vendas reconhecidas (mesma regra do histórico do backend).
SELECT date_trunc('month', p.ped_criado_em)::date AS mes,
       count(DISTINCT p.ped_id) AS pedidos_concluidos,
       COALESCE(sum(ip.ite_ped_valor_unitario * ip.ite_ped_quantidade), 0)::numeric(12,2) AS valor_itens
FROM pedido p
JOIN item_pedido ip ON ip.ped_id = p.ped_id
JOIN carrinho car ON car.car_id = p.car_id
WHERE car.car_token LIKE 'SEED-DASH-%'
  AND p.ped_status IN ('ENTREGUE', 'FINALIZADO')
GROUP BY date_trunc('month', p.ped_criado_em)
ORDER BY mes;

-- Checagens que devem retornar zero linhas.
SELECT c.cli_email, p.ped_id
FROM seed_dash_clientes s
JOIN cliente c ON c.cli_email = s.email
JOIN carrinho car ON car.cli_id = c.cli_id
                AND car.car_token = 'SEED-DASH-' || lpad(s.numero::text, 2, '0')
JOIN pedido p ON p.car_id = car.car_id
LEFT JOIN item_pedido ip ON ip.ped_id = p.ped_id
GROUP BY c.cli_email, p.ped_id
HAVING count(ip.ite_ped_id) = 0;
