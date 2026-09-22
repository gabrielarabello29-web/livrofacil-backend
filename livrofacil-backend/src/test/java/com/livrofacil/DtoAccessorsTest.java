package com.livrofacil;

import com.livrofacil.modulos.cliente.dto.ClienteCadastroRequest;
import com.livrofacil.modulos.cliente.dto.AlterarSenhaRequest;
import com.livrofacil.modulos.livro.dto.DimensaoRequest;
import com.livrofacil.modulos.livro.dto.EstoqueRequest;
import com.livrofacil.modulos.livro.dto.LivroRequest;
import com.livrofacil.modulos.compra.dto.AtualizarStatusPedidoRequest;
import com.livrofacil.modulos.compra.dto.CriarCarrinhoRequest;
import com.livrofacil.modulos.compra.dto.FinalizarCompraRequest;
import com.livrofacil.modulos.compra.dto.IniciarCompraRequest;
import com.livrofacil.modulos.compra.dto.ItemCarrinhoRequest;
import com.livrofacil.modulos.compra.dto.PagamentoRequest;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoAccessorsTest {
    @Test void deveCobrirRequestsDeClienteELivro() {
        ClienteCadastroRequest cliente = new ClienteCadastroRequest(); cliente.setNome("N"); cliente.setEmail("e@teste.com"); cliente.setTelefone("(11) 91234-5678"); cliente.setDataNascimento(LocalDate.now().minusYears(20)); cliente.setCpf("123.456.789-09"); cliente.setGenero("MASCULINO"); cliente.setSenha("N@123456"); cliente.setConfirmarSenha("N@123456");
        assertEquals("N", cliente.getNome()); assertEquals("e@teste.com", cliente.getEmail()); assertEquals("(11) 91234-5678", cliente.getTelefone());
        AlterarSenhaRequest senha = new AlterarSenhaRequest(); senha.setSenhaAtual("a"); senha.setNovaSenha("N@123456"); senha.setConfirmarNovaSenha("N@123456"); assertTrue(senha.isSenhasIguais());
        LivroRequest livro = new LivroRequest(); livro.setCodigo("c"); livro.setTitulo("t"); livro.setAno(2020); livro.setEdicao(1); livro.setIsbn("i"); livro.setNumeroPaginas(10); livro.setSinopse("s"); livro.setImagemUrl("http://i"); livro.setCodigoBarras("b"); livro.setValorVenda(BigDecimal.TEN); livro.setAtivo(true); livro.setAutorId(1L); livro.setEditoraId(2L); livro.setGrupoPrecificacaoId(3L); livro.setCategoriaIds(List.of(4L));
        assertEquals("c", livro.getCodigo()); assertEquals("t", livro.getTitulo()); assertEquals(2020, livro.getAno()); assertEquals(1, livro.getEdicao()); assertEquals("i", livro.getIsbn()); assertEquals(10, livro.getNumeroPaginas()); assertEquals(BigDecimal.TEN, livro.getValorVenda()); assertEquals(List.of(4L), livro.getCategoriaIds());
    }

    @Test void deveCobrirRequestsDeCompra() {
        DimensaoRequest dimensao = new DimensaoRequest(); dimensao.setAltura(BigDecimal.ONE); dimensao.setLargura(BigDecimal.TEN); dimensao.setProfundidade(BigDecimal.ONE); dimensao.setPeso(BigDecimal.ONE); assertEquals(BigDecimal.TEN, dimensao.getLargura());
        EstoqueRequest estoque = new EstoqueRequest(); estoque.setQuantidadeDisponivel(5); estoque.setQuantidadeBloqueada(2); estoque.setQuantidadeVendida(1); assertTrue(estoque.isEstoqueCoerente());
        AtualizarStatusPedidoRequest status = new AtualizarStatusPedidoRequest(); status.setStatus(StatusPedido.PENDENTE); assertEquals(StatusPedido.PENDENTE, status.getStatus());
        CriarCarrinhoRequest carrinho = new CriarCarrinhoRequest(); carrinho.setToken("t"); carrinho.setClienteId(UUID.randomUUID()); assertEquals("t", carrinho.getToken());
        IniciarCompraRequest iniciar = new IniciarCompraRequest(); iniciar.setClienteId(UUID.randomUUID()); iniciar.setCarrinhoId(1L); iniciar.setEnderecoEntregaId(2L); iniciar.setEnderecoCobranca("c"); iniciar.setCupom("d"); assertEquals("d", iniciar.getCupom());
        ItemCarrinhoRequest item = new ItemCarrinhoRequest(); item.setLivroId(3L); item.setQuantidade(2); assertEquals(2, item.getQuantidade());
        PagamentoRequest pagamento = new PagamentoRequest(); pagamento.setFormaPagamentoId(4L); pagamento.setValor(BigDecimal.TEN); pagamento.setParcelas(1); assertEquals(BigDecimal.TEN, pagamento.getValor());
        FinalizarCompraRequest finalizar = new FinalizarCompraRequest(); finalizar.setCarrinhoId(1L); finalizar.setPagamentos(List.of(pagamento)); assertEquals(1, finalizar.getPagamentos().size());
    }
}
