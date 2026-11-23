-- Remove SEED50- do liv_codigo dos livros da carga de 50 obras.
-- Prévia: confira os códigos atuais e os códigos finais antes de executar a alteração.
-- Se o ISBN já estiver sendo usado como código por outro livro, o script aborta sem alterar dados.

BEGIN;

SELECT liv_id, liv_codigo AS codigo_atual, liv_isbn AS codigo_novo, liv_titulo
FROM public.livro
WHERE liv_codigo LIKE 'SEED50-%'
ORDER BY liv_codigo;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.livro alvo
        JOIN public.livro conflito
          ON conflito.liv_codigo = alvo.liv_isbn
         AND conflito.liv_id <> alvo.liv_id
        WHERE alvo.liv_codigo LIKE 'SEED50-%'
    ) THEN
        RAISE EXCEPTION 'Há conflito: outro livro já usa o ISBN como liv_codigo. Nenhum código foi alterado.';
    END IF;
END $$;

UPDATE public.livro
SET liv_codigo = liv_isbn
WHERE liv_codigo LIKE 'SEED50-%';

COMMIT;

-- Verificação: deve retornar zero linhas.
SELECT liv_id, liv_codigo, liv_titulo
FROM public.livro
WHERE liv_codigo LIKE 'SEED50-%'
ORDER BY liv_codigo;
