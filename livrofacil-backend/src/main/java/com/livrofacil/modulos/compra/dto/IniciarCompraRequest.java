package com.livrofacil.modulos.compra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public class IniciarCompraRequest {
    @NotNull(message = "O cliente e obrigatorio")
    private UUID clienteId;

    @NotNull(message = "O carrinho e obrigatorio")
    @Positive(message = "O ID do carrinho deve ser maior que zero")
    private Long carrinhoId;

    @NotNull(message = "O endereco de entrega e obrigatorio")
    @Positive(message = "O ID do endereco deve ser maior que zero")
    private Long enderecoEntregaId;

    @NotBlank(message = "O endereco de cobranca e obrigatorio")
    private String enderecoCobranca;

    private String cupom;

    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
    public Long getCarrinhoId() { return carrinhoId; }
    public void setCarrinhoId(Long carrinhoId) { this.carrinhoId = carrinhoId; }
    public Long getEnderecoEntregaId() { return enderecoEntregaId; }
    public void setEnderecoEntregaId(Long enderecoEntregaId) { this.enderecoEntregaId = enderecoEntregaId; }
    public String getEnderecoCobranca() { return enderecoCobranca; }
    public void setEnderecoCobranca(String enderecoCobranca) { this.enderecoCobranca = enderecoCobranca; }
    public String getCupom() { return cupom; }
    public void setCupom(String cupom) { this.cupom = cupom; }
}