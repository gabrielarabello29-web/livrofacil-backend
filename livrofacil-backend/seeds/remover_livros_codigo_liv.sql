-- Remove os livros antigos cujo código começa com LIV.
-- Segurança: linhas referenciadas por item_pedido são preservadas para não quebrar
-- o histórico de compras; esses livros são desativados (liv_ativo = false).
-- Também remove estoque, categorias e itens de carrinho dos livros que puderem ser excluídos.
-- Execute no banco de desenvolvimento/teste. Revise o resultado da prévia antes de rodar.

BEGIN;

DROP TABLE IF EXISTS pg_temp.livros_liv_alvo;
DROP TABLE IF EXISTS pg_temp.livros_liv_excluiveis;

CREATE TEMPORARY TABLE livros_liv_alvo ON COMMIT PRESERVE ROWS AS
SELECT liv_id, liv_codigo, liv_titulo, liv_isbn, liv_imagem_url
FROM public.livro
WHERE upper(liv_codigo) LIKE 'LIV%';

-- PRÉVIA: livros com código LIV e respectivas referências em itens de pedido.
SELECT a.liv_id,
       a.liv_codigo,
       a.liv_titulo,
       a.liv_isbn,
       a.liv_imagem_url,
       EXISTS (SELECT 1 FROM public.item_pedido ip WHERE ip.liv_id = a.liv_id) AS possui_historico_pedido
FROM livros_liv_alvo a
ORDER BY a.liv_codigo;

CREATE TEMPORARY TABLE livros_liv_excluiveis ON COMMIT PRESERVE ROWS AS
SELECT a.liv_id
FROM livros_liv_alvo a
WHERE NOT EXISTS (
    SELECT 1 FROM public.item_pedido ip WHERE ip.liv_id = a.liv_id
);

-- Mantém a referência histórica e retira do catálogo livros que não podem ser apagados.
UPDATE public.livro l
SET liv_ativo = FALSE
FROM livros_liv_alvo a
WHERE l.liv_id = a.liv_id
  AND NOT EXISTS (
      SELECT 1 FROM livros_liv_excluiveis e WHERE e.liv_id = l.liv_id
  );

-- Remove dependências dos livros sem histórico de venda e, por fim, os livros.
DELETE FROM public.item_carrinho ic
USING livros_liv_excluiveis e
WHERE ic.liv_id = e.liv_id;

DELETE FROM public.livro_categoria lc
USING livros_liv_excluiveis e
WHERE lc.liv_id = e.liv_id;

DELETE FROM public.estoque est
USING livros_liv_excluiveis e
WHERE est.liv_id = e.liv_id;

DELETE FROM public.livro l
USING livros_liv_excluiveis e
WHERE l.liv_id = e.liv_id;

COMMIT;

-- Resultado: deve listar zero livros excluíveis restantes; livros com histórico ficam inativos.
SELECT l.liv_id, l.liv_codigo, l.liv_titulo, l.liv_ativo,
       EXISTS (SELECT 1 FROM public.item_pedido ip WHERE ip.liv_id = l.liv_id) AS possui_historico_pedido
FROM public.livro l
WHERE upper(l.liv_codigo) LIKE 'LIV%'
ORDER BY l.liv_codigo;

-- Contagens da operação desta sessão.
SELECT (SELECT count(*) FROM livros_liv_alvo) AS encontrados_com_codigo_liv,
       (SELECT count(*) FROM livros_liv_excluiveis) AS excluiveis_sem_pedido,
       (SELECT count(*) FROM livros_liv_alvo a
        WHERE EXISTS (SELECT 1 FROM public.item_pedido ip WHERE ip.liv_id = a.liv_id)) AS preservados_com_historico;
