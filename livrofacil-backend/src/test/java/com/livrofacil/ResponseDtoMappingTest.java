package com.livrofacil;

import com.livrofacil.modulos.cliente.dto.ClienteResponse;
import com.livrofacil.modulos.cliente.dto.EnderecoResponse;
import com.livrofacil.modulos.cliente.dto.FormaPagamentoResponse;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.entity.Endereco;
import com.livrofacil.modulos.cliente.entity.FormaPagamento;
import com.livrofacil.modulos.compra.dto.ItemPedidoResponse;
import com.livrofacil.modulos.compra.entity.ItemPedido;
import com.livrofacil.modulos.compra.entity.Pedido;
import com.livrofacil.modulos.livro.entity.Livro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseDtoMappingTest {
    @Test
    void deveMapearRespostasDeClienteEnderecoCartaoEPedido() throws Exception {
        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente("Nome", "email", "telefone"); setId(cliente, id); cliente.setCpf("12345678909"); cliente.setNumeroRegistro(7L); cliente.setDataExclusao(LocalDateTime.now());
        ClienteResponse clienteResponse = new ClienteResponse(cliente);
        assertEquals("123.456.789-09", clienteResponse.getCpf()); assertEquals(7L, clienteResponse.getNumeroRegistro());

        Endereco endereco = new Endereco("CASA", "Rua", "1", "Apto", "Centro", "Cidade", "SP", "01000-000", true, cliente);
        EnderecoResponse enderecoResponse = new EnderecoResponse(endereco); assertEquals(id, enderecoResponse.getClienteId()); assertEquals("Rua", enderecoResponse.getLogradouro());

        FormaPagamento cartao = new FormaPagamento("Nome", "CREDITO", "4444", "12/28", "VISA", true, cliente);
        FormaPagamentoResponse cartaoResponse = new FormaPagamentoResponse(cartao); assertEquals("4444", cartaoResponse.getUltimosDigitos()); assertEquals(true, cartaoResponse.isAtivo());

        Livro livro = org.mockito.Mockito.mock(Livro.class); whenId(livro, 3L);
        Pedido pedido = new Pedido(); setId(pedido, 9L);
        ItemPedido item = new ItemPedido(); item.setPedido(pedido); item.setLivro(livro); item.setQuantidade(2); item.setValorUnitario(BigDecimal.TEN);
        ItemPedidoResponse itemResponse = new ItemPedidoResponse(item); assertEquals(9L, itemResponse.getPedidoId()); assertEquals(2, itemResponse.getQuantidade());
    }

    private void setId(Cliente cliente, UUID id) throws Exception { Field field = Cliente.class.getDeclaredField("id"); field.setAccessible(true); field.set(cliente, id); }
    private void setId(Pedido pedido, Long id) throws Exception { Field field = Pedido.class.getDeclaredField("id"); field.setAccessible(true); field.set(pedido, id); }
    private void whenId(Livro livro, Long id) { org.mockito.Mockito.when(livro.getId()).thenReturn(id); }
}
