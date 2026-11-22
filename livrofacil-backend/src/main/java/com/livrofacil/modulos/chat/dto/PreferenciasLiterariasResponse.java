package com.livrofacil.modulos.chat.dto;

import java.math.BigDecimal;
import java.util.List;

public record PreferenciasLiterariasResponse(
        List<Long> categoriaIds,
        List<String> temasDeInteresse,
        BigDecimal precoMinimo,
        BigDecimal precoMaximo
) {
}
