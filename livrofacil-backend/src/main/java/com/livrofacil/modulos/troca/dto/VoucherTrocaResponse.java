package com.livrofacil.modulos.troca.dto;

import com.livrofacil.modulos.troca.entity.VoucherTroca;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoucherTrocaResponse {
    private final Long id;
    private final String codigo;
    private final BigDecimal valor;
    private final LocalDateTime criadoEm;
    private final LocalDateTime resgatadoEm;
    private final Long pedidoResgateId;

    public VoucherTrocaResponse(VoucherTroca voucher) {
        this.id = voucher.getId();
        this.codigo = voucher.getCodigo();
        this.valor = voucher.getValor();
        this.criadoEm = voucher.getCriadoEm();
        this.resgatadoEm = voucher.getResgatadoEm();
        this.pedidoResgateId = voucher.getPedidoResgate() == null ? null : voucher.getPedidoResgate().getId();
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getResgatadoEm() { return resgatadoEm; }
    public Long getPedidoResgateId() { return pedidoResgateId; }
}
