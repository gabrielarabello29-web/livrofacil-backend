package com.livrofacil.modulos.compra.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class FinalizarCompraRequest {
    @NotNull(message = "O carrinho e obrigatorio")
    @Positive(message = "O ID do carrinho deve ser maior que zero")
    private Long carrinhoId;

    @Valid
    @NotEmpty(message = "Informe pelo menos uma forma de pagamento")
    private List<PagamentoRequest> pagamentos;

    public List<PagamentoRequest> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<PagamentoRequest> pagamentos) { this.pagamentos = pagamentos; }
    public Long getCarrinhoId() { return carrinhoId; }
    public void setCarrinhoId(Long carrinhoId) { this.carrinhoId = carrinhoId; }
}