package com.livrofacil.modulos.cliente.service;

import com.livrofacil.modulos.cliente.dto.FormaPagamentoRequest;
import com.livrofacil.modulos.cliente.dto.FormaPagamentoResponse;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.entity.FormaPagamento;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.FormaPagamentoRepository;
import com.livrofacil.modulos.cliente.repository.BandeiraPagamentoRepository;
import com.livrofacil.modulos.cliente.entity.BandeiraPagamento;
import com.livrofacil.modulos.compra.repository.PagamentoPedidoRepository;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FormaPagamentoUseCase {

    private final FormaPagamentoRepository formaPagamentoRepository;
    private final ClienteRepository clienteRepository;
    private final BandeiraPagamentoRepository bandeiraPagamentoRepository;
    private final PagamentoPedidoRepository pagamentoPedidoRepository;

    public FormaPagamentoUseCase(FormaPagamentoRepository formaPagamentoRepository, ClienteRepository clienteRepository,
                                 BandeiraPagamentoRepository bandeiraPagamentoRepository,
                                 PagamentoPedidoRepository pagamentoPedidoRepository) {
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.clienteRepository = clienteRepository;
        this.bandeiraPagamentoRepository = bandeiraPagamentoRepository;
        this.pagamentoPedidoRepository = pagamentoPedidoRepository;
    }

    @Transactional
    public FormaPagamentoResponse criar(UUID clienteId, FormaPagamentoRequest request) {
        Cliente cliente = buscarCliente(clienteId);
        validarBandeiraDisponivel(request.getBandeira());
        if (request.isPreferencial()) {
            desmarcarPreferenciais(clienteId);
        }
        String numeroCartao = request.getNumeroCartao().replaceAll("\\D", "");
        if (numeroCartao.length() != 16) {
            throw new RegraDeNegocioException("O numero do cartao deve possuir exatamente 16 digitos");
        }
        FormaPagamento formaPagamento = new FormaPagamento(
            request.getNomeTitular(), request.getTipoCartao(), numeroCartao.substring(numeroCartao.length() - 4),
            request.getValidade(), request.getBandeira(), request.isPreferencial(), cliente
        );
        return new FormaPagamentoResponse(formaPagamentoRepository.save(formaPagamento));
    }

    @Transactional(readOnly = true)
    public List<FormaPagamentoResponse> listar(UUID clienteId, boolean crescente) {
        buscarCliente(clienteId);
        List<FormaPagamento> formas = crescente
                ? formaPagamentoRepository.findByClienteIdOrderByCriadoEmAsc(clienteId)
                : formaPagamentoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId);
        return formas.stream()
                .map(FormaPagamentoResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public FormaPagamentoResponse buscarPorId(UUID clienteId, Long formaPagamentoId) {
        buscarCliente(clienteId);
        return new FormaPagamentoResponse(buscarFormaPagamento(clienteId, formaPagamentoId));
    }

    @Transactional
    public FormaPagamentoResponse atualizar(UUID clienteId, Long formaPagamentoId, FormaPagamentoRequest request) {
        buscarCliente(clienteId);
        FormaPagamento formaPagamento = buscarFormaPagamento(clienteId, formaPagamentoId);
        validarBandeiraDisponivel(request.getBandeira());
        if (!formaPagamento.isAtivo() && request.isPreferencial()) {
            throw new RegraDeNegocioException("Um cartao inativo nao pode ser preferencial");
        }
        if (request.isPreferencial()) {
            desmarcarPreferenciais(clienteId);
        }
        String numeroCartao = request.getNumeroCartao().replaceAll("\\D", "");
        if (numeroCartao.length() != 16) {
            throw new RegraDeNegocioException("O numero do cartao deve possuir exatamente 16 digitos");
        }
        formaPagamento.setNomeTitular(request.getNomeTitular());
        formaPagamento.setTipoCartao(request.getTipoCartao());
        formaPagamento.setValidade(request.getValidade());
        formaPagamento.setBandeira(request.getBandeira());
        formaPagamento.setUltimosDigitos(numeroCartao.substring(numeroCartao.length() - 4));
        formaPagamento.setPreferencial(request.isPreferencial());
        return new FormaPagamentoResponse(formaPagamentoRepository.save(formaPagamento));
    }

    @Transactional
    public FormaPagamentoResponse definirPreferencial(UUID clienteId, Long formaPagamentoId) {
        buscarCliente(clienteId);
        FormaPagamento formaPagamento = buscarFormaPagamento(clienteId, formaPagamentoId);
        if (!formaPagamento.isAtivo()) {
            throw new RegraDeNegocioException("Somente cartoes ativos podem ser preferenciais");
        }
        desmarcarPreferenciais(clienteId);
        formaPagamento.setPreferencial(true);
        return new FormaPagamentoResponse(formaPagamentoRepository.save(formaPagamento));
    }

    @Transactional
    public FormaPagamentoResponse excluir(UUID clienteId, Long formaPagamentoId) {
        buscarCliente(clienteId);
        FormaPagamento formaPagamento = buscarFormaPagamento(clienteId, formaPagamentoId);
        if (!formaPagamento.isAtivo()) {
            throw new RegraDeNegocioException("A forma de pagamento ja esta inativa");
        }
        formaPagamento.setAtivo(false);
        formaPagamento.setPreferencial(false);
        return new FormaPagamentoResponse(formaPagamentoRepository.save(formaPagamento));
    }

    @Transactional
    public void excluirDefinitivamente(UUID clienteId, Long formaPagamentoId) {
        buscarCliente(clienteId);
        FormaPagamento formaPagamento = buscarFormaPagamento(clienteId, formaPagamentoId);
        if (formaPagamento.isAtivo()) {
            throw new RegraDeNegocioException("Somente cartoes inativos podem ser excluidos definitivamente");
        }
        if (pagamentoPedidoRepository.existsByFormaPagamentoId(formaPagamentoId)) {
            throw new RegraDeNegocioException("O cartao nao pode ser excluido definitivamente porque esta vinculado ao historico de pedidos");
        }
        formaPagamentoRepository.delete(formaPagamento);
    }

    @Transactional
    public FormaPagamentoResponse reativar(UUID clienteId, Long formaPagamentoId) {
        buscarCliente(clienteId);
        FormaPagamento formaPagamento = buscarFormaPagamento(clienteId, formaPagamentoId);
        validarBandeiraDisponivel(formaPagamento.getBandeira());
        formaPagamento.setAtivo(true);
        return new FormaPagamentoResponse(formaPagamentoRepository.save(formaPagamento));
    }

    private Cliente buscarCliente(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + id));
    }

    private FormaPagamento buscarFormaPagamento(UUID clienteId, Long id) {
        return formaPagamentoRepository.findByIdAndClienteId(id, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Forma de pagamento nao encontrada: " + id));
    }

    private void desmarcarPreferenciais(UUID clienteId) {
        formaPagamentoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId).forEach(formaPagamento -> {
            formaPagamento.setPreferencial(false);
            formaPagamentoRepository.save(formaPagamento);
        });
    }

    private void validarBandeiraDisponivel(String nome) {
        BandeiraPagamento bandeira = bandeiraPagamentoRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new RegraDeNegocioException("Bandeira de cartao nao cadastrada para pagamento"));
        if (!bandeira.isDisponivel()) {
            throw new RegraDeNegocioException("A bandeira de cartao nao esta disponivel para o cliente");
        }
    }
}