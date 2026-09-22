package com.livrofacil.modulos.compra.dto;

import com.livrofacil.modulos.compra.entity.ItemPedido;

import java.math.BigDecimal;

public class ItemPedidoResponse {
    private Long id;
    private Long pedidoId;
    private Long livroId;
    private Integer quantidade;
    private BigDecimal valorUnitario;

    public ItemPedidoResponse(ItemPedido item) {
        this.id = item.getId();
        this.pedidoId = item.getPedido().getId();
        this.livroId = item.getLivro().getId();
        this.quantidade = item.getQuantidade();
        this.valorUnitario = item.getValorUnitario();
    }
    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public Long getLivroId() { return livroId; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
}