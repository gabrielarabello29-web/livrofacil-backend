package com.livrofacil.modulos.chat.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.modulos.chat.dto.EnviarMensagemRequest;
import com.livrofacil.modulos.chat.dto.EnviarMensagemResponse;
import com.livrofacil.modulos.chat.dto.LivroRecomendadoResponse;
import com.livrofacil.modulos.chat.dto.PreferenciasLiterariasRequest;
import com.livrofacil.modulos.chat.dto.PreferenciasLiterariasResponse;
import com.livrofacil.modulos.chat.dto.RegistrarFeedbackRequest;
import com.livrofacil.modulos.chat.dto.RegistrarFeedbackResponse;
import com.livrofacil.modulos.chat.entity.ChatFeedback;
import com.livrofacil.modulos.chat.entity.ChatMensagem;
import com.livrofacil.modulos.chat.entity.ChatSessao;
import com.livrofacil.modulos.chat.entity.PreferenciasLiterarias;
import com.livrofacil.modulos.chat.repository.ChatFeedbackRepository;
import com.livrofacil.modulos.chat.repository.ChatMensagemRepository;
import com.livrofacil.modulos.chat.repository.ChatSessaoRepository;
import com.livrofacil.modulos.chat.repository.PreferenciasLiterariasRepository;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.compra.entity.ItemPedido;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.repository.ItemPedidoRepository;
import com.livrofacil.modulos.livro.entity.Categoria;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.repository.CategoriaRepository;
import com.livrofacil.modulos.livro.repository.LivroRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ChatService {
    static final String INSTRUCAO = """
            Voce e o assistente de recomendacao de livros da Loja LivroFacil.
            Responda em portugues brasileiro de forma clara, direta e natural.

            Use somente os livros presentes no catalogo recebido e informe apenas dados presentes nesse catalogo.
            Nunca invente titulo, autor, categoria, sinopse, preco, disponibilidade ou ID.
            A recomendacao deve considerar a mensagem atual, o historico recente, as preferencias do cliente,
            o historico de compras e a faixa de preco. Se o catalogo nao tiver livros adequados, diga isso e
            pergunte somente a informacao essencial que falta; nao force uma recomendacao.

            Entenda restricoes cotidianas com precisao:
            - "passou de 20 reais", "ate 20 reais" ou equivalente significa preco no maximo R$ 20,00;
            - "mais barato que 20 reais" significa preco menor que R$ 20,00;
            - nao use um limite como menor que o informado quando o usuario disser "ate";
            - "para ler no banho", "na praia" ou outras situacoes cotidianas nao significam que o livro
              seja resistente a agua. Recomende somente pelo catalogo e nao atribua propriedades nao informadas.

            Recomende no maximo 5 livros. Priorize correspondencia com genero, tema, preco e situacao da mensagem.
            Evite repetir os mesmos livros de mensagens anteriores sem motivo.
            Diga o motivo da recomendacao em uma ou duas frases curtas. Mostre apenas livros cujos IDs estejam no catalogo.
            Responda exclusivamente com um objeto JSON: {"texto":"resposta ao cliente","livroIds":[123]}.
            "livroIds" deve conter apenas IDs numericos presentes no catalogo fornecido.
            """;

    private final ClienteRepository clienteRepository;
    private final ChatSessaoRepository sessaoRepository;
    private final ChatMensagemRepository mensagemRepository;
    private final ChatFeedbackRepository feedbackRepository;
    private final PreferenciasLiterariasRepository preferenciasRepository;
    private final CategoriaRepository categoriaRepository;
    private final LivroRepository livroRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ChatCompletionClient completionClient;
    private final ObjectMapper objectMapper;

    public ChatService(
            ClienteRepository clienteRepository,
            ChatSessaoRepository sessaoRepository,
            ChatMensagemRepository mensagemRepository,
            ChatFeedbackRepository feedbackRepository,
            PreferenciasLiterariasRepository preferenciasRepository,
            CategoriaRepository categoriaRepository,
            LivroRepository livroRepository,
            ItemPedidoRepository itemPedidoRepository,
            ChatCompletionClient completionClient,
            ObjectMapper objectMapper
    ) {
        this.clienteRepository = clienteRepository;
        this.sessaoRepository = sessaoRepository;
        this.mensagemRepository = mensagemRepository;
        this.feedbackRepository = feedbackRepository;
        this.preferenciasRepository = preferenciasRepository;
        this.categoriaRepository = categoriaRepository;
        this.livroRepository = livroRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.completionClient = completionClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EnviarMensagemResponse enviarMensagem(EnviarMensagemRequest request) {
        Cliente cliente = buscarCliente(request.clienteId());
        ChatSessao sessao;
        if (request.sessaoId() == null) {
            sessao = sessaoRepository.save(new ChatSessao(cliente));
        } else {
            sessao = sessaoRepository.findByIdAndClienteId(request.sessaoId(), cliente.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Sessao nao encontrada"));
        }

        sessao.atualizarAtividade();
        List<ChatMensagem> historico = new ArrayList<>(
                mensagemRepository.findTop20BySessaoIdOrderByCriadoEmDesc(sessao.getId(), PageRequest.of(0, 20)));
        java.util.Collections.reverse(historico);
        mensagemRepository.save(new ChatMensagem(sessao, ChatMensagem.Tipo.CLIENTE, request.mensagem()));

        PreferenciasLiterarias preferencias = preferenciasRepository.findByClienteId(cliente.getId()).orElse(null);
        List<ItemPedido> compras = itemPedidoRepository.buscarHistoricoConcluido(
                cliente.getId(), List.of(StatusPedido.ENTREGUE, StatusPedido.FINALIZADO));
        List<Livro> catalogo = filtrarCatalogo(preferencias, request.mensagem());

        String contextoModelo = contexto(request.mensagem(), historico, preferencias, compras, catalogo);
        String resposta = completionClient.completar(INSTRUCAO, contextoModelo);
        JsonNode resultado;
        try {
            resultado = objectMapper.readTree(resposta);
        } catch (JacksonException exception) {
            throw new ChatProviderException(HttpStatus.BAD_GATEWAY,
                    "A integracao generativa retornou um formato invalido", exception);
        }
        String texto = resultado.path("texto").asText("").trim();
        if (texto.isBlank() || !resultado.path("livroIds").isArray()) {
            throw new ChatProviderException(HttpStatus.BAD_GATEWAY,
                    "A integracao generativa retornou uma resposta incompleta");
        }

        Map<Long, Livro> livrosPermitidos = new HashMap<>();
        catalogo.forEach(livro -> livrosPermitidos.put(livro.getId(), livro));
        List<Livro> recomendados = new ArrayList<>();
        Set<Long> idsJaIncluidos = new HashSet<>();
        resultado.path("livroIds").forEach(id -> {
            if (id.canConvertToLong()) {
                long livroId = id.asLong();
                Livro livro = livrosPermitidos.get(livroId);
                if (livro != null && idsJaIncluidos.add(livroId) && recomendados.size() < 5) {
                    recomendados.add(livro);
                }
            }
        });

        ChatMensagem respostaIa = new ChatMensagem(sessao, ChatMensagem.Tipo.IA, texto);
        respostaIa.setContextoJson(contextoModelo);
        respostaIa.getLivros().addAll(recomendados);
        mensagemRepository.save(respostaIa);

        List<LivroRecomendadoResponse> livrosResponse = recomendados.stream()
                .map(this::mapearLivro)
                .toList();
        return new EnviarMensagemResponse(sessao.getId(), respostaIa.getId(), texto, livrosResponse);
    }

    @Transactional
    public RegistrarFeedbackResponse registrarFeedback(RegistrarFeedbackRequest request) {
        Cliente cliente = clienteRepository.findByIdForUpdate(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado"));
        if (!"S".equals(cliente.getAtivo())) {
            throw new RecursoNaoEncontradoException("Cliente nao encontrado");
        }
        ChatMensagem mensagem = mensagemRepository.findByIdAndSessaoClienteIdAndTipo(
                        request.mensagemId(), cliente.getId(), ChatMensagem.Tipo.IA)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Resposta do chat nao encontrada"));

        ChatFeedback feedback = feedbackRepository.findByClienteIdAndMensagemId(
                cliente.getId(), mensagem.getId()).orElse(null);
        if (feedback == null) {
            feedback = new ChatFeedback(cliente, mensagem, request.avaliacao(), request.comentario());
        } else {
            feedback.atualizar(request.avaliacao(), request.comentario());
        }
        feedback = feedbackRepository.save(feedback);
        return new RegistrarFeedbackResponse(
                feedback.getId(), mensagem.getId(), feedback.getAvaliacao(), feedback.getCriadoEm().toString());
    }

    @Transactional
    public PreferenciasLiterariasResponse buscarPreferencias(UUID clienteId) {
        buscarCliente(clienteId);
        PreferenciasLiterarias preferencias = preferenciasRepository.findByClienteId(clienteId).orElse(null);
        return mapearPreferencias(preferencias);
    }

    @Transactional
    public PreferenciasLiterariasResponse atualizarPreferencias(
            UUID clienteId,
            PreferenciasLiterariasRequest request
    ) {
        Cliente cliente = buscarCliente(clienteId);
        if (request.precoMinimo() != null && request.precoMaximo() != null
                && request.precoMaximo().compareTo(request.precoMinimo()) < 0) {
            throw new ChatRequestException(
                    "precoMaximo deve ser maior ou igual a precoMinimo");
        }

        List<Long> ids = request.categoriaIds().stream().distinct().toList();
        List<Categoria> categorias = categoriaRepository.findAllById(ids);
        if (categorias.size() != ids.size()) {
            throw new ChatRequestException(
                    "Uma ou mais categorias informadas nao existem");
        }
        PreferenciasLiterarias preferencias = preferenciasRepository.findByClienteId(clienteId)
                .orElseGet(() -> new PreferenciasLiterarias(cliente));
        preferencias.atualizar(new HashSet<>(categorias), request.temasDeInteresse(),
                request.precoMinimo(), request.precoMaximo());
        return mapearPreferencias(preferenciasRepository.save(preferencias));
    }

    private Cliente buscarCliente(UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado"));
        if (!"S".equals(cliente.getAtivo())) {
            throw new RecursoNaoEncontradoException("Cliente nao encontrado");
        }
        return cliente;
    }

    private List<Livro> filtrarCatalogo(PreferenciasLiterarias preferencias, String mensagem) {
        List<Livro> livros = livroRepository.findDisponiveisAtivos(PageRequest.of(0, 100));
        BigDecimal limiteMensagem = limitePrecoMaximoMensagem(mensagem);

        if (preferencias == null && limiteMensagem == null) {
            return livros;
        }

        Set<Long> categoriaIds = preferencias == null ? Set.of()
                : preferencias.getCategorias().stream().map(Categoria::getId).collect(java.util.stream.Collectors.toSet());
        return livros.stream()
                .filter(livro -> preferencias == null || preferencias.getPrecoMinimo() == null
                        || livro.getValorVenda().compareTo(preferencias.getPrecoMinimo()) >= 0)
                .filter(livro -> preferencias == null || preferencias.getPrecoMaximo() == null
                        || livro.getValorVenda().compareTo(preferencias.getPrecoMaximo()) <= 0)
                .filter(livro -> limiteMensagem == null
                        || livro.getValorVenda().compareTo(limiteMensagem) <= 0)
                .filter(livro -> categoriaIds.isEmpty()
                        || livro.getCategorias().stream().anyMatch(categoria -> categoriaIds.contains(categoria.getId())))
                .toList();
    }

    private BigDecimal limitePrecoMaximoMensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            return null;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                        "(?iu)(?:ate|até|no\\s+maximo|no\\s+m[áa]ximo|passou\\s+de|mais\\s+barato\\s+que|abaixo\\s+de|menos\\s+que|menor\\s+que)\\s*(?:r\\$\\s*)?(\\d+(?:[,.]\\d{1,2})?)"
                )
                .matcher(mensagem);
        if (!matcher.find()) {
            return null;
        }

        try {
            return new BigDecimal(matcher.group(1).replace(".", "").replace(',', '.'));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String contexto(
            String mensagem,
            List<ChatMensagem> historico,
            PreferenciasLiterarias preferencias,
            List<ItemPedido> compras,
            List<Livro> catalogo
    ) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("mensagemAtual", mensagem);
        dados.put("historico", historico.stream()
                .map(item -> Map.of("autor", item.getTipo().name(), "texto", item.getTexto()))
                .toList());
        Map<String, Object> perfil = new LinkedHashMap<>();
        perfil.put("categoriaIds", preferencias == null ? List.of()
                : preferencias.getCategorias().stream().map(Categoria::getId).sorted().toList());
        perfil.put("categorias", preferencias == null ? List.of()
                : preferencias.getCategorias().stream().map(Categoria::getNome).sorted().toList());
        perfil.put("temasDeInteresse", preferencias == null ? List.of() : preferencias.getTemasDeInteresse());
        perfil.put("precoMinimo", preferencias == null ? null : preferencias.getPrecoMinimo());
        perfil.put("precoMaximo", preferencias == null ? null : preferencias.getPrecoMaximo());
        dados.put("preferencias", perfil);
        dados.put("comprasConcluidas", compras.stream()
                .map(item -> {
                    Map<String, Object> compra = new LinkedHashMap<>();
                    compra.put("titulo", item.getTitulo() == null ? item.getLivro().getTitulo() : item.getTitulo());
                    compra.put("autor", item.getLivro().getAutor().getNome());
                    compra.put("categorias", item.getLivro().getCategorias().stream().map(Categoria::getNome).toList());
                    return compra;
                })
                .toList());
        dados.put("catalogo", catalogo.stream().map(livro -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("livroId", livro.getId());
            item.put("titulo", livro.getTitulo());
            item.put("autor", livro.getAutor().getNome());
            item.put("categorias", livro.getCategorias().stream().map(Categoria::getNome).toList());
            item.put("sinopse", livro.getSinopse().length() > 300
                    ? livro.getSinopse().substring(0, 300) : livro.getSinopse());
            item.put("preco", livro.getValorVenda());
            return item;
        }).toList());
        try {
            return objectMapper.writeValueAsString(dados);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Nao foi possivel preparar o contexto do chat", exception);
        }
    }

    private LivroRecomendadoResponse mapearLivro(Livro livro) {
        return new LivroRecomendadoResponse(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor().getNome(),
                livro.getImagemUrl(),
                livro.getValorVenda(),
                "/livros/" + livro.getId()
        );
    }

    private PreferenciasLiterariasResponse mapearPreferencias(PreferenciasLiterarias preferencias) {
        if (preferencias == null) {
            return new PreferenciasLiterariasResponse(List.of(), List.of(), null, null);
        }
        List<Long> categoriaIds = preferencias.getCategorias().stream()
                .map(Categoria::getId)
                .sorted(Comparator.naturalOrder())
                .toList();
        return new PreferenciasLiterariasResponse(
                categoriaIds,
                List.copyOf(preferencias.getTemasDeInteresse()),
                preferencias.getPrecoMinimo(),
                preferencias.getPrecoMaximo()
        );
    }
}
