package com.livrofacil.modulos.chat.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.modulos.chat.dto.EnviarMensagemRequest;
import com.livrofacil.modulos.chat.dto.PreferenciasLiterariasRequest;
import com.livrofacil.modulos.chat.dto.RegistrarFeedbackRequest;
import com.livrofacil.modulos.chat.entity.ChatFeedback;
import com.livrofacil.modulos.chat.entity.ChatMensagem;
import com.livrofacil.modulos.chat.entity.ChatSessao;
import com.livrofacil.modulos.chat.repository.ChatFeedbackRepository;
import com.livrofacil.modulos.chat.repository.ChatMensagemRepository;
import com.livrofacil.modulos.chat.repository.ChatSessaoRepository;
import com.livrofacil.modulos.chat.repository.PreferenciasLiterariasRepository;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.compra.repository.ItemPedidoRepository;
import com.livrofacil.modulos.livro.repository.CategoriaRepository;
import com.livrofacil.modulos.livro.repository.LivroRepository;
import com.livrofacil.modulos.livro.entity.Autor;
import com.livrofacil.modulos.livro.entity.Livro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock private ClienteRepository clienteRepository;
    @Mock private ChatSessaoRepository sessaoRepository;
    @Mock private ChatMensagemRepository mensagemRepository;
    @Mock private ChatFeedbackRepository feedbackRepository;
    @Mock private PreferenciasLiterariasRepository preferenciasRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private LivroRepository livroRepository;
    @Mock private ItemPedidoRepository itemPedidoRepository;
    @Mock private ChatCompletionClient completionClient;

    private ChatService service;
    private final UUID clienteId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ChatService(clienteRepository, sessaoRepository, mensagemRepository,
                feedbackRepository, preferenciasRepository, categoriaRepository, livroRepository,
                itemPedidoRepository, completionClient, new ObjectMapper());
    }

    @Test
    void criaSessaoEContinuaSomenteParaOClienteAssociado() {
        Cliente cliente = clienteAtivo(clienteId);
        ChatSessao sessao = new ChatSessao(cliente);
        UUID sessaoId = UUID.randomUUID();
        ReflectionTestUtils.setField(sessao, "id", sessaoId);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(sessaoRepository.save(any(ChatSessao.class))).thenReturn(sessao);
        when(sessaoRepository.findByIdAndClienteId(sessaoId, clienteId)).thenReturn(Optional.of(sessao));
        configurarContextoVazio(sessaoId);
        when(completionClient.completar(any(), any())).thenReturn("{\"texto\":\"Oi\",\"livroIds\":[]}");

        var primeiraResposta = service.enviarMensagem(new EnviarMensagemRequest(clienteId, null, "Oi"));
        var segundaResposta = service.enviarMensagem(new EnviarMensagemRequest(clienteId, sessaoId, "Mais uma"));

        assertEquals(sessaoId, primeiraResposta.sessaoId());
        assertEquals(sessaoId, segundaResposta.sessaoId());
        verify(sessaoRepository).save(any(ChatSessao.class));
        verify(sessaoRepository).findByIdAndClienteId(sessaoId, clienteId);
        verify(mensagemRepository, org.mockito.Mockito.times(4)).save(any(ChatMensagem.class));
    }

    @Test
    void enviaCatalogoSemPreferenciasOuHistorico() throws Exception {
        Cliente cliente = clienteAtivo(clienteId);
        ChatSessao sessao = new ChatSessao(cliente);
        UUID sessaoId = UUID.randomUUID();
        ReflectionTestUtils.setField(sessao, "id", sessaoId);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(sessaoRepository.save(any(ChatSessao.class))).thenReturn(sessao);
        configurarContextoVazio(sessaoId);
        when(completionClient.completar(any(), any())).thenReturn("{\"texto\":\"Que tema prefere?\",\"livroIds\":[]}");

        var resposta = service.enviarMensagem(new EnviarMensagemRequest(clienteId, null, "Me indique um livro"));
        org.mockito.ArgumentCaptor<String> contexto = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(completionClient).completar(any(), contexto.capture());
        JsonNode dados = new ObjectMapper().readTree(contexto.getValue());

        assertEquals(0, dados.path("preferencias").path("categoriaIds").size());
        assertEquals(0, dados.path("comprasConcluidas").size());
        assertEquals(0, dados.path("historico").size());
        assertTrue(resposta.mensagemId() != null);
    }

    @Test
    void retornaSomenteLivrosDoCatalogoDisponivelEAtivo() {
        Cliente cliente = clienteAtivo(clienteId);
        ChatSessao sessao = new ChatSessao(cliente);
        UUID sessaoId = UUID.randomUUID();
        ReflectionTestUtils.setField(sessao, "id", sessaoId);
        Livro livro = new Livro();
        livro.setId(123L);
        livro.setTitulo("Título");
        livro.setAutor(new Autor("Nome do autor"));
        livro.setImagemUrl("https://exemplo.com/capa.jpg");
        livro.setValorVenda(new BigDecimal("39.90"));
        livro.setSinopse("Romance e mistério");
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(sessaoRepository.save(any(ChatSessao.class))).thenReturn(sessao);
        when(mensagemRepository.findTop20BySessaoIdOrderByCriadoEmDesc(sessaoId, PageRequest.of(0, 20)))
                .thenReturn(List.of());
        when(preferenciasRepository.findByClienteId(clienteId)).thenReturn(Optional.empty());
        when(itemPedidoRepository.buscarHistoricoConcluido(any(), any())).thenReturn(List.of());
        when(livroRepository.findDisponiveisAtivos(any())).thenReturn(List.of(livro));
        when(completionClient.completar(any(), any()))
                .thenReturn("{\"texto\":\"Uma opção para você.\",\"livroIds\":[123,999]}");
        when(mensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            if (mensagem.getId() == null) {
                ReflectionTestUtils.setField(mensagem, "id", UUID.randomUUID());
            }
            return mensagem;
        });

        var resposta = service.enviarMensagem(new EnviarMensagemRequest(clienteId, null, "Quero um romance"));

        assertEquals(1, resposta.livros().size());
        assertEquals(123L, resposta.livros().get(0).livroId());
        assertEquals("/livros/123", resposta.livros().get(0).url());
        assertEquals("https://exemplo.com/capa.jpg", resposta.livros().get(0).imagemUrl());
        org.mockito.ArgumentCaptor<ChatMensagem> mensagens =
                org.mockito.ArgumentCaptor.forClass(ChatMensagem.class);
        verify(mensagemRepository, org.mockito.Mockito.times(2)).save(mensagens.capture());
        ChatMensagem respostaSalva = mensagens.getAllValues().get(1);
        assertTrue(respostaSalva.getContextoJson() != null);
        assertEquals("Quero um romance",
                new ObjectMapper().readTree(respostaSalva.getContextoJson()).path("mensagemAtual").asText());
    }

    @Test
    void naoPermiteContinuarSessaoDeOutroCliente() {
        UUID sessaoId = UUID.randomUUID();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteAtivo(clienteId)));
        when(sessaoRepository.findByIdAndClienteId(sessaoId, clienteId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.enviarMensagem(new EnviarMensagemRequest(clienteId, sessaoId, "Oi")));
        verify(completionClient, never()).completar(any(), any());
    }

    @Test
    void alteraFeedbackExistenteSemCriarOutroRegistro() {
        Cliente cliente = clienteAtivo(clienteId);
        ChatSessao sessao = new ChatSessao(cliente);
        UUID sessaoId = UUID.randomUUID();
        ReflectionTestUtils.setField(sessao, "id", sessaoId);
        ChatMensagem mensagem = new ChatMensagem(sessao, ChatMensagem.Tipo.IA, "Resposta");
        UUID mensagemId = UUID.randomUUID();
        ReflectionTestUtils.setField(mensagem, "id", mensagemId);
        ChatFeedback feedback = new ChatFeedback(cliente, mensagem, ChatFeedback.Avaliacao.UTIL, "");
        UUID feedbackId = UUID.randomUUID();
        ReflectionTestUtils.setField(feedback, "id", feedbackId);
        when(clienteRepository.findByIdForUpdate(clienteId)).thenReturn(Optional.of(cliente));
        when(mensagemRepository.findByIdAndSessaoClienteIdAndTipo(
                mensagemId, clienteId, ChatMensagem.Tipo.IA)).thenReturn(Optional.of(mensagem));
        when(feedbackRepository.findByClienteIdAndMensagemId(clienteId, mensagemId))
                .thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(feedback)).thenReturn(feedback);

        var resposta = service.registrarFeedback(new RegistrarFeedbackRequest(
                clienteId, mensagemId, ChatFeedback.Avaliacao.NAO_UTIL, "Não recomendou livros relevantes"));

        assertEquals(feedbackId, resposta.feedbackId());
        assertEquals(ChatFeedback.Avaliacao.NAO_UTIL, resposta.avaliacao());
        assertEquals("Não recomendou livros relevantes", feedback.getComentario());
        verify(feedbackRepository).save(feedback);
    }

    @Test
    void naoPermiteFeedbackEmRespostaDeOutroCliente() {
        UUID mensagemId = UUID.randomUUID();
        when(clienteRepository.findByIdForUpdate(clienteId)).thenReturn(Optional.of(clienteAtivo(clienteId)));
        when(mensagemRepository.findByIdAndSessaoClienteIdAndTipo(
                mensagemId, clienteId, ChatMensagem.Tipo.IA)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.registrarFeedback(
                new RegistrarFeedbackRequest(clienteId, mensagemId, ChatFeedback.Avaliacao.UTIL, "")));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void devolvePreferenciasVaziasERejeitaCategoriaInexistente() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteAtivo(clienteId)));
        when(preferenciasRepository.findByClienteId(clienteId)).thenReturn(Optional.empty());
        var vazias = service.buscarPreferencias(clienteId);
        assertEquals(List.of(), vazias.categoriaIds());
        assertEquals(List.of(), vazias.temasDeInteresse());
        assertEquals(null, vazias.precoMinimo());

        when(categoriaRepository.findAllById(List.of(121L))).thenReturn(List.of());
        assertThrows(ChatRequestException.class, () -> service.atualizarPreferencias(clienteId,
                new PreferenciasLiterariasRequest(List.of(121L), List.of("mistério"), null, BigDecimal.TEN)));
        assertThrows(ChatRequestException.class, () -> service.atualizarPreferencias(clienteId,
                new PreferenciasLiterariasRequest(List.of(), List.of(), new BigDecimal("90"), new BigDecimal("80"))));
    }

    private void configurarContextoVazio(UUID sessaoId) {
        when(mensagemRepository.findTop20BySessaoIdOrderByCriadoEmDesc(sessaoId, PageRequest.of(0, 20)))
                .thenReturn(List.of());
        when(preferenciasRepository.findByClienteId(clienteId)).thenReturn(Optional.empty());
        when(itemPedidoRepository.buscarHistoricoConcluido(any(), any())).thenReturn(List.of());
        when(livroRepository.findDisponiveisAtivos(any())).thenReturn(List.of());
        when(mensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            if (mensagem.getId() == null) {
                ReflectionTestUtils.setField(mensagem, "id", UUID.randomUUID());
            }
            return mensagem;
        });
    }

    private Cliente clienteAtivo(UUID id) {
        Cliente cliente = new Cliente();
        ReflectionTestUtils.setField(cliente, "id", id);
        cliente.setAtivo("S");
        return cliente;
    }
}
