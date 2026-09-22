package com.livrofacil.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern CONSTRAINT_PATTERN = Pattern.compile("(?:constraint|key)\\s+[\\\"]([^\\\"]+)[\\\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNQUOTED_CONSTRAINT_PATTERN = Pattern.compile("constraint\\s+([a-zA-Z0-9_.-]+)", Pattern.CASE_INSENSITIVE);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraDeNegocio(
            RegraDeNegocioException exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : exception.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(erro.getField(), mensagemDaValidacao(erro));
        }
        exception.getBindingResult().getGlobalErrors()
            .forEach(erro -> erros.putIfAbsent(campoDoErroGlobal(erro.getDefaultMessage()), erro.getDefaultMessage()));

        return resposta(HttpStatus.BAD_REQUEST, "Existem dados invalidos na requisicao", request, erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        Map<String, String> erros = new LinkedHashMap<>();
        String campo = campoDoJson(exception);
        if (campo != null) {
            erros.put(campo, "Informe um valor valido para este campo");
        }
        String mensagem = campo == null
            ? "O corpo da requisicao e invalido ou possui tipos incompativeis"
            : "O campo " + campo + " possui um valor invalido";
        return resposta(HttpStatus.BAD_REQUEST, mensagem, request, erros);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class
    })
    public ResponseEntity<Map<String, Object>> tratarParametroInvalido(
            Exception exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.BAD_REQUEST, "Um parametro obrigatorio e invalido ou nao foi informado", request, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> tratarMetodoNaoSuportado(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "O metodo HTTP nao e suportado para este endpoint", request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> tratarIntegridade(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        String causa = todasAsCausas(exception);
        String nomeConstraint = nomeDaConstraint(exception, causa);
        Map<String, String> detalhes = new LinkedHashMap<>();
        String id = registrarErro(exception, request);
        if (nomeConstraint != null) {
            detalhes.put("constraint", nomeConstraint);
        }
        detalhes.put("idErro", id);

        String mensagem = mensagemDeIntegridade(detalhes.get("constraint"), causa);
        return resposta(HttpStatus.CONFLICT, mensagem, request, detalhes);
    }

        @ExceptionHandler(EmptyResultDataAccessException.class)
        public ResponseEntity<Map<String, Object>> tratarResultadoAusente(
            EmptyResultDataAccessException exception,
            HttpServletRequest request
        ) {
        return resposta(HttpStatus.NOT_FOUND, "O recurso informado nao foi encontrado", request, Map.of());
        }

        @ExceptionHandler(DataAccessException.class)
        public ResponseEntity<Map<String, Object>> tratarAcessoDados(
            DataAccessException exception,
            HttpServletRequest request
        ) {
        String id = registrarErro(exception, request);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
            "Nao foi possivel acessar os dados. Informe o identificador do erro ao suporte: " + id,
            request, Map.of("idErro", id));
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroInterno(
            Exception exception,
            HttpServletRequest request
    ) {
        String id = registrarErro(exception, request);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocorreu um erro interno no servidor. Informe o identificador do erro ao suporte: " + id,
            request, Map.of("idErro", id));
    }

        @ExceptionHandler({TransactionSystemException.class, JpaSystemException.class})
        public ResponseEntity<Map<String, Object>> tratarFalhaNaTransacao(
            RuntimeException exception,
            HttpServletRequest request
        ) {
        String causa = todasAsCausas(exception).toLowerCase(Locale.ROOT);
        String id = registrarErro(exception, request);
        if (causa.contains("foreign key") || causa.contains("referenced") || causa.contains("constraint")) {
            return resposta(HttpStatus.CONFLICT,
                "Nao foi possivel excluir o registro porque ele esta vinculado a outra operacao.",
                request, Map.of("idErro", id));
        }
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
            "Nao foi possivel concluir a operacao no banco de dados. Informe o identificador do erro ao suporte: " + id,
            request, Map.of("idErro", id));
        }

        private String registrarErro(Exception exception, HttpServletRequest request) {
        String id = UUID.randomUUID().toString();
        LOGGER.error("Erro {} em {} {}: {}", id, request.getMethod(), request.getRequestURI(),
            todasAsCausas(exception), exception);
        return id;
        }

    private String mensagemDaValidacao(FieldError erro) {
        return erro.getDefaultMessage() == null ? "Valor invalido" : erro.getDefaultMessage();
    }

    private String campoDoErroGlobal(String mensagem) {
        if (mensagem == null) return "requisicao";
        String texto = mensagem.toLowerCase(Locale.ROOT);
        if (texto.contains("idade") || texto.contains("nascimento")) return "dataNascimento";
        if (texto.contains("senha")) return "confirmarSenha";
        return "requisicao";
    }

    private String campoDoJson(HttpMessageNotReadableException exception) {
        String mensagem = exception.getMessage();
        if (mensagem == null) return null;
        Matcher quotedField = Pattern.compile("\\[\\\"([^\\\"]+)\\\"\\]").matcher(mensagem);
        if (quotedField.find()) {
            return quotedField.group(1);
        }
        Matcher fieldName = Pattern.compile("property ['\\\"]([^'\\\"]+)['\\\"]").matcher(mensagem);
        return fieldName.find() ? fieldName.group(1) : null;
    }

    private String todasAsCausas(Throwable exception) {
        StringBuilder mensagens = new StringBuilder();
        Throwable causa = exception;
        while (causa != null) {
            if (causa.getMessage() != null) {
                mensagens.append(causa.getMessage()).append(" ");
            }
            causa = causa.getCause();
        }
        return mensagens.toString();
    }

    private String nomeDaConstraint(Throwable exception, String mensagens) {
        Throwable causa = exception;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null) {
                return violation.getConstraintName();
            }
            causa = causa.getCause();
        }

        Matcher quoted = CONSTRAINT_PATTERN.matcher(mensagens);
        if (quoted.find()) {
            return quoted.group(1);
        }

        Matcher unquoted = UNQUOTED_CONSTRAINT_PATTERN.matcher(mensagens);
        if (unquoted.find() && !Set.of("detalhe", "detail", "do", "banco").contains(unquoted.group(1).toLowerCase())) {
            return unquoted.group(1);
        }
        return null;
    }

    private String mensagemDeIntegridade(String constraint, String causa) {
        String texto = causa == null ? "" : causa.toLowerCase(Locale.ROOT);
        if (texto.contains("foreign key") || texto.contains("violates fk") || texto.contains("referenced")) {
            return "Nao e possivel excluir este registro porque ele esta sendo usado por outra operacao.";
        }
        if (texto.contains("duplicate") || texto.contains("unique constraint")) {
            return "Ja existe um registro com os mesmos dados informados.";
        }
        if (constraint != null && !constraint.isBlank()) {
            return "Nao foi possivel concluir a operacao porque os dados violam uma regra de integridade.";
        }
        return "Nao foi possivel concluir a operacao porque os dados violam uma regra de integridade.";
    }

    private ResponseEntity<Map<String, Object>> resposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> erros
    ) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", status.value());
        corpo.put("erro", status.getReasonPhrase());
        corpo.put("mensagem", mensagem);
        corpo.put("erros", erros);
        corpo.put("caminho", request.getRequestURI());
        corpo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(corpo);
    }
}