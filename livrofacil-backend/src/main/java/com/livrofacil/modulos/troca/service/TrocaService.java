package com.livrofacil.modulos.troca.service;

import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.repository.ItemPedidoRepository;
import com.livrofacil.modulos.compra.repository.PedidoRepository;
import com.livrofacil.modulos.livro.repository.EstoqueRepository;
import com.livrofacil.modulos.troca.dto.SolicitarTrocaRequest;
import com.livrofacil.modulos.troca.dto.TrocaResponse;
import com.livrofacil.modulos.troca.dto.VoucherTrocaResponse;
import com.livrofacil.modulos.troca.entity.StatusTroca;
import com.livrofacil.modulos.troca.entity.Troca;
import com.livrofacil.modulos.troca.entity.TrocaHistorico;
import com.livrofacil.modulos.troca.entity.TipoSolicitacaoTroca;
import com.livrofacil.modulos.troca.entity.VoucherTroca;
import com.livrofacil.modulos.troca.repository.TrocaHistoricoRepository;
import com.livrofacil.modulos.troca.repository.TrocaRepository;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TrocaService {
    private static final EnumSet<StatusTroca> STATUS_CONFLITANTES = EnumSet.of(
        StatusTroca.EM_TROCA, StatusTroca.TROCA_AUTORIZADA, StatusTroca.RECEBIDA,
        StatusTroca.TROCADO,
        StatusTroca.SOLICITADA, StatusTroca.AUTORIZADA, StatusTroca.AGUARDANDO_RECEBIMENTO,
        StatusTroca.TROCADA, StatusTroca.CONCLUIDA);

    private final TrocaRepository trocaRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final EstoqueRepository estoqueRepository;
    private final VoucherTrocaRepository voucherRepository;
    private final TrocaHistoricoRepository historicoRepository;
    private final ZoneId zoneId;

    public TrocaService(TrocaRepository trocaRepository, PedidoRepository pedidoRepository,
                        ItemPedidoRepository itemPedidoRepository, EstoqueRepository estoqueRepository,
                        VoucherTrocaRepository voucherRepository) {
        this(trocaRepository, pedidoRepository, itemPedidoRepository, estoqueRepository, voucherRepository,
            null, "America/Sao_Paulo");
    }

    @Autowired
    public TrocaService(TrocaRepository trocaRepository, PedidoRepository pedidoRepository,
                        ItemPedidoRepository itemPedidoRepository, EstoqueRepository estoqueRepository,
                        VoucherTrocaRepository voucherRepository, TrocaHistoricoRepository historicoRepository,
                        @Value("${livrofacil.time-zone:America/Sao_Paulo}") String timeZone) {
        this.trocaRepository = trocaRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.estoqueRepository = estoqueRepository;
        this.voucherRepository = voucherRepository;
        this.historicoRepository = historicoRepository;
        this.zoneId = ZoneId.of(timeZone);
    }

    @Transactional
    public TrocaResponse solicitar(SolicitarTrocaRequest request) {
        var pedido = pedidoRepository.findByIdAndClienteId(request.getPedidoId(), request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido nao encontrado para o cliente"));
        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw new RegraDeNegocioException("A troca ou devolucao so pode ser solicitada para pedidos entregues");
        }
        if (pedido.getEntregueEm() == null) {
            throw new RegraDeNegocioException("Este pedido nao possui data de entrega registrada e nao pode receber solicitacao");
        }
        if (request.getItemPedidoId() == null && request.getLivroId() == null) {
            throw new RegraDeNegocioException("Informe o item do pedido ou o livro solicitado");
        }
        LocalDate hoje = LocalDate.now(zoneId);
        LocalDate dataLimite = pedido.getEntregueEm().toLocalDate().plusDays(7);
        if (hoje.isAfter(dataLimite)) {
            throw new RegraDeNegocioException("O prazo de 7 dias corridos para solicitar troca ou devolucao expirou");
        }
        var item = request.getItemPedidoId() == null
            ? itemPedidoRepository.findFirstByPedidoIdAndLivroId(request.getPedidoId(), request.getLivroId())
            : itemPedidoRepository.findByIdAndPedidoId(request.getItemPedidoId(), request.getPedidoId());
        var itemPedido = item.orElseThrow(() -> new RecursoNaoEncontradoException("Item nao encontrado no pedido"));
        itemPedido = itemPedidoRepository.findByIdAndPedidoIdForUpdate(itemPedido.getId(), request.getPedidoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Item nao encontrado no pedido"));
        if (request.getQuantidade() == null || request.getQuantidade() < 1
            || request.getQuantidade() > itemPedido.getQuantidade()) {
            throw new RegraDeNegocioException("A quantidade solicitada deve estar entre 1 e a quantidade comprada");
        }
        var existentes = trocaRepository.findByItemPedidoIdAndStatusIn(itemPedido.getId(), STATUS_CONFLITANTES);
        int comprometida = existentes.stream().mapToInt(t -> t.getQuantidadeRecebida() != null
            ? t.getQuantidadeRecebida() : t.getQuantidade() == null ? t.getItemPedido().getQuantidade() : t.getQuantidade()).sum();
        int pendente = existentes.stream().filter(t -> t.getQuantidadeRecebida() == null)
            .mapToInt(t -> t.getQuantidade() == null ? t.getItemPedido().getQuantidade() : t.getQuantidade()).sum();
        if (pendente > 0 || comprometida + request.getQuantidade() > itemPedido.getQuantidade()) {
            throw new RegraDeNegocioException("A quantidade solicitada ja esta coberta por outra solicitacao ativa ou concluida");
        }

        Troca troca = new Troca();
        troca.setPedido(pedido);
        troca.setItemPedido(itemPedido);
        troca.setMotivo(request.getMotivo().trim());
        troca.setDetalhes(request.getDetalhes());
        troca.setTipo(request.getTipo() == null ? TipoSolicitacaoTroca.TROCA : request.getTipo());
        troca.setQuantidade(request.getQuantidade());
        Troca salva = trocaRepository.save(troca);
        registrarHistorico(salva, "SOLICITADA", "Solicitacao registrada pelo cliente " + request.getClienteId());
        return new TrocaResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<TrocaResponse> listarTodas() {
        return trocaRepository.findAllByOrderByCriadoEmDesc().stream().map(TrocaResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<TrocaResponse> listarTodas(StatusTroca status, TipoSolicitacaoTroca tipo) {
        return trocaRepository.findAllByOrderByCriadoEmDesc().stream()
                .filter(troca -> status == null || troca.getStatus() == status)
                .filter(troca -> tipo == null || troca.getTipo() == tipo)
                .map(TrocaResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public TrocaResponse buscarAdmin(Long trocaId) {
        return new TrocaResponse(buscarTroca(trocaId));
    }

    @Transactional(readOnly = true)
    public List<TrocaResponse> listarCliente(UUID clienteId) {
        return trocaRepository.findByPedidoClienteIdOrderByCriadoEmDesc(clienteId).stream().map(TrocaResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public TrocaResponse buscarCliente(Long trocaId, UUID clienteId) {
        Troca troca = trocaRepository.findById(trocaId)
                .filter(item -> item.getPedido().getCliente().getId().equals(clienteId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao nao encontrada para o cliente"));
        return new TrocaResponse(troca);
    }

    @Transactional(readOnly = true)
    public List<TrocaHistorico> listarHistorico(Long trocaId) {
        buscarTroca(trocaId);
        return historicoRepository == null ? List.of() : historicoRepository.findByTrocaIdOrderByCriadoEmAsc(trocaId);
    }

    @Transactional(readOnly = true)
    public List<TrocaHistorico> listarHistoricoCliente(Long trocaId, UUID clienteId) {
        buscarCliente(trocaId, clienteId);
        return listarHistorico(trocaId);
    }

    @Transactional(readOnly = true)
    public List<VoucherTrocaResponse> listarVouchersCliente(UUID clienteId) {
        return voucherRepository.findByClienteIdOrderByCriadoEmDesc(clienteId).stream()
                .map(VoucherTrocaResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public VoucherTrocaResponse buscarVoucherCliente(Long voucherId, UUID clienteId) {
        VoucherTroca voucher = voucherRepository.findByIdAndClienteId(voucherId, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cupom nao encontrado para o cliente"));
        return new VoucherTrocaResponse(voucher);
    }

    @Transactional
    public TrocaResponse autorizar(Long trocaId) {
        Troca troca = buscarTrocaBloqueada(trocaId);
        exigirStatus(troca, StatusTroca.EM_TROCA);
        troca.setStatus(StatusTroca.TROCA_AUTORIZADA);
        troca.setDecididoEm(LocalDateTime.now(zoneId));
        Troca salva = trocaRepository.save(troca);
        registrarHistorico(salva, "TROCA_AUTORIZADA", "Solicitacao autorizada; aguardando recebimento");
        return new TrocaResponse(salva);
    }

    @Transactional
    public TrocaResponse recusar(Long trocaId, String motivo) {
        Troca troca = buscarTrocaBloqueada(trocaId);
        exigirStatus(troca, StatusTroca.EM_TROCA);
        troca.setStatus(StatusTroca.RECUSADA);
        troca.setMotivoRecusa(motivo.trim());
        troca.setDecididoEm(LocalDateTime.now(zoneId));
        Troca salva = trocaRepository.save(troca);
        registrarHistorico(salva, "RECUSADA", motivo.trim());
        return new TrocaResponse(salva);
    }

    @Transactional
    public TrocaResponse receber(Long trocaId, boolean retornarEstoque) {
        Troca troca = buscarTrocaBloqueada(trocaId);
        if (troca.getStatus() != StatusTroca.TROCA_AUTORIZADA &&
            troca.getStatus() != StatusTroca.AGUARDANDO_RECEBIMENTO &&
            troca.getStatus() != StatusTroca.AUTORIZADA) {
            throw new RegraDeNegocioException("A solicitacao deve estar autorizada e aguardando recebimento");
        }
        return concluirRecebimento(troca, retornarEstoque,
                troca.getQuantidade() == null ? troca.getItemPedido().getQuantidade() : troca.getQuantidade());
    }

    @Transactional
    public TrocaResponse receber(Long trocaId, boolean retornarEstoque, Integer quantidadeRecebida) {
        Troca troca = buscarTrocaBloqueada(trocaId);
        if (troca.getStatus() != StatusTroca.TROCA_AUTORIZADA &&
            troca.getStatus() != StatusTroca.AGUARDANDO_RECEBIMENTO &&
            troca.getStatus() != StatusTroca.AUTORIZADA) {
            throw new RegraDeNegocioException("A solicitacao deve estar autorizada e aguardando recebimento");
        }
        return concluirRecebimento(troca, retornarEstoque, quantidadeRecebida);
    }

    private TrocaResponse concluirRecebimento(Troca troca, boolean retornarEstoque, Integer quantidadeRecebida) {
        int quantidadeSolicitada = troca.getQuantidade() == null ? troca.getItemPedido().getQuantidade() : troca.getQuantidade();
        if (quantidadeRecebida == null || quantidadeRecebida < 1 || quantidadeRecebida > quantidadeSolicitada) {
            throw new RegraDeNegocioException("A quantidade recebida deve estar entre 1 e a quantidade autorizada");
        }
        troca.setRecebidoEm(LocalDateTime.now(zoneId));
        troca.setQuantidadeRecebida(quantidadeRecebida);
        troca.setRetornarEstoque(retornarEstoque);
        troca.setStatus(StatusTroca.RECEBIDA);
        trocaRepository.save(troca);
        registrarHistorico(troca, "RECEBIDA", "Recebidas " + quantidadeRecebida + " unidade(s); retornar ao estoque: " + retornarEstoque);
        if (retornarEstoque) {
            var item = troca.getItemPedido();
            var estoque = estoqueRepository.findWithLockByLivroId(item.getLivro().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Estoque nao encontrado para o livro devolvido"));
            if (estoque.getQuantidadeVendida() < quantidadeRecebida) {
                throw new RegraDeNegocioException("Estoque vendido inconsistente para o livro devolvido");
            }
            estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() + quantidadeRecebida);
            estoque.setQuantidadeVendida(estoque.getQuantidadeVendida() - quantidadeRecebida);
            estoqueRepository.save(estoque);
        }

        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo(gerarCodigo());
        voucher.setValor(troca.getItemPedido().getValorUnitario().multiply(BigDecimal.valueOf(quantidadeRecebida)));
        voucher.setValorOriginal(voucher.getValor());
        voucher.setCliente(troca.getPedido().getCliente());
        voucher.setTroca(troca);
        troca.setVoucher(voucher);
        troca.setCreditoEm(LocalDateTime.now(zoneId));
        troca.setStatus(StatusTroca.TROCADO);
        Troca salva = trocaRepository.save(troca);
        registrarHistorico(salva, "TROCADO", "Crédito emitido: " + voucher.getValor());
        return new TrocaResponse(salva);
    }

    private Troca buscarTrocaBloqueada(Long id) {
        return trocaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Troca nao encontrada: " + id));
    }

    private void registrarHistorico(Troca troca, String status, String observacao) {
        if (historicoRepository != null && troca.getId() != null) {
            historicoRepository.save(new TrocaHistorico(troca, status, observacao));
        }
    }

    private Troca buscarTroca(Long id) {
        return trocaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Troca nao encontrada: " + id));
    }

    private void exigirStatus(Troca troca, StatusTroca statusEsperado) {
        if (troca.getStatus() != statusEsperado) {
            throw new RegraDeNegocioException("A troca deve estar no status " + statusEsperado);
        }
    }

    private String gerarCodigo() {
        String codigo;
        do {
            codigo = "TR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                    .toUpperCase(Locale.ROOT);
        } while (voucherRepository.existsByCodigoIgnoreCase(codigo));
        return codigo;
    }
}