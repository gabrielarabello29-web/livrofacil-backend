package com.livrofacil.modulos.chat.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class GoogleAdcAccessTokenProvider implements VertexAccessTokenProvider {
    private static final String CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private GoogleCredentials credentials;

    @Override
    public String getAccessToken() {
        try {
            GoogleCredentials currentCredentials = getCredentials();
            currentCredentials.refreshIfExpired();
            AccessToken accessToken = currentCredentials.getAccessToken();
            if (accessToken == null || accessToken.getTokenValue() == null
                    || accessToken.getTokenValue().isBlank()) {
                throw new ChatProviderException(HttpStatus.SERVICE_UNAVAILABLE,
                        "As credenciais Google ADC nao forneceram um token de acesso");
            }
            return accessToken.getTokenValue();
        } catch (IOException exception) {
            throw new ChatProviderException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Credenciais Google ADC ausentes ou indisponiveis para o Vertex AI", exception);
        }
    }

    private synchronized GoogleCredentials getCredentials() throws IOException {
        if (credentials == null) {
            credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(List.of(CLOUD_PLATFORM_SCOPE));
        }
        return credentials;
    }
}
