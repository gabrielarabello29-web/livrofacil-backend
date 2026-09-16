package com.livrofacil.produtos.livro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class DimensaoRequest {

    @NotNull(message = "A altura e obrigatoria")
    @Positive(message = "A altura deve ser maior que zero")
    private BigDecimal altura;

    @NotNull(message = "A largura e obrigatoria")
    @Positive(message = "A largura deve ser maior que zero")
    private BigDecimal largura;

    @NotNull(message = "A profundidade e obrigatoria")
    @Positive(message = "A profundidade deve ser maior que zero")
    private BigDecimal profundidade;

    @NotNull(message = "O peso e obrigatorio")
    @Positive(message = "O peso deve ser maior que zero")
    private BigDecimal peso;

    public BigDecimal getAltura() {
        return altura;
    }

    public void setAltura(BigDecimal altura) {
        this.altura = altura;
    }

    public BigDecimal getLargura() {
        return largura;
    }

    public void setLargura(BigDecimal largura) {
        this.largura = largura;
    }

    public BigDecimal getProfundidade() {
        return profundidade;
    }

    public void setProfundidade(BigDecimal profundidade) {
        this.profundidade = profundidade;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }
}