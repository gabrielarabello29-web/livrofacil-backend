package com.livrofacil.modulos.troca.dto;

import jakarta.validation.constraints.Positive;

public class ReceberTrocaRequest {
    private Boolean retornarEstoque = false;
    @Positive
    private Integer quantidadeRecebida;

    public Boolean getRetornarEstoque() { return retornarEstoque; }
    public void setRetornarEstoque(Boolean retornarEstoque) { this.retornarEstoque = retornarEstoque; }
    public Integer getQuantidadeRecebida() { return quantidadeRecebida; }
    public void setQuantidadeRecebida(Integer quantidadeRecebida) { this.quantidadeRecebida = quantidadeRecebida; }
}