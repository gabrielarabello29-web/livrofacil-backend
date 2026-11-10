package com.livrofacil.modulos.compra.dto;

import java.math.BigDecimal;

public class ValidarCupomResponse {
    private final boolean valido;
    private final String tipo;
    private final BigDecimal valor;
    private final String codigo;
    private final String mensagem;

    public ValidarCupomResponse(boolean valido, String tipo, BigDecimal valor, String codigo, String mensagem) {
        this.valido = valido;
        this.tipo = tipo;
        this.valor = valor;
        this.codigo = codigo;
        this.mensagem = mensagem;
    }

    public boolean isValido() { return valido; }
    public String getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public String getCodigo() { return codigo; }
    public String getMensagem() { return mensagem; }
}