package com.livrofacil.modulos.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FormaPagamentoRequest {

    @NotBlank(message = "O nome do titular e obrigatorio")
    @Size(max = 100, message = "O nome do titular deve ter no maximo 100 caracteres")
    private String nomeTitular;

    @NotBlank(message = "O tipo do cartao e obrigatorio")
    @Pattern(regexp = "^CREDITO$", message = "Somente cartoes de credito podem ser cadastrados para pagamento")
    private String tipoCartao;

    @NotBlank(message = "O numero do cartao e obrigatorio")
    @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$", message = "O numero do cartao deve seguir o formato 1111 1111 1111 1111")
    private String numeroCartao;

    @NotBlank(message = "A validade do cartao e obrigatoria")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "A validade deve seguir o formato MM/AA")
    private String validade;

    @NotBlank(message = "A bandeira do cartao e obrigatoria")
    @Pattern(regexp = "^(VISA|MASTERCARD|ELO|AMEX|HIPERCARD)$", message = "Bandeira de cartao invalida")
    private String bandeira;

    private boolean preferencial;

    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }
    public String getTipoCartao() { return tipoCartao; }
    public void setTipoCartao(String tipoCartao) { this.tipoCartao = tipoCartao; }
    public String getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(String numeroCartao) { this.numeroCartao = formatarNumeroCartao(numeroCartao); }

    private String formatarNumeroCartao(String numeroCartao) {
        if (numeroCartao == null) {
            return null;
        }

        String apenasNumeros = numeroCartao.replaceAll("\\D", "");
        if (apenasNumeros.length() > 16) {
            apenasNumeros = apenasNumeros.substring(0, 16);
        }

        StringBuilder formatado = new StringBuilder();
        for (int i = 0; i < apenasNumeros.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatado.append(" ");
            }
            formatado.append(apenasNumeros.charAt(i));
        }

        return formatado.toString();
    }
    public String getValidade() { return validade; }
    public void setValidade(String validade) { this.validade = validade; }
    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }
    public boolean isPreferencial() { return preferencial; }
    public void setPreferencial(boolean preferencial) { this.preferencial = preferencial; }
}