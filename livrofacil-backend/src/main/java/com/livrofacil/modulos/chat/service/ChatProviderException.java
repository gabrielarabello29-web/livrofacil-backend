package com.livrofacil.modulos.chat.service;

import org.springframework.http.HttpStatus;

public class ChatProviderException extends RuntimeException {
    private final HttpStatus status;

    public ChatProviderException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ChatProviderException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
