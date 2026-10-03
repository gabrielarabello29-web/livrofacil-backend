package com.livrofacil.modulos.troca.service;

import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.compra.entity.ItemPedido;
import com.livrofacil.modulos.compra.entity.Pedido;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.troca.dto.SolicitarTrocaRequest;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.repository.EstoqueRepository;
import com.livrofacil.modulos.compra.repository.ItemPedidoRepository;
import com.livrofacil.modulos.compra.repository.PedidoRepository;
import com.livrofacil.modulos.troca.entity.StatusTroca;
import com.livrofacil.modulos.troca.entity.Troca;
import com.livrofacil.modulos.troca.repository.TrocaRepository;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrocaServiceTest {
    @Mock private TrocaRepository trocaRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private ItemPedidoRepository itemPedidoRepository;
    @Mock private EstoqueRepository estoqueRepository;
    @Mock private VoucherTrocaRepository voucherTrocaRepository;

    @Test
    void deveEmitirVoucherPeloValorHistoricoDoItemAoReceberTroca() throws Exception {
        var service = new TrocaService(trocaRepository, pedidoRepository, itemPedidoRepository,
            estoqueRepository, voucherTrocaRepository);
        Cliente cliente = new Cliente("Cliente", "cliente@email.com", "11999999999");
        setId(cliente, "id", java.util.UUID.randomUUID());
        Pedido pedido = new Pedido();
        setId(pedido, "id", 21L);
        pedido.setCliente(cliente);
        Livro livro = org.mockito.Mockito.mock(Livro.class);
        org.mockito.Mockito.when(livro.getId()).thenReturn(8L);
        ItemPedido item = new ItemPedido();
        setId(item, "id", 31L);
        item.setLivro(livro);
        item.setPedido(pedido);
        item.setTitulo("Livro trocado");
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("12.50"));
        Troca troca = new Troca();
        setId(troca, "id", 41L);
        troca.setPedido(pedido);
        troca.setItemPedido(item);
        troca.setMotivo("Produto danificado");
        troca.setStatus(StatusTroca.AUTORIZADA);
        when(trocaRepository.findById(41L)).thenReturn(Optional.of(troca));
        when(trocaRepository.save(any(Troca.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.receber(41L, false);

        assertEquals("TROCADA", response.getStatus());
        assertTrue(response.getVoucherCodigo().startsWith("TR-"));
        assertEquals(new BigDecimal("25.00"), response.getVoucherValor());
    }

    @Test
    void deveRejeitarTrocaDePedidoAindaNaoEntregue() {
        var service = new TrocaService(trocaRepository, pedidoRepository, itemPedidoRepository,
                estoqueRepository, voucherTrocaRepository);
        UUID clienteId = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
        when(pedidoRepository.findByIdAndClienteId(21L, clienteId)).thenReturn(Optional.of(pedido));
        SolicitarTrocaRequest request = new SolicitarTrocaRequest();
        request.setClienteId(clienteId);
        request.setPedidoId(21L);
        request.setLivroId(8L);
        request.setMotivo("Produto danificado");

        assertThrows(com.livrofacil.exception.RegraDeNegocioException.class, () -> service.solicitar(request));
        verify(trocaRepository, never()).save(any(Troca.class));
    }

    private void setId(Object target, String fieldName, Object id) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, id);
    }
}