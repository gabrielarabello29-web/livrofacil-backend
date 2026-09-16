package com.livrofacil.produtos.livro.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "grupos_precificacao")
public class GrupoPrecificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualMargem;

    public GrupoPrecificacao() {
    }

    public GrupoPrecificacao(String nome, BigDecimal percentualMargem) {
        this.nome = nome;
        this.percentualMargem = percentualMargem;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPercentualMargem() {
        return percentualMargem;
    }

    public void setPercentualMargem(BigDecimal percentualMargem) {
        this.percentualMargem = percentualMargem;
    }
}
