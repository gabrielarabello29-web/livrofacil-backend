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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

		Autor autor = resolverAutor(request);
		Editora editora = resolverEditora(request);
		GrupoPrecificacao grupoPrecificacao = resolverGrupoPrecificacao(request);
		Set<Categoria> categorias = resolverCategorias(request);
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
		return listar(null, null, null, null);
	}

	@Transactional(readOnly = true)
	public List<LivroResponse> listar(Long autorId, Long editoraId) {
		return listar(autorId, editoraId, null, null);
	}

	@Transactional(readOnly = true)
	public List<LivroResponse> listar(Long autorId, Long editoraId, Long categoriaId, Long grupoPrecificacaoId) {
		List<Livro> livros;
		if (autorId != null && editoraId != null) {
			livros = livroRepository.findByAutor_IdAndEditora_Id(autorId, editoraId);
		} else if (autorId != null) {
			livros = livroRepository.findByAutor_Id(autorId);
		} else if (editoraId != null) {
			livros = livroRepository.findByEditora_Id(editoraId);
		} else if (categoriaId != null) {
			livros = livroRepository.findByCategorias_Id(categoriaId);
		} else if (grupoPrecificacaoId != null) {
			livros = livroRepository.findByGrupoPrecificacao_Id(grupoPrecificacaoId);
		} else {
			livros = livroRepository.findAll();
		}
		return mapearLivrosComEstoque(livros);
	}

	@Transactional(readOnly = true)
	public List<LivroResponse> listarAtivos() {
		return mapearLivrosComEstoque(livroRepository.findByAtivoTrue());
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
		return mapearLivrosComEstoque(livroRepository.findByTituloContainingIgnoreCase(titulo));
	}

	private List<LivroResponse> mapearLivrosComEstoque(List<Livro> livros) {
		if (livros.isEmpty()) {
			return List.of();
		}
		Map<Long, Estoque> estoquesPorLivro = estoqueRepository.findByLivro_IdIn(
				livros.stream().map(Livro::getId).toList())
				.stream()
				.collect(Collectors.toMap(estoque -> estoque.getLivro().getId(), Function.identity()));
		return livros.stream()
				.map(livro -> new LivroResponse(livro, estoquesPorLivro.get(livro.getId())))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarAutores(String nome) {
		List<Autor> autores = nome == null || nome.isBlank()
				? autorRepository.findAll()
				: autorRepository.findByNomeContainingIgnoreCase(nome.trim());
		return autores.stream().map(LivroOpcaoResponse::new).toList();
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarEditoras(String nome) {
		List<Editora> editoras = nome == null || nome.isBlank()
				? editoraRepository.findAll()
				: editoraRepository.findByNomeContainingIgnoreCase(nome.trim());
		return editoras.stream().map(LivroOpcaoResponse::new).toList();
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarCategorias() {
		return listarCategorias(null);
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarCategorias(String nome) {
		List<Categoria> categorias = nome == null || nome.isBlank()
				? categoriaRepository.findAll()
				: categoriaRepository.findByNomeContainingIgnoreCase(nome.trim());
		return categorias.stream().map(LivroOpcaoResponse::new).toList();
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarGruposPrecificacao() {
		return listarGruposPrecificacao(null);
	}

	@Transactional(readOnly = true)
	public List<LivroOpcaoResponse> listarGruposPrecificacao(String nome) {
		List<GrupoPrecificacao> grupos = nome == null || nome.isBlank()
				? grupoPrecificacaoRepository.findAll()
				: grupoPrecificacaoRepository.findByNomeContainingIgnoreCase(nome.trim());
		return grupos.stream().map(LivroOpcaoResponse::new).toList();
	}

	@Transactional
	public LivroOpcaoResponse criarCategoria(String nome) {
		String nomeNormalizado = normalizarNome(nome, "O nome da categoria e obrigatorio");
		Categoria categoria = categoriaRepository.findFirstByNomeIgnoreCase(nomeNormalizado)
				.orElseGet(() -> categoriaRepository.save(new Categoria(nomeNormalizado)));
		return new LivroOpcaoResponse(categoria);
	}

	@Transactional
	public LivroOpcaoResponse criarGrupoPrecificacao(String nome, BigDecimal percentualMargem) {
		String nomeNormalizado = normalizarNome(nome, "O nome do grupo de precificacao e obrigatorio");
		BigDecimal percentual = percentualMargem == null ? BigDecimal.ZERO : percentualMargem;
		GrupoPrecificacao grupo = grupoPrecificacaoRepository.findFirstByNomeIgnoreCase(nomeNormalizado)
				.orElseGet(() -> grupoPrecificacaoRepository.save(new GrupoPrecificacao(nomeNormalizado, percentual)));
		return new LivroOpcaoResponse(grupo);
	}

	@Transactional
	public LivroResponse atualizar(Long id, LivroRequest request) {
		Livro livro = buscarLivro(id);
		validarDadosUnicos(request.getCodigo(), request.getIsbn(), request.getCodigoBarras(), id);

		Autor autor = resolverAutor(request);
		Editora editora = resolverEditora(request);
		GrupoPrecificacao grupoPrecificacao = resolverGrupoPrecificacao(request);
		Set<Categoria> categorias = resolverCategorias(request);
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

	private Autor resolverAutor(LivroRequest request) {
		if (request.getAutorId() != null) {
			return buscarAutor(request.getAutorId());
		}
		String nome = normalizarNome(request.getAutorNome(), "O nome do autor e obrigatorio");
		return autorRepository.findFirstByNomeIgnoreCase(nome)
				.orElseGet(() -> autorRepository.save(new Autor(nome)));
	}

	private Editora buscarEditora(Long id) {
		return editoraRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Editora nao encontrada: " + id));
	}

	private Editora resolverEditora(LivroRequest request) {
		if (request.getEditoraId() != null) {
			return buscarEditora(request.getEditoraId());
		}
		String nome = normalizarNome(request.getEditoraNome(), "O nome da editora e obrigatorio");
		return editoraRepository.findFirstByNomeIgnoreCase(nome)
				.orElseGet(() -> editoraRepository.save(new Editora(nome)));
	}

	private String normalizarNome(String nome, String mensagemSeAusente) {
		if (nome == null || nome.isBlank()) {
			throw new RegraDeNegocioException(mensagemSeAusente);
		}
		return nome.trim();
	}

	private GrupoPrecificacao buscarGrupoPrecificacao(Long id) {
		return grupoPrecificacaoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Grupo de precificacao nao encontrado: " + id));
	}

	private GrupoPrecificacao resolverGrupoPrecificacao(LivroRequest request) {
		if (request.getGrupoPrecificacaoId() != null) {
			return buscarGrupoPrecificacao(request.getGrupoPrecificacaoId());
		}
		String nome = normalizarNome(request.getGrupoPrecificacaoNome(), "O nome do grupo de precificacao e obrigatorio");
		BigDecimal percentual = request.getGrupoPrecificacaoPercentualMargem() == null
				? BigDecimal.ZERO
				: request.getGrupoPrecificacaoPercentualMargem();
		return grupoPrecificacaoRepository.findFirstByNomeIgnoreCase(nome)
				.orElseGet(() -> grupoPrecificacaoRepository.save(new GrupoPrecificacao(nome, percentual)));
	}

	private Set<Categoria> resolverCategorias(LivroRequest request) {
		Set<Categoria> categorias = new HashSet<>();
		Set<String> nomesNormalizados = new HashSet<>();

		if (request.getCategoriaIds() != null) {
			if (request.getCategoriaIds().size() != request.getCategoriaIds().stream().distinct().count()) {
				throw new RegraDeNegocioException("O livro nao pode possuir categorias duplicadas");
			}
			List<Categoria> categoriasEncontradas = categoriaRepository.findAllById(request.getCategoriaIds());
			if (categoriasEncontradas.size() != request.getCategoriaIds().size()) {
				throw new RecursoNaoEncontradoException("Uma ou mais categorias nao foram encontradas");
			}
			categorias.addAll(categoriasEncontradas);
		}

		if (request.getCategoriaNomes() != null) {
			for (String nome : request.getCategoriaNomes()) {
				String nomeNormalizado = normalizarNome(nome, "O nome da categoria e obrigatorio");
				if (!nomesNormalizados.add(nomeNormalizado.toLowerCase())) {
					throw new RegraDeNegocioException("O livro nao pode possuir categorias duplicadas");
				}
				Categoria categoria = categoriaRepository.findFirstByNomeIgnoreCase(nomeNormalizado)
						.orElseGet(() -> categoriaRepository.save(new Categoria(nomeNormalizado)));
				categorias.add(categoria);
			}
		}

		if (categorias.isEmpty()) {
			throw new RegraDeNegocioException("O livro precisa ter pelo menos uma categoria");
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
