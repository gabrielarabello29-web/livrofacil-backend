package com.livrofacil.produtos.livro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class Dimensao {

    @Column(name = "liv_altura", nullable = false, precision = 10, scale = 2)
    private BigDecimal altura;

    @Column(name = "liv_largura", nullable = false, precision = 10, scale = 2)
    private BigDecimal largura;

    @Column(name = "liv_profundidade", nullable = false, precision = 10, scale = 2)
    private BigDecimal profundidade;

    @Column(name = "liv_peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal peso;

    public Dimensao() {
    }

    public Dimensao(
            BigDecimal altura,
            BigDecimal largura,
            BigDecimal profundidade,
            BigDecimal peso
    ) {
        this.altura = altura;
        this.largura = largura;
        this.profundidade = profundidade;
        this.peso = peso;
    }

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
