package com.livrofacil.modulos.troca.entity;

public enum StatusTroca {
    SOLICITADA,
    AGUARDANDO_RECEBIMENTO,
    RECEBIDA,
    CONCLUIDA,
    // Mantidos para leitura de registros produzidos pela versão anterior.
    AUTORIZADA,
    RECUSADA,
    TROCADA
}