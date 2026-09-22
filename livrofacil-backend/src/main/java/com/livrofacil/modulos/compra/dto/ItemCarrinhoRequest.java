package com.livrofacil.modulos.compra.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ItemCarrinhoRequest {
    @NotNull(message = "O livro e obrigatorio")
    @Positive(message = "O ID do livro deve ser maior que zero")
    private Long livroId;

    @NotNull(message = "A quantidade e obrigatoria")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer quantidade;

    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}