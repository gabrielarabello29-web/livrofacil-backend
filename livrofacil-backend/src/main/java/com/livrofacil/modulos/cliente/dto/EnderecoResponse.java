package com.livrofacil.modulos.cliente.dto;

import com.livrofacil.modulos.cliente.entity.Endereco;
import java.util.UUID;

public class EnderecoResponse {
    private Long id;
    private UUID clienteId;
    private String tipoEndereco;
    private String tipoResidencia;
    private String logradouro;
    private String tipoLogradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String pais;
    private String observacoes;
    private boolean principal;

    public EnderecoResponse(Endereco endereco) {
        this.id = endereco.getId();
        this.clienteId = endereco.getCliente().getId();
        this.tipoEndereco = endereco.getTipoEndereco();
        this.tipoResidencia = endereco.getTipoResidencia();
        this.logradouro = endereco.getLogradouro();
        this.tipoLogradouro = endereco.getTipoLogradouro();
        this.numero = endereco.getNumero();
        this.complemento = endereco.getComplemento();
        this.bairro = endereco.getBairro();
        this.cidade = endereco.getCidade();
        this.estado = endereco.getEstado();
        this.cep = endereco.getCep();
        this.pais = endereco.getPais();
        this.observacoes = endereco.getObservacoes();
        this.principal = endereco.isPrincipal();
    }

    public Long getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public String getTipoEndereco() { return tipoEndereco; }
    public String getTipoResidencia() { return tipoResidencia; }
    public String getLogradouro() { return logradouro; }
    public String getTipoLogradouro() { return tipoLogradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getCep() { return cep; }
    public String getPais() { return pais; }
    public String getObservacoes() { return observacoes; }
    public boolean isPrincipal() { return principal; }
}