package com.livrofacil.cliente.dto;

import com.livrofacil.cliente.entity.FormaPagamento;

public class FormaPagamentoResponse {
    private Long id;
    private Long clienteId;
    private String nomeTitular;
    private String tipoCartao;
    private String ultimosDigitos;
    private String validade;
    private String bandeira;
    private boolean preferencial;
    private boolean ativo;

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
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getNomeTitular() { return nomeTitular; }
    public String getTipoCartao() { return tipoCartao; }
    public String getUltimosDigitos() { return ultimosDigitos; }
    public String getValidade() { return validade; }
    public String getBandeira() { return bandeira; }
    public boolean isPreferencial() { return preferencial; }
    public boolean isAtivo() { return ativo; }
}