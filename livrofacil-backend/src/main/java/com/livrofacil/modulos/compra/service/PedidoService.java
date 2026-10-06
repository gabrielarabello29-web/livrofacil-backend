package com.livrofacil.modulos.compra.service;

import com.livrofacil.modulos.cliente.entity.Endereco;
import com.livrofacil.modulos.cliente.entity.FormaPagamento;
import com.livrofacil.modulos.cliente.repository.EnderecoRepository;
import com.livrofacil.modulos.cliente.repository.FormaPagamentoRepository;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.BandeiraPagamentoRepository;
import com.livrofacil.modulos.compra.dto.*;
import com.livrofacil.modulos.compra.entity.*;
import com.livrofacil.modulos.compra.repository.*;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.livro.entity.Estoque;
import com.livrofacil.modulos.livro.repository.EstoqueRepository;
import com.livrofacil.modulos.troca.entity.VoucherTroca;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PedidoService {
    private static final BigDecimal MINIMO_PAGAMENTO = BigDecimal.ONE;
    private static final BigDecimal VALOR_MINIMO_CARTAO = new BigDecimal("10.00");
    private static final Set<StatusPedido> STATUS_CHECKOUT_PENDENTE = EnumSet.of(
            StatusPedido.PENDENTE,
            StatusPedido.AGUARDANDO_PAGAMENTO,
            StatusPedido.EM_CHECKOUT,
            StatusPedido.EM_PROCESSAMENTO
    );
            private static final Set<StatusPedido> STATUS_RESERVA = EnumSet.of(
                StatusPedido.PENDENTE,
                StatusPedido.AGUARDANDO_PAGAMENTO,
                StatusPedido.EM_CHECKOUT,
                StatusPedido.EM_PROCESSAMENTO
            );
    private final PedidoRepository pedidoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final EstoqueRepository estoqueRepository;
    private final CupomRepository cupomRepository;
    private final BandeiraPagamentoRepository bandeiraPagamentoRepository;
    private final VoucherTrocaRepository voucherTrocaRepository;
    private final long expiracaoCheckoutMinutos;

    public PedidoService(PedidoRepository pedidoRepository, CarrinhoRepository carrinhoRepository,
                         ClienteRepository clienteRepository, EnderecoRepository enderecoRepository,
                         FormaPagamentoRepository formaPagamentoRepository, EstoqueRepository estoqueRepository,
                         CupomRepository cupomRepository,
                         BandeiraPagamentoRepository bandeiraPagamentoRepository,
                         VoucherTrocaRepository voucherTrocaRepository,
                         @Value("${livrofacil.checkout.expiracao-minutos:30}") long expiracaoCheckoutMinutos) {
        this.pedidoRepository = pedidoRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.estoqueRepository = estoqueRepository;
        this.cupomRepository = cupomRepository;
        this.bandeiraPagamentoRepository = bandeiraPagamentoRepository;
        this.voucherTrocaRepository = voucherTrocaRepository;
        this.expiracaoCheckoutMinutos = expiracaoCheckoutMinutos;
    }

    @Transactional
    public PedidoResponse iniciar(IniciarCompraRequest request) {
        var cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + request.getClienteId()));
        Carrinho carrinho = carrinhoRepository.findByIdAndClienteId(request.getCarrinhoId(), request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho nao encontrado para o cliente"));

        limparChavesDeCheckoutEncerradas(carrinho.getId(), cliente.getId());
        Pedido pedidoPendente = buscarCheckoutPendente(carrinho.getId(), cliente.getId());
        if (pedidoPendente != null) {
            if (pedidoPendente.getCheckoutChave() == null) {
                pedidoPendente.setCheckoutChave(carrinho.getId());
            }
            pedidoPendente.atualizarAtividade();
            return new PedidoResponse(pedidoRepository.save(pedidoPendente));
        }
        if (carrinho.getItens().isEmpty()) throw new RegraDeNegocioException("O carrinho esta vazio");
        Endereco entrega = enderecoRepository.findByIdAndClienteId(request.getEnderecoEntregaId(), request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereco de entrega nao encontrado"));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setCarrinho(carrinho);
        pedido.setCheckoutChave(carrinho.getId());
        pedido.setEnderecoEntrega(formatarEndereco(entrega));
        pedido.setEnderecoCobranca(request.getEnderecoCobranca());
        pedido.setReservaExpiraEm(LocalDateTime.now().plusMinutes(expiracaoCheckoutMinutos));
        BigDecimal subtotal = BigDecimal.ZERO;
        for (var itemCarrinho : carrinho.getItens()) {
            Estoque estoque = estoqueRepository.findWithLockByLivroId(itemCarrinho.getLivro().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Estoque nao encontrado para o livro: " + itemCarrinho.getLivro().getId()));
            if (estoque.getQuantidadeDisponivel() < itemCarrinho.getQuantidade()) {
                throw new RegraDeNegocioException("Estoque insuficiente para o livro: " + itemCarrinho.getLivro().getTitulo());
            }
            estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() - itemCarrinho.getQuantidade());
            estoque.setQuantidadeBloqueada(estoque.getQuantidadeBloqueada() + itemCarrinho.getQuantidade());
            estoqueRepository.save(estoque);
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setLivro(itemCarrinho.getLivro());
            item.setTitulo(itemCarrinho.getLivro().getTitulo());
            item.setQuantidade(itemCarrinho.getQuantidade());
            item.setValorUnitario(itemCarrinho.getLivro().getValorVenda());
            pedido.getItens().add(item);
            subtotal = subtotal.add(itemCarrinho.getLivro().getValorVenda().multiply(BigDecimal.valueOf(itemCarrinho.getQuantidade())));
        }
        BigDecimal desconto = calcularDesconto(request.getCupom(), subtotal);
        BigDecimal frete = calcularFrete(subtotal, entrega);
        pedido.setSubtotal(subtotal);
        pedido.setDesconto(desconto);
        pedido.setFrete(frete);
        pedido.setCupom(request.getCupom());
        pedido.setTotal(subtotal.add(frete).subtract(desconto).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        return new PedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse finalizar(Long pedidoId, UUID clienteId, FinalizarCompraRequest request) {
        Pedido pedido = buscarPedidoCliente(pedidoId, clienteId);
        if (!STATUS_CHECKOUT_PENDENTE.contains(pedido.getStatus())
            || pedido.getReservaExpiraEm() == null
            || pedido.getReservaExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RegraDeNegocioException("A reserva do pedido expirou ou nao esta mais disponivel");
        }
        VoucherTroca voucherResgate = buscarVoucherAplicadoParaResgate(pedido, clienteId);
        Carrinho carrinho = carrinhoRepository.findByIdAndClienteId(request.getCarrinhoId(), clienteId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho nao encontrado para o cliente"));
        if (request.getPagamentos().isEmpty() && pedido.getTotal().signum() > 0) {
            throw new RegraDeNegocioException("Informe pelo menos uma forma de pagamento");
        }
        if (!request.getPagamentos().isEmpty() && pedido.getTotal().signum() == 0) {
            throw new RegraDeNegocioException("O pedido ja esta totalmente coberto pelo voucher");
        }
        BigDecimal soma = BigDecimal.ZERO;
        BigDecimal totalComJuros = BigDecimal.ZERO;
        for (PagamentoRequest pagamento : request.getPagamentos()) {
            if (pagamento.getValor().compareTo(MINIMO_PAGAMENTO) < 0) throw new RegraDeNegocioException("Cada forma de pagamento deve pagar no minimo 1 real");
            if (pagamento.getParcelas() < 1 || pagamento.getParcelas() > 12) throw new RegraDeNegocioException("O parcelamento deve estar entre 1 e 12 vezes");
            FormaPagamento forma = formaPagamentoRepository.findByIdAndClienteId(pagamento.getFormaPagamentoId(), clienteId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Forma de pagamento nao encontrada"));
            if (!forma.isAtivo()) throw new RegraDeNegocioException("A forma de pagamento esta inativa");
            if (!"CREDITO".equals(forma.getTipoCartao())) {
                throw new RegraDeNegocioException("Somente cartoes de credito podem ser usados no pagamento");
            }
            bandeiraPagamentoRepository.findByNomeIgnoreCase(forma.getBandeira())
                    .filter(bandeira -> bandeira.isDisponivel())
                    .orElseThrow(() -> new RegraDeNegocioException("A bandeira de cartao nao esta disponivel para pagamento"));
            boolean cupomAplicado = pedido.getCupom() != null && !pedido.getCupom().isBlank() && pedido.getDesconto().compareTo(BigDecimal.ZERO) > 0;
            boolean pagamentoAbaixoDoMinimoPermitido = pagamento.getValor().compareTo(VALOR_MINIMO_CARTAO) < 0 && cupomAplicado;
            if (pagamento.getValor().compareTo(VALOR_MINIMO_CARTAO) < 0 && !pagamentoAbaixoDoMinimoPermitido) {
                throw new RegraDeNegocioException("Cada cartao deve pagar no minimo 10 reais, exceto quando a regra de cupom + cartao permitir o restante");
            }
            PagamentoPedido pagamentoPedido = new PagamentoPedido();
            pagamentoPedido.setPedido(pedido);
            pagamentoPedido.setFormaPagamento(forma);
            pagamentoPedido.setValor(pagamento.getValor());
            pagamentoPedido.setParcelas(pagamento.getParcelas());
            pedido.getPagamentos().add(pagamentoPedido);
            soma = soma.add(pagamento.getValor());
            BigDecimal juros = pagamento.getParcelas() <= 3 ? BigDecimal.ZERO : BigDecimal.valueOf(0.02).multiply(BigDecimal.valueOf(pagamento.getParcelas() - 3));
            totalComJuros = totalComJuros.add(pagamento.getValor().multiply(BigDecimal.ONE.add(juros)));
        }
        if (soma.compareTo(pedido.getTotal()) != 0) throw new RegraDeNegocioException("A soma dos pagamentos deve ser igual ao total do pedido");
        registrarUsoCupom(pedido.getCupom());
        pedido.setTotal(totalComJuros.setScale(2, RoundingMode.HALF_UP));
        pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
        VoucherTroca voucherSaldoGerado = null;
        if (voucherResgate != null) {
            BigDecimal valorUsado = pedido.getDesconto().min(voucherResgate.getValor() == null ? BigDecimal.ZERO : voucherResgate.getValor());
            if (valorUsado.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal saldoRestante = voucherResgate.getValor().subtract(valorUsado).max(BigDecimal.ZERO);
                voucherResgate.setValor(BigDecimal.ZERO);
                voucherResgate.setPedidoResgate(pedido);
                voucherResgate.setResgatadoEm(LocalDateTime.now());
                voucherTrocaRepository.save(voucherResgate);
                if (saldoRestante.compareTo(BigDecimal.ZERO) > 0) {
                    voucherSaldoGerado = new VoucherTroca();
                    voucherSaldoGerado.setCodigo(gerarCodigoVoucher());
                    voucherSaldoGerado.setValor(saldoRestante);
                    voucherSaldoGerado.setCliente(voucherResgate.getCliente());
                    voucherTrocaRepository.save(voucherSaldoGerado);
                }
            }
        }
        pedido.setCheckoutChave(null);
        pedido.atualizarAtividade();
        pedido.setReservaExpiraEm(null);
        pedido.getItens().forEach(item -> estoqueRepository.findWithLockByLivroId(item.getLivro().getId()).ifPresent(estoque -> {
            int quantidadeVendida = Math.min(item.getQuantidade(), Math.max(0, estoque.getQuantidadeBloqueada()));
            if (quantidadeVendida <= 0) return;
            estoque.setQuantidadeBloqueada(estoque.getQuantidadeBloqueada() - quantidadeVendida);
            estoque.setQuantidadeVendida(estoque.getQuantidadeVendida() + quantidadeVendida);
            estoqueRepository.save(estoque);
        }));
        carrinho.getItens().clear();
        carrinhoRepository.save(carrinho);
        return new PedidoResponse(pedidoRepository.save(pedido), voucherSaldoGerado);
    }

    @Transactional
    public PedidoResponse aplicarCupom(Long pedidoId, UUID clienteId, String codigo) {
        Pedido pedido = buscarPedidoCliente(pedidoId, clienteId);
        if (!STATUS_CHECKOUT_PENDENTE.contains(pedido.getStatus())
                || pedido.getReservaExpiraEm() == null
                || pedido.getReservaExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RegraDeNegocioException("A reserva do pedido expirou ou nao esta mais disponivel");
        }
        String codigoNormalizado = codigo == null ? "" : codigo.trim();
        if (pedido.getCupom() != null && !pedido.getCupom().isBlank() && !pedido.getCupom().equalsIgnoreCase(codigoNormalizado)) {
            if (cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue(pedido.getCupom()).isPresent()) {
                throw new RegraDeNegocioException("O pedido ja possui um cupom promocional aplicado");
            }
        }
        var cupom = cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue(codigoNormalizado);
        BigDecimal desconto;
        String codigoAplicado;
        if (cupom.isPresent()) {
            codigoAplicado = cupom.get().getCodigo();
            if (pedido.getCupom() != null && !pedido.getCupom().isBlank() && !pedido.getCupom().equalsIgnoreCase(codigoAplicado)) {
                throw new RegraDeNegocioException("O pedido ja possui um cupom promocional aplicado");
            }
            desconto = calcularDesconto(codigoAplicado, pedido.getSubtotal());
        } else {
            VoucherTroca voucher = voucherTrocaRepository.findByCodigoIgnoreCase(codigoNormalizado)
                .orElseThrow(() -> new RegraDeNegocioException("Cupom ou voucher invalido"));
            validarVoucher(voucher, pedido, clienteId);
            codigoAplicado = voucher.getCodigo();
            desconto = voucher.getValor().min(pedido.getSubtotal().add(pedido.getFrete()));
        }
        pedido.setCupom(codigoAplicado);
        pedido.setDesconto(desconto);
        pedido.setTotal(pedido.getSubtotal().add(pedido.getFrete()).subtract(desconto).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        pedido.atualizarAtividade();
        return new PedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarCliente(UUID clienteId) {
        return pedidoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId).stream().map(PedidoResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarDetalhe(Long pedidoId, UUID clienteId) {
        Pedido pedido = buscarPedidoCliente(pedidoId, clienteId);
        return new PedidoResponse(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarDetalheAdmin(Long pedidoId) {
        return new PedidoResponse(buscarPedido(pedidoId));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream().map(PedidoResponse::new).toList();
    }

    @Transactional
    public PedidoResponse atualizarStatus(Long pedidoId, StatusPedido status) {
        Pedido pedido = buscarPedido(pedidoId);
        if (status.ordinal() < pedido.getStatus().ordinal() && status != StatusPedido.CANCELADO) throw new RegraDeNegocioException("O status do pedido nao pode retroceder");
        if (status == StatusPedido.CANCELADO) {
            cancelarELiberar(pedido);
            return new PedidoResponse(pedidoRepository.save(pedido));
        }
        pedido.setStatus(status);
        return new PedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cancelarCliente(Long pedidoId, UUID clienteId) {
        Pedido pedido = buscarPedidoCliente(pedidoId, clienteId);
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            return new PedidoResponse(pedido);
        }
        if (pedido.getStatus().ordinal() >= StatusPedido.EM_ROTA_DE_ENTREGA.ordinal()) throw new RegraDeNegocioException("O pedido esta em rota e precisa de solicitacao administrativa");
        cancelarELiberar(pedido);
        return new PedidoResponse(pedidoRepository.save(pedido));
    }

    private void cancelarELiberar(Pedido pedido) {
        if (pedido.getReservaExpiraEm() != null) pedido.getItens().forEach(item -> estoqueRepository.findWithLockByLivroId(item.getLivro().getId()).ifPresent(estoque -> {
            int quantidadeLiberada = Math.min(item.getQuantidade(), Math.max(0, estoque.getQuantidadeBloqueada()));
            if (quantidadeLiberada <= 0) return;
            estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() + quantidadeLiberada);
            estoque.setQuantidadeBloqueada(estoque.getQuantidadeBloqueada() - quantidadeLiberada);
            estoqueRepository.save(estoque);
        }));
        pedido.setStatus(StatusPedido.CANCELADO);
        pedido.setCheckoutChave(null);
        pedido.setReservaExpiraEm(null);
        pedido.getPagamentos().clear();
        pedido.setCupom(null);
        pedido.atualizarAtividade();
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expirarReservas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(expiracaoCheckoutMinutos);
        pedidoRepository.findByStatusInAndAtualizadoEmBefore(STATUS_RESERVA, limite).forEach(pedido -> {
            cancelarELiberar(pedido);
            pedidoRepository.delete(pedido);
        });
    }

    private Pedido buscarCheckoutPendente(Long carrinhoId, UUID clienteId) {
        Pedido pedido = pedidoRepository
                .findFirstByCarrinhoIdAndClienteIdAndStatusInOrderByCriadoEmDesc(carrinhoId, clienteId, STATUS_CHECKOUT_PENDENTE)
                .orElse(null);
        if (pedido == null) return null;

        if (pedido.getAtualizadoEm() == null
                || pedido.getAtualizadoEm().isBefore(LocalDateTime.now().minusMinutes(expiracaoCheckoutMinutos))) {
            cancelarELiberar(pedido);
            pedidoRepository.delete(pedido);
            return null;
        }
        return pedido;
    }

    private void limparChavesDeCheckoutEncerradas(Long carrinhoId, UUID clienteId) {
        pedidoRepository.findByCarrinhoIdAndClienteId(carrinhoId, clienteId).stream()
                .filter(pedido -> pedido.getStatus() == StatusPedido.CANCELADO
                        || pedido.getStatus() == StatusPedido.FINALIZADO
                        || pedido.getStatus() == StatusPedido.ENTREGUE)
                .filter(pedido -> pedido.getCheckoutChave() != null)
                .forEach(pedido -> {
                    pedido.setCheckoutChave(null);
                    pedidoRepository.save(pedido);
                });
    }

    private Pedido buscarPedido(Long id) { return pedidoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido nao encontrado: " + id)); }
    private Pedido buscarPedidoCliente(Long id, UUID clienteId) { return pedidoRepository.findByIdAndClienteId(id, clienteId).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido nao encontrado: " + id)); }
    private BigDecimal calcularDesconto(String codigo, BigDecimal subtotal) {
        if (codigo == null || codigo.isBlank()) return BigDecimal.ZERO;
        Cupom cupom = cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue(codigo)
            .or(() -> cupomRepository.findByCodigoIgnoreCase(codigo))
            .orElseThrow(() -> new RegraDeNegocioException("Cupom invalido ou inativo"));
        if (cupom.getAtivo() != null && !cupom.getAtivo()) {
            throw new RegraDeNegocioException("Cupom invalido, inativo ou expirado");
        }
        if (cupom.getDataFimVigencia() != null && LocalDate.now().isAfter(cupom.getDataFimVigencia())) {
            throw new RegraDeNegocioException("Cupom invalido, inativo ou expirado");
        }
        if (cupom.getNumeroUsoMaximo() != null && cupom.getNumeroUsoAtual() != null && cupom.getNumeroUsoAtual() >= cupom.getNumeroUsoMaximo()) {
            throw new RegraDeNegocioException("Cupom invalido, inativo ou expirado");
        }
        if ("FIXO".equalsIgnoreCase(cupom.getTipoDesconto())) {
            return cupom.getValorDesconto() == null ? BigDecimal.ZERO : cupom.getValorDesconto().min(subtotal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }
        return subtotal.multiply(cupom.getPercentualDesconto()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .min(subtotal).max(BigDecimal.ZERO);
    }

    private void registrarUsoCupom(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return;
        }
        cupomRepository.findByCodigoIgnoreCaseForUpdate(codigo.trim()).ifPresent(cupom -> {
            if (!cupom.isDisponivel()) {
                throw new RegraDeNegocioException("Cupom invalido, inativo ou expirado");
            }
            cupom.registrarUso();
            cupomRepository.save(cupom);
        });
    }

    private BigDecimal calcularFrete(BigDecimal subtotal, Endereco endereco) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        if (subtotal.compareTo(new BigDecimal("150.00")) >= 0) return BigDecimal.ZERO;
        if (endereco != null && "SP".equalsIgnoreCase(endereco.getEstado())) {
            return new BigDecimal("19.90");
        }
        return new BigDecimal("29.90");
    }

    private VoucherTroca buscarVoucherAplicadoParaResgate(Pedido pedido, UUID clienteId) {
        if (pedido.getCupom() == null
                || cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue(pedido.getCupom()).isPresent()) {
            return null;
        }
        VoucherTroca voucher = voucherTrocaRepository.findByCodigoForUpdate(pedido.getCupom())
                .orElseThrow(() -> new RegraDeNegocioException("Voucher aplicado nao esta mais disponivel"));
        validarVoucher(voucher, pedido, clienteId);
        return voucher;
    }

    private void validarVoucher(VoucherTroca voucher, Pedido pedido, UUID clienteId) {
        if (!voucher.getCliente().getId().equals(clienteId)
                || !voucher.getCliente().getId().equals(pedido.getCliente().getId())) {
            throw new RegraDeNegocioException("Este voucher pertence a outro cliente");
        }

        if (voucher.getValor() == null || voucher.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Voucher aplicado nao esta mais disponivel");
        }
        if (voucher.getResgatadoEm() != null) {
            throw new RegraDeNegocioException("Este voucher ja foi utilizado");
        }
    }

    private String gerarCodigoVoucher() {
        String codigo;
        do {
            codigo = "TR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
        } while (voucherTrocaRepository.existsByCodigoIgnoreCase(codigo)
                || cupomRepository.findByCodigoIgnoreCase(codigo).isPresent());
        return codigo;
    }

    private String formatarEndereco(Endereco endereco) {
        return endereco.getLogradouro() + ", " + endereco.getNumero() + " - " + endereco.getBairro() + ", " + endereco.getCidade() + "/" + endereco.getEstado() + " - CEP " + endereco.getCep();
    }
}