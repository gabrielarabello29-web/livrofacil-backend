package com.livrofacil.modulos.troca.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class SolicitarTrocaRequest {
    @NotNull(message = "O cliente e obrigatorio")
    private UUID clienteId;

    @NotNull(message = "O pedido e obrigatorio")
    @Positive
    private Long pedidoId;

    @JsonAlias("produtoId")
    @Positive
    private Long livroId;

    @Positive
    private Long itemPedidoId;

    @NotBlank(message = "O motivo da troca e obrigatorio")
    private String motivo;

    private String detalhes;

    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }
    public Long getItemPedidoId() { return itemPedidoId; }
    public void setItemPedidoId(Long itemPedidoId) { this.itemPedidoId = itemPedidoId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
}