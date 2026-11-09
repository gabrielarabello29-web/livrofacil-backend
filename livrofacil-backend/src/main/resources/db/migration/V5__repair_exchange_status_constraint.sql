-- Repair databases where V4 was already recorded but the legacy V2 CHECK
-- constraint remained in place. Drop status checks before normalizing data so
-- both legacy and canonical values can be reconciled safely.
DO $$
DECLARE
    constraint_record record;
BEGIN
    IF to_regclass('public.troca') IS NOT NULL THEN
        FOR constraint_record IN
            SELECT conname
            FROM pg_constraint
            WHERE conrelid = 'public.troca'::regclass
              AND contype = 'c'
              AND pg_get_constraintdef(oid) ILIKE '%tro_status%'
        LOOP
            EXECUTE format('ALTER TABLE public.troca DROP CONSTRAINT %I', constraint_record.conname);
        END LOOP;

        UPDATE public.troca
        SET tro_status = CASE tro_status
            WHEN 'SOLICITADA' THEN 'EM_TROCA'
            WHEN 'AGUARDANDO_RECEBIMENTO' THEN 'TROCA_AUTORIZADA'
            WHEN 'AUTORIZADA' THEN 'TROCA_AUTORIZADA'
            WHEN 'CONCLUIDA' THEN 'TROCADO'
            WHEN 'TROCADA' THEN 'TROCADO'
            ELSE tro_status
        END
        WHERE tro_status IN (
            'SOLICITADA', 'AGUARDANDO_RECEBIMENTO', 'AUTORIZADA', 'CONCLUIDA', 'TROCADA'
        );

        ALTER TABLE public.troca
            ADD CONSTRAINT ck_troca_status_validos CHECK (tro_status IN (
                'EM_TROCA', 'TROCA_AUTORIZADA', 'RECEBIDA', 'TROCADO', 'RECUSADA'
            ));
    END IF;

    IF to_regclass('public.troca_historico') IS NOT NULL THEN
        UPDATE public.troca_historico
        SET tha_status = CASE tha_status
            WHEN 'SOLICITADA' THEN 'EM_TROCA'
            WHEN 'AGUARDANDO_RECEBIMENTO' THEN 'TROCA_AUTORIZADA'
            WHEN 'AUTORIZADA' THEN 'TROCA_AUTORIZADA'
            WHEN 'CONCLUIDA' THEN 'TROCADO'
            WHEN 'TROCADA' THEN 'TROCADO'
            ELSE tha_status
        END
        WHERE tha_status IN (
            'SOLICITADA', 'AGUARDANDO_RECEBIMENTO', 'AUTORIZADA', 'CONCLUIDA', 'TROCADA'
        );
    END IF;
END $$;
