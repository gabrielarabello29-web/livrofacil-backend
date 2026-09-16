package com.livrofacil.produtos.livro.dto;

import com.livrofacil.produtos.livro.entity.Categoria;
import com.livrofacil.produtos.livro.entity.Estoque;
import com.livrofacil.produtos.livro.entity.Livro;

import java.math.BigDecimal;
import java.util.List;

public class LivroResponse {

    private Long id;
    private String codigo;
    private String titulo;
    private Integer ano;
    private Integer edicao;
    private String isbn;
    private Integer numeroPaginas;
    private String sinopse;
    private String imagemUrl;
    private String codigoBarras;
    private BigDecimal valorVenda;
    private Boolean ativo;
    private Long autorId;
    private String autorNome;
    private Long editoraId;
    private String editoraNome;
    private Long grupoPrecificacaoId;
    private String grupoPrecificacaoNome;
    private List<Long> categoriaIds;
    private List<String> categoriaNomes;
    private DimensaoRequest dimensao;
    private EstoqueResponse estoque;

    public LivroResponse(Livro livro) {

        this(livro, null);
    }

    public LivroResponse(Livro livro, Estoque estoque) {
        this.id = livro.getId();
        this.codigo = livro.getCodigo();
        this.titulo = livro.getTitulo();
        this.ano = livro.getAno();
        this.edicao = livro.getEdicao();
        this.isbn = livro.getIsbn();
        this.numeroPaginas = livro.getNumeroPaginas();
        this.sinopse = livro.getSinopse();
        this.imagemUrl = livro.getImagemUrl();
        this.codigoBarras = livro.getCodigoBarras();
        this.valorVenda = livro.getValorVenda();
        this.ativo = livro.getAtivo();
        this.autorId = livro.getAutor() != null ? livro.getAutor().getId() : null;
        this.autorNome = livro.getAutor() != null ? livro.getAutor().getNome() : null;
        this.editoraId = livro.getEditora() != null ? livro.getEditora().getId() : null;
        this.editoraNome = livro.getEditora() != null ? livro.getEditora().getNome() : null;
        this.grupoPrecificacaoId = livro.getGrupoPrecificacao() != null ? livro.getGrupoPrecificacao().getId() : null;
        this.grupoPrecificacaoNome = livro.getGrupoPrecificacao() != null ? livro.getGrupoPrecificacao().getNome() : null;
        this.categoriaIds = livro.getCategorias() == null ? List.of() : livro.getCategorias().stream().map(Categoria::getId).toList();
        this.categoriaNomes = livro.getCategorias() == null ? List.of() : livro.getCategorias().stream().map(Categoria::getNome).toList();
        if (livro.getDimensao() != null) {
            DimensaoRequest dimensaoRequest = new DimensaoRequest();
            dimensaoRequest.setAltura(livro.getDimensao().getAltura());
            dimensaoRequest.setLargura(livro.getDimensao().getLargura());
            dimensaoRequest.setProfundidade(livro.getDimensao().getProfundidade());
            dimensaoRequest.setPeso(livro.getDimensao().getPeso());
            this.dimensao = dimensaoRequest;
        }
        this.estoque = estoque == null ? null : new EstoqueResponse(estoque);
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getAno() {
        return ano;
    }

    public Integer getEdicao() {
        return edicao;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public String getSinopse() {
        return sinopse;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public Long getEditoraId() {
        return editoraId;
    }

    public String getEditoraNome() {
        return editoraNome;
    }

    public Long getGrupoPrecificacaoId() {
        return grupoPrecificacaoId;
    }

    public String getGrupoPrecificacaoNome() {
        return grupoPrecificacaoNome;
    }

    public List<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public List<String> getCategoriaNomes() {
        return categoriaNomes;
    }

    public DimensaoRequest getDimensao() {
        return dimensao;
    }

    public EstoqueResponse getEstoque() {
        return estoque;
    }
}