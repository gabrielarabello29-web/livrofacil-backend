package com.livrofacil.modulos.troca.entity;

import com.livrofacil.modulos.compra.entity.ItemPedido;
import com.livrofacil.modulos.compra.entity.Pedido;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "troca")
public class Troca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tro_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ped_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ite_ped_id", nullable = false)
    private ItemPedido itemPedido;

    @Column(name = "tro_motivo", nullable = false, length = 120)
    private String motivo;

    @Column(name = "tro_detalhes", length = 1000)
    private String detalhes;

    @Column(name = "tro_motivo_recusa", length = 500)
    private String motivoRecusa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tro_status", nullable = false, length = 20)
    private StatusTroca status = StatusTroca.SOLICITADA;

    @Column(name = "tro_criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @OneToOne(mappedBy = "troca", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoucherTroca voucher;

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public ItemPedido getItemPedido() { return itemPedido; }
    public void setItemPedido(ItemPedido itemPedido) { this.itemPedido = itemPedido; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public void setMotivoRecusa(String motivoRecusa) { this.motivoRecusa = motivoRecusa; }
    public StatusTroca getStatus() { return status; }
    public void setStatus(StatusTroca status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public VoucherTroca getVoucher() { return voucher; }
    public void setVoucher(VoucherTroca voucher) { this.voucher = voucher; }
}