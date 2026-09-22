package com.livrofacil.modulos.cliente.controller;

import com.livrofacil.modulos.cliente.dto.AtualizarBandeiraPagamentoRequest;
import com.livrofacil.modulos.cliente.service.BandeiraPagamentoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BandeiraPagamentoControllerTest {
    @Mock private BandeiraPagamentoUseCase useCase;
    @InjectMocks private BandeiraPagamentoController controller;

    @Test void deveListar() { when(useCase.listar()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listar().getStatusCode()); }
    @Test void deveAtualizar() { when(useCase.atualizar(1L, null)).thenReturn(null); assertEquals(HttpStatus.OK, controller.atualizar(1L, null).getStatusCode()); }
}
