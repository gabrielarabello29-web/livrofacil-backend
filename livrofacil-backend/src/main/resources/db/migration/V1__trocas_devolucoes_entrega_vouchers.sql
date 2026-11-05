ALTER TABLE IF EXISTS pedido ADD COLUMN IF NOT EXISTS ped_entregue_em TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE IF EXISTS troca ADD COLUMN IF NOT EXISTS tro_tipo VARCHAR(20) NOT NULL DEFAULT 'TROCA';
ALTER TABLE IF EXISTS troca ADD COLUMN IF NOT EXISTS tro_quantidade INTEGER;
ALTER TABLE IF EXISTS troca ADD COLUMN IF NOT EXISTS tro_decidido_em TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE IF EXISTS troca ADD COLUMN IF NOT EXISTS tro_recebido_em TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE IF EXISTS troca ADD COLUMN IF NOT EXISTS tro_recebido_quantidade INTEGER;
ALTER TABLE IF EXISTS troca ADD COLUMN IF NOT EXISTS tro_retornar_estoque BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS troca ADD COLUMN IF NOT EXISTS tro_credito_em TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE IF EXISTS troca ALTER COLUMN tro_status TYPE VARCHAR(30);

ALTER TABLE IF EXISTS voucher_troca ADD COLUMN IF NOT EXISTS vot_origem_id BIGINT;
ALTER TABLE IF EXISTS voucher_troca ADD COLUMN IF NOT EXISTS vot_valor_original NUMERIC(12, 2);

CREATE TABLE IF NOT EXISTS troca_historico (
    tha_id BIGSERIAL PRIMARY KEY,
    tro_id BIGINT NOT NULL,
    tha_status VARCHAR(30) NOT NULL,
    tha_observacao VARCHAR(1000),
    tha_criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_troca_historico_tro_id_data
    ON troca_historico (tro_id, tha_criado_em);
DO $$
BEGIN
    IF to_regclass('troca') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_troca_historico_troca') THEN
        ALTER TABLE troca_historico ADD CONSTRAINT fk_troca_historico_troca
            FOREIGN KEY (tro_id) REFERENCES troca (tro_id) ON DELETE CASCADE;
    END IF;
END $$;
DO $$
BEGIN
    IF to_regclass('troca') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_troca_status_tipo ON troca (tro_status, tro_tipo);
    END IF;
    IF to_regclass('voucher_troca') IS NOT NULL THEN
        UPDATE voucher_troca SET vot_valor_original = vot_valor WHERE vot_valor_original IS NULL;
        CREATE INDEX IF NOT EXISTS idx_voucher_troca_origem ON voucher_troca (vot_origem_id);
    END IF;
END $$;