package com.livrofacil.modulos.cliente.dto;

import com.livrofacil.modulos.cliente.entity.FormaPagamento;

import java.time.LocalDateTime;
import java.util.UUID;

public class FormaPagamentoResponse {
    private Long id;
    private UUID clienteId;
    private String nomeTitular;
    private String tipoCartao;
    private String ultimosDigitos;
    private String validade;
    private String bandeira;
    private boolean preferencial;
    private boolean ativo;
    private LocalDateTime criadoEm;

    public FormaPagamentoResponse(FormaPagamento formaPagamento) {
        this.id = formaPagamento.getId();
        this.clienteId = formaPagamento.getCliente().getId();
        this.nomeTitular = formaPagamento.getNomeTitular();
        this.tipoCartao = formaPagamento.getTipoCartao();
        this.ultimosDigitos = formaPagamento.getUltimosDigitos();
        this.validade = formaPagamento.getValidade();
        this.bandeira = formaPagamento.getBandeira();
        this.preferencial = formaPagamento.isPreferencial();
        this.ativo = formaPagamento.isAtivo();
        this.criadoEm = formaPagamento.getCriadoEm();
    }

    public Long getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public String getNomeTitular() { return nomeTitular; }
    public String getTipoCartao() { return tipoCartao; }
    public String getUltimosDigitos() { return ultimosDigitos; }
    public String getValidade() { return validade; }
    public String getBandeira() { return bandeira; }
    public boolean isPreferencial() { return preferencial; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}