package com.livrofacil.modulos.chat.service;

public interface ChatCompletionClient {
    String completar(String instrucao, String contexto);
}
