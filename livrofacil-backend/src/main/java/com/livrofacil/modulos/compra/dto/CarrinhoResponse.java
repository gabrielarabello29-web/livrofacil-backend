package com.livrofacil.modulos.compra.dto;

import com.livrofacil.modulos.compra.entity.Carrinho;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CarrinhoResponse {
    private Long id;
    private String token;
    private UUID clienteId;
    private List<ItemCarrinhoResponse> itens;
    private BigDecimal total;

    public CarrinhoResponse(Carrinho carrinho) {
        this.id = carrinho.getId();
        this.token = carrinho.getToken();
        this.clienteId = carrinho.getCliente() == null ? null : carrinho.getCliente().getId();
        this.itens = carrinho.getItens().stream().map(ItemCarrinhoResponse::new).toList();
        this.total = itens.stream().map(ItemCarrinhoResponse::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public UUID getClienteId() { return clienteId; }
    public List<ItemCarrinhoResponse> getItens() { return itens; }
    public BigDecimal getTotal() { return total; }
}