package com.livrofacil.modulos.livro.service;

import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.modulos.livro.dto.DimensaoRequest;
import com.livrofacil.modulos.livro.dto.LivroRequest;
import com.livrofacil.modulos.livro.dto.LivroOpcaoResponse;
import com.livrofacil.modulos.livro.dto.EstoqueRequest;
import com.livrofacil.modulos.livro.dto.EstoqueResponse;
import com.livrofacil.modulos.livro.dto.LivroResponse;
import com.livrofacil.modulos.livro.dto.LivroCatalogoResponse;
import com.livrofacil.modulos.livro.entity.Autor;
import com.livrofacil.modulos.livro.entity.Categoria;
import com.livrofacil.modulos.livro.entity.Dimensao;
import com.livrofacil.modulos.livro.entity.Editora;
import com.livrofacil.modulos.livro.entity.Estoque;
import com.livrofacil.modulos.livro.entity.GrupoPrecificacao;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.repository.AutorRepository;
import com.livrofacil.modulos.livro.repository.CategoriaRepository;
import com.livrofacil.modulos.livro.repository.EditoraRepository;
import com.livrofacil.modulos.livro.repository.GrupoPrecificacaoRepository;
import com.livrofacil.modulos.livro.repository.LivroRepository;
import com.livrofacil.modulos.livro.repository.EstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CadastrarLivroUseCase {

	private final LivroRepository livroRepository;
	private final AutorRepository autorRepository;
	private final EditoraRepository editoraRepository;
	private final CategoriaRepository categoriaRepository;
	private final GrupoPrecificacaoRepository grupoPrecificacaoRepository;
	private final EstoqueRepository estoqueRepository;

	public CadastrarLivroUseCase(
			LivroRepository livroRepository,
			AutorRepository autorRepository,
			EditoraRepository editoraRepository,
			CategoriaRepository categoriaRepository,
				GrupoPrecificacaoRepository grupoPrecificacaoRepository,
				EstoqueRepository estoqueRepository
	) {
		this.livroRepository = livroRepository;
		this.autorRepository = autorRepository;
		this.editoraRepository = editoraRepository;
		this.categoriaRepository = categoriaRepository;
		this.grupoPrecificacaoRepository = grupoPrecificacaoRepository;
		this.estoqueRepository = estoqueRepository;
	}

	@Transactional
	public LivroResponse criar(LivroRequest request) {
		validarDadosUnicos(request.getCodigo(), request.getIsbn(), request.getCodigoBarras(), null);

		Autor autor = buscarAutor(request.getAutorId());
		Editora editora = buscarEditora(request.getEditoraId());
		GrupoPrecificacao grupoPrecificacao = buscarGrupoPrecificacao(request.getGrupoPrecificacaoId());
		Set<Categoria> categorias = buscarCategorias(request.getCategoriaIds());
		Dimensao dimensao = converterDimensao(request.getDimensao());

		Livro livro = new Livro(
				request.getCodigo(),
				request.getTitulo(),
				request.getAno(),
				request.getEdicao(),
				request.getIsbn(),
				request.getNumeroPaginas(),
				request.getSinopse(),
				request.getImagemUrl(),
				request.getCodigoBarras(),
				request.getValorVenda(),
				request.getAtivo(),
				autor,
				editora,
				grupoPrecificacao,
				dimensao
		);
		livro.setCategorias(categorias);

		Livro livroSalvo = livroRepository.save(livro);
		estoqueRepository.save(new Estoque(livroSalvo));
		return new LivroResponse(livroSalvo, estoqueRepository.findByLivroId(livroSalvo.getId()).orElseThrow());
	}

	@Transactional(readOnly = true)
	public List<LivroResponse> listar() {
		return livroRepository.findAll().stream()
				.map(livro -> new LivroResponse(livro, estoqueRepository.findByLivroId(livro.getId()).orElse(null)))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LivroResponse> listarAtivos() {
		return livroRepository.findByAtivoTrue().stream()
				.map(livro -> new LivroResponse(livro, estoqueRepository.findByLivroId(livro.getId()).orElse(null)))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LivroCatalogoResponse> listarCatalogo() {
		return livroRepository.findByAtivoTrue().stream()
				.map(LivroCatalogoResponse::new)
				.toList();
	}

	@Transactional(readOnly = true)
	public LivroCatalogoResponse buscarNoCatalogo(Long id) {
		Livro livro = buscarLivro(id);
		if (!Boolean.TRUE.equals(livro.getAtivo())) {
			throw new RecursoNaoEncontradoException("Livro nao encontrado no catalogo: " + id);
		}
		return new LivroCatalogoResponse(livro);
	}

	@Transactional(readOnly = true)
	public List<LivroCatalogoResponse> buscarNoCatalogoPorTitulo(String titulo) {
		return livroRepository.findByTituloContainingIgnoreCase(titulo).stream()
				.filter(livro -> Boolean.TRUE.equals(livro.getAtivo()))
				.map(LivroCatalogoResponse::new)
				.toList();
	}

	@Transactional(readOnly = true)
	public LivroResponse buscarPorId(Long id) {
		return new LivroResponse(buscarLivro(id), estoqueRepository.findByLivroId(id).orElse(null));
	}

	@Transactional(readOnly = true)
	public List<LivroResponse> buscarPorTitulo(String titulo) {
		return livroRepository.findByTituloContainingIgnoreCase(titulo).stream()
				.map(livro -> new LivroResponse(livro, estoqueRepository.findByLivroId(livro.getId()).orElse(null)))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarAutores() {
		return autorRepository.findAll().stream().map(LivroOpcaoResponse::new).toList();
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarEditoras() {
		return editoraRepository.findAll().stream().map(LivroOpcaoResponse::new).toList();
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarCategorias() {
		return categoriaRepository.findAll().stream().map(LivroOpcaoResponse::new).toList();
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarGruposPrecificacao() {
		return grupoPrecificacaoRepository.findAll().stream().map(LivroOpcaoResponse::new).toList();
	}

	@Transactional
	public LivroResponse atualizar(Long id, LivroRequest request) {
		Livro livro = buscarLivro(id);
		validarDadosUnicos(request.getCodigo(), request.getIsbn(), request.getCodigoBarras(), id);

		Autor autor = buscarAutor(request.getAutorId());
		Editora editora = buscarEditora(request.getEditoraId());
		GrupoPrecificacao grupoPrecificacao = buscarGrupoPrecificacao(request.getGrupoPrecificacaoId());
		Set<Categoria> categorias = buscarCategorias(request.getCategoriaIds());
		Dimensao dimensao = converterDimensao(request.getDimensao());

		livro.setCodigo(request.getCodigo());
		livro.setTitulo(request.getTitulo());
		livro.setAno(request.getAno());
		livro.setEdicao(request.getEdicao());
		livro.setIsbn(request.getIsbn());
		livro.setNumeroPaginas(request.getNumeroPaginas());
		livro.setSinopse(request.getSinopse());
		livro.setImagemUrl(request.getImagemUrl());
		livro.setCodigoBarras(request.getCodigoBarras());
		livro.setValorVenda(request.getValorVenda());
		livro.setAtivo(request.getAtivo());
		livro.setAutor(autor);
		livro.setEditora(editora);
		livro.setGrupoPrecificacao(grupoPrecificacao);
		livro.setDimensao(dimensao);
		livro.setCategorias(categorias);

		return new LivroResponse(livroRepository.save(livro), estoqueRepository.findByLivroId(id).orElse(null));
	}

	@Transactional(readOnly = true)
	public EstoqueResponse buscarEstoque(Long livroId) {
		buscarLivro(livroId);
		return new EstoqueResponse(buscarEstoqueEntity(livroId));
	}

	@Transactional
	public EstoqueResponse atualizarEstoque(Long livroId, EstoqueRequest request) {
		buscarLivro(livroId);
		Estoque estoque = buscarEstoqueEntity(livroId);
		estoque.setQuantidadeDisponivel(request.getQuantidadeDisponivel());
		estoque.setQuantidadeBloqueada(request.getQuantidadeBloqueada());
		estoque.setQuantidadeVendida(request.getQuantidadeVendida());
		return new EstoqueResponse(estoqueRepository.save(estoque));
	}

	@Transactional
	public void ativar(Long id) {
		Livro livro = buscarLivro(id);
		livro.setAtivo(true);
		livroRepository.save(livro);
	}

	@Transactional
	public void inativar(Long id) {
		Livro livro = buscarLivro(id);
		livro.setAtivo(false);
		livroRepository.save(livro);
	}

	private Livro buscarLivro(Long id) {
		return livroRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Livro nao encontrado: " + id));
	}

	private Estoque buscarEstoqueEntity(Long livroId) {
		return estoqueRepository.findByLivroId(livroId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estoque nao encontrado para o livro: " + livroId));
	}

	private Autor buscarAutor(Long id) {
		return autorRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Autor nao encontrado: " + id));
	}

	private Editora buscarEditora(Long id) {
		return editoraRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Editora nao encontrada: " + id));
	}

	private GrupoPrecificacao buscarGrupoPrecificacao(Long id) {
		return grupoPrecificacaoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Grupo de precificacao nao encontrado: " + id));
	}

	private Set<Categoria> buscarCategorias(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new RegraDeNegocioException("O livro precisa ter pelo menos uma categoria");
		}
		if (ids.size() != ids.stream().distinct().count()) {
			throw new RegraDeNegocioException("O livro nao pode possuir categorias duplicadas");
		}

		Set<Categoria> categorias = new HashSet<>(categoriaRepository.findAllById(ids));
		if (categorias.size() != ids.size()) {
			throw new RecursoNaoEncontradoException("Uma ou mais categorias nao foram encontradas");
		}
		return categorias;
	}

	private Dimensao converterDimensao(DimensaoRequest request) {
		if (request == null) {
			throw new RegraDeNegocioException("As dimensoes do livro sao obrigatorias");
		}

		return new Dimensao(
				request.getAltura(),
				request.getLargura(),
				request.getProfundidade(),
				request.getPeso()
		);
	}

	private void validarDadosUnicos(String codigo, String isbn, String codigoBarras, Long idAtual) {
		boolean codigoDuplicado = idAtual == null
				? livroRepository.existsByCodigo(codigo)
				: livroRepository.existsByCodigoAndIdNot(codigo, idAtual);

		boolean isbnDuplicado = idAtual == null
				? livroRepository.existsByIsbn(isbn)
				: livroRepository.existsByIsbnAndIdNot(isbn, idAtual);

		boolean codigoBarrasDuplicado = idAtual == null
				? livroRepository.existsByCodigoBarras(codigoBarras)
				: livroRepository.existsByCodigoBarrasAndIdNot(codigoBarras, idAtual);

		if (codigoDuplicado) {
			throw new RegraDeNegocioException("Ja existe um livro cadastrado com este codigo");
		}
		if (isbnDuplicado) {
			throw new RegraDeNegocioException("Ja existe um livro cadastrado com este ISBN");
		}
		if (codigoBarrasDuplicado) {
			throw new RegraDeNegocioException("Ja existe um livro cadastrado com este codigo de barras");
		}
	}
}
