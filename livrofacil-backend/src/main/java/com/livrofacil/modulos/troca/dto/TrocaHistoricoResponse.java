package com.livrofacil.modulos.troca.dto;

import com.livrofacil.modulos.troca.entity.TrocaHistorico;

import java.time.LocalDateTime;

public class TrocaHistoricoResponse {
    private final Long id;
    private final Long trocaId;
    private final String status;
    private final String observacao;
    private final LocalDateTime criadoEm;

    public TrocaHistoricoResponse(TrocaHistorico historico) {
        this.id = historico.getId();
        this.trocaId = historico.getTrocaId();
        this.status = historico.getStatus();
        this.observacao = historico.getObservacao();
        this.criadoEm = historico.getCriadoEm();
    }

    public Long getId() { return id; }
    public Long getTrocaId() { return trocaId; }
    public String getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}