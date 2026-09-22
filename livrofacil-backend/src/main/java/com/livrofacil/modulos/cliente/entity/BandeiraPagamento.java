package com.livrofacil.modulos.cliente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bandeira_pagamento")
public class BandeiraPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_pag_id")
    private Long id;

    @Column(name = "ban_pag_nome", nullable = false, unique = true, length = 20)
    private String nome;

    @Column(name = "ban_pag_disponivel", nullable = false, length = 1)
    private String disponivel = "S";

    public BandeiraPagamento() {
    }

    public BandeiraPagamento(String nome) {
        this.nome = nome;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public boolean isDisponivel() { return "S".equals(disponivel); }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel ? "S" : "N"; }
}
