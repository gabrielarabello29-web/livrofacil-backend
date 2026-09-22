package com.livrofacil.modulos.compra.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cupom")
public class Cupom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cup_id")
    private Long id;

    @Column(name = "cup_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "cup_percentual_desconto", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualDesconto;

    @Column(name = "cup_ativo", nullable = false)
    private Boolean ativo = true;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public BigDecimal getPercentualDesconto() { return percentualDesconto; }
    public Boolean getAtivo() { return ativo; }
}