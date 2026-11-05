-- Repair databases where earlier migrations ran while the troca table was
-- already present with nullable or missing feature columns.
DO $$
BEGIN
    IF to_regclass('public.troca') IS NOT NULL THEN
        ALTER TABLE public.troca ADD COLUMN IF NOT EXISTS tro_tipo VARCHAR(20);
        ALTER TABLE public.troca ADD COLUMN IF NOT EXISTS tro_quantidade INTEGER;
        ALTER TABLE public.troca ADD COLUMN IF NOT EXISTS tro_decidido_em TIMESTAMP WITHOUT TIME ZONE;
        ALTER TABLE public.troca ADD COLUMN IF NOT EXISTS tro_recebido_em TIMESTAMP WITHOUT TIME ZONE;
        ALTER TABLE public.troca ADD COLUMN IF NOT EXISTS tro_recebido_quantidade INTEGER;
        ALTER TABLE public.troca ADD COLUMN IF NOT EXISTS tro_retornar_estoque BOOLEAN;
        ALTER TABLE public.troca ADD COLUMN IF NOT EXISTS tro_credito_em TIMESTAMP WITHOUT TIME ZONE;

        UPDATE public.troca SET tro_tipo = 'TROCA' WHERE tro_tipo IS NULL;
        UPDATE public.troca SET tro_retornar_estoque = FALSE WHERE tro_retornar_estoque IS NULL;

        ALTER TABLE public.troca ALTER COLUMN tro_tipo SET DEFAULT 'TROCA';
        ALTER TABLE public.troca ALTER COLUMN tro_tipo SET NOT NULL;
        ALTER TABLE public.troca ALTER COLUMN tro_retornar_estoque SET DEFAULT FALSE;
        ALTER TABLE public.troca ALTER COLUMN tro_retornar_estoque SET NOT NULL;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'troca' AND column_name = 'tro_status'
        ) THEN
            ALTER TABLE public.troca ALTER COLUMN tro_status TYPE VARCHAR(30);
        END IF;
    END IF;
END $$;