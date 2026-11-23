package com.livrofacil.modulos.chat.service;

import com.livrofacil.modulos.chat.entity.ChatFeedback;
import com.livrofacil.modulos.chat.entity.ChatMensagem;
import com.livrofacil.modulos.chat.repository.ChatFeedbackRepository;
import com.livrofacil.modulos.compra.entity.ItemPedido;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.repository.ItemPedidoRepository;
import com.livrofacil.modulos.livro.entity.Categoria;
import com.livrofacil.modulos.livro.entity.Livro;
import com.livrofacil.modulos.livro.repository.LivroRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DatasetExportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetExportService.class);
    private static final int TOTAL_LINHAS_TREINO = 100;
    private static final int LINHAS_GUARDRAIL_TREINO = 15;
    private static final int TOTAL_LINHAS_VALIDACAO = 20;
    private static final int LINHAS_GUARDRAIL_VALIDACAO = 3;
    private static final int MAX_FEEDBACKS_TREINO = 68;
    private static final int EXEMPLOS_POSITIVOS_TREINO = TOTAL_LINHAS_TREINO - LINHAS_GUARDRAIL_TREINO;
    private static final int EXEMPLOS_POSITIVOS_VALIDACAO = TOTAL_LINHAS_VALIDACAO - LINHAS_GUARDRAIL_VALIDACAO;
    private static final String GUARDRAIL = """
            Posso ajudar apenas com livros, autores, sinopses, catálogo e recomendações da LivroFácil.
            Não posso ajudar com este assunto. Posso indicar uma obra do nosso catálogo ou esclarecer uma dúvida literária.
            """.strip();

    private final ChatFeedbackRepository feedbackRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final LivroRepository livroRepository;
    private final ObjectMapper objectMapper;

    public DatasetExportService(
            ChatFeedbackRepository feedbackRepository,
            ItemPedidoRepository itemPedidoRepository,
            LivroRepository livroRepository,
            ObjectMapper objectMapper
    ) {
        this.feedbackRepository = feedbackRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.livroRepository = livroRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public String exportarJsonl() {
        return exportarTreinoJsonl();
    }

    @Transactional(readOnly = true)
    public String exportarTreinoJsonl() {
        List<Livro> catalogo = buscarCatalogo();
        List<Map<String, Object>> linhas = new ArrayList<>();
        adicionarExemplosReais(linhas, catalogo);
        adicionarExemplosDePedidos(linhas, catalogo);
        adicionarExemplosDoCatalogo(
                linhas, catalogo, EXEMPLOS_POSITIVOS_TREINO - linhas.size(), linhas.size(), false);
        adicionarGuardrails(linhas, catalogo, 0, LINHAS_GUARDRAIL_TREINO);
        validarQuantidade(linhas, TOTAL_LINHAS_TREINO);
        return serializarJsonl(linhas);
    }

    @Transactional(readOnly = true)
    public String exportarValidacaoJsonl() {
        List<Livro> catalogo = buscarCatalogo();
        List<Map<String, Object>> linhas = new ArrayList<>();
        adicionarExemplosDoCatalogo(linhas, catalogo, EXEMPLOS_POSITIVOS_VALIDACAO, 101, true);
        adicionarGuardrails(linhas, catalogo, LINHAS_GUARDRAIL_TREINO, LINHAS_GUARDRAIL_VALIDACAO);
        validarQuantidade(linhas, TOTAL_LINHAS_VALIDACAO);
        return serializarJsonl(linhas);
    }

    private List<Livro> buscarCatalogo() {
        List<Livro> catalogo = livroRepository.findDisponiveisAtivos(PageRequest.of(0, 100));
        if (catalogo.isEmpty()) {
            throw new IllegalStateException("Não há livros disponíveis para gerar o dataset");
        }
        return catalogo;
    }

    private void validarQuantidade(List<Map<String, Object>> linhas, int quantidadeEsperada) {
        if (linhas.size() != quantidadeEsperada) {
            throw new IllegalStateException(
                    "O dataset deveria conter " + quantidadeEsperada + " exemplos, mas contém " + linhas.size());
        }
    }

    private String serializarJsonl(List<Map<String, Object>> linhas) {
        StringBuilder saida = new StringBuilder();
        for (Map<String, Object> linha : linhas) {
            try {
                if (!saida.isEmpty()) {
                    saida.append('\n');
                }
                saida.append(objectMapper.writeValueAsString(linha));
            } catch (JacksonException exception) {
                throw new IllegalStateException("Não foi possível gerar o dataset JSONL", exception);
            }
        }
        return saida.toString();
    }

    private void adicionarExemplosReais(List<Map<String, Object>> linhas, List<Livro> catalogo) {
        var idsCatalogo = catalogo.stream().map(Livro::getId).collect(java.util.stream.Collectors.toSet());
        int ignorados = 0;
        for (ChatFeedback feedback : feedbackRepository.findByAvaliacaoOrderByCriadoEmDesc(ChatFeedback.Avaliacao.UTIL)) {
            if (linhas.size() >= MAX_FEEDBACKS_TREINO) {
                break;
            }
            ChatMensagem resposta = feedback.getMensagem();
            if (resposta == null || resposta.getTexto().isBlank()) {
                continue;
            }
            String contexto = resposta.getContextoJson();
            if (contexto == null || contexto.isBlank()) {
                ignorados++;
                continue;
            }
            JsonNode dadosContexto = lerContextoSalvo(contexto);
            if (!dadosContexto.path("catalogo").isArray()
                    || !dadosContexto.path("mensagemAtual").isTextual()) {
                throw new IllegalStateException("Um contexto salvo de chat não segue o formato esperado");
            }
            var idsContexto = new java.util.HashSet<Long>();
            dadosContexto.path("catalogo").forEach(livro -> {
                if (livro.path("livroId").canConvertToLong()) {
                    idsContexto.add(livro.path("livroId").asLong());
                }
            });
            boolean respostaForaDoContexto = resposta.getLivros().stream()
                    .anyMatch(livro -> !idsContexto.contains(livro.getId()) || !idsCatalogo.contains(livro.getId()));
            if (respostaForaDoContexto) {
                ignorados++;
                continue;
            }
            linhas.add(conversaComContexto(contexto, respostaJson(resposta.getTexto(), resposta.getLivros())));
        }
        if (ignorados > 0) {
            LOGGER.info("Foram ignorados {} feedbacks UTIL sem contexto ou com livros fora do catálogo", ignorados);
        }
    }

    private JsonNode lerContextoSalvo(String contexto) {
        try {
            JsonNode dados = objectMapper.readTree(contexto);
            if (dados == null || !dados.isObject()) {
                throw new IllegalStateException("Um contexto salvo de chat não é um objeto JSON");
            }
            return dados;
        } catch (JacksonException exception) {
            throw new IllegalStateException("Um contexto salvo de chat contém JSON inválido", exception);
        }
    }

    private void adicionarExemplosDePedidos(List<Map<String, Object>> linhas, List<Livro> catalogo) {
        for (ChatFeedback feedback : feedbackRepository.findByAvaliacaoOrderByCriadoEmDesc(ChatFeedback.Avaliacao.UTIL)) {
            if (linhas.size() >= EXEMPLOS_POSITIVOS_TREINO) {
                break;
            }
            UUID clienteId = feedback.getCliente().getId();
            List<ItemPedido> compras = itemPedidoRepository.buscarHistoricoConcluido(
                    clienteId, List.of(StatusPedido.ENTREGUE, StatusPedido.FINALIZADO));
            if (compras.isEmpty()) {
                continue;
            }
            Livro comprado = compras.get(0).getLivro();
            if (comprado.getCategorias().isEmpty()) {
                continue;
            }
            Livro sugestao = catalogo.stream()
                    .filter(livro -> livro.getId().equals(comprado.getId()) == false)
                    .filter(livro -> livro.getCategorias().stream()
                            .anyMatch(categoria -> comprado.getCategorias().contains(categoria)))
                    .findFirst()
                    .orElse(null);
            if (sugestao == null) {
                continue;
            }
            String prompt = "Comprei \"" + nomeLivro(comprado) + "\" e gosto de "
                    + categoriaPrincipal(comprado) + ". Qual livro do catálogo combina comigo?";
            String texto = "Como você gostou de \"" + nomeLivro(comprado)
                    + "\", recomendo \"" + sugestao.getTitulo() + "\" de "
                    + sugestao.getAutor().getNome() + ", que também se conecta ao gênero "
                    + categoriaPrincipal(sugestao) + ".";
            linhas.add(conversa(prompt, respostaJson(texto, List.of(sugestao)), catalogo, List.of(comprado)));
        }
    }

    private void adicionarGuardrails(
            List<Map<String, Object>> linhas,
            List<Livro> catalogo,
            int inicio,
            int quantidade
    ) {
        String[] perguntas = {
                "Me ajude com código Python.",
                "Qual é a previsão do tempo?",
                "Escreva um currículo para mim.",
                "Resolva esta equação matemática.",
                "Faça uma cirurgia jurídica para meu processo.",
                "Qual o melhor modelo de carro?",
                "Me dê dicas de investimento em ações.",
                "Reescreva este contrato.",
                "Conte uma piada.",
                "Qual é minha localização atual?",
                "Faça uma receita de bolo.",
                "Explique física quântica.",
                "Qual diagnóstico devo seguir para estes sintomas?",
                "Planeje minhas férias na praia.",
                "Como conserto o roteador de internet?",
                "Quais são as notícias de hoje?",
                "Escreva um programa em Java.",
                "Traduza este texto para inglês."
        };
        for (int i = inicio; i < inicio + quantidade; i++) {
            linhas.add(conversa(perguntas[i], respostaJson(GUARDRAIL, List.of()), catalogo, List.of()));
        }
    }

    private void adicionarExemplosDoCatalogo(
            List<Map<String, Object>> linhas,
            List<Livro> catalogo,
            int quantidade,
            int indiceInicial,
            boolean validacao
    ) {
        for (int posicao = 0; posicao < quantidade; posicao++) {
            int indice = indiceInicial + posicao;
            Livro livro = catalogo.get(indice % catalogo.size());
            Livro alternativa = catalogo.get((indice + 1) % catalogo.size());
            if (alternativa.getId().equals(livro.getId())) {
                alternativa = catalogo.get((indice + 2) % catalogo.size());
            }
            String categoria = categoriaPrincipal(livro);
            int tipo = indice % 4;
            if (alternativa.getId().equals(livro.getId()) && tipo == 3) {
                tipo = 0;
            }
            String prompt;
            String texto;
            List<Livro> recomendados;
            if (tipo == 0) {
                prompt = validacao
                        ? "Procuro uma leitura de " + categoria + "; qual obra disponível você indicaria?"
                        : "Gosto de " + categoria + ". Qual livro do catálogo você recomenda?";
                texto = "Para quem gosta de " + categoria + ", recomendo \"" + livro.getTitulo()
                        + "\" de " + livro.getAutor().getNome() + ".";
                recomendados = List.of(livro);
            } else if (tipo == 1) {
                prompt = validacao
                        ? "Do que trata o livro \"" + livro.getTitulo() + "\"?"
                        : "Qual é a sinopse de \"" + livro.getTitulo() + "\"?";
                texto = livro.getSinopse();
                recomendados = List.of(livro);
            } else if (tipo == 2) {
                prompt = validacao
                        ? "Qual pessoa é autora de \"" + livro.getTitulo() + "\"?"
                        : "Quem escreveu \"" + livro.getTitulo() + "\"?";
                texto = "\"" + livro.getTitulo() + "\" foi escrito por " + livro.getAutor().getNome()
                        + ", disponível no catálogo da LivroFácil.";
                recomendados = List.of(livro);
            } else {
                prompt = validacao
                        ? "Terminei \"" + livro.getTitulo() + "\". Qual outra leitura do catálogo pode me agradar?"
                        : "Já li \"" + livro.getTitulo() + "\" e quero outra opção parecida.";
                texto = "Você pode experimentar \"" + alternativa.getTitulo() + "\" de "
                        + alternativa.getAutor().getNome() + ", também presente no catálogo.";
                recomendados = List.of(alternativa);
            }
            linhas.add(conversa(prompt, respostaJson(texto, recomendados), catalogo, List.of()));
        }
    }

    private Map<String, Object> conversa(
            String mensagem,
            String respostaJson,
            List<Livro> catalogo,
            List<Livro> compras
    ) {
        return conversaComContexto(contextoJson(mensagem, catalogo, compras), respostaJson);
    }

    private Map<String, Object> conversaComContexto(String contexto, String respostaJson) {
        Map<String, Object> instrucao = new LinkedHashMap<>();
        instrucao.put("parts", List.of(Map.of("text", ChatService.INSTRUCAO)));

        Map<String, Object> exemplo = new LinkedHashMap<>();
        exemplo.put("systemInstruction", instrucao);
        exemplo.put("contents", List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", contexto))),
                Map.of("role", "model", "parts", List.of(Map.of("text", respostaJson)))
        ));
        return exemplo;
    }

    private String contextoJson(String mensagem, List<Livro> catalogo, List<Livro> compras) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("mensagemAtual", mensagem);
        dados.put("historico", List.of());

        Map<String, Object> preferencias = new LinkedHashMap<>();
        preferencias.put("categoriaIds", List.of());
        preferencias.put("categorias", List.of());
        preferencias.put("temasDeInteresse", List.of());
        preferencias.put("precoMinimo", null);
        preferencias.put("precoMaximo", null);
        dados.put("preferencias", preferencias);
        dados.put("comprasConcluidas", compras.stream().map(this::mapearCompra).toList());
        dados.put("catalogo", catalogo.stream().map(this::mapearLivro).toList());
        return serializar(dados);
    }

    private Map<String, Object> mapearCompra(Livro livro) {
        Map<String, Object> compra = new LinkedHashMap<>();
        compra.put("titulo", livro.getTitulo());
        compra.put("autor", livro.getAutor().getNome());
        compra.put("categorias", livro.getCategorias().stream().map(Categoria::getNome).toList());
        return compra;
    }

    private Map<String, Object> mapearLivro(Livro livro) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("livroId", livro.getId());
        item.put("titulo", livro.getTitulo());
        item.put("autor", livro.getAutor().getNome());
        item.put("categorias", livro.getCategorias().stream().map(Categoria::getNome).toList());
        String sinopse = livro.getSinopse();
        item.put("sinopse", sinopse.length() > 300 ? sinopse.substring(0, 300) : sinopse);
        item.put("preco", livro.getValorVenda());
        return item;
    }

    private String respostaJson(String texto, List<Livro> livros) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("texto", texto);
        resposta.put("livroIds", livros.stream().map(Livro::getId).toList());
        return serializar(resposta);
    }

    private String nomeLivro(Livro livro) {
        return livro.getTitulo();
    }

    private String categoriaPrincipal(Livro livro) {
        return livro.getCategorias().stream()
                .map(Categoria::getNome)
                .sorted()
                .findFirst()
                .orElse("literatura");
    }

    private String serializar(Object valor) {
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Não foi possível serializar um exemplo do dataset", exception);
        }
    }
}
