package com.livrofacil.modulos.compra.entity;

import com.livrofacil.modulos.cliente.entity.FormaPagamento;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pagamento_pedido")
public class PagamentoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pag_ped_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ped_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "for_pag_id", nullable = false)
    private FormaPagamento formaPagamento;

    @Column(name = "pag_ped_valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "pag_ped_parcelas", nullable = false)
    private Integer parcelas;

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
}