package com.livrofacil.modulos.compra.dto;

import java.util.ArrayList;
import java.util.List;

public class HistoricoVendasPorCategoriaResponse {
    private String dataInicio;
    private String dataFim;
    private List<String> meses = new ArrayList<>();
    private List<HistoricoVendasPorCategoriaSerie> series = new ArrayList<>();

    public String getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getDataFim() {
        return dataFim;
    }

    public void setDataFim(String dataFim) {
        this.dataFim = dataFim;
    }

    public List<String> getMeses() {
        return meses;
    }

    public void setMeses(List<String> meses) {
        this.meses = meses;
    }

    public List<HistoricoVendasPorCategoriaSerie> getSeries() {
        return series;
    }

    public void setSeries(List<HistoricoVendasPorCategoriaSerie> series) {
        this.series = series;
    }
}
