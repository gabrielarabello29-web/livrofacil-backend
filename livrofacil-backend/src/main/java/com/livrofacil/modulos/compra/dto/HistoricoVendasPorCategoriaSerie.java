package com.livrofacil.modulos.compra.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HistoricoVendasPorCategoriaSerie {
    private Long categoriaId;
    private String categoriaNome;
    private List<BigDecimal> valores = new ArrayList<>();

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public void setCategoriaNome(String categoriaNome) {
        this.categoriaNome = categoriaNome;
    }

    public List<BigDecimal> getValores() {
        return valores;
    }

    public void setValores(List<BigDecimal> valores) {
        this.valores = valores;
    }
}
