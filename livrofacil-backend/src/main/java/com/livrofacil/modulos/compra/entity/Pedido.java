package com.livrofacil.modulos.compra.entity;

import com.livrofacil.modulos.cliente.entity.Cliente;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ped_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cli_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Carrinho carrinho;

    @Column(name = "ped_checkout_chave")
    private Long checkoutChave;

    @Enumerated(EnumType.STRING)
    @Column(name = "ped_status", nullable = false, length = 30)
    private StatusPedido status = StatusPedido.EM_CHECKOUT;

    @Column(name = "ped_criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "ped_atualizado_em")
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    @Column(name = "ped_reserva_expira_em")
    private LocalDateTime reservaExpiraEm;

    @Column(name = "ped_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "ped_desconto", nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(name = "ped_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "ped_cupom", length = 50)
    private String cupom;

    @Column(name = "ped_entrega_endereco", nullable = false, length = 500)
    private String enderecoEntrega;

    @Column(name = "ped_cobranca_endereco", nullable = false, length = 500)
    private String enderecoCobranca;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagamentoPedido> pagamentos = new ArrayList<>();

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Carrinho getCarrinho() { return carrinho; }
    public void setCarrinho(Carrinho carrinho) { this.carrinho = carrinho; }
    public Long getCheckoutChave() { return checkoutChave; }
    public void setCheckoutChave(Long checkoutChave) { this.checkoutChave = checkoutChave; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void atualizarAtividade() { this.atualizadoEm = LocalDateTime.now(); }
    public LocalDateTime getReservaExpiraEm() { return reservaExpiraEm; }
    public void setReservaExpiraEm(LocalDateTime reservaExpiraEm) { this.reservaExpiraEm = reservaExpiraEm; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getCupom() { return cupom; }
    public void setCupom(String cupom) { this.cupom = cupom; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }
    public String getEnderecoCobranca() { return enderecoCobranca; }
    public void setEnderecoCobranca(String enderecoCobranca) { this.enderecoCobranca = enderecoCobranca; }
    public List<ItemPedido> getItens() { return itens; }
    public List<PagamentoPedido> getPagamentos() { return pagamentos; }
}