package com.livrofacil.modulos.chat.service;

import com.livrofacil.modulos.chat.entity.ChatFeedback;
import com.livrofacil.modulos.chat.entity.ChatMensagem;
import com.livrofacil.modulos.chat.entity.ChatSessao;
import com.livrofacil.modulos.chat.repository.ChatFeedbackRepository;
import com.livrofacil.modulos.compra.repository.ItemPedidoRepository;
import com.livrofacil.modulos.livro.entity.Autor;
import com.livrofacil.modulos.livro.entity.Categoria;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.repository.LivroRepository;
import com.livrofacil.modulos.cliente.entity.Cliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetExportServiceTest {
    @Mock private ChatFeedbackRepository feedbackRepository;
    @Mock private ItemPedidoRepository itemPedidoRepository;
    @Mock private LivroRepository livroRepository;

    @Test
    void gera100LinhasDeTreinoCom15PorCentoDeGuardrailsNoFormatoVertex() throws Exception {
        Autor autor = new Autor("Autor Teste");
        Categoria categoria = new Categoria("Ficção científica");
        Livro livro = new Livro(
                "TESTE-001", "Livro Teste", 2026, 1, "9780000000001", 300,
                "Uma sinopse realista para o teste.", "https://exemplo.com/capa.jpg",
                "000000000001", new BigDecimal("39.90"), true, autor, null, null, null);
        livro.setId(1L);
        livro.adicionarCategoria(categoria);

        when(feedbackRepository.findByAvaliacaoOrderByCriadoEmDesc(ChatFeedback.Avaliacao.UTIL))
                .thenReturn(List.of());
        when(livroRepository.findDisponiveisAtivos(any(Pageable.class))).thenReturn(List.of(livro));

        DatasetExportService service = criarService();

        String jsonl = service.exportarTreinoJsonl();
        String[] linhas = jsonl.split("\\R");

        assertEquals(100, linhas.length);
        assertEquals(15, countContains(linhas, "Não posso ajudar com este assunto"));
        validarFormatoVertex(linhas);
    }

    @Test
    void gera20LinhasDeValidacaoCom15PorCentoDeGuardrails() throws Exception {
        Livro livro = criarLivro();
        when(livroRepository.findDisponiveisAtivos(any(Pageable.class))).thenReturn(List.of(livro));
        DatasetExportService service = criarService();

        String[] linhas = service.exportarValidacaoJsonl().split("\\R");

        assertEquals(20, linhas.length);
        assertEquals(3, countContains(linhas, "Não posso ajudar com este assunto"));
        validarFormatoVertex(linhas);
    }

    @Test
    void usaSnapshotDoContextoEmFeedbackUtil() throws Exception {
        Livro livro = criarLivro();
        String contexto = """
                {"mensagemAtual":"Quero algo de ficção científica","historico":[],"preferencias":{"categorias":["Ficção científica"]},"comprasConcluidas":[],"catalogo":[{"livroId":1}]}
                """.strip();
        Cliente cliente = new Cliente();
        ChatSessao sessao = new ChatSessao(cliente);
        ChatMensagem resposta = new ChatMensagem(sessao, ChatMensagem.Tipo.IA, "Resposta revisada");
        resposta.setContextoJson(contexto);
        resposta.getLivros().add(livro);
        ChatFeedback feedback = new ChatFeedback(cliente, resposta, ChatFeedback.Avaliacao.UTIL, "");

        when(feedbackRepository.findByAvaliacaoOrderByCriadoEmDesc(ChatFeedback.Avaliacao.UTIL))
                .thenReturn(List.of(feedback));
        when(itemPedidoRepository.buscarHistoricoConcluido(any(), any())).thenReturn(List.of());
        when(livroRepository.findDisponiveisAtivos(any(Pageable.class))).thenReturn(List.of(livro));

        String primeiraLinha = criarService().exportarTreinoJsonl().split("\\R")[0];
        var primeiroExemplo = new ObjectMapper().readTree(primeiraLinha);

        assertEquals(contexto, primeiroExemplo.path("contents").get(0).path("parts").get(0).path("text").asText());
        assertEquals("Resposta revisada",
                new ObjectMapper().readTree(primeiroExemplo.path("contents").get(1)
                        .path("parts").get(0).path("text").asText()).path("texto").asText());
    }

    @Test
    void ignoraFeedbackHistoricoSemSnapshotDeContexto() {
        Cliente cliente = new Cliente();
        ChatSessao sessao = new ChatSessao(cliente);
        ChatMensagem resposta = new ChatMensagem(sessao, ChatMensagem.Tipo.IA, "Resposta sem contexto histórico");
        ChatFeedback feedback = new ChatFeedback(cliente, resposta, ChatFeedback.Avaliacao.UTIL, "");
        Livro livro = criarLivro();

        when(feedbackRepository.findByAvaliacaoOrderByCriadoEmDesc(ChatFeedback.Avaliacao.UTIL))
                .thenReturn(List.of(feedback));
        when(itemPedidoRepository.buscarHistoricoConcluido(any(), any())).thenReturn(List.of());
        when(livroRepository.findDisponiveisAtivos(any(Pageable.class))).thenReturn(List.of(livro));

        String jsonl = criarService().exportarTreinoJsonl();

        assertFalse(jsonl.contains("Resposta sem contexto histórico"));
    }

    private Livro criarLivro() {
        Autor autor = new Autor("Autor Teste");
        Categoria categoria = new Categoria("Ficção científica");
        Livro livro = new Livro(
                "TESTE-001", "Livro Teste", 2026, 1, "9780000000001", 300,
                "Uma sinopse realista para o teste.", "https://exemplo.com/capa.jpg",
                "000000000001", new BigDecimal("39.90"), true, autor, null, null, null);
        livro.setId(1L);
        livro.adicionarCategoria(categoria);
        return livro;
    }

    private DatasetExportService criarService() {
        return new DatasetExportService(
                feedbackRepository, itemPedidoRepository, livroRepository, new ObjectMapper());
    }

    private void validarFormatoVertex(String[] linhas) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        for (String linha : linhas) {
            var exemplo = mapper.readTree(linha);
            assertTrue(exemplo.isObject());
            assertTrue(exemplo.has("systemInstruction"));
            assertFalse(exemplo.has("messages"));

            var contents = exemplo.path("contents");
            assertEquals("user", contents.get(0).path("role").asText());
            assertEquals("model", contents.get(1).path("role").asText());

            var contexto = mapper.readTree(contents.get(0).path("parts").get(0).path("text").asText());
            assertTrue(contexto.has("mensagemAtual"));
            assertTrue(contexto.has("preferencias"));
            assertTrue(contexto.has("catalogo"));

            var resposta = mapper.readTree(contents.get(1).path("parts").get(0).path("text").asText());
            assertTrue(resposta.path("texto").isTextual());
            assertTrue(resposta.path("livroIds").isArray());
        }
    }

    private long countContains(String[] linhas, String texto) {
        long total = 0;
        for (String linha : linhas) {
            if (linha.contains(texto)) {
                total++;
            }
        }
        return total;
    }
}
