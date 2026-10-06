package com.livrofacil.modulos.compra.service;

import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.cliente.entity.BandeiraPagamento;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.entity.Endereco;
import com.livrofacil.modulos.cliente.entity.FormaPagamento;
import com.livrofacil.modulos.cliente.repository.BandeiraPagamentoRepository;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.EnderecoRepository;
import com.livrofacil.modulos.cliente.repository.FormaPagamentoRepository;
import com.livrofacil.modulos.compra.dto.FinalizarCompraRequest;
import com.livrofacil.modulos.compra.dto.IniciarCompraRequest;
import com.livrofacil.modulos.compra.dto.PagamentoRequest;
import com.livrofacil.modulos.compra.entity.Carrinho;
import com.livrofacil.modulos.compra.entity.Cupom;
import com.livrofacil.modulos.compra.entity.ItemCarrinho;
import com.livrofacil.modulos.compra.entity.ItemPedido;
import com.livrofacil.modulos.compra.entity.Pedido;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.repository.CarrinhoRepository;
import com.livrofacil.modulos.compra.repository.CupomRepository;
import com.livrofacil.modulos.compra.repository.PedidoRepository;
import com.livrofacil.modulos.livro.entity.Estoque;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.repository.EstoqueRepository;
import com.livrofacil.modulos.troca.entity.VoucherTroca;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {
    @Mock private PedidoRepository pedidoRepository;
    @Mock private CarrinhoRepository carrinhoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private FormaPagamentoRepository formaPagamentoRepository;
    @Mock private EstoqueRepository estoqueRepository;
    @Mock private CupomRepository cupomRepository;
    @Mock private BandeiraPagamentoRepository bandeiraPagamentoRepository;
    @Mock private VoucherTrocaRepository voucherTrocaRepository;
    private PedidoService service;

    private final UUID clienteId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new PedidoService(pedidoRepository, carrinhoRepository, clienteRepository, enderecoRepository,
            formaPagamentoRepository, estoqueRepository, cupomRepository, bandeiraPagamentoRepository,
            voucherTrocaRepository, 30L);
    }

    @Test
    void deveIniciarCompraReservandoEstoque() throws Exception {
        Cliente cliente = cliente();
        Carrinho carrinho = carrinhoComItem();
        Endereco endereco = endereco();
        Estoque estoque = estoque(10, 0, 0);
        IniciarCompraRequest request = iniciarRequest();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(carrinho));
        when(pedidoRepository.findByCarrinhoIdAndClienteId(1L, clienteId)).thenReturn(List.of());
        when(enderecoRepository.findByIdAndClienteId(2L, clienteId)).thenReturn(Optional.of(endereco));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.iniciar(request);

        assertEquals(new BigDecimal("10"), response.getSubtotal());
        assertEquals(new BigDecimal("29.90"), response.getTotal());
        assertEquals(9, estoque.getQuantidadeDisponivel());
        assertEquals(1, estoque.getQuantidadeBloqueada());
        verify(estoqueRepository).save(estoque);
    }

    @Test
    void deveAplicarCupomAoIniciarCompra() throws Exception {
        Cliente cliente = cliente();
        Carrinho carrinho = carrinhoComItem();
        Cupom cupom = org.mockito.Mockito.mock(Cupom.class);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(carrinho));
        when(pedidoRepository.findByCarrinhoIdAndClienteId(1L, clienteId)).thenReturn(List.of());
        when(enderecoRepository.findByIdAndClienteId(2L, clienteId)).thenReturn(Optional.of(endereco()));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque(5, 0, 0)));
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("DESC10")).thenReturn(Optional.of(cupom));
        lenient().when(cupom.getAtivo()).thenReturn(true);
        lenient().when(cupom.getTipoDesconto()).thenReturn("PERCENTUAL");
        lenient().when(cupom.getDataFimVigencia()).thenReturn(null);
        lenient().when(cupom.getNumeroUsoMaximo()).thenReturn(null);
        lenient().when(cupom.getNumeroUsoAtual()).thenReturn(0);
        when(cupom.getPercentualDesconto()).thenReturn(new BigDecimal("10"));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        IniciarCompraRequest request = iniciarRequest();
        request.setCupom("DESC10");

        var response = service.iniciar(request);

        assertEquals(new BigDecimal("1.00"), response.getDesconto());
        assertEquals(new BigDecimal("28.90"), response.getTotal());
    }

    @Test
    void deveAplicarCupomAoPedidoIniciadoEAtualizarTotal() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setSubtotal(new BigDecimal("100.00"));
        pedido.setTotal(new BigDecimal("100.00"));
        Cupom cupom = org.mockito.Mockito.mock(Cupom.class);
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("DESC10")).thenReturn(Optional.of(cupom));
        when(cupom.getCodigo()).thenReturn("DESC10");
        lenient().when(cupom.getAtivo()).thenReturn(true);
        lenient().when(cupom.getTipoDesconto()).thenReturn("PERCENTUAL");
        lenient().when(cupom.getDataFimVigencia()).thenReturn(null);
        lenient().when(cupom.getNumeroUsoMaximo()).thenReturn(null);
        lenient().when(cupom.getNumeroUsoAtual()).thenReturn(0);
        when(cupom.getPercentualDesconto()).thenReturn(new BigDecimal("10"));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        var response = service.aplicarCupom(1L, clienteId, "DESC10");

        assertEquals("DESC10", response.getCupom());
        assertEquals(new BigDecimal("10.00"), response.getDesconto());
        assertEquals(new BigDecimal("90.00"), response.getTotal());
    }

    @Test
    void deveRegistrarUsoDeCupomSomenteAoFinalizarPedido() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setSubtotal(new BigDecimal("20.00"));
        pedido.setDesconto(BigDecimal.ZERO);
        pedido.setTotal(new BigDecimal("20.00"));
        pedido.setCupom("DESC10");
        Cupom cupom = new Cupom();
        cupom.setCodigo("DESC10");
        cupom.setTipoDesconto("PERCENTUAL");
        cupom.setPercentualDesconto(new BigDecimal("10.00"));
        cupom.setNumeroUsoAtual(0);
        cupom.setAtivo(true);
        FormaPagamento forma = new FormaPagamento("Cliente", "CREDITO", "4444", "12/28", "VISA", false, pedido.getCliente());
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("DESC10")).thenReturn(Optional.of(cupom));
        when(cupomRepository.findByCodigoIgnoreCaseForUpdate("DESC10")).thenReturn(Optional.of(cupom));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido.getCarrinho()));
        when(formaPagamentoRepository.findByIdAndClienteId(4L, clienteId)).thenReturn(Optional.of(forma));
        when(bandeiraPagamentoRepository.findByNomeIgnoreCase("VISA")).thenReturn(Optional.of(new BandeiraPagamento("VISA")));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque(0, 1, 0)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.aplicarCupom(1L, clienteId, "DESC10");
        assertEquals(0, cupom.getNumeroUsoAtual());

        service.finalizar(1L, clienteId, finalizarRequest(new BigDecimal("18.00"), 1));

        assertEquals(1, cupom.getNumeroUsoAtual());
    }

    @Test
    void deveAplicarVoucherSemMarcarComoResgatado() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setSubtotal(new BigDecimal("10.00"));
        pedido.setTotal(new BigDecimal("10.00"));
        Cliente cliente = pedido.getCliente();
        setId(cliente, clienteId);
        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo("TR-NAO-CONSUMIR");
        voucher.setValor(new BigDecimal("25.00"));
        voucher.setCliente(cliente);
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("TR-NAO-CONSUMIR")).thenReturn(Optional.empty());
        when(voucherTrocaRepository.findByCodigoIgnoreCase("TR-NAO-CONSUMIR")).thenReturn(Optional.of(voucher));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        var response = service.aplicarCupom(1L, clienteId, "TR-NAO-CONSUMIR");

        assertEquals(new BigDecimal("25.00"), voucher.getValor());
        assertEquals(new BigDecimal("10.00"), response.getDesconto());
        org.junit.jupiter.api.Assertions.assertNull(voucher.getResgatadoEm());
        verify(voucherTrocaRepository, never()).save(voucher);
    }

    @Test
    void deveRejeitarCarrinhoVazio() throws Exception {
        Carrinho carrinho = new Carrinho();
        setId(carrinho, 1L);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente()));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(carrinho));
        when(pedidoRepository.findByCarrinhoIdAndClienteId(1L, clienteId)).thenReturn(List.of());

        assertThrows(RegraDeNegocioException.class, () -> service.iniciar(iniciarRequest()));
    }

    @Test
    void deveRejeitarEstoqueInsuficiente() throws Exception {
        Carrinho carrinho = carrinhoComItem();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente()));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(carrinho));
        when(pedidoRepository.findByCarrinhoIdAndClienteId(1L, clienteId)).thenReturn(List.of());
        when(enderecoRepository.findByIdAndClienteId(2L, clienteId)).thenReturn(Optional.of(endereco()));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque(0, 0, 0)));

        assertThrows(RegraDeNegocioException.class, () -> service.iniciar(iniciarRequest()));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveFinalizarCompraComJurosETransferirEstoque() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setTotal(new BigDecimal("20.00"));
        Carrinho carrinho = pedido.getCarrinho();
        FormaPagamento forma = new FormaPagamento("Cliente", "CREDITO", "4444", "12/28", "VISA", false, pedido.getCliente());
        Estoque estoque = estoque(0, 1, 0);
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(carrinho));
        when(formaPagamentoRepository.findByIdAndClienteId(4L, clienteId)).thenReturn(Optional.of(forma));
        when(bandeiraPagamentoRepository.findByNomeIgnoreCase("VISA")).thenReturn(Optional.of(new BandeiraPagamento("VISA")));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FinalizarCompraRequest request = finalizarRequest(new BigDecimal("20.00"), 4);

        var response = service.finalizar(1L, clienteId, request);

        assertEquals("EM_PROCESSAMENTO", response.getStatus());
        assertEquals(new BigDecimal("20.40"), response.getTotal());
        assertEquals(0, estoque.getQuantidadeBloqueada());
        assertEquals(1, estoque.getQuantidadeVendida());
        assertEquals(0, carrinho.getItens().size());
    }

    @Test
    void deveFinalizarPedidoQuitadoIntegralmentePorVoucher() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setSubtotal(new BigDecimal("10.00"));
        pedido.setDesconto(new BigDecimal("10.00"));
        pedido.setTotal(BigDecimal.ZERO.setScale(2));
        pedido.setCupom("TR-ABC123");
        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo("TR-ABC123");
        Cliente cliente = pedido.getCliente();
        setId(cliente, clienteId);
        voucher.setCliente(cliente);
        voucher.setValor(new BigDecimal("10.00"));
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("TR-ABC123")).thenReturn(Optional.empty());
        when(voucherTrocaRepository.findByCodigoForUpdate("TR-ABC123")).thenReturn(Optional.of(voucher));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido.getCarrinho()));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque(0, 1, 0)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FinalizarCompraRequest request = new FinalizarCompraRequest();
        request.setCarrinhoId(1L);
        request.setPagamentos(List.of());

        var response = service.finalizar(1L, clienteId, request);

        assertEquals("EM_PROCESSAMENTO", response.getStatus());
        assertEquals(BigDecimal.ZERO.setScale(2), response.getTotal());
        assertEquals(pedido, voucher.getPedidoResgate());
        org.junit.jupiter.api.Assertions.assertNotNull(voucher.getResgatadoEm());
    }

    @Test
    void deveEmitirNovoVoucherQuandoUsoForParcial() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setSubtotal(new BigDecimal("10.00"));
        pedido.setDesconto(new BigDecimal("10.00"));
        pedido.setTotal(BigDecimal.ZERO.setScale(2));
        pedido.setCupom("TR-RESTANTE");
        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo("TR-RESTANTE");
        Cliente cliente = pedido.getCliente();
        setId(cliente, clienteId);
        voucher.setCliente(cliente);
        voucher.setValor(new BigDecimal("50.00"));
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("TR-RESTANTE")).thenReturn(Optional.empty());
        when(voucherTrocaRepository.findByCodigoForUpdate("TR-RESTANTE")).thenReturn(Optional.of(voucher));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido.getCarrinho()));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque(0, 1, 0)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FinalizarCompraRequest request = new FinalizarCompraRequest();
        request.setCarrinhoId(1L);
        request.setPagamentos(List.of());

        var response = service.finalizar(1L, clienteId, request);

        assertEquals(BigDecimal.ZERO, voucher.getValor());
        assertEquals(pedido, voucher.getPedidoResgate());
        org.junit.jupiter.api.Assertions.assertNotNull(voucher.getResgatadoEm());
        assertEquals(new BigDecimal("40.00"), response.getVoucherSaldoGerado().getValor());
        org.junit.jupiter.api.Assertions.assertNotEquals(voucher.getCodigo(), response.getVoucherSaldoGerado().getCodigo());
        verify(voucherTrocaRepository, times(2)).save(any(VoucherTroca.class));
    }

    @Test
    void deveAplicarVoucherAoFreteEmitirSaldoNovoEImpedirDuplicidade() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setSubtotal(new BigDecimal("100.00"));
        pedido.setFrete(new BigDecimal("19.90"));
        pedido.setTotal(new BigDecimal("119.90"));
        Cliente cliente = pedido.getCliente();
        setId(cliente, clienteId);
        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo("TR-ORIGINAL");
        voucher.setCliente(cliente);
        voucher.setValor(new BigDecimal("150.00"));

        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("TR-ORIGINAL")).thenReturn(Optional.empty());
        when(voucherTrocaRepository.findByCodigoIgnoreCase("TR-ORIGINAL")).thenReturn(Optional.of(voucher));
        when(voucherTrocaRepository.findByCodigoForUpdate("TR-ORIGINAL")).thenReturn(Optional.of(voucher));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido.getCarrinho()));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque(0, 1, 0)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var aplicacao = service.aplicarCupom(1L, clienteId, "TR-ORIGINAL");

        assertEquals(new BigDecimal("119.90"), aplicacao.getDesconto());
        assertEquals(new BigDecimal("0.00"), aplicacao.getTotal());

        FinalizarCompraRequest request = new FinalizarCompraRequest();
        request.setCarrinhoId(1L);
        request.setPagamentos(List.of());
        var finalizacao = service.finalizar(1L, clienteId, request);

        assertEquals("EM_PROCESSAMENTO", finalizacao.getStatus());
        assertEquals(new BigDecimal("30.10"), finalizacao.getVoucherSaldoGerado().getValor());
        assertTrue(finalizacao.getVoucherSaldoGerado().getCodigo().startsWith("TR-"));
        org.junit.jupiter.api.Assertions.assertNotEquals(voucher.getCodigo(), finalizacao.getVoucherSaldoGerado().getCodigo());
        assertEquals(clienteId, voucher.getCliente().getId());
        assertEquals(BigDecimal.ZERO, voucher.getValor());
        assertEquals(pedido, voucher.getPedidoResgate());
        org.junit.jupiter.api.Assertions.assertNotNull(voucher.getResgatadoEm());
        verify(voucherTrocaRepository, times(2)).save(any(VoucherTroca.class));

        assertThrows(RegraDeNegocioException.class, () -> service.finalizar(1L, clienteId, request));
        verify(voucherTrocaRepository, times(2)).save(any(VoucherTroca.class));
    }

    @Test
    void deveRejeitarPagamentoComCartaoInativo() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setTotal(BigDecimal.TEN);
        FormaPagamento forma = new FormaPagamento("Cliente", "CREDITO", "4444", "12/28", "VISA", false, pedido.getCliente());
        forma.setAtivo(false);
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido.getCarrinho()));
        when(formaPagamentoRepository.findByIdAndClienteId(4L, clienteId)).thenReturn(Optional.of(forma));

        assertThrows(RegraDeNegocioException.class,
                () -> service.finalizar(1L, clienteId, finalizarRequest(BigDecimal.TEN, 1)));
    }

    @Test
    void deveRejeitarPagamentoQueNaoFechaTotal() throws Exception {
        Pedido pedido = pedidoBase();
        pedido.setTotal(BigDecimal.TEN);
        FormaPagamento forma = new FormaPagamento("Cliente", "CREDITO", "4444", "12/28", "VISA", false, pedido.getCliente());
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(carrinhoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido.getCarrinho()));
        when(formaPagamentoRepository.findByIdAndClienteId(4L, clienteId)).thenReturn(Optional.of(forma));
        when(bandeiraPagamentoRepository.findByNomeIgnoreCase("VISA")).thenReturn(Optional.of(new BandeiraPagamento("VISA")));

        assertThrows(RegraDeNegocioException.class,
                () -> service.finalizar(1L, clienteId, finalizarRequest(BigDecimal.ONE, 1)));
    }

    @Test
    void deveCancelarPedidoEReporEstoque() throws Exception {
        Pedido pedido = pedidoBase();
        Estoque estoque = estoque(2, 1, 0);
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        var response = service.cancelarCliente(1L, clienteId);

        assertEquals("CANCELADO", response.getStatus());
        assertEquals(3, estoque.getQuantidadeDisponivel());
        assertEquals(0, estoque.getQuantidadeBloqueada());
    }

    @Test
    void deveNaoGerarEstoqueBloqueadoNegativoQuandoAReservaJaFoiLiberada() throws Exception {
        Pedido pedido = pedidoBase();
        Estoque estoque = estoque(2, 0, 0);
        when(pedidoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(pedido));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.of(estoque));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        service.cancelarCliente(1L, clienteId);

        assertEquals(2, estoque.getQuantidadeDisponivel());
        assertEquals(0, estoque.getQuantidadeBloqueada());
    }

    @Test
    void deveRejeitarRetrocessoDeStatus() {
        Pedido pedido = new Pedido();
        pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(RegraDeNegocioException.class, () -> service.atualizarStatus(1L, StatusPedido.PENDENTE));
    }

    @Test
    void deveAtualizarRastreamentoQuandoStatusDoPedidoMudar() throws Exception {
        Pedido pedido = pedidoSemItens();
        pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
        LocalDateTime ultimaModificacao = LocalDateTime.now().minusHours(1);
        setField(pedido, "atualizadoEm", ultimaModificacao);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        var response = service.atualizarStatus(1L, StatusPedido.PAGAMENTO_APROVADO);

        assertEquals(StatusPedido.PAGAMENTO_APROVADO.name(), response.getRastreamento().getEtapa());
        assertTrue(response.getRastreamento().getUltimaModificacao().isAfter(ultimaModificacao));
    }

    @Test
    void deveListarPedidosDoCliente() {
        Pedido pedido = pedidoSemItens();
        when(pedidoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId)).thenReturn(List.of(pedido));

        assertEquals(1, service.listarCliente(clienteId).size());
    }

    @Test
    void deveRetornarPedidosPersistidosNaListagemAdministrativa() {
        Pedido pedido = pedidoSemItens();
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));

        var pedidos = service.listarTodos();

        assertEquals(1, pedidos.size());
        assertEquals(pedido.getStatus().name(), pedidos.get(0).getStatus());
    }

    @Test
    void deveExpirarReservasEExcluirPedidos() throws Exception {
        Pedido pedido = pedidoBase();
        when(pedidoRepository.findByStatusInAndAtualizadoEmBefore(any(Set.class), any(LocalDateTime.class)))
                .thenReturn(List.of(pedido));
        when(estoqueRepository.findWithLockByLivroId(3L)).thenReturn(Optional.empty());

        service.expirarReservas();

        verify(pedidoRepository).delete(pedido);
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    private Cliente cliente() throws Exception {
        Cliente cliente = new Cliente("Cliente", "cliente@email.com", "11999999999");
        setId(cliente, clienteId);
        return cliente;
    }

    private Carrinho carrinhoComItem() throws Exception {
        Carrinho carrinho = new Carrinho();
        setId(carrinho, 1L);
        Livro livro = org.mockito.Mockito.mock(Livro.class);
        org.mockito.Mockito.lenient().when(livro.getId()).thenReturn(3L);
        org.mockito.Mockito.lenient().when(livro.getTitulo()).thenReturn("Livro");
        org.mockito.Mockito.lenient().when(livro.getValorVenda()).thenReturn(BigDecimal.TEN);
        ItemCarrinho item = new ItemCarrinho();
        item.setLivro(livro);
        item.setQuantidade(1);
        item.setCarrinho(carrinho);
        carrinho.getItens().add(item);
        return carrinho;
    }

    private Pedido pedidoBase() throws Exception {
        Pedido pedido = pedidoSemItens();
        pedido.setStatus(StatusPedido.EM_CHECKOUT);
        pedido.setReservaExpiraEm(LocalDateTime.now().plusMinutes(20));
        Carrinho carrinho = carrinhoComItem();
        pedido.setCarrinho(carrinho);
        Livro livro = carrinho.getItens().get(0).getLivro();
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setLivro(livro);
        item.setTitulo("Livro");
        item.setQuantidade(1);
        item.setValorUnitario(BigDecimal.TEN);
        pedido.getItens().add(item);
        setId(pedido, 1L);
        return pedido;
    }

    private Pedido pedidoSemItens() {
        Pedido pedido = new Pedido();
        pedido.setCliente(new Cliente("Cliente", "cliente@email.com", "11999999999"));
        pedido.setCarrinho(new Carrinho());
        pedido.setSubtotal(BigDecimal.ZERO);
        pedido.setDesconto(BigDecimal.ZERO);
        pedido.setTotal(BigDecimal.ZERO);
        pedido.setEnderecoEntrega("Entrega");
        pedido.setEnderecoCobranca("Cobranca");
        return pedido;
    }

    private Estoque estoque(int disponivel, int bloqueada, int vendida) {
        Estoque estoque = new Estoque();
        estoque.setQuantidadeDisponivel(disponivel);
        estoque.setQuantidadeBloqueada(bloqueada);
        estoque.setQuantidadeVendida(vendida);
        return estoque;
    }

    private Endereco endereco() {
        return new Endereco("CASA", "Rua A", "10", "", "Centro", "Sao Paulo", "SP", "01000-000", true, null);
    }

    private IniciarCompraRequest iniciarRequest() {
        IniciarCompraRequest request = new IniciarCompraRequest();
        request.setClienteId(clienteId);
        request.setCarrinhoId(1L);
        request.setEnderecoEntregaId(2L);
        request.setEnderecoCobranca("Rua B, 20");
        return request;
    }

    private FinalizarCompraRequest finalizarRequest(BigDecimal valor, int parcelas) {
        PagamentoRequest pagamento = new PagamentoRequest();
        pagamento.setFormaPagamentoId(4L);
        pagamento.setValor(valor);
        pagamento.setParcelas(parcelas);
        FinalizarCompraRequest request = new FinalizarCompraRequest();
        request.setCarrinhoId(1L);
        request.setPagamentos(List.of(pagamento));
        return request;
    }

    private void setId(Object target, Object id) throws Exception {
        Field field = target instanceof Pedido ? Pedido.class.getDeclaredField("id")
                : target instanceof Carrinho ? Carrinho.class.getDeclaredField("id")
                : Cliente.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
