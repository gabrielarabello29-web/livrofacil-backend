package com.livrofacil.modulos.chat.dto;

import com.livrofacil.modulos.chat.entity.ChatFeedback;

import java.util.UUID;

public record RegistrarFeedbackResponse(
        UUID feedbackId,
        UUID mensagemId,
        ChatFeedback.Avaliacao avaliacao,
        String criadoEm
) {
}
