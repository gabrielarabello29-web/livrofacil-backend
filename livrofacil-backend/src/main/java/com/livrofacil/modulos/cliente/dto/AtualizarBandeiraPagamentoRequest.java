package com.livrofacil.modulos.cliente.dto;

import jakarta.validation.constraints.NotNull;

public class AtualizarBandeiraPagamentoRequest {
    @NotNull(message = "A disponibilidade da bandeira e obrigatoria")
    private Boolean disponivel;

    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }
}
