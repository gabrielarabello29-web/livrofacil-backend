-- Canonicalize persisted statuses without deleting orders, exchanges, or history.
-- The Java enums retain legacy constants temporarily for safe reads while deploys roll out.

DO $$
DECLARE constraint_record record;
BEGIN
    IF to_regclass('public.pedido') IS NOT NULL THEN
        FOR constraint_record IN
            SELECT conname
            FROM pg_constraint
            WHERE conrelid = 'public.pedido'::regclass
              AND contype = 'c'
              AND pg_get_constraintdef(oid) ILIKE '%ped_status%'
        LOOP
            EXECUTE format('ALTER TABLE public.pedido DROP CONSTRAINT %I', constraint_record.conname);
        END LOOP;

        UPDATE public.pedido
        SET ped_status = CASE ped_status
            WHEN 'PAGAMENTO_APROVADO' THEN 'APROVADA'
            WHEN 'EM_SEPARACAO' THEN 'APROVADA'
            WHEN 'NA_TRANSPORTADORA' THEN 'EM_TRANSPORTE'
            WHEN 'EM_ROTA_DE_ENTREGA' THEN 'EM_TRANSPORTE'
            ELSE ped_status
        END
        WHERE ped_status IN (
            'PAGAMENTO_APROVADO', 'EM_SEPARACAO', 'NA_TRANSPORTADORA', 'EM_ROTA_DE_ENTREGA'
        );

        ALTER TABLE public.pedido
            ADD CONSTRAINT ck_pedido_status_validos CHECK (ped_status IN (
                'PENDENTE', 'AGUARDANDO_PAGAMENTO', 'EM_CHECKOUT', 'EM_PROCESSAMENTO',
                'APROVADA', 'REPROVADA', 'EM_TRANSPORTE', 'ENTREGUE', 'FINALIZADO', 'CANCELADO'
            ));
    END IF;

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

        ALTER TABLE public.troca
            ADD CONSTRAINT ck_troca_status_validos CHECK (tro_status IN (
                'EM_TROCA', 'TROCA_AUTORIZADA', 'RECEBIDA', 'TROCADO', 'RECUSADA'
            ));
    END IF;
END $$;
