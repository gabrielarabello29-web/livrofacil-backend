package com.livrofacil.produtos.livro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class EstoqueRequest {

    @NotNull(message = "A quantidade disponivel e obrigatoria")
    @PositiveOrZero(message = "A quantidade disponivel nao pode ser negativa")
    private Integer quantidadeDisponivel;

    @NotNull(message = "A quantidade bloqueada e obrigatoria")
    @PositiveOrZero(message = "A quantidade bloqueada nao pode ser negativa")
    private Integer quantidadeBloqueada;

    @NotNull(message = "A quantidade vendida e obrigatoria")
    @PositiveOrZero(message = "A quantidade vendida nao pode ser negativa")
    private Integer quantidadeVendida;

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public Integer getQuantidadeBloqueada() {
        return quantidadeBloqueada;
    }

    public void setQuantidadeBloqueada(Integer quantidadeBloqueada) {
        this.quantidadeBloqueada = quantidadeBloqueada;
    }

    public Integer getQuantidadeVendida() {
        return quantidadeVendida;
    }

    public void setQuantidadeVendida(Integer quantidadeVendida) {
        this.quantidadeVendida = quantidadeVendida;
    }
}