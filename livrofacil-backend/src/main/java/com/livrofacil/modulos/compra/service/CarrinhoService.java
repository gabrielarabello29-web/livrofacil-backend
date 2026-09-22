package com.livrofacil.modulos.compra.service;

import com.livrofacil.modulos.compra.dto.CarrinhoResponse;
import com.livrofacil.modulos.compra.dto.CriarCarrinhoRequest;
import com.livrofacil.modulos.compra.dto.ItemCarrinhoRequest;
import com.livrofacil.modulos.compra.dto.ItemCarrinhoResponse;
import com.livrofacil.modulos.compra.entity.Carrinho;
import com.livrofacil.modulos.compra.entity.ItemCarrinho;
import com.livrofacil.modulos.compra.repository.CarrinhoRepository;
import com.livrofacil.modulos.compra.repository.ItemCarrinhoRepository;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class CarrinhoService {
    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemRepository;
    private final ClienteRepository clienteRepository;
    private final LivroRepository livroRepository;

    public CarrinhoService(CarrinhoRepository carrinhoRepository, ItemCarrinhoRepository itemRepository,
                           ClienteRepository clienteRepository, LivroRepository livroRepository) {
        this.carrinhoRepository = carrinhoRepository;
        this.itemRepository = itemRepository;
        this.clienteRepository = clienteRepository;
        this.livroRepository = livroRepository;
    }

    @Transactional
    public CarrinhoResponse criarOuAssociar(CriarCarrinhoRequest request) {
        Carrinho carrinho = request.getToken() == null || request.getToken().isBlank()
                ? new Carrinho()
                : carrinhoRepository.findByToken(request.getToken()).orElseGet(Carrinho::new);
        if (request.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + request.getClienteId()));
            carrinho.setCliente(cliente);
        }
        return new CarrinhoResponse(carrinhoRepository.save(carrinho));
    }

    @Transactional(readOnly = true)
    public CarrinhoResponse buscar(Long id, UUID clienteId, String token) {
        return new CarrinhoResponse(buscarCarrinho(id, clienteId, token));
    }

    @Transactional
    public ItemCarrinhoResponse adicionar(Long carrinhoId, UUID clienteId, String token, ItemCarrinhoRequest request) {
        Carrinho carrinho = buscarCarrinho(carrinhoId, clienteId, token);
        Livro livro = livroRepository.findById(request.getLivroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro nao encontrado: " + request.getLivroId()));
        if (!Boolean.TRUE.equals(livro.getAtivo())) {
            throw new RegraDeNegocioException("O livro nao esta disponivel para compra");
        }
        ItemCarrinho item = itemRepository.findByCarrinhoIdAndLivroId(carrinhoId, request.getLivroId()).orElseGet(() -> {
            ItemCarrinho novo = new ItemCarrinho();
            novo.setCarrinho(carrinho);
            novo.setLivro(livro);
            novo.setQuantidade(0);
            carrinho.getItens().add(novo);
            return novo;
        });
        item.setQuantidade(item.getQuantidade() + request.getQuantidade());
        carrinho.atualizarData();
        return new ItemCarrinhoResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemCarrinhoResponse atualizarItem(Long carrinhoId, UUID clienteId, String token, Long itemId, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new RegraDeNegocioException("A quantidade deve ser maior que zero");
        }
        Carrinho carrinho = buscarCarrinho(carrinhoId, clienteId, token);
        ItemCarrinho item = itemRepository.findByIdAndCarrinhoId(itemId, carrinho.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do carrinho nao encontrado: " + itemId));
        item.setQuantidade(quantidade);
        carrinho.atualizarData();
        return new ItemCarrinhoResponse(itemRepository.save(item));
    }

    @Transactional
    public void removerItem(Long carrinhoId, UUID clienteId, String token, Long itemId) {
        Carrinho carrinho = buscarCarrinho(carrinhoId, clienteId, token);
        ItemCarrinho item = itemRepository.findByIdAndCarrinhoId(itemId, carrinho.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do carrinho nao encontrado: " + itemId));
        itemRepository.delete(item);
        carrinho.atualizarData();
    }

    private Carrinho buscarCarrinho(Long id, UUID clienteId, String token) {
        Carrinho carrinho = carrinhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho nao encontrado: " + id));
        boolean clienteValido = clienteId != null && carrinho.getCliente() != null && carrinho.getCliente().getId().equals(clienteId);
        boolean tokenValido = token != null && token.equals(carrinho.getToken());
        if (!clienteValido && !tokenValido) {
            throw new RegraDeNegocioException("O carrinho nao pertence ao cliente informado");
        }
        return carrinho;
    }
}