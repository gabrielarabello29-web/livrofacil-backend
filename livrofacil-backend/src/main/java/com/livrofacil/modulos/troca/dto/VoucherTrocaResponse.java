package com.livrofacil.modulos.troca.dto;

import com.livrofacil.modulos.troca.entity.VoucherTroca;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoucherTrocaResponse {
    private final Long id;
    private final String codigo;
    private final BigDecimal valor;
    private final BigDecimal valorOriginal;
    private final String tipo;
    private final String status;
    private final Long trocaId;
    private final Long voucherOrigemId;
    private final LocalDateTime criadoEm;
    private final LocalDateTime resgatadoEm;
    private final Long pedidoResgateId;

    public VoucherTrocaResponse(VoucherTroca voucher) {
        this.id = voucher.getId();
        this.codigo = voucher.getCodigo();
        this.valor = voucher.getValor();
        this.valorOriginal = voucher.getValorOriginal();
        this.tipo = "TROCA";
        this.status = voucher.getResgatadoEm() == null ? "DISPONIVEL" : "UTILIZADO";
        this.trocaId = voucher.getTroca() == null ? null : voucher.getTroca().getId();
        this.voucherOrigemId = voucher.getVoucherOrigem() == null ? null : voucher.getVoucherOrigem().getId();
        this.criadoEm = voucher.getCriadoEm();
        this.resgatadoEm = voucher.getResgatadoEm();
        this.pedidoResgateId = voucher.getPedidoResgate() == null ? null : voucher.getPedidoResgate().getId();
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getSaldo() { return valor; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public String getTipo() { return tipo; }
    public String getStatus() { return status; }
    public Long getTrocaId() { return trocaId; }
    public Long getVoucherOrigemId() { return voucherOrigemId; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getResgatadoEm() { return resgatadoEm; }
    public Long getPedidoResgateId() { return pedidoResgateId; }
}
