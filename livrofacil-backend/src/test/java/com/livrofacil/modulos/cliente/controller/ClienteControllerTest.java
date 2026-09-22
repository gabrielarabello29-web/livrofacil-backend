package com.livrofacil.modulos.cliente.controller;

import com.livrofacil.modulos.cliente.dto.AlterarSenhaRequest;
import com.livrofacil.modulos.cliente.dto.ClienteLoginRequest;
import com.livrofacil.modulos.cliente.dto.ClienteResponse;
import com.livrofacil.modulos.cliente.dto.ClienteUpdateRequest;
import com.livrofacil.modulos.cliente.service.ClienteUseCase;
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
class ClienteControllerTest {
    @Mock private ClienteUseCase useCase;
    @InjectMocks private ClienteController controller;
    private final UUID id = UUID.randomUUID();

    @Test void deveListar() { when(useCase.listar()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listar().getStatusCode()); }
    @Test void deveBuscar() { when(useCase.buscar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.buscar(null, null, null, null, null, null, null, null, null, null, null, null, null, null).getStatusCode()); }
    @Test void deveBuscarPorId() { when(useCase.buscarPorId(id)).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscarPorId(id).getStatusCode()); }
    @Test void deveFazerLogin() { ClienteLoginRequest request = new ClienteLoginRequest(); request.setEmail("a@b.com"); request.setSenha("Senha@123"); when(useCase.login("a@b.com", "Senha@123")).thenReturn(null); assertEquals(HttpStatus.OK, controller.login(request).getStatusCode()); }
    @Test void deveAtualizar() { when(useCase.atualizar(any(), any())).thenReturn(null); assertEquals(HttpStatus.OK, controller.atualizar(id, new ClienteUpdateRequest()).getStatusCode()); }
    @Test void deveAlterarSenha() { AlterarSenhaRequest request = new AlterarSenhaRequest(); controller.alterarSenha(id, request); verify(useCase).alterarSenha(id, request); assertEquals(HttpStatus.NO_CONTENT, controller.alterarSenha(id, request).getStatusCode()); }
    @Test void deveInativar() { controller.inativar(id); verify(useCase).inativar(id); assertEquals(HttpStatus.NO_CONTENT, controller.inativar(id).getStatusCode()); }
}
