package com.livrofacil.modulos.compra.entity;

public enum StatusPedido {
    PENDENTE,
    AGUARDANDO_PAGAMENTO,
    EM_CHECKOUT,
    EM_PROCESSAMENTO,
    APROVADA,
    REPROVADA,
    EM_TRANSPORTE,
    // Valores antigos mantidos para leitura de bancos ainda não migrados.
    PAGAMENTO_APROVADO,
    EM_SEPARACAO,
    NA_TRANSPORTADORA,
    EM_ROTA_DE_ENTREGA,
    ENTREGUE,
    FINALIZADO,
    CANCELADO
}