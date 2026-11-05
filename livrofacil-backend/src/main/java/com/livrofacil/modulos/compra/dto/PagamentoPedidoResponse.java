package com.livrofacil.modulos.compra.dto;

import com.livrofacil.modulos.compra.entity.PagamentoPedido;

import java.math.BigDecimal;

public class PagamentoPedidoResponse {
    private final Long id;
    private final Long formaPagamentoId;
    private final BigDecimal valor;
    private final Integer parcelas;
    private final String bandeira;
    private final String ultimosDigitos;

    public PagamentoPedidoResponse(PagamentoPedido pagamento) {
        this.id = pagamento.getId();
        this.formaPagamentoId = pagamento.getFormaPagamento().getId();
        this.valor = pagamento.getValor();
        this.parcelas = pagamento.getParcelas();
        this.bandeira = pagamento.getFormaPagamento().getBandeira();
        this.ultimosDigitos = pagamento.getFormaPagamento().getUltimosDigitos();
    }

    public Long getId() { return id; }
    public Long getFormaPagamentoId() { return formaPagamentoId; }
    public BigDecimal getValor() { return valor; }
    public Integer getParcelas() { return parcelas; }
    public String getBandeira() { return bandeira; }
    public String getUltimosDigitos() { return ultimosDigitos; }
}