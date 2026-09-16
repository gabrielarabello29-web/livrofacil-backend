package com.livrofacil.cliente.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CamposFormatadosTest {

    @Test
    void cpfDeveSerFormatadoComPontosETraco() {
        ClienteCadastroRequest request = new ClienteCadastroRequest();
        request.setCpf("12345678909");

        assertEquals("123.456.789-09", request.getCpf());
    }

    @Test
    void numeroCartaoDeveSerFormatadoEmGruposDeQuatro() {
        FormaPagamentoRequest request = new FormaPagamentoRequest();
        request.setNumeroCartao("1111111111111111");

        assertEquals("1111 1111 1111 1111", request.getNumeroCartao());
    }
}
