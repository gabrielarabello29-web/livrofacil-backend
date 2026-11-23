package com.livrofacil.modulos.chat.dto;

import com.livrofacil.modulos.chat.entity.ChatFeedback;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegistrarFeedbackRequest(
        @NotNull UUID clienteId,
        @NotNull UUID mensagemId,
        @NotNull ChatFeedback.Avaliacao avaliacao,
        @NotNull @Size(max = 2000) String comentario
) {
}
