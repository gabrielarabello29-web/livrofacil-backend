package com.livrofacil.modulos.livro.dto;

import com.livrofacil.modulos.livro.entity.Autor;
import com.livrofacil.modulos.livro.entity.Categoria;
import com.livrofacil.modulos.livro.entity.Dimensao;
import com.livrofacil.modulos.livro.entity.Editora;
import com.livrofacil.modulos.livro.entity.Estoque;
import com.livrofacil.modulos.livro.entity.GrupoPrecificacao;
import com.livrofacil.modulos.livro.entity.Livro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LivroResponseTest {
    @Test
    void deveMapearLivroCompleto() {
        Autor autor = new Autor(); autor.setNome("Autor");
        Editora editora = new Editora("Editora");
        GrupoPrecificacao grupo = new GrupoPrecificacao(); grupo.setNome("Grupo"); grupo.setPercentualMargem(new BigDecimal("20"));
        Categoria categoria = new Categoria("Categoria");
        Livro livro = new Livro("COD", "Titulo", 2024, 1, "ISBN", 100, "Sinopse", "http://imagem", "BARRAS", BigDecimal.TEN, true, autor, editora, grupo, new Dimensao(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE));
        livro.getCategorias().add(categoria);
        Estoque estoque = new Estoque(livro); estoque.setQuantidadeDisponivel(5); estoque.setQuantidadeBloqueada(1); estoque.setQuantidadeVendida(2);

        LivroResponse response = new LivroResponse(livro, estoque);

        assertEquals("COD", response.getCodigo()); assertEquals("Titulo", response.getTitulo()); assertEquals("Autor", response.getAutorNome());
        assertEquals("Editora", response.getEditoraNome()); assertEquals("Grupo", response.getGrupoPrecificacaoNome());
        assertEquals(1, response.getCategoriaIds().size()); assertEquals(BigDecimal.ONE, response.getDimensao().getAltura());
        assertEquals(5, response.getEstoque().getQuantidadeDisponivel());
    }

    @Test
    void deveMapearLivroCatalogoComRelacoesNulas() {
        Livro livro = new Livro(); livro.setTitulo("Titulo"); livro.setCategorias(null);
        LivroCatalogoResponse response = new LivroCatalogoResponse(livro);
        assertEquals("Titulo", response.getTitulo()); assertNull(response.getAutorNome()); assertEquals(0, response.getCategoriaNomes().size());
    }

    @Test
    void deveMapearOpcoesDeLivro() {
        Autor autor = new Autor(); autor.setNome("Autor");
        Editora editora = new Editora("Editora");
        Categoria categoria = new Categoria("Categoria");
        GrupoPrecificacao grupo = new GrupoPrecificacao(); grupo.setNome("Grupo"); grupo.setPercentualMargem(BigDecimal.ONE);
        assertEquals("Autor", new LivroOpcaoResponse(autor).getNome()); assertEquals("Editora", new LivroOpcaoResponse(editora).getNome());
        assertEquals("Categoria", new LivroOpcaoResponse(categoria).getNome()); assertEquals(BigDecimal.ONE, new LivroOpcaoResponse(grupo).getPercentualMargem());
    }
}
