package com.livrofacil.modulos.compra.controller;

import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {
    @Mock private PedidoService service;
    @InjectMocks private PedidoController controller;
    private final UUID clienteId = UUID.randomUUID();

    @Test void deveIniciar() { when(service.iniciar(null)).thenReturn(null); assertEquals(HttpStatus.CREATED, controller.iniciar(null).getStatusCode()); }
    @Test void deveFinalizar() { when(service.finalizar(1L, clienteId, null)).thenReturn(null); assertEquals(HttpStatus.OK, controller.finalizar(1L, clienteId, null).getStatusCode()); }
    @Test void deveListarCliente() { when(service.listarCliente(clienteId)).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listarCliente(clienteId).getStatusCode()); }
    @Test void deveBuscarCliente() { when(service.buscarDetalhe(1L, clienteId)).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscarCliente(1L, clienteId).getStatusCode()); }
    @Test void deveCancelar() { when(service.cancelarCliente(1L, clienteId)).thenReturn(null); assertEquals(HttpStatus.OK, controller.cancelar(1L, clienteId).getStatusCode()); }
    @Test void deveListarTodos() { when(service.listarTodos()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listarTodos().getStatusCode()); }
    @Test void deveBuscarAdmin() { when(service.buscarDetalheAdmin(1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscarDetalheAdmin(1L).getStatusCode()); }
    @Test void deveAtualizarStatus() { when(service.atualizarStatus(1L, StatusPedido.FINALIZADO)).thenReturn(null); var request = new com.livrofacil.modulos.compra.dto.AtualizarStatusPedidoRequest(); request.setStatus(StatusPedido.FINALIZADO); assertEquals(HttpStatus.OK, controller.atualizarStatus(1L, request).getStatusCode()); }
    @Test void deveCancelarAdmin() { when(service.atualizarStatus(1L, StatusPedido.CANCELADO)).thenReturn(null); assertEquals(HttpStatus.OK, controller.cancelarAdmin(1L).getStatusCode()); verify(service).atualizarStatus(1L, StatusPedido.CANCELADO); }
}
