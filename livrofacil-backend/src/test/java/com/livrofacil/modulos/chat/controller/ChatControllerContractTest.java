package com.livrofacil.modulos.chat.controller;

import com.livrofacil.exception.GlobalExceptionHandler;
import com.livrofacil.modulos.chat.dto.EnviarMensagemResponse;
import com.livrofacil.modulos.chat.dto.LivroRecomendadoResponse;
import com.livrofacil.modulos.chat.dto.PreferenciasLiterariasResponse;
import com.livrofacil.modulos.chat.dto.RegistrarFeedbackResponse;
import com.livrofacil.modulos.chat.entity.ChatFeedback;
import com.livrofacil.modulos.chat.service.ChatService;
import com.livrofacil.modulos.chat.service.ChatRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerContractTest {
    @Mock private ChatService chatService;
    private MockMvc mockMvc;
    private final UUID clienteId = UUID.fromString("c1baea49-b8e8-4903-b272-2bd3264ca75a");
    private final UUID sessaoId = UUID.fromString("1807d74e-d19a-43d6-a66a-cfb62e1928e1");
    private final UUID mensagemId = UUID.fromString("740693ee-8044-4a80-8c31-880f7c030708");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(chatService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void retornaContratoExatoDaMensagemEFeedback() throws Exception {
        var livro = new LivroRecomendadoResponse(123L, "Título", "Nome do autor",
                "https://exemplo.com/capa.jpg", new BigDecimal("39.90"), "/livros/123");
        when(chatService.enviarMensagem(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new EnviarMensagemResponse(sessaoId, mensagemId, "Encontrei opções.", List.of(livro)));
        mockMvc.perform(post("/api/chat/mensagens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":"c1baea49-b8e8-4903-b272-2bd3264ca75a","sessaoId":null,"mensagem":"Quero um romance"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "sessaoId":"1807d74e-d19a-43d6-a66a-cfb62e1928e1",
                          "mensagemId":"740693ee-8044-4a80-8c31-880f7c030708",
                          "texto":"Encontrei opções.",
                          "livros":[{"livroId":123,"titulo":"Título","autor":"Nome do autor","imagemUrl":"https://exemplo.com/capa.jpg","preco":39.90,"url":"/livros/123"}]
                        }
                        """, true));

        when(chatService.registrarFeedback(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new RegistrarFeedbackResponse(UUID.fromString("104f0bd4-b651-497c-b1ae-19bece941ad4"),
                        mensagemId, ChatFeedback.Avaliacao.UTIL, Instant.parse("2026-11-19T12:00:00Z").toString()));
        mockMvc.perform(post("/api/chat/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":"c1baea49-b8e8-4903-b272-2bd3264ca75a","mensagemId":"740693ee-8044-4a80-8c31-880f7c030708","avaliacao":"UTIL","comentario":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "feedbackId":"104f0bd4-b651-497c-b1ae-19bece941ad4",
                          "mensagemId":"740693ee-8044-4a80-8c31-880f7c030708",
                          "avaliacao":"UTIL",
                          "criadoEm":"2026-11-19T12:00:00Z"
                        }
                        """, true));
    }

    @Test
    void retornaPreferenciasVaziasEValidaIdsUuid() throws Exception {
        when(chatService.buscarPreferencias(clienteId))
                .thenReturn(new PreferenciasLiterariasResponse(List.of(), List.of(), null, null));
        mockMvc.perform(get("/api/clientes/{clienteId}/preferencias-literarias", clienteId))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"categoriaIds":[],"temasDeInteresse":[],"precoMinimo":null,"precoMaximo":null}
                        """, true));
        mockMvc.perform(get("/api/clientes/invalido/preferencias-literarias"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizaPreferenciasNoFormatoDoContratoEValidaFaixaDePreco() throws Exception {
        when(chatService.atualizarPreferencias(org.mockito.ArgumentMatchers.eq(clienteId),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PreferenciasLiterariasResponse(
                        List.of(12L, 34L), List.of("mistério", "ficção histórica"), null, new BigDecimal("80")));
        mockMvc.perform(put("/api/clientes/{clienteId}/preferencias-literarias", clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaIds":[12,34],"temasDeInteresse":["mistério","ficção histórica"],"precoMinimo":null,"precoMaximo":80}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"categoriaIds":[12,34],"temasDeInteresse":["mistério","ficção histórica"],"precoMinimo":null,"precoMaximo":80}
                        """, true));

        mockMvc.perform(put("/api/clientes/{clienteId}/preferencias-literarias", clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaIds":[],"temasDeInteresse":[],"precoMinimo":90,"precoMaximo":80}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaCategoriaInexistenteComoErroDeValidacao() throws Exception {
        when(chatService.atualizarPreferencias(org.mockito.ArgumentMatchers.eq(clienteId),
                org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ChatRequestException("Uma ou mais categorias informadas nao existem"));
        mockMvc.perform(put("/api/clientes/{clienteId}/preferencias-literarias", clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaIds":[99999],"temasDeInteresse":[],"precoMinimo":null,"precoMaximo":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"status":400,"erro":"Bad Request","mensagem":"Uma ou mais categorias informadas nao existem","erros":{},"caminho":"/api/clientes/c1baea49-b8e8-4903-b272-2bd3264ca75a/preferencias-literarias"}
                        """, false));
    }
}
