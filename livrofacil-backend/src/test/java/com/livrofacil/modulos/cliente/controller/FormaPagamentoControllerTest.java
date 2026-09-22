package com.livrofacil.modulos.cliente.controller;

import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.cliente.service.FormaPagamentoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormaPagamentoControllerTest {
    @Mock private FormaPagamentoUseCase useCase;
    @InjectMocks private FormaPagamentoController controller;
    private final UUID clienteId = UUID.randomUUID();

    @Test void deveCriar() { when(useCase.criar(clienteId, null)).thenReturn(null); assertEquals(HttpStatus.CREATED, controller.criar(clienteId, null).getStatusCode()); }
    @Test void deveListarAsc() { when(useCase.listar(clienteId, true)).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listar(clienteId, "asc").getStatusCode()); }
    @Test void deveListarDesc() { when(useCase.listar(clienteId, false)).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listar(clienteId, "desc").getStatusCode()); }
    @Test void deveRejeitarOrdemInvalida() { assertThrows(RegraDeNegocioException.class, () -> controller.listar(clienteId, "aleatoria")); }
    @Test void deveBuscar() { when(useCase.buscarPorId(clienteId, 1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscarPorId(clienteId, 1L).getStatusCode()); }
    @Test void deveAtualizar() { when(useCase.atualizar(clienteId, 1L, null)).thenReturn(null); assertEquals(HttpStatus.OK, controller.atualizar(clienteId, 1L, null).getStatusCode()); }
    @Test void deveDefinirPreferencial() { when(useCase.definirPreferencial(clienteId, 1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.definirPreferencial(clienteId, 1L).getStatusCode()); }
    @Test void deveExcluir() { when(useCase.excluir(clienteId, 1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.excluir(clienteId, 1L).getStatusCode()); }
    @Test void deveExcluirDefinitivamente() { assertEquals(HttpStatus.NO_CONTENT, controller.excluirDefinitivamente(clienteId, 1L).getStatusCode()); }
    @Test void deveReativar() { when(useCase.reativar(clienteId, 1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.reativar(clienteId, 1L).getStatusCode()); }
}
