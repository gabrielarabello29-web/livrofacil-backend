package com.livrofacil.modulos.troca.dto;

import com.livrofacil.modulos.troca.entity.Troca;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TrocaResponse {
    private final Long id;
    private final Long pedidoId;
    private final Long itemPedidoId;
    private final Long livroId;
    private final String titulo;
    private final Integer quantidade;
    private final BigDecimal valorOriginal;
    private final String motivo;
    private final String detalhes;
    private final String motivoRecusa;
    private final String status;
    private final LocalDateTime criadoEm;
    private final String voucherCodigo;
    private final BigDecimal voucherValor;
    private final LocalDateTime voucherResgatadoEm;

    public TrocaResponse(Troca troca) {
        var item = troca.getItemPedido();
        var voucher = troca.getVoucher();
        this.id = troca.getId();
        this.pedidoId = troca.getPedido().getId();
        this.itemPedidoId = item.getId();
        this.livroId = item.getLivro().getId();
        this.titulo = item.getTitulo();
        this.quantidade = item.getQuantidade();
        this.valorOriginal = item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
        this.motivo = troca.getMotivo();
        this.detalhes = troca.getDetalhes();
        this.motivoRecusa = troca.getMotivoRecusa();
        this.status = troca.getStatus().name();
        this.criadoEm = troca.getCriadoEm();
        this.voucherCodigo = voucher == null ? null : voucher.getCodigo();
        this.voucherValor = voucher == null ? null : voucher.getValor();
        this.voucherResgatadoEm = voucher == null ? null : voucher.getResgatadoEm();
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public Long getItemPedidoId() { return itemPedidoId; }
    public Long getLivroId() { return livroId; }
    public String getTitulo() { return titulo; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public String getMotivo() { return motivo; }
    public String getDetalhes() { return detalhes; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public String getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public String getVoucherCodigo() { return voucherCodigo; }
    public BigDecimal getVoucherValor() { return voucherValor; }
    public LocalDateTime getVoucherResgatadoEm() { return voucherResgatadoEm; }
}