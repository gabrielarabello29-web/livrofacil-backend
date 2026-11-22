package com.livrofacil.modulos.chat.service;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class GeminiVertexChatCompletionClient implements ChatCompletionClient {
    private final String projectId;
    private final String location;
    private final String model;
    private final VertexAccessTokenProvider accessTokenProvider;
    private final RestClient restClient;

    public GeminiVertexChatCompletionClient(
            @Value("${livrofacil.chat.gemini.project-id:}") String projectId,
            @Value("${livrofacil.chat.gemini.location:us}") String location,
            @Value("${livrofacil.chat.gemini.model:gemini-3.5-flash-lite}") String model,
            VertexAccessTokenProvider accessTokenProvider,
            RestClient.Builder restClientBuilder
    ) {
        this.projectId = projectId;
        this.location = location;
        this.model = model;
        this.accessTokenProvider = accessTokenProvider;
        this.restClient = restClientBuilder.baseUrl(endpointForLocation(location)).build();
    }

    private static String endpointForLocation(String location) {
        return switch (location) {
            case "global" -> "https://aiplatform.googleapis.com";
            case "us", "eu" -> "https://aiplatform." + location + ".rep.googleapis.com";
            default -> "https://" + location + "-aiplatform.googleapis.com";
        };
    }

    @Override
    public String completar(String instrucao, String contexto) {
        if (projectId == null || projectId.isBlank()) {
            throw new ChatProviderException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O projeto Google Cloud para o Vertex AI nao esta configurado");
        }
        if (location == null || location.isBlank() || model == null || model.isBlank()) {
            throw new ChatProviderException(HttpStatus.SERVICE_UNAVAILABLE,
                    "A regiao ou o modelo Gemini para o Vertex AI nao esta configurado");
        }

        String accessToken = accessTokenProvider.getAccessToken();
        Map<String, Object> request = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", instrucao))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", contexto)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        try {
            JsonNode body = restClient.post()
                    .uri("/v1/projects/{project}/locations/{location}/publishers/google/models/{model}:generateContent",
                            projectId, location, model)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode parts = body == null
                    ? null
                    : body.path("candidates").path(0).path("content").path("parts");
            StringBuilder textoFinal = new StringBuilder();
            if (parts != null && parts.isArray()) {
                for (JsonNode part : parts) {
                    JsonNode texto = part.path("text");
                    if (!part.path("thought").asBoolean(false) && texto.isTextual()) {
                        textoFinal.append(texto.asText());
                    }
                }
            }
            String text = textoFinal.toString();
            if (text.isBlank()) {
                throw new ChatProviderException(HttpStatus.BAD_GATEWAY,
                        "A integracao Gemini retornou uma resposta invalida");
            }
            return text;
        } catch (RestClientException exception) {
            throw new ChatProviderException(HttpStatus.BAD_GATEWAY,
                    "Nao foi possivel obter uma resposta do Vertex AI", exception);
        }
    }
}
