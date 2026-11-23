package com.livrofacil.modulos.chat.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiVertexChatCompletionClientTest {
    @Test
    void enviaFormatoVertexEExtraiTextoDoCandidatoGemini() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        VertexAccessTokenProvider tokenProvider = () -> "access-token";
        GeminiVertexChatCompletionClient client = new GeminiVertexChatCompletionClient(
                "livrofacil-producao", "us-central1", "gemini-3.5-flash-lite", tokenProvider, builder);

        server.expect(requestTo("https://us-central1-aiplatform.googleapis.com/v1/projects/livrofacil-producao"
                        + "/locations/us-central1/publishers/google/models/gemini-3.5-flash-lite:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andExpect(content().json("""
                        {
                          "systemInstruction":{"parts":[{"text":"Instrucao do sistema"}]},
                          "contents":[{"role":"user","parts":[{"text":"Contexto do chat"}]}],
                          "generationConfig":{"responseMimeType":"application/json"}
                        }
                        """, true))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"raciocinio interno","thought":true},{"text":"{\\"texto\\":\\"Oi\\",\\"livroIds\\":[]}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertEquals("{\"texto\":\"Oi\",\"livroIds\":[]}",
                client.completar("Instrucao do sistema", "Contexto do chat"));
        server.verify();
    }

    @Test
    void retorna503SemProjetoEComCredenciaisNaoDisponiveis() {
        VertexAccessTokenProvider tokenProvider = mock(VertexAccessTokenProvider.class);
        GeminiVertexChatCompletionClient semProjeto = new GeminiVertexChatCompletionClient(
                "", "us-central1", "gemini-3.5-flash-lite", tokenProvider, RestClient.builder());

        ChatProviderException erroProjeto = assertThrows(ChatProviderException.class,
                () -> semProjeto.completar("instrucao", "contexto"));
        assertEquals(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, erroProjeto.getStatus());
        verify(tokenProvider, never()).getAccessToken();

        when(tokenProvider.getAccessToken()).thenThrow(new ChatProviderException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "ADC ausente"));
        GeminiVertexChatCompletionClient semCredenciais = new GeminiVertexChatCompletionClient(
                "livrofacil-producao", "us-central1", "gemini-3.5-flash-lite", tokenProvider, RestClient.builder());
        ChatProviderException erroCredencial = assertThrows(ChatProviderException.class,
                () -> semCredenciais.completar("instrucao", "contexto"));
        assertEquals(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, erroCredencial.getStatus());
    }

    @Test
    void rejeitaRespostaGeminiSemTexto() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiVertexChatCompletionClient client = new GeminiVertexChatCompletionClient(
                "livrofacil-producao", "us-central1", "gemini-3.5-flash-lite", () -> "access-token", builder);
        server.expect(requestTo("https://us-central1-aiplatform.googleapis.com/v1/projects/livrofacil-producao"
                        + "/locations/us-central1/publishers/google/models/gemini-3.5-flash-lite:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        ChatProviderException exception = assertThrows(ChatProviderException.class,
                () -> client.completar("instrucao", "contexto"));

        assertEquals(org.springframework.http.HttpStatus.BAD_GATEWAY, exception.getStatus());
        server.verify();
    }

    @Test
    void usaEndpointGlobalQuandoLocalizacaoGlobal() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiVertexChatCompletionClient client = new GeminiVertexChatCompletionClient(
                "livrofacil-producao", "global", "gemini-3.5-flash-lite", () -> "access-token", builder);
        server.expect(requestTo("https://aiplatform.googleapis.com/v1/projects/livrofacil-producao"
                        + "/locations/global/publishers/google/models/gemini-3.5-flash-lite:generateContent"))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\"texto\\":\\"Oi\\",\\"livroIds\\":[]}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertEquals("{\"texto\":\"Oi\",\"livroIds\":[]}",
                client.completar("Instrucao do sistema", "Contexto do chat"));
        server.verify();
    }

    @Test
    void usaEndpointMultiRegiaoDosEstadosUnidos() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiVertexChatCompletionClient client = new GeminiVertexChatCompletionClient(
                "livrofacil-producao", "us", "gemini-3.5-flash-lite", () -> "access-token", builder);
        server.expect(requestTo("https://aiplatform.us.rep.googleapis.com/v1/projects/livrofacil-producao"
                        + "/locations/us/publishers/google/models/gemini-3.5-flash-lite:generateContent"))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\"texto\\":\\"Oi\\",\\"livroIds\\":[]}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertEquals("{\"texto\":\"Oi\",\"livroIds\":[]}",
                client.completar("Instrucao do sistema", "Contexto do chat"));
        server.verify();
    }
}
