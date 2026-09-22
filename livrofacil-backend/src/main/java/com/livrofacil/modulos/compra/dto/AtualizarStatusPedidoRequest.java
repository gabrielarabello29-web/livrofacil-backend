package com.livrofacil.modulos.compra.dto;

import com.livrofacil.modulos.compra.entity.StatusPedido;
import jakarta.validation.constraints.NotNull;

public class AtualizarStatusPedidoRequest {
    @NotNull(message = "O status e obrigatorio")
    private StatusPedido status;

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
}