package com.livrofacil.modulos.livro.service;

import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.modulos.livro.dto.DimensaoRequest;
import com.livrofacil.modulos.livro.dto.LivroRequest;
import com.livrofacil.modulos.livro.entity.Autor;
import com.livrofacil.modulos.livro.entity.Categoria;
import com.livrofacil.modulos.livro.entity.Editora;
import com.livrofacil.modulos.livro.entity.GrupoPrecificacao;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.entity.Estoque;
import com.livrofacil.modulos.livro.repository.AutorRepository;
import com.livrofacil.modulos.livro.repository.CategoriaRepository;
import com.livrofacil.modulos.livro.repository.EditoraRepository;
import com.livrofacil.modulos.livro.repository.EstoqueRepository;
import com.livrofacil.modulos.livro.repository.GrupoPrecificacaoRepository;
import com.livrofacil.modulos.livro.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarLivroUseCaseTest {

    @Mock private LivroRepository livroRepository;
    @Mock private AutorRepository autorRepository;
    @Mock private EditoraRepository editoraRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private GrupoPrecificacaoRepository grupoPrecificacaoRepository;
    @Mock private EstoqueRepository estoqueRepository;
    @InjectMocks private CadastrarLivroUseCase useCase;

    @Test
    void deveCriarLivroEEstoque() {
        LivroRequest request = requestComCategorias(List.of(1L));
        Autor autor = new Autor();
        Editora editora = new Editora("Editora");
        GrupoPrecificacao grupo = new GrupoPrecificacao();
        Categoria categoria = new Categoria("Categoria");
        Livro livro = new Livro();
        Estoque estoque = new Estoque(livro);

        configurarReferencias(autor, editora, grupo, categoria);
        when(livroRepository.save(any(Livro.class))).thenReturn(livro);
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoque);
        when(estoqueRepository.findByLivroId(null)).thenReturn(Optional.of(estoque));

        useCase.criar(request);

        verify(livroRepository).save(any(Livro.class));
        verify(estoqueRepository).save(any(Estoque.class));
    }

    @Test
    void deveRejeitarCodigoDuplicado() {
        LivroRequest request = requestComCategorias(List.of(1L));
        when(livroRepository.existsByCodigo(request.getCodigo())).thenReturn(true);

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, () -> useCase.criar(request));

        assertEquals("Ja existe um livro cadastrado com este codigo", exception.getMessage());
        verify(autorRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarIsbnDuplicado() {
        LivroRequest request = requestComCategorias(List.of(1L));
        when(livroRepository.existsByIsbn(request.getIsbn())).thenReturn(true);

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, () -> useCase.criar(request));

        assertEquals("Ja existe um livro cadastrado com este ISBN", exception.getMessage());
    }

    @Test
    void deveRejeitarCodigoDeBarrasDuplicado() {
        LivroRequest request = requestComCategorias(List.of(1L));
        when(livroRepository.existsByCodigoBarras(request.getCodigoBarras())).thenReturn(true);

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, () -> useCase.criar(request));

        assertEquals("Ja existe um livro cadastrado com este codigo de barras", exception.getMessage());
    }

    @Test
    void deveRejeitarAutorInexistente() {
        LivroRequest request = requestComCategorias(List.of(1L));
        when(autorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.criar(request));
    }

    @Test
    void deveRejeitarCategoriasDuplicadas() {
        LivroRequest request = requestComCategorias(List.of(1L, 1L));
        configurarReferencias(new Autor(), new Editora("Editora"), new GrupoPrecificacao(), new Categoria("Categoria"));

        assertThrows(RegraDeNegocioException.class, () -> useCase.criar(request));
    }

    @Test
    void deveRejeitarCategoriasVazias() {
        LivroRequest request = requestComCategorias(List.of());
        configurarReferencias(new Autor(), new Editora("Editora"), new GrupoPrecificacao(), new Categoria("Categoria"));

        assertThrows(RegraDeNegocioException.class, () -> useCase.criar(request));
    }

    private void configurarReferencias(Autor autor, Editora editora, GrupoPrecificacao grupo, Categoria categoria) {
        when(livroRepository.existsByCodigo(any())).thenReturn(false);
        when(livroRepository.existsByIsbn(any())).thenReturn(false);
        when(livroRepository.existsByCodigoBarras(any())).thenReturn(false);
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(editoraRepository.findById(1L)).thenReturn(Optional.of(editora));
        when(grupoPrecificacaoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        lenient().when(categoriaRepository.findAllById(List.of(1L))).thenReturn(List.of(categoria));
    }

    private LivroRequest requestComCategorias(List<Long> categorias) {
        LivroRequest request = new LivroRequest();
        request.setCodigo("LIV-001");
        request.setTitulo("Livro");
        request.setAno(2024);
        request.setEdicao(1);
        request.setIsbn("9780000000000");
        request.setNumeroPaginas(100);
        request.setSinopse("Sinopse");
        request.setImagemUrl("https://example.com/livro.jpg");
        request.setCodigoBarras("7890000000000");
        request.setValorVenda(BigDecimal.TEN);
        request.setAtivo(true);
        request.setAutorId(1L);
        request.setEditoraId(1L);
        request.setGrupoPrecificacaoId(1L);
        request.setCategoriaIds(categorias);
        DimensaoRequest dimensao = new DimensaoRequest();
        dimensao.setAltura(BigDecimal.ONE);
        dimensao.setLargura(BigDecimal.ONE);
        dimensao.setProfundidade(BigDecimal.ONE);
        dimensao.setPeso(BigDecimal.ONE);
        request.setDimensao(dimensao);
        return request;
    }
}
