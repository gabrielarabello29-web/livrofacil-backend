package com.livrofacil.Service;

import com.livrofacil.produtos.livro.dto.DimensaoRequest;
import com.livrofacil.produtos.livro.dto.LivroRequest;
import com.livrofacil.produtos.livro.repository.AutorRepository;
import com.livrofacil.produtos.livro.repository.CategoriaRepository;
import com.livrofacil.produtos.livro.repository.EditoraRepository;
import com.livrofacil.produtos.livro.repository.GrupoPrecificacaoRepository;
import com.livrofacil.produtos.livro.repository.LivroRepository;
import com.livrofacil.produtos.livro.service.CadastrarLivroUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private EditoraRepository editoraRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private GrupoPrecificacaoRepository grupoPrecificacaoRepository;

    @InjectMocks
    private CadastrarLivroUseCase service;

    @Test
    void deveCriarInstanciaDoServicoDeLivro() {
        assertNotNull(service);
    }

    @Test
    void deveAceitarDadosDeCadastroDeLivro() {
        LivroRequest request = new LivroRequest();
        request.setCodigo("LIV-001");
        request.setTitulo("Clean Code");
        request.setAno(2024);
        request.setEdicao(1);
        request.setIsbn("9788576082675");
        request.setNumeroPaginas(464);
        request.setSinopse("Livro de referência sobre qualidade de software.");
        request.setImagemUrl("https://exemplo.com/capas/clean-code.jpg");
        request.setCodigoBarras("9788576082675");
        request.setValorVenda(new BigDecimal("89.90"));
        request.setAtivo(true);
        request.setAutorId(1L);
        request.setEditoraId(1L);
        request.setGrupoPrecificacaoId(1L);
        request.setCategoriaIds(List.of(1L, 2L));

        DimensaoRequest dimensao = new DimensaoRequest();
        dimensao.setAltura(new BigDecimal("2.00"));
        dimensao.setLargura(new BigDecimal("16.00"));
        dimensao.setProfundidade(new BigDecimal("23.00"));
        dimensao.setPeso(new BigDecimal("0.60"));
        request.setDimensao(dimensao);

        assertNotNull(request);
    }
}
