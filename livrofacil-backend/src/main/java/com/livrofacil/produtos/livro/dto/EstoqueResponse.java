package com.livrofacil.produtos.livro.dto;

import com.livrofacil.produtos.livro.entity.Estoque;

public class EstoqueResponse {

    private Long id;
    private Long livroId;
    private Integer quantidadeDisponivel;
    private Integer quantidadeBloqueada;
    private Integer quantidadeVendida;

    public EstoqueResponse(Estoque estoque) {
        this.id = estoque.getId();
        this.livroId = estoque.getLivro().getId();
        this.quantidadeDisponivel = estoque.getQuantidadeDisponivel();
        this.quantidadeBloqueada = estoque.getQuantidadeBloqueada();
        this.quantidadeVendida = estoque.getQuantidadeVendida();
    }

    public Long getId() {
        return id;
    }

    public Long getLivroId() {
        return livroId;
    }

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public Integer getQuantidadeBloqueada() {
        return quantidadeBloqueada;
    }

    public Integer getQuantidadeVendida() {
        return quantidadeVendida;
    }
}