package com.livrofacil.produtos.livro.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "grupo_precificacao")
public class GrupoPrecificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grp_pre_id")
    private Long id;

    @Column(name = "grp_pre_nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "grp_pre_percentual_margem", nullable = false, precision = 5, scale = 2)
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
