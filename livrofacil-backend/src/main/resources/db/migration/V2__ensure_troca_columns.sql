-- Repair existing tables whether V1 ran before or after Hibernate created troca.
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

        -- Old schema constraints only allowed the original exchange statuses.
        DO $inner$
        DECLARE constraint_record record;
        BEGIN
            FOR constraint_record IN
                SELECT conname
                FROM pg_constraint
                WHERE conrelid = 'public.troca'::regclass
                  AND contype = 'c'
                  AND pg_get_constraintdef(oid) ILIKE '%tro_status%'
            LOOP
                EXECUTE format('ALTER TABLE public.troca DROP CONSTRAINT %I', constraint_record.conname);
            END LOOP;
        END
        $inner$;

        ALTER TABLE public.troca DROP CONSTRAINT IF EXISTS ck_troca_status_validos;
        ALTER TABLE public.troca ADD CONSTRAINT ck_troca_status_validos
            CHECK (tro_status IN (
                'SOLICITADA', 'AGUARDANDO_RECEBIMENTO', 'RECEBIDA', 'CONCLUIDA',
                'AUTORIZADA', 'RECUSADA', 'TROCADA'
            ));

        CREATE INDEX IF NOT EXISTS idx_troca_status_tipo ON public.troca (tro_status, tro_tipo);
    END IF;

    IF to_regclass('public.pedido') IS NOT NULL THEN
        ALTER TABLE public.pedido ADD COLUMN IF NOT EXISTS ped_entregue_em TIMESTAMP WITHOUT TIME ZONE;
    END IF;

    IF to_regclass('public.voucher_troca') IS NOT NULL THEN
        ALTER TABLE public.voucher_troca ADD COLUMN IF NOT EXISTS vot_origem_id BIGINT;
        ALTER TABLE public.voucher_troca ADD COLUMN IF NOT EXISTS vot_valor_original NUMERIC(12, 2);
        UPDATE public.voucher_troca SET vot_valor_original = vot_valor WHERE vot_valor_original IS NULL;
        CREATE INDEX IF NOT EXISTS idx_voucher_troca_origem ON public.voucher_troca (vot_origem_id);
    END IF;

    IF to_regclass('public.troca_historico') IS NULL AND to_regclass('public.troca') IS NOT NULL THEN
        CREATE TABLE public.troca_historico (
            tha_id BIGSERIAL PRIMARY KEY,
            tro_id BIGINT NOT NULL REFERENCES public.troca (tro_id) ON DELETE CASCADE,
            tha_status VARCHAR(30) NOT NULL,
            tha_observacao VARCHAR(1000),
            tha_criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
    END IF;

    IF to_regclass('public.troca_historico') IS NOT NULL
       AND to_regclass('public.troca') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_troca_historico_troca') THEN
        ALTER TABLE public.troca_historico ADD CONSTRAINT fk_troca_historico_troca
            FOREIGN KEY (tro_id) REFERENCES public.troca (tro_id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_troca_historico_tro_id_data
    ON public.troca_historico (tro_id, tha_criado_em);