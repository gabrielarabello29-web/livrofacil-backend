package com.livrofacil.modulos.compra.controller;

import com.livrofacil.modulos.compra.service.CarrinhoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoControllerTest {
    @Mock private CarrinhoService service;
    @InjectMocks private CarrinhoController controller;
    private final UUID clienteId = UUID.randomUUID();

    @Test void deveCriar() { when(service.criarOuAssociar(null)).thenReturn(null); assertEquals(HttpStatus.OK, controller.criarOuAssociar(null).getStatusCode()); }
    @Test void deveBuscar() { when(service.buscar(1L, clienteId, "token")).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscar(1L, clienteId, "token").getStatusCode()); }
    @Test void deveAdicionar() { when(service.adicionar(1L, clienteId, "token", null)).thenReturn(null); assertEquals(HttpStatus.CREATED, controller.adicionar(1L, clienteId, "token", null).getStatusCode()); }
    @Test void deveAtualizar() { when(service.atualizarItem(1L, clienteId, "token", 2L, 3)).thenReturn(null); assertEquals(HttpStatus.OK, controller.atualizar(1L, 2L, clienteId, "token", 3).getStatusCode()); }
    @Test void deveRemover() { controller.remover(1L, 2L, clienteId, "token"); verify(service).removerItem(1L, clienteId, "token", 2L); assertEquals(HttpStatus.NO_CONTENT, controller.remover(1L, 2L, clienteId, "token").getStatusCode()); }
}
