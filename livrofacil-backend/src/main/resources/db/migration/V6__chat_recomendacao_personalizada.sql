CREATE TABLE IF NOT EXISTS chat_sessao (
    ses_id UUID PRIMARY KEY,
    cli_id UUID NOT NULL REFERENCES cliente (cli_id) ON DELETE CASCADE,
    ses_criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ses_atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_chat_sessao_id_cliente UNIQUE (ses_id, cli_id)
);

CREATE INDEX IF NOT EXISTS idx_chat_sessao_cliente_atualizado
    ON chat_sessao (cli_id, ses_atualizado_em DESC);

CREATE TABLE IF NOT EXISTS chat_mensagem (
    men_id UUID PRIMARY KEY,
    ses_id UUID NOT NULL REFERENCES chat_sessao (ses_id) ON DELETE CASCADE,
    men_tipo VARCHAR(10) NOT NULL,
    men_texto TEXT NOT NULL,
    men_criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_chat_mensagem_tipo CHECK (men_tipo IN ('CLIENTE', 'IA'))
);

CREATE INDEX IF NOT EXISTS idx_chat_mensagem_sessao_criado
    ON chat_mensagem (ses_id, men_criado_em DESC);

CREATE TABLE IF NOT EXISTS chat_mensagem_livro (
    men_id UUID NOT NULL REFERENCES chat_mensagem (men_id) ON DELETE CASCADE,
    liv_id BIGINT NOT NULL REFERENCES livro (liv_id),
    ordem INTEGER NOT NULL,
    PRIMARY KEY (men_id, liv_id),
    CONSTRAINT uk_chat_mensagem_livro_ordem UNIQUE (men_id, ordem)
);

CREATE TABLE IF NOT EXISTS chat_feedback (
    fee_id UUID PRIMARY KEY,
    cli_id UUID NOT NULL REFERENCES cliente (cli_id) ON DELETE CASCADE,
    men_id UUID NOT NULL REFERENCES chat_mensagem (men_id) ON DELETE CASCADE,
    fee_avaliacao VARCHAR(10) NOT NULL,
    fee_comentario TEXT NOT NULL,
    fee_criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fee_atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_chat_feedback_cliente_mensagem UNIQUE (cli_id, men_id),
    CONSTRAINT ck_chat_feedback_avaliacao CHECK (fee_avaliacao IN ('UTIL', 'NAO_UTIL'))
);

CREATE TABLE IF NOT EXISTS chat_preferencia (
    pref_id UUID PRIMARY KEY,
    cli_id UUID NOT NULL UNIQUE REFERENCES cliente (cli_id) ON DELETE CASCADE,
    pref_preco_minimo NUMERIC(10, 2),
    pref_preco_maximo NUMERIC(10, 2),
    CONSTRAINT ck_chat_preferencia_precos CHECK (
        (pref_preco_minimo IS NULL OR pref_preco_minimo >= 0)
        AND (pref_preco_maximo IS NULL OR pref_preco_maximo >= 0)
        AND (pref_preco_minimo IS NULL OR pref_preco_maximo IS NULL
             OR pref_preco_maximo >= pref_preco_minimo)
    )
);

CREATE TABLE IF NOT EXISTS chat_preferencia_categoria (
    pref_id UUID NOT NULL REFERENCES chat_preferencia (pref_id) ON DELETE CASCADE,
    cat_id BIGINT NOT NULL REFERENCES categoria (cat_id),
    PRIMARY KEY (pref_id, cat_id)
);

CREATE TABLE IF NOT EXISTS chat_preferencia_tema (
    pref_id UUID NOT NULL REFERENCES chat_preferencia (pref_id) ON DELETE CASCADE,
    ordem INTEGER NOT NULL,
    tem_tema VARCHAR(100) NOT NULL,
    PRIMARY KEY (pref_id, ordem)
);
