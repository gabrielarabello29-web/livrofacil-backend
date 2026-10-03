package com.livrofacil.modulos.troca.service;

import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.repository.ItemPedidoRepository;
import com.livrofacil.modulos.compra.repository.PedidoRepository;
import com.livrofacil.modulos.livro.repository.EstoqueRepository;
import com.livrofacil.modulos.troca.dto.SolicitarTrocaRequest;
import com.livrofacil.modulos.troca.dto.TrocaResponse;
import com.livrofacil.modulos.troca.entity.StatusTroca;
import com.livrofacil.modulos.troca.entity.Troca;
import com.livrofacil.modulos.troca.entity.VoucherTroca;
import com.livrofacil.modulos.troca.repository.TrocaRepository;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TrocaService {
    private static final EnumSet<StatusTroca> TROCAS_ABERTAS = EnumSet.of(StatusTroca.SOLICITADA, StatusTroca.AUTORIZADA);

    private final TrocaRepository trocaRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final EstoqueRepository estoqueRepository;
    private final VoucherTrocaRepository voucherRepository;

    public TrocaService(TrocaRepository trocaRepository, PedidoRepository pedidoRepository,
                        ItemPedidoRepository itemPedidoRepository, EstoqueRepository estoqueRepository,
                        VoucherTrocaRepository voucherRepository) {
        this.trocaRepository = trocaRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.estoqueRepository = estoqueRepository;
        this.voucherRepository = voucherRepository;
    }

    @Transactional
    public TrocaResponse solicitar(SolicitarTrocaRequest request) {
        var pedido = pedidoRepository.findByIdAndClienteId(request.getPedidoId(), request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido nao encontrado para o cliente"));
        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw new RegraDeNegocioException("A troca so pode ser solicitada para pedidos entregues");
        }
        var item = request.getItemPedidoId() == null
                ? itemPedidoRepository.findFirstByPedidoIdAndLivroId(request.getPedidoId(), request.getLivroId())
                : itemPedidoRepository.findByIdAndPedidoId(request.getItemPedidoId(), request.getPedidoId());
        var itemPedido = item.orElseThrow(() -> new RecursoNaoEncontradoException("Item nao encontrado no pedido"));
        if (trocaRepository.existsByItemPedidoIdAndStatusIn(itemPedido.getId(), TROCAS_ABERTAS)
                || trocaRepository.existsByItemPedidoIdAndStatusIn(itemPedido.getId(), List.of(StatusTroca.TROCADA))) {
            throw new RegraDeNegocioException("Este item ja possui uma troca solicitada ou concluida");
        }

        Troca troca = new Troca();
        troca.setPedido(pedido);
        troca.setItemPedido(itemPedido);
        troca.setMotivo(request.getMotivo().trim());
        troca.setDetalhes(request.getDetalhes());
        return new TrocaResponse(trocaRepository.save(troca));
    }

    @Transactional(readOnly = true)
    public List<TrocaResponse> listarTodas() {
        return trocaRepository.findAllByOrderByCriadoEmDesc().stream().map(TrocaResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<TrocaResponse> listarCliente(UUID clienteId) {
        return trocaRepository.findByPedidoClienteIdOrderByCriadoEmDesc(clienteId).stream().map(TrocaResponse::new).toList();
    }

    @Transactional
    public TrocaResponse autorizar(Long trocaId) {
        Troca troca = buscarTroca(trocaId);
        exigirStatus(troca, StatusTroca.SOLICITADA);
        troca.setStatus(StatusTroca.AUTORIZADA);
        return new TrocaResponse(trocaRepository.save(troca));
    }

    @Transactional
    public TrocaResponse recusar(Long trocaId, String motivo) {
        Troca troca = buscarTroca(trocaId);
        exigirStatus(troca, StatusTroca.SOLICITADA);
        troca.setStatus(StatusTroca.RECUSADA);
        troca.setMotivoRecusa(motivo.trim());
        return new TrocaResponse(trocaRepository.save(troca));
    }

    @Transactional
    public TrocaResponse receber(Long trocaId, boolean retornarEstoque) {
        Troca troca = buscarTroca(trocaId);
        exigirStatus(troca, StatusTroca.AUTORIZADA);
        if (retornarEstoque) {
            var item = troca.getItemPedido();
            var estoque = estoqueRepository.findWithLockByLivroId(item.getLivro().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Estoque nao encontrado para o livro devolvido"));
            estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() + item.getQuantidade());
            estoque.setQuantidadeVendida(Math.max(0, estoque.getQuantidadeVendida() - item.getQuantidade()));
            estoqueRepository.save(estoque);
        }

        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo(gerarCodigo());
        voucher.setValor(troca.getItemPedido().getValorUnitario()
                .multiply(BigDecimal.valueOf(troca.getItemPedido().getQuantidade())));
        voucher.setCliente(troca.getPedido().getCliente());
        voucher.setTroca(troca);
        troca.setVoucher(voucher);
        troca.setStatus(StatusTroca.TROCADA);
        Troca salva = trocaRepository.save(troca);
        voucherRepository.save(voucher);
        return new TrocaResponse(salva);
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
        return "TR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
    }
}