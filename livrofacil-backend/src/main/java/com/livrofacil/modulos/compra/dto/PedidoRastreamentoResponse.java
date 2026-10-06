package com.livrofacil.modulos.compra.dto;

import java.time.LocalDateTime;

public class PedidoRastreamentoResponse {
    private final String etapa;
    private final LocalDateTime ultimaModificacao;

    public PedidoRastreamentoResponse(String etapa, LocalDateTime ultimaModificacao) {
        this.etapa = etapa;
        this.ultimaModificacao = ultimaModificacao;
    }

    public String getEtapa() { return etapa; }
    public LocalDateTime getUltimaModificacao() { return ultimaModificacao; }
}
