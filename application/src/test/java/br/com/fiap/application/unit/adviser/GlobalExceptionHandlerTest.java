package br.com.fiap.application.unit.adviser;

import br.com.fiap.application.adviser.GlobalExceptionHandler;
import br.com.fiap.domain.exceptions.PresignedUrlGenerationException;
import br.com.fiap.domain.exceptions.VideoZipNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn404ForZipNotFound() {
        assertThat(handler.handleNotFound(new VideoZipNotFoundException(UUID.randomUUID(), "u"))
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn500ForPresignError() {
        assertThat(handler.handlePresignError(
                new PresignedUrlGenerationException(UUID.randomUUID(), new RuntimeException()))
                .getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturn400ForTypeMismatch() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "bad-value", UUID.class, "videoId", null, new IllegalArgumentException("invalid uuid"));
        assertThat(handler.handleTypeMismatch(ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn500ForGeneric() {
        assertThat(handler.handleGeneric(new RuntimeException("err"))
                .getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}