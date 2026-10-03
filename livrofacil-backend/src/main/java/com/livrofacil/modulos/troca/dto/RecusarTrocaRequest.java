package com.livrofacil.modulos.troca.dto;

import jakarta.validation.constraints.NotBlank;

public class RecusarTrocaRequest {
    @NotBlank(message = "O motivo da recusa e obrigatorio")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}