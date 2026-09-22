package com.livrofacil.modulos.cliente.service;

import com.livrofacil.modulos.cliente.dto.AlterarSenhaRequest;
import com.livrofacil.modulos.cliente.dto.ClienteCadastroRequest;
import com.livrofacil.modulos.cliente.dto.ClienteResponse;
import com.livrofacil.modulos.cliente.dto.ClienteUpdateRequest;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.entity.Endereco;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.EnderecoRepository;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.repository.PedidoRepository;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ClienteUseCase {

    private static final Set<StatusPedido> STATUS_PEDIDO_ATIVO = EnumSet.of(
            StatusPedido.PENDENTE,
            StatusPedido.AGUARDANDO_PAGAMENTO,
            StatusPedido.EM_CHECKOUT,
            StatusPedido.EM_PROCESSAMENTO,
            StatusPedido.PAGAMENTO_APROVADO,
            StatusPedido.EM_SEPARACAO,
            StatusPedido.NA_TRANSPORTADORA,
            StatusPedido.EM_ROTA_DE_ENTREGA
    );
    private static final int DIAS_RETENCAO_EXCLUSAO = 30;

    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteUseCase(ClienteRepository clienteRepository, EnderecoRepository enderecoRepository,
                          PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public ClienteResponse criar(ClienteCadastroRequest request) {
        validarDadosUnicos(request.getEmail(), request.getTelefone(), request.getCpf().replaceAll("\\D", ""), null);

        Cliente cliente = new Cliente(
                request.getNome(),
                request.getEmail(),
                request.getTelefone()
        );
        cliente.setCpf(request.getCpf().replaceAll("\\D", ""));
        cliente.setNumeroRegistro(clienteRepository.maiorNumeroRegistro() + 1);
        cliente.setSenha(request.getSenha());
        cliente.setPerfil("CLIENTE");
        cliente.setDataNascimento(request.getDataNascimento());
        cliente.setGenero(request.getGenero());
        cliente = clienteRepository.save(cliente);

        Endereco endereco = new Endereco(
                request.getEndereco().getTipoEndereco(),
                request.getEndereco().getLogradouro(),
                request.getEndereco().getNumero(),
                request.getEndereco().getComplemento(),
                request.getEndereco().getBairro(),
                request.getEndereco().getCidade(),
                request.getEndereco().getEstado(),
                request.getEndereco().getCep(),
                true,
                cliente
        );
        enderecoRepository.save(endereco);
        return new ClienteResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .filter(this::visivelNoAdmin)
                .map(ClienteResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> buscar(Long registro, String nome, String email, String cpf, String telefone,
                                        LocalDate dataNascimento, String genero, String tipoEndereco,
                                        String endereco, String complemento, String bairro, String cidade,
                                        String estado, String cep) {
        List<Cliente> clientes = clienteRepository.buscarComFiltros(
            registro, filtro(nome), filtro(email),
                cpf == null || cpf.isBlank() ? null : cpf.replaceAll("\\D", ""),
                filtro(telefone), dataNascimento, filtro(genero), filtro(tipoEndereco), filtro(endereco),
                filtro(complemento), filtro(bairro), filtro(cidade), filtro(estado), filtro(cep));

        return clientes.stream()
            .filter(this::visivelNoAdmin)
                .map(ClienteResponse::new)
                .toList();
    }

    private String filtro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(UUID id) {
        Cliente cliente = buscarCliente(id);
        if (!visivelNoAdmin(cliente)) {
            throw new RecursoNaoEncontradoException("Cliente nao encontrado: " + id);
        }
        return new ClienteResponse(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse login(String email, String senha) {
        Optional<Cliente> clienteOpt = clienteRepository.findByEmailAndSenha(email, senha);
        if (clienteOpt.isEmpty()) {
            throw new RegraDeNegocioException("E-mail ou senha invalidos");
        }

        Cliente cliente = clienteOpt.get();
        if (cliente.getPerfil() == null || cliente.getPerfil().isBlank()) {
            cliente.setPerfil("CLIENTE");
            clienteRepository.save(cliente);
        }
        if ("N".equalsIgnoreCase(cliente.getAtivo())) {
            throw new RegraDeNegocioException("Cliente inativo");
        }

        return new ClienteResponse(cliente);
    }

    @Transactional
    public ClienteResponse atualizar(UUID id, ClienteUpdateRequest request) {
        Cliente cliente = buscarClienteAtivo(id);
        validarDadosUnicos(request.getEmail(), request.getTelefone(), request.getCpf().replaceAll("\\D", ""), id);

        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setCpf(request.getCpf().replaceAll("\\D", ""));
        cliente.setTelefone(request.getTelefone());
        cliente.setDataNascimento(request.getDataNascimento());
        cliente.setGenero(request.getGenero());
        return new ClienteResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void alterarSenha(UUID id, AlterarSenhaRequest request) {
        Cliente cliente = buscarClienteAtivo(id);
        if (!Objects.equals(cliente.getSenha(), request.getSenhaAtual())) {
            throw new RegraDeNegocioException("A senha atual esta incorreta");
        }
        cliente.setSenha(request.getNovaSenha());
        clienteRepository.save(cliente);
    }

    @Transactional
    public void inativar(UUID id) {
        Cliente cliente = buscarClienteAtivo(id);
        if (pedidoRepository.existsByClienteIdAndStatusIn(id, STATUS_PEDIDO_ATIVO)) {
            throw new RegraDeNegocioException("Nao e possivel excluir a conta enquanto houver pedidos ativos");
        }
        cliente.setAtivo("N");
        cliente.setDataExclusao(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    @Scheduled(fixedDelay = 86400000)
    @Transactional
    public void anonimizarContasExcluidas() {
        LocalDateTime limite = LocalDateTime.now().minusDays(DIAS_RETENCAO_EXCLUSAO);
        clienteRepository.findByDataExclusaoBeforeAndDadosAnonimizadosFalse(limite).forEach(cliente -> {
            cliente.setNome("Conta excluida");
            cliente.setEmail("conta-excluida-" + cliente.getId() + "@anonimizada.local");
            cliente.setSenha(null);
            cliente.setCpf(null);
            cliente.setTelefone(null);
            cliente.setDataNascimento(null);
            cliente.setGenero(null);
            cliente.setDadosAnonimizados(true);
            clienteRepository.save(cliente);
        });
    }

    private Cliente buscarCliente(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + id));
    }

    private Cliente buscarClienteAtivo(UUID id) {
        Cliente cliente = buscarCliente(id);
        if (!"S".equalsIgnoreCase(cliente.getAtivo()) || cliente.getDataExclusao() != null) {
            throw new RegraDeNegocioException("Cliente inativo");
        }
        return cliente;
    }

    private boolean visivelNoAdmin(Cliente cliente) {
        return cliente.getDataExclusao() == null
                || cliente.getDataExclusao().isAfter(LocalDateTime.now().minusDays(DIAS_RETENCAO_EXCLUSAO));
    }

    private void validarDadosUnicos(String email, String telefone, String cpf, UUID idAtual) {
        boolean emailDuplicado = idAtual == null
                ? clienteRepository.existsByEmail(email)
                : clienteRepository.existsByEmailAndIdNot(email, idAtual);
        boolean telefoneDuplicado = idAtual == null
                ? clienteRepository.existsByTelefone(telefone)
                : clienteRepository.existsByTelefoneAndIdNot(telefone, idAtual);
        boolean cpfDuplicado = idAtual == null
            ? clienteRepository.existsByCpf(cpf)
            : clienteRepository.existsByCpfAndIdNot(cpf, idAtual);

        if (emailDuplicado) {
            throw new RegraDeNegocioException("Este e-mail ja esta cadastrado");
        }

        if (telefoneDuplicado) {
            throw new RegraDeNegocioException("Este telefone ja esta cadastrado");
        }
        if (cpfDuplicado) {
            throw new RegraDeNegocioException("Este CPF ja esta cadastrado");
        }
    }

}