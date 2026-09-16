package com.livrofacil.produtos.livro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class LivroRequest {

    @NotBlank(message = "O codigo do livro e obrigatorio")
    @Size(max = 50, message = "O codigo do livro deve ter no maximo 50 caracteres")
    private String codigo;

    @NotBlank(message = "O titulo do livro e obrigatorio")
    @Size(max = 200, message = "O titulo do livro deve ter no maximo 200 caracteres")
    private String titulo;

    @NotNull(message = "O ano do livro e obrigatorio")
    @Positive(message = "O ano do livro deve ser maior que zero")
    private Integer ano;

    @NotNull(message = "A edicao do livro e obrigatoria")
    @Positive(message = "A edicao deve ser maior que zero")
    private Integer edicao;

    @NotBlank(message = "O ISBN e obrigatorio")
    @Size(max = 20, message = "O ISBN deve ter no maximo 20 caracteres")
    private String isbn;

    @NotNull(message = "O numero de paginas e obrigatorio")
    @Positive(message = "O numero de paginas deve ser maior que zero")
    private Integer numeroPaginas;

    @NotBlank(message = "A sinopse e obrigatoria")
    @Size(max = 5000, message = "A sinopse deve ter no maximo 5000 caracteres")
    private String sinopse;

    @NotBlank(message = "A URL da imagem da capa e obrigatoria")
    @Size(max = 500, message = "A URL da imagem da capa deve ter no maximo 500 caracteres")
    @Pattern(regexp = "^https?://.+$", message = "A URL da imagem da capa deve iniciar com http:// ou https://")
    private String imagemUrl;

    @NotBlank(message = "O codigo de barras e obrigatorio")
    @Size(max = 50, message = "O codigo de barras deve ter no maximo 50 caracteres")
    private String codigoBarras;

    @NotNull(message = "O valor de venda e obrigatorio")
    @Positive(message = "O valor de venda deve ser maior que zero")
    private BigDecimal valorVenda;

    @NotNull(message = "O status do livro e obrigatorio")
    private Boolean ativo;

    @NotNull(message = "O autor e obrigatorio")
    private Long autorId;

    @NotNull(message = "A editora e obrigatoria")
    private Long editoraId;

    @NotNull(message = "O grupo de precificacao e obrigatorio")
    private Long grupoPrecificacaoId;

    @NotNull(message = "O livro precisa ter pelo menos uma categoria")
    @Size(min = 1, message = "O livro precisa ter pelo menos uma categoria")
    private List<@NotNull(message = "O ID da categoria e obrigatorio") Long> categoriaIds;

    @Valid
    @NotNull(message = "As dimensoes do livro sao obrigatorias")
    private DimensaoRequest dimensao;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getEdicao() {
        return edicao;
    }

    public void setEdicao(Integer edicao) {
        this.edicao = edicao;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }

    public void setValorVenda(BigDecimal valorVenda) {
        this.valorVenda = valorVenda;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public Long getEditoraId() {
        return editoraId;
    }

    public void setEditoraId(Long editoraId) {
        this.editoraId = editoraId;
    }

    public Long getGrupoPrecificacaoId() {
        return grupoPrecificacaoId;
    }

    public void setGrupoPrecificacaoId(Long grupoPrecificacaoId) {
        this.grupoPrecificacaoId = grupoPrecificacaoId;
    }

    public List<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public void setCategoriaIds(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }

    public DimensaoRequest getDimensao() {
        return dimensao;
    }

    public void setDimensao(DimensaoRequest dimensao) {
        this.dimensao = dimensao;
    }
}