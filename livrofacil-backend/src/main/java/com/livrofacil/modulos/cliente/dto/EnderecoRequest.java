package com.livrofacil.modulos.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EnderecoRequest {

    @NotBlank(message = "O tipo de endereco e obrigatorio")
    @Size(max = 50, message = "O tipo de endereco deve ter no maximo 50 caracteres")
    private String tipoEndereco;

    @NotBlank(message = "O logradouro e obrigatorio")
    @Size(max = 150, message = "O logradouro deve ter no maximo 150 caracteres")
    private String logradouro;

    @NotBlank(message = "O numero e obrigatorio")
    @Pattern(regexp = "^(?:\\d+[A-Za-z]?|S/N)$", message = "O numero deve ser numerico, como 100, 100A ou S/N")
    private String numero;

    @Size(max = 100, message = "O complemento deve ter no maximo 100 caracteres")
    private String complemento;

    @NotBlank(message = "O bairro e obrigatorio")
    @Size(max = 100, message = "O bairro deve ter no maximo 100 caracteres")
    private String bairro;

    @NotBlank(message = "A cidade e obrigatoria")
    @Size(max = 100, message = "A cidade deve ter no maximo 100 caracteres")
    private String cidade;

    @NotBlank(message = "O estado e obrigatorio")
    @Pattern(regexp = "^[A-Z]{2}$", message = "O estado deve possuir 2 letras maiusculas")
    private String estado;

    @NotBlank(message = "O CEP e obrigatorio")
    @Pattern(regexp = "^\\d{5}-\\d{3}$", message = "O CEP deve seguir o formato 00000-000")
    private String cep;
    private boolean principal;

    public String getLogradouro() { return logradouro; }
    public String getTipoEndereco() { return tipoEndereco; }
    public void setTipoEndereco(String tipoEndereco) { this.tipoEndereco = tipoEndereco; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
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
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
}