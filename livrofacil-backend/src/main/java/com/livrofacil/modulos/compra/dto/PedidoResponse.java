package com.livrofacil.modulos.compra.dto;

import com.livrofacil.modulos.cliente.dto.ClienteResponse;
import com.livrofacil.modulos.compra.entity.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PedidoResponse {
    private Long id;
    private UUID clienteId;
    private Long carrinhoId;
    private ClienteResponse cliente;
    private String status;
    private LocalDateTime criadoEm;
    private LocalDateTime reservaExpiraEm;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal total;
    private String cupom;
    private String enderecoEntrega;
    private String enderecoCobranca;
    private List<ItemPedidoResponse> itens;

    public PedidoResponse(Pedido pedido) {
        this.id = pedido.getId();
        this.clienteId = pedido.getCliente().getId();
        this.carrinhoId = pedido.getCarrinho() == null ? null : pedido.getCarrinho().getId();
        this.cliente = new ClienteResponse(pedido.getCliente());
        this.status = pedido.getStatus().name();
        this.criadoEm = pedido.getCriadoEm();
        this.reservaExpiraEm = pedido.getReservaExpiraEm();
        this.subtotal = pedido.getSubtotal();
        this.desconto = pedido.getDesconto();
        this.total = pedido.getTotal();
        this.cupom = pedido.getCupom();
        this.enderecoEntrega = pedido.getEnderecoEntrega();
        this.enderecoCobranca = pedido.getEnderecoCobranca();
        this.itens = pedido.getItens().stream().map(ItemPedidoResponse::new).toList();
    }
    public Long getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public Long getCarrinhoId() { return carrinhoId; }
    public ClienteResponse getCliente() { return cliente; }
    public String getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getReservaExpiraEm() { return reservaExpiraEm; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDesconto() { return desconto; }
    public BigDecimal getTotal() { return total; }
    public String getCupom() { return cupom; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public String getEnderecoCobranca() { return enderecoCobranca; }
    public List<ItemPedidoResponse> getItens() { return itens; }
}