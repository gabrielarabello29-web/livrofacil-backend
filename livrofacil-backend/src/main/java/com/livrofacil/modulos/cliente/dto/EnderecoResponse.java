package com.livrofacil.modulos.cliente.dto;

import com.livrofacil.modulos.cliente.entity.Endereco;
import java.util.UUID;

public class EnderecoResponse {
    private Long id;
    private UUID clienteId;
    private String tipoEndereco;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private boolean principal;

    public EnderecoResponse(Endereco endereco) {
        this.id = endereco.getId();
        this.clienteId = endereco.getCliente().getId();
        this.tipoEndereco = endereco.getTipoEndereco();
        this.logradouro = endereco.getLogradouro();
        this.numero = endereco.getNumero();
        this.complemento = endereco.getComplemento();
        this.bairro = endereco.getBairro();
        this.cidade = endereco.getCidade();
        this.estado = endereco.getEstado();
        this.cep = endereco.getCep();
        this.principal = endereco.isPrincipal();
    }

    public Long getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public String getTipoEndereco() { return tipoEndereco; }
    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getCep() { return cep; }
    public boolean isPrincipal() { return principal; }
}