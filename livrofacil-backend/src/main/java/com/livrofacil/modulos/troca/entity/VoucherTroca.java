package com.livrofacil.modulos.troca.entity;

import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.compra.entity.Pedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_troca", uniqueConstraints = @UniqueConstraint(name = "uk_voucher_troca_codigo", columnNames = "vot_codigo"))
public class VoucherTroca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vot_id")
    private Long id;

    @Column(name = "vot_codigo", nullable = false, length = 40)
    private String codigo;

    @Column(name = "vot_valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cli_id", nullable = false)
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tro_id", nullable = false, unique = true)
    private Troca troca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ped_resgate_id")
    private Pedido pedidoResgate;

    @Column(name = "vot_criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "vot_resgatado_em")
    private LocalDateTime resgatadoEm;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Troca getTroca() { return troca; }
    public void setTroca(Troca troca) { this.troca = troca; }
    public Pedido getPedidoResgate() { return pedidoResgate; }
    public void setPedidoResgate(Pedido pedidoResgate) { this.pedidoResgate = pedidoResgate; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getResgatadoEm() { return resgatadoEm; }
    public void setResgatadoEm(LocalDateTime resgatadoEm) { this.resgatadoEm = resgatadoEm; }
}