package com.livrofacil.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(RecursoNaoEncontradoException exception) {
        return resposta(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraDeNegocio(RegraDeNegocioException exception) {
        return resposta(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException exception) {
        Map<String, String> erros = new HashMap<>();
        for (FieldError erro : exception.getBindingResult().getFieldErrors()) {
            erros.put(erro.getField(), erro.getDefaultMessage());
        }

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("status", HttpStatus.BAD_REQUEST.value());
        corpo.put("mensagem", "Existem dados invalidos na requisicao");
        corpo.put("erros", erros);
        corpo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(corpo);
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("status", status.value());
        corpo.put("mensagem", mensagem);
        corpo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(corpo);
    }
}