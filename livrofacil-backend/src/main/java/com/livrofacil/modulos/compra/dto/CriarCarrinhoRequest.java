package com.livrofacil.modulos.compra.dto;

import java.util.UUID;

public class CriarCarrinhoRequest {
    private String token;
    private UUID clienteId;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
}