package com.livrofacil.modulos.cliente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "endereco")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "end_id")
    private Long id;

    @Column(name = "end_logradouro", length = 150)
    private String logradouro;

    @Column(name = "end_tipo", length = 50)
    private String tipoEndereco;

    @Column(name = "end_numero", length = 20)
    private String numero;

    @Column(name = "end_complemento", length = 100)
    private String complemento;

    @Column(name = "end_bairro", length = 100)
    private String bairro;

    @Column(name = "end_cidade", length = 100)
    private String cidade;

    @Column(name = "end_estado", length = 2)
    private String estado;

    @Column(name = "end_cep", length = 10)
    private String cep;

    @Column(name = "end_pais", length = 100)
    private String pais;

    @Column(name = "end_observacoes", length = 500)
    private String observacoes;

    @Column(name = "end_principal", length = 1)
    private String principal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cli_id", referencedColumnName = "cli_id", nullable = false)
    private Cliente cliente;

    public Endereco() {
    }

    public Endereco(String tipoEndereco, String logradouro, String numero, String complemento, String bairro,
                    String cidade, String estado, String cep, boolean principal, Cliente cliente) {
        this(tipoEndereco, logradouro, numero, complemento, bairro, cidade, estado, cep, null, null, principal, cliente);
    }

    public Endereco(String tipoEndereco, String logradouro, String numero, String complemento, String bairro,
                    String cidade, String estado, String cep, String pais, String observacoes,
                    boolean principal, Cliente cliente) {
        this.tipoEndereco = tipoEndereco;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.pais = pais;
        this.observacoes = observacoes;
        this.principal = principal ? "S" : "N";
        this.cliente = cliente;
    }

    public Long getId() { return id; }
    public String getTipoEndereco() { return tipoEndereco; }
    public void setTipoEndereco(String tipoEndereco) { this.tipoEndereco = tipoEndereco; }
    public String getTipoResidencia() { return getTipoEndereco(); }
    public void setTipoResidencia(String tipoResidencia) { setTipoEndereco(tipoResidencia); }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getTipoLogradouro() { return getLogradouro(); }
    public void setTipoLogradouro(String tipoLogradouro) { setLogradouro(tipoLogradouro); }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public boolean isPrincipal() { return "S".equals(principal); }
    public void setPrincipal(boolean principal) { this.principal = principal ? "S" : "N"; }
    public Cliente getCliente() { return cliente; }
}