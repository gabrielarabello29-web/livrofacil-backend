package com.livrofacil.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = request();

    @Test void deveTratarNaoEncontrado() { var response = handler.tratarNaoEncontrado(new RecursoNaoEncontradoException("nao"), request); assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()); }
    @Test void deveTratarRegra() { var response = handler.tratarRegraDeNegocio(new RegraDeNegocioException("regra"), request); assertEquals(HttpStatus.CONFLICT, response.getStatusCode()); }
    @Test void deveTratarJsonInvalidoSemCampo() { var response = handler.tratarJsonInvalido(new HttpMessageNotReadableException("invalido", null), request); assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()); }
    @Test void deveTratarJsonInvalidoComCampo() { var response = handler.tratarJsonInvalido(new HttpMessageNotReadableException("[\"idade\"]", null), request); assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()); }
    @Test void deveTratarMetodoNaoSuportado() { var exception = new HttpRequestMethodNotSupportedException("TRACE"); assertEquals(HttpStatus.METHOD_NOT_ALLOWED, handler.tratarMetodoNaoSuportado(exception, request).getStatusCode()); }
    @Test void deveTratarAcessoDados() { DataAccessException exception = new DataAccessException("falha") {}; assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.tratarAcessoDados(exception, request).getStatusCode()); }
    @Test void deveTratarIntegridadeDuplicada() { var exception = new DataIntegrityViolationException("duplicate key constraint \"uk_email\""); var response = handler.tratarIntegridade(exception, request); assertEquals(HttpStatus.CONFLICT, response.getStatusCode()); assertEquals("uk_email", ((Map<?, ?>) response.getBody().get("erros")).get("constraint")); }
    @Test void deveTratarErroInterno() { assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.tratarErroInterno(new RuntimeException("erro"), request).getStatusCode()); }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/teste");
        return request;
    }
}
