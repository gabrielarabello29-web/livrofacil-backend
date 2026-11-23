package com.livrofacil.modulos.troca.entity;

public enum StatusTroca {
    EM_TROCA,
    TROCA_AUTORIZADA,
    RECEBIDA,
    TROCADO,
    RECUSADA,
    // Valores antigos mantidos para leitura durante a migração de dados.
    SOLICITADA,
    AGUARDANDO_RECEBIMENTO,
    CONCLUIDA,
    AUTORIZADA,
    TROCADA
}