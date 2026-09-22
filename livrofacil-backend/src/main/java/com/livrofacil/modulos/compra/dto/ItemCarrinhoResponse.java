package com.livrofacil.modulos.compra.dto;

import com.livrofacil.modulos.compra.entity.ItemCarrinho;

import java.math.BigDecimal;

public class ItemCarrinhoResponse {
    private Long id;
    private Long livroId;
    private String titulo;
    private String imagemUrl;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal subtotal;

    public ItemCarrinhoResponse(ItemCarrinho item) {
        this.id = item.getId();
        this.livroId = item.getLivro().getId();
        this.titulo = item.getLivro().getTitulo();
        this.imagemUrl = item.getLivro().getImagemUrl();
        this.quantidade = item.getQuantidade();
        this.valorUnitario = item.getLivro().getValorVenda();
        this.subtotal = valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getId() { return id; }
    public Long getLivroId() { return livroId; }
    public String getTitulo() { return titulo; }
    public String getImagemUrl() { return imagemUrl; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}