package com.livrofacil.modulos.cliente.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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

    @Test
    void cpfNuloDeveContinuarNulo() {
        ClienteCadastroRequest request = new ClienteCadastroRequest();
        request.setCpf(null);
        assertEquals(null, request.getCpf());
    }

    @Test
    void cartaoComMaisDeDezesseisDigitosDeveSerLimitado() {
        FormaPagamentoRequest request = new FormaPagamentoRequest();
        request.setNumeroCartao("11112222333344445555");
        assertEquals("1111 2222 3333 4444", request.getNumeroCartao());
    }

    @Test
    void cadastroDeveAceitarClienteMaiorDeIdade() {
        ClienteCadastroRequest request = new ClienteCadastroRequest();
        request.setDataNascimento(LocalDate.now().minusYears(18));
        assertEquals(true, request.isMaiorDeIdade());
    }

    @Test
    void cadastroDeveRejeitarMenorDeIdade() {
        ClienteCadastroRequest request = new ClienteCadastroRequest();
        request.setDataNascimento(LocalDate.now().minusYears(17));
        assertEquals(false, request.isMaiorDeIdade());
    }

    @Test
    void cadastroDeveValidarConfirmacaoDeSenha() {
        ClienteCadastroRequest request = new ClienteCadastroRequest();
        request.setSenha("Senha@123");
        request.setConfirmarSenha("Senha@123");
        assertEquals(true, request.isSenhasIguais());
    }
}
