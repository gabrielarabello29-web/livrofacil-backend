package com.livrofacil.modulos.cliente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "forma_pagamento")
public class FormaPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "for_pag_id")
    private Long id;

    @Column(name = "for_pag_nome_titular", length = 100)
    private String nomeTitular;

    @Column(name = "for_pag_tipo_cartao", length = 10)
    private String tipoCartao;

    @Column(name = "for_pag_ultimos_digitos", length = 4)
    private String ultimosDigitos;

    @Column(name = "for_pag_validade", length = 5)
    private String validade;

    @Column(name = "for_pag_bandeira", length = 20)
    private String bandeira;

    @Column(name = "for_pag_preferencial", length = 1)
    private String preferencial;

    @Column(name = "for_pag_ativo", length = 1)
    private String ativo;

    @Column(name = "for_pag_criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cli_id", referencedColumnName = "cli_id", nullable = false)
    private Cliente cliente;

    public FormaPagamento() {
    }

    public FormaPagamento(String nomeTitular, String tipoCartao, String ultimosDigitos, String validade,
                          String bandeira, boolean preferencial, Cliente cliente) {
        this.nomeTitular = nomeTitular;
        this.tipoCartao = tipoCartao;
        this.ultimosDigitos = ultimosDigitos;
        this.validade = validade;
        this.bandeira = bandeira;
        this.preferencial = preferencial ? "S" : "N";
        this.ativo = "S";
        this.cliente = cliente;
    }

    public Long getId() { return id; }
    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }
    public String getTipoCartao() { return tipoCartao; }
    public void setTipoCartao(String tipoCartao) { this.tipoCartao = tipoCartao; }
    public String getUltimosDigitos() { return ultimosDigitos; }
    public void setUltimosDigitos(String ultimosDigitos) { this.ultimosDigitos = ultimosDigitos; }
    public String getValidade() { return validade; }
    public void setValidade(String validade) { this.validade = validade; }
    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }
    public boolean isPreferencial() { return "S".equals(preferencial); }
    public void setPreferencial(boolean preferencial) { this.preferencial = preferencial ? "S" : "N"; }
    public boolean isAtivo() { return "S".equals(ativo); }
    public void setAtivo(boolean ativo) { this.ativo = ativo ? "S" : "N"; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public Cliente getCliente() { return cliente; }
}