package com.livrofacil.cliente.service;

import com.livrofacil.cliente.dto.AlterarSenhaRequest;
import com.livrofacil.cliente.dto.ClienteCadastroRequest;
import com.livrofacil.cliente.dto.ClienteResponse;
import com.livrofacil.cliente.dto.ClienteUpdateRequest;
import com.livrofacil.cliente.entity.Cliente;
import com.livrofacil.cliente.entity.Endereco;
import com.livrofacil.cliente.repository.ClienteRepository;
import com.livrofacil.cliente.repository.EnderecoRepository;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;

    public ClienteUseCase(ClienteRepository clienteRepository, EnderecoRepository enderecoRepository) {
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
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
                .map(ClienteResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> buscar(String nome, String email) {
        List<Cliente> clientes;

        if (Objects.nonNull(nome) && Objects.nonNull(email)) {
            clientes = clienteRepository.findByNomeContainingIgnoreCaseAndEmailContainingIgnoreCase(nome, email);
        } else if (Objects.nonNull(nome)) {
            clientes = clienteRepository.findByNomeContainingIgnoreCase(nome);
        } else if (Objects.nonNull(email)) {
            clientes = clienteRepository.findByEmailContainingIgnoreCase(email);
        } else {
            clientes = clienteRepository.findAll();
        }

        return clientes.stream()
                .map(ClienteResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return new ClienteResponse(buscarCliente(id));
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
    public ClienteResponse atualizar(Long id, ClienteUpdateRequest request) {
        Cliente cliente = buscarCliente(id);
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
    public void alterarSenha(Long id, AlterarSenhaRequest request) {
        Cliente cliente = buscarCliente(id);
        if (!Objects.equals(cliente.getSenha(), request.getSenhaAtual())) {
            throw new RegraDeNegocioException("A senha atual esta incorreta");
        }
        cliente.setSenha(request.getNovaSenha());
        clienteRepository.save(cliente);
    }

    @Transactional
    public void inativar(Long id) {
        Cliente cliente = buscarCliente(id);
        cliente.setAtivo("N");
        clienteRepository.save(cliente);
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + id));
    }

    private void validarDadosUnicos(String email, String telefone, String cpf, Long idAtual) {
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