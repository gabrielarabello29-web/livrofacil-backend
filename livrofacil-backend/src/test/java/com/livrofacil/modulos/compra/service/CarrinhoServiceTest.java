package com.livrofacil.modulos.compra.service;

import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.compra.dto.CriarCarrinhoRequest;
import com.livrofacil.modulos.compra.dto.ItemCarrinhoRequest;
import com.livrofacil.modulos.compra.entity.Carrinho;
import com.livrofacil.modulos.compra.entity.ItemCarrinho;
import com.livrofacil.modulos.compra.repository.CarrinhoRepository;
import com.livrofacil.modulos.compra.repository.ItemCarrinhoRepository;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoServiceTest {
    @Mock private CarrinhoRepository carrinhoRepository;
    @Mock private ItemCarrinhoRepository itemRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private LivroRepository livroRepository;
    @InjectMocks private CarrinhoService service;

    @Test
    void deveAssociarCarrinhoAoCliente() throws Exception {
        UUID clienteId = UUID.randomUUID();
        CriarCarrinhoRequest request = new CriarCarrinhoRequest();
        request.setClienteId(clienteId);
        Cliente cliente = new Cliente("Cliente", "cliente@email.com", "(11) 99999-9999");
        setId(cliente, clienteId);
        Carrinho carrinho = new Carrinho();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(carrinhoRepository.save(any(Carrinho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.criarOuAssociar(request);

        assertEquals(clienteId, response.getClienteId());
    }

    @Test
    void deveReaproveitarCarrinhoEncontradoPorToken() {
        CriarCarrinhoRequest request = new CriarCarrinhoRequest();
        request.setToken("token");
        Carrinho carrinho = new Carrinho();
        carrinho.setToken("token");
        when(carrinhoRepository.findByToken("token")).thenReturn(Optional.of(carrinho));
        when(carrinhoRepository.save(carrinho)).thenReturn(carrinho);

        var response = service.criarOuAssociar(request);

        assertEquals("token", response.getToken());
        verify(carrinhoRepository).save(carrinho);
    }

    @Test
    void deveCriarCarrinhoQuandoTokenNaoForEncontrado() {
        CriarCarrinhoRequest request = new CriarCarrinhoRequest();
        request.setToken("token-novo");
        when(carrinhoRepository.findByToken("token-novo")).thenReturn(Optional.empty());
        when(carrinhoRepository.save(any(Carrinho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.criarOuAssociar(request);

        assertEquals(null, response.getClienteId());
        verify(carrinhoRepository).save(any(Carrinho.class));
    }

    @Test
    void deveRejeitarClienteInexistenteAoCriarCarrinho() {
        UUID clienteId = UUID.randomUUID();
        CriarCarrinhoRequest request = new CriarCarrinhoRequest();
        request.setClienteId(clienteId);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.criarOuAssociar(request));
    }

    @Test
    void deveRejeitarCarrinhoDeOutroCliente() {
        UUID clienteId = UUID.randomUUID();
        Carrinho carrinho = new Carrinho();
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));

        assertThrows(RegraDeNegocioException.class, () -> service.buscar(1L, clienteId, null));
    }

    @Test
    void deveBuscarCarrinhoPorClienteValido() throws Exception {
        UUID clienteId = UUID.randomUUID();
        Cliente cliente = new Cliente("Cliente", "cliente@email.com", "(11) 99999-9999");
        setId(cliente, clienteId);
        Carrinho carrinho = new Carrinho();
        carrinho.setCliente(cliente);
        setId(carrinho, 1L);
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));

        var response = service.buscar(1L, clienteId, null);

        assertEquals(clienteId, response.getClienteId());
    }

    @Test
    void deveRejeitarCarrinhoInexistente() {
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(1L, null, "token"));
    }

    @Test
    void deveRejeitarLivroInativoAoAdicionar() {
        UUID clienteId = UUID.randomUUID();
        Livro livro = new Livro();
        livro.setAtivo(false);
        Carrinho carrinho = new Carrinho();
        carrinho.setToken("token");
        ItemCarrinhoRequest request = new ItemCarrinhoRequest();
        request.setLivroId(1L);
        request.setQuantidade(1);
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        assertThrows(RegraDeNegocioException.class, () -> service.adicionar(1L, clienteId, "token", request));
    }

    @Test
    void deveAdicionarLivroNovoAoCarrinho() throws Exception {
        Carrinho carrinho = carrinhoComToken();
        Livro livro = livroAtivo();
        ItemCarrinhoRequest request = itemRequest(1);
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(itemRepository.findByCarrinhoIdAndLivroId(1L, 1L)).thenReturn(Optional.empty());
        when(itemRepository.save(any(ItemCarrinho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.adicionar(1L, null, "token", request);

        assertEquals(1, response.getQuantidade());
        assertEquals(1, carrinho.getItens().size());
    }

    @Test
    void deveSomarQuantidadeDeLivroJaExistente() throws Exception {
        Carrinho carrinho = carrinhoComToken();
        Livro livro = livroAtivo();
        ItemCarrinho item = new ItemCarrinho();
        item.setLivro(livro);
        item.setQuantidade(2);
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(itemRepository.findByCarrinhoIdAndLivroId(1L, 1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        var response = service.adicionar(1L, null, "token", itemRequest(3));

        assertEquals(5, response.getQuantidade());
    }

    @Test
    void deveRejeitarQuantidadeInvalidaAoAtualizar() {
        assertThrows(RegraDeNegocioException.class, () -> service.atualizarItem(1L, UUID.randomUUID(), null, 1L, 0));
        verify(carrinhoRepository, never()).findById(any());
    }

    @Test
    void deveAtualizarQuantidadeDoItem() throws Exception {
        Carrinho carrinho = carrinhoComToken();
        ItemCarrinho item = new ItemCarrinho();
        item.setLivro(livroAtivo());
        item.setQuantidade(1);
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));
        when(itemRepository.findByIdAndCarrinhoId(2L, 1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        var response = service.atualizarItem(1L, null, "token", 2L, 4);

        assertEquals(4, response.getQuantidade());
    }

    @Test
    void deveRejeitarItemInexistenteAoAtualizar() throws Exception {
        Carrinho carrinho = carrinhoComToken();
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));
        when(itemRepository.findByIdAndCarrinhoId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.atualizarItem(1L, null, "token", 2L, 4));
    }

    @Test
    void deveRemoverItemDoCarrinho() throws Exception {
        UUID clienteId = UUID.randomUUID();
        Cliente cliente = new Cliente("Cliente", "cliente@email.com", "(11) 99999-9999");
        setId(cliente, clienteId);
        Carrinho carrinho = new Carrinho();
        carrinho.setToken("token");
        carrinho.setCliente(cliente);
        setId(carrinho, 1L);
        ItemCarrinho item = new ItemCarrinho();
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));
        when(itemRepository.findByIdAndCarrinhoId(2L, 1L)).thenReturn(Optional.of(item));

        service.removerItem(1L, clienteId, "token", 2L);

        verify(itemRepository).delete(item);
    }

    @Test
    void deveRejeitarRemocaoDeItemInexistente() throws Exception {
        Carrinho carrinho = carrinhoComToken();
        when(carrinhoRepository.findById(1L)).thenReturn(Optional.of(carrinho));
        when(itemRepository.findByIdAndCarrinhoId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.removerItem(1L, null, "token", 2L));
        verify(itemRepository, never()).delete(any());
    }

    private Carrinho carrinhoComToken() throws Exception {
        Carrinho carrinho = new Carrinho();
        carrinho.setToken("token");
        setId(carrinho, 1L);
        return carrinho;
    }

    private Livro livroAtivo() {
        Livro livro = org.mockito.Mockito.mock(Livro.class);
        when(livro.getId()).thenReturn(1L);
        when(livro.getTitulo()).thenReturn("Livro");
        when(livro.getImagemUrl()).thenReturn("imagem");
        when(livro.getValorVenda()).thenReturn(BigDecimal.TEN);
        org.mockito.Mockito.lenient().when(livro.getAtivo()).thenReturn(true);
        return livro;
    }

    private ItemCarrinhoRequest itemRequest(int quantidade) {
        ItemCarrinhoRequest request = new ItemCarrinhoRequest();
        request.setLivroId(1L);
        request.setQuantidade(quantidade);
        return request;
    }

    private void setId(Cliente cliente, UUID id) throws Exception {
        Field field = Cliente.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(cliente, id);
    }

    private void setId(Carrinho carrinho, Long id) throws Exception {
        Field field = Carrinho.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(carrinho, id);
    }
}
