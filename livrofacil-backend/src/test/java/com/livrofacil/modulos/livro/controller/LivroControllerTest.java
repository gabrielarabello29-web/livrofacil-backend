package com.livrofacil.modulos.livro.controller;

import com.livrofacil.modulos.livro.service.CadastrarLivroUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivroControllerTest {
    @Mock private CadastrarLivroUseCase useCase;
    @InjectMocks private LivroController controller;

    @Test void deveListar() { when(useCase.listar()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listar().getStatusCode()); }
    @Test void deveListarAtivos() { when(useCase.listarAtivos()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listarAtivos().getStatusCode()); }
    @Test void deveListarCatalogo() { when(useCase.listarCatalogo()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listarCatalogo().getStatusCode()); }
    @Test void deveBuscarCatalogo() { when(useCase.buscarNoCatalogoPorTitulo("Livro")).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.buscarCatalogo("Livro").getStatusCode()); }
    @Test void deveBuscarLivroCatalogo() { when(useCase.buscarNoCatalogo(1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscarLivroCatalogo(1L).getStatusCode()); }
    @Test void deveListarAutores() { when(useCase.listarAutores()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listarAutores().getStatusCode()); }
    @Test void deveListarEditoras() { when(useCase.listarEditoras()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listarEditoras().getStatusCode()); }
    @Test void deveListarCategorias() { when(useCase.listarCategorias()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listarCategorias().getStatusCode()); }
    @Test void deveListarGrupos() { when(useCase.listarGruposPrecificacao()).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.listarGruposPrecificacao().getStatusCode()); }
    @Test void deveBuscarPorTitulo() { when(useCase.buscarPorTitulo("Livro")).thenReturn(List.of()); assertEquals(HttpStatus.OK, controller.buscarPorTitulo("Livro").getStatusCode()); }
    @Test void deveBuscarPorId() { when(useCase.buscarPorId(1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscarPorId(1L).getStatusCode()); }
    @Test void deveBuscarEstoque() { when(useCase.buscarEstoque(1L)).thenReturn(null); assertEquals(HttpStatus.OK, controller.buscarEstoque(1L).getStatusCode()); }
    @Test void deveAtualizarEstoque() { when(useCase.atualizarEstoque(1L, null)).thenReturn(null); assertEquals(HttpStatus.OK, controller.atualizarEstoque(1L, null).getStatusCode()); }
    @Test void deveAtualizar() { when(useCase.atualizar(1L, null)).thenReturn(null); assertEquals(HttpStatus.OK, controller.atualizar(1L, null).getStatusCode()); }
    @Test void deveAtivar() { controller.ativar(1L); verify(useCase).ativar(1L); assertEquals(HttpStatus.NO_CONTENT, controller.ativar(1L).getStatusCode()); }
    @Test void deveInativar() { controller.inativar(1L); verify(useCase).inativar(1L); assertEquals(HttpStatus.NO_CONTENT, controller.inativar(1L).getStatusCode()); }
}
