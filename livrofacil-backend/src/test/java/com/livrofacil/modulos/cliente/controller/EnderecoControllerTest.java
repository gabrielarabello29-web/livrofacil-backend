package com.livrofacil.modulos.cliente.controller;

import com.livrofacil.modulos.cliente.dto.EnderecoRequest;
import com.livrofacil.modulos.cliente.service.EnderecoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderecoControllerTest {
    @Mock private EnderecoUseCase useCase;
    @InjectMocks private EnderecoController controller;
    private final UUID clienteId = UUID.randomUUID();

    @Test void deveCriar() { when(useCase.criar(clienteId, null)).thenReturn(null); assertEquals(HttpStatus.CREATED, controller.criar(clienteId, null).getStatusCode()); }
    @Test void deveListar() { when(useCase.listar(clienteId)).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listar(clienteId).getStatusCode()); }
    @Test void deveBuscar() { when(useCase.buscarPorId(clienteId, 1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscarPorId(clienteId, 1L).getStatusCode()); }
    @Test void deveAtualizar() { EnderecoRequest request = new EnderecoRequest(); when(useCase.atualizar(clienteId, 1L, request)).thenReturn(null); assertEquals(HttpStatus.OK, controller.atualizar(clienteId, 1L, request).getStatusCode()); }
    @Test void deveExcluir() { controller.excluir(clienteId, 1L); verify(useCase).excluir(clienteId, 1L); assertEquals(HttpStatus.NO_CONTENT, controller.excluir(clienteId, 1L).getStatusCode()); }
}
