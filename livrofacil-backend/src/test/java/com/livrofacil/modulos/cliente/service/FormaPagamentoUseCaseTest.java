package com.livrofacil.modulos.cliente.service;

import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.cliente.dto.FormaPagamentoRequest;
import com.livrofacil.modulos.cliente.dto.FormaPagamentoResponse;
import com.livrofacil.modulos.cliente.entity.BandeiraPagamento;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.entity.FormaPagamento;
import com.livrofacil.modulos.cliente.repository.BandeiraPagamentoRepository;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.FormaPagamentoRepository;
import com.livrofacil.modulos.compra.repository.PagamentoPedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FormaPagamentoUseCaseTest {
    @Mock private FormaPagamentoRepository formaPagamentoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private BandeiraPagamentoRepository bandeiraPagamentoRepository;
    @Mock private PagamentoPedidoRepository pagamentoPedidoRepository;
    @InjectMocks private FormaPagamentoUseCase useCase;

    private UUID clienteId;
    private Cliente cliente;
    private BandeiraPagamento bandeira;

    @BeforeEach
    void setup() {
        clienteId = UUID.randomUUID();
        cliente = new Cliente("Cliente", "cliente@email.com", "(11) 99999-9999");
        bandeira = new BandeiraPagamento("VISA");
        lenient().when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        lenient().when(bandeiraPagamentoRepository.findByNomeIgnoreCase("VISA")).thenReturn(Optional.of(bandeira));
    }

    @Test
    void deveDefinirCartaoAtivoComoPreferencialEDesmarcarOsDemais() {
        FormaPagamento atual = cartao(true, false);
        FormaPagamento anterior = cartao(true, true);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(atual));
        when(formaPagamentoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId)).thenReturn(List.of(anterior, atual));
        when(formaPagamentoRepository.save(any(FormaPagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FormaPagamentoResponse response = useCase.definirPreferencial(clienteId, 1L);

        assertEquals(true, response.isPreferencial());
        assertEquals(false, anterior.isPreferencial());
        verify(formaPagamentoRepository).save(anterior);
        verify(formaPagamentoRepository, org.mockito.Mockito.times(2)).save(atual);
    }

    @Test
    void naoDeveDefinirCartaoInativoComoPreferencial() {
        FormaPagamento inativo = cartao(false, false);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(inativo));

        assertThrows(RegraDeNegocioException.class, () -> useCase.definirPreferencial(clienteId, 1L));
        verify(formaPagamentoRepository, never()).save(any());
    }

    @Test
    void deveInativarCartaoERemoverPreferencia() {
        FormaPagamento cartao = cartao(true, true);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));
        when(formaPagamentoRepository.save(cartao)).thenReturn(cartao);

        FormaPagamentoResponse response = useCase.excluir(clienteId, 1L);

        assertEquals(false, response.isAtivo());
        assertEquals(false, response.isPreferencial());
    }

    @Test
    void naoDeveExcluirDefinitivamenteCartaoAtivo() {
        FormaPagamento cartao = cartao(true, false);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));

        assertThrows(RegraDeNegocioException.class, () -> useCase.excluirDefinitivamente(clienteId, 1L));
        verify(formaPagamentoRepository, never()).delete(any());
    }

    @Test
    void naoDeveExcluirCartaoVinculadoAoHistorico() {
        FormaPagamento cartao = cartao(false, false);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));
        when(pagamentoPedidoRepository.existsByFormaPagamentoId(1L)).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> useCase.excluirDefinitivamente(clienteId, 1L));
        verify(formaPagamentoRepository, never()).delete(any());
    }

    @Test
    void deveRejeitarBandeiraIndisponivelNoCadastro() {
        bandeira.setDisponivel(false);

        assertThrows(RegraDeNegocioException.class, () -> useCase.criar(clienteId, request()));
        verify(formaPagamentoRepository, never()).save(any());
    }

    @Test
    void deveCriarCartaoComUltimosDigitosEPreferencia() {
        FormaPagamento anterior = cartao(true, true);
        FormaPagamento criado = cartao(true, true);
        FormaPagamentoRequest request = request();
        request.setPreferencial(true);
        when(formaPagamentoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId)).thenReturn(List.of(anterior));
        when(formaPagamentoRepository.save(any(FormaPagamento.class))).thenReturn(criado);

        FormaPagamentoResponse response = useCase.criar(clienteId, request);

        assertEquals("4444", response.getUltimosDigitos());
        assertEquals(false, anterior.isPreferencial());
        verify(formaPagamentoRepository).save(anterior);
    }

    @Test
    void deveRejeitarBandeiraNaoCadastrada() {
        when(bandeiraPagamentoRepository.findByNomeIgnoreCase("VISA")).thenReturn(Optional.empty());

        assertThrows(RegraDeNegocioException.class, () -> useCase.criar(clienteId, request()));
    }

    @Test
    void deveRejeitarNumeroInvalidoAoCriar() {
        FormaPagamentoRequest request = request();
        request.setNumeroCartao("1234");

        assertThrows(RegraDeNegocioException.class, () -> useCase.criar(clienteId, request));
        verify(formaPagamentoRepository, never()).save(any());
    }

    @Test
    void deveListarCartoesNaOrdemSolicitada() {
        FormaPagamento primeiro = cartao(true, false);
        FormaPagamento segundo = cartao(true, true);
        when(formaPagamentoRepository.findByClienteIdOrderByCriadoEmAsc(clienteId)).thenReturn(List.of(primeiro));
        when(formaPagamentoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId)).thenReturn(List.of(segundo));

        assertEquals(1, useCase.listar(clienteId, true).size());
        assertEquals(1, useCase.listar(clienteId, false).size());
        verify(formaPagamentoRepository).findByClienteIdOrderByCriadoEmAsc(clienteId);
        verify(formaPagamentoRepository).findByClienteIdOrderByCriadoEmDesc(clienteId);
    }

    @Test
    void deveAtualizarCartaoAtivo() {
        FormaPagamento cartao = cartao(true, false);
        FormaPagamentoRequest request = request();
        request.setNomeTitular("Novo titular");
        request.setPreferencial(true);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));
        when(formaPagamentoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId)).thenReturn(List.of(cartao));
        when(formaPagamentoRepository.save(any(FormaPagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FormaPagamentoResponse response = useCase.atualizar(clienteId, 1L, request);

        assertEquals("Novo titular", response.getNomeTitular());
        assertEquals("4444", response.getUltimosDigitos());
        assertEquals(true, response.isPreferencial());
    }

    @Test
    void naoDeveAtualizarCartaoInativoComoPreferencial() {
        FormaPagamento cartao = cartao(false, false);
        FormaPagamentoRequest request = request();
        request.setPreferencial(true);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));

        assertThrows(RegraDeNegocioException.class, () -> useCase.atualizar(clienteId, 1L, request));
        verify(formaPagamentoRepository, never()).save(any());
    }

    @Test
    void naoDeveExcluirCartaoJaInativo() {
        FormaPagamento cartao = cartao(false, false);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));

        assertThrows(RegraDeNegocioException.class, () -> useCase.excluir(clienteId, 1L));
        verify(formaPagamentoRepository, never()).save(any());
    }

    @Test
    void deveExcluirDefinitivamenteCartaoInativoSemHistorico() {
        FormaPagamento cartao = cartao(false, false);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));
        when(pagamentoPedidoRepository.existsByFormaPagamentoId(1L)).thenReturn(false);

        useCase.excluirDefinitivamente(clienteId, 1L);

        verify(formaPagamentoRepository).delete(cartao);
    }

    @Test
    void deveReativarCartaoComBandeiraDisponivel() {
        FormaPagamento cartao = cartao(false, false);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));
        when(formaPagamentoRepository.save(cartao)).thenReturn(cartao);

        FormaPagamentoResponse response = useCase.reativar(clienteId, 1L);

        assertEquals(true, response.isAtivo());
    }

    @Test
    void deveRejeitarReativacaoComBandeiraIndisponivel() {
        FormaPagamento cartao = cartao(false, false);
        bandeira.setDisponivel(false);
        when(formaPagamentoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(cartao));

        assertThrows(RegraDeNegocioException.class, () -> useCase.reativar(clienteId, 1L));
        verify(formaPagamentoRepository, never()).save(any());
    }

    private FormaPagamento cartao(boolean ativo, boolean preferencial) {
        FormaPagamento cartao = new FormaPagamento("Cliente", "CREDITO", "4444", "12/28", "VISA", preferencial, cliente);
        cartao.setAtivo(ativo);
        return cartao;
    }

    private FormaPagamentoRequest request() {
        FormaPagamentoRequest request = new FormaPagamentoRequest();
        request.setNomeTitular("Cliente");
        request.setTipoCartao("CREDITO");
        request.setNumeroCartao("1111 2222 3333 4444");
        request.setValidade("12/28");
        request.setBandeira("VISA");
        return request;
    }
}
