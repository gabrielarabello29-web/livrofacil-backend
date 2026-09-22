package com.livrofacil.modulos.compra.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PagamentoRequest {
    @NotNull(message = "A forma de pagamento e obrigatoria")
    @Positive(message = "O ID da forma de pagamento deve ser maior que zero")
    private Long formaPagamentoId;

    @NotNull(message = "O valor do pagamento e obrigatorio")
    @Positive(message = "Cada forma de pagamento deve possuir no minimo 1 real")
    private BigDecimal valor;

    @NotNull(message = "O numero de parcelas e obrigatorio")
    @Positive(message = "O numero de parcelas deve ser maior que zero")
    private Integer parcelas;

    public Long getFormaPagamentoId() { return formaPagamentoId; }
    public void setFormaPagamentoId(Long formaPagamentoId) { this.formaPagamentoId = formaPagamentoId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
}