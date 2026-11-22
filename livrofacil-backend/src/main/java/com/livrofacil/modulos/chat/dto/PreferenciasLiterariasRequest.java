package com.livrofacil.modulos.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record PreferenciasLiterariasRequest(
        @NotNull List<@Positive Long> categoriaIds,
        @NotNull List<@NotBlank @Size(max = 100) String> temasDeInteresse,
        @DecimalMin("0.00") BigDecimal precoMinimo,
        @DecimalMin("0.00") BigDecimal precoMaximo
) {
    @AssertTrue(message = "precoMaximo deve ser maior ou igual a precoMinimo")
    @JsonIgnore
    public boolean isFaixaDePrecoValida() {
        return precoMinimo == null || precoMaximo == null || precoMaximo.compareTo(precoMinimo) >= 0;
    }
}
