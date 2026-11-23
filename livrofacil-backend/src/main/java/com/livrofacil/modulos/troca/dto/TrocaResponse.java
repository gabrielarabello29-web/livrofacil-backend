package com.livrofacil.modulos.troca.dto;

import com.livrofacil.modulos.troca.entity.Troca;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TrocaResponse {
    private final Long id;
    private final Long pedidoId;
    private final UUID clienteId;
    private final String clienteNome;
    private final String pedidoStatus;
    private final LocalDateTime entregueEm;
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
    private final String tipo;
    private final Integer quantidadeSolicitada;
    private final Integer quantidadeRecebida;
    private final LocalDateTime decididoEm;
    private final LocalDateTime recebidoEm;
    private final LocalDateTime creditoEm;
    private final boolean retornarEstoque;
    private final String voucherCodigo;
    private final BigDecimal voucherValor;
    private final LocalDateTime voucherResgatadoEm;

    public TrocaResponse(Troca troca) {
        var item = troca.getItemPedido();
        var voucher = troca.getVoucher();
        this.id = troca.getId();
        this.pedidoId = troca.getPedido().getId();
        this.clienteId = troca.getPedido().getCliente().getId();
        this.clienteNome = troca.getPedido().getCliente().getNome();
        this.pedidoStatus = troca.getPedido().getStatus().name();
        this.entregueEm = troca.getPedido().getEntregueEm();
        this.itemPedidoId = item.getId();
        this.livroId = item.getLivro().getId();
        this.titulo = item.getTitulo();
        this.quantidade = item.getQuantidade();
        this.valorOriginal = item.getValorUnitario().multiply(BigDecimal.valueOf(
            troca.getQuantidade() == null ? item.getQuantidade() : troca.getQuantidade()));
        this.motivo = troca.getMotivo();
        this.detalhes = troca.getDetalhes();
        this.motivoRecusa = troca.getMotivoRecusa();
        this.status = troca.getStatus().name();
        this.criadoEm = troca.getCriadoEm();
        this.tipo = troca.getTipo().name();
        this.quantidadeSolicitada = troca.getQuantidade() == null ? item.getQuantidade() : troca.getQuantidade();
        this.quantidadeRecebida = troca.getQuantidadeRecebida();
        this.decididoEm = troca.getDecididoEm();
        this.recebidoEm = troca.getRecebidoEm();
        this.creditoEm = troca.getCreditoEm();
        this.retornarEstoque = troca.isRetornarEstoque();
        this.voucherCodigo = voucher == null ? null : voucher.getCodigo();
        this.voucherValor = voucher == null ? null : voucher.getValor();
        this.voucherResgatadoEm = voucher == null ? null : voucher.getResgatadoEm();
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public UUID getClienteId() { return clienteId; }
    public String getClienteNome() { return clienteNome; }
    public String getPedidoStatus() { return pedidoStatus; }
    public LocalDateTime getEntregueEm() { return entregueEm; }
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
    public String getTipo() { return tipo; }
    public Integer getQuantidadeSolicitada() { return quantidadeSolicitada; }
    public Integer getQuantidadeRecebida() { return quantidadeRecebida; }
    public LocalDateTime getDecididoEm() { return decididoEm; }
    public LocalDateTime getRecebidoEm() { return recebidoEm; }
    public LocalDateTime getCreditoEm() { return creditoEm; }
    public boolean isRetornarEstoque() { return retornarEstoque; }
    public String getVoucherCodigo() { return voucherCodigo; }
    public BigDecimal getVoucherValor() { return voucherValor; }
    public LocalDateTime getVoucherResgatadoEm() { return voucherResgatadoEm; }
}