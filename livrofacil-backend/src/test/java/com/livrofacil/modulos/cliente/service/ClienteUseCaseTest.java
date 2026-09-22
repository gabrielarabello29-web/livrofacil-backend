package com.livrofacil.modulos.cliente.service;

import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.modulos.cliente.dto.AlterarSenhaRequest;
import com.livrofacil.modulos.cliente.dto.ClienteCadastroRequest;
import com.livrofacil.modulos.cliente.dto.ClienteUpdateRequest;
import com.livrofacil.modulos.cliente.dto.EnderecoRequest;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.EnderecoRepository;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteUseCaseTest {
    @Mock private ClienteRepository clienteRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @InjectMocks private ClienteUseCase useCase;
    private UUID id;

    @BeforeEach
    void setup() { id = UUID.randomUUID(); }

    @Test
    void deveCriarClienteComEndereco() {
        ClienteCadastroRequest request = cadastro();
        when(clienteRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(clienteRepository.existsByTelefone(request.getTelefone())).thenReturn(false);
        when(clienteRepository.existsByCpf("12345678909")).thenReturn(false);
        when(clienteRepository.maiorNumeroRegistro()).thenReturn(10L);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.criar(request);

        assertEquals("Cliente", response.getNome());
        verify(enderecoRepository).save(any());
        assertEquals(11L, response.getNumeroRegistro());
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        ClienteCadastroRequest request = cadastro();
        when(clienteRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> useCase.criar(request));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveListarApenasClientesVisiveis() throws Exception {
        Cliente visivel = cliente();
        Cliente antigo = cliente();
        antigo.setDataExclusao(LocalDateTime.now().minusDays(31));
        when(clienteRepository.findAll()).thenReturn(List.of(visivel, antigo));

        assertEquals(1, useCase.listar().size());
    }

    @Test
    void deveBuscarComFiltrosNormalizados() {
        Cliente cliente = cliente();
        when(clienteRepository.buscarComFiltros(3L, "Ana", "a@b.com", "123", "999", null,
                "FEMININO", null, null, null, null, null, null, null)).thenReturn(List.of(cliente));

        assertEquals(1, useCase.buscar(3L, " Ana ", " a@b.com ", "123", "999", null,
                " FEMININO ", "", null, null, null, null, null, null).size());
    }

    @Test
    void deveRejeitarBuscaPorIdDeClienteExcluido() throws Exception {
        Cliente cliente = cliente();
        cliente.setDataExclusao(LocalDateTime.now().minusDays(31));
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.buscarPorId(id));
    }

    @Test
    void deveCompletarPerfilAusenteNoLogin() {
        Cliente cliente = cliente();
        cliente.setPerfil(null);
        when(clienteRepository.findByEmailAndSenha("email", "senha")).thenReturn(Optional.of(cliente));

        var response = useCase.login("email", "senha");

        assertEquals("CLIENTE", response.getPerfil());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void deveRejeitarLoginDeClienteInativo() {
        Cliente cliente = cliente();
        cliente.setAtivo("N");
        when(clienteRepository.findByEmailAndSenha("email", "senha")).thenReturn(Optional.of(cliente));

        assertThrows(RegraDeNegocioException.class, () -> useCase.login("email", "senha"));
    }

    @Test
    void deveAtualizarClienteAtivo() {
        Cliente cliente = cliente();
        ClienteUpdateRequest request = update();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmailAndIdNot(request.getEmail(), id)).thenReturn(false);
        when(clienteRepository.existsByTelefoneAndIdNot(request.getTelefone(), id)).thenReturn(false);
        when(clienteRepository.existsByCpfAndIdNot("12345678909", id)).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        var response = useCase.atualizar(id, request);

        assertEquals("Novo nome", response.getNome());
    }

    @Test
    void deveAlterarSenhaComSenhaAtualCorreta() {
        Cliente cliente = cliente();
        cliente.setSenha("Antiga@123");
        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setSenhaAtual("Antiga@123");
        request.setNovaSenha("Nova@123");
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        useCase.alterarSenha(id, request);

        assertEquals("Nova@123", cliente.getSenha());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void deveInativarClienteSemPedidosAtivos() {
        Cliente cliente = cliente();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.existsByClienteIdAndStatusIn(any(), anySet())).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        useCase.inativar(id);

        assertEquals("N", cliente.getAtivo());
    }

    @Test
    void naoDeveInativarClienteComPedidoAtivo() {
        Cliente cliente = cliente();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.existsByClienteIdAndStatusIn(any(), any())).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> useCase.inativar(id));
    }

    @Test
    void deveAnonimizarContasExcluidas() throws Exception {
        Cliente cliente = cliente();
        setId(cliente, id);
        cliente.setDataExclusao(LocalDateTime.now().minusDays(31));
        cliente.setCpf("123");
        cliente.setSenha("Senha@123");
        when(clienteRepository.findByDataExclusaoBeforeAndDadosAnonimizadosFalse(any())).thenReturn(List.of(cliente));

        useCase.anonimizarContasExcluidas();

        assertEquals("Conta excluida", cliente.getNome());
        assertEquals(true, cliente.isDadosAnonimizados());
        assertEquals(null, cliente.getCpf());
        verify(clienteRepository).save(cliente);
    }

    private Cliente cliente() throws RuntimeException {
        Cliente cliente = new Cliente("Cliente", "email", "999");
        try { setId(cliente, id); } catch (Exception exception) { throw new RuntimeException(exception); }
        return cliente;
    }

    private ClienteCadastroRequest cadastro() {
        ClienteCadastroRequest request = new ClienteCadastroRequest();
        request.setNome("Cliente"); request.setEmail("email@teste.com"); request.setCpf("12345678909");
        request.setTelefone("(11) 99999-9999"); request.setDataNascimento(LocalDate.now().minusYears(20));
        request.setGenero("OUTRO"); request.setSenha("Senha@123"); request.setConfirmarSenha("Senha@123");
        request.setEndereco(enderecoRequest());
        return request;
    }

    private ClienteUpdateRequest update() {
        ClienteUpdateRequest request = new ClienteUpdateRequest();
        request.setNome("Novo nome"); request.setEmail("novo@teste.com"); request.setCpf("12345678909");
        request.setTelefone("(11) 98888-8888"); request.setDataNascimento(LocalDate.now().minusYears(20));
        request.setGenero("OUTRO");
        return request;
    }

    private EnderecoRequest enderecoRequest() {
        EnderecoRequest request = new EnderecoRequest();
        request.setTipoEndereco("CASA"); request.setLogradouro("Rua A"); request.setNumero("10");
        request.setBairro("Centro"); request.setCidade("Sao Paulo"); request.setEstado("SP"); request.setCep("01000-000");
        return request;
    }

    private void setId(Object target, Object value) throws Exception {
        Field field = Cliente.class.getDeclaredField("id"); field.setAccessible(true); field.set(target, value);
    }
}
