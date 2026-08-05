package br.com.fiap.domain.unit.exceptions;

import br.com.fiap.domain.exceptions.PresignedUrlGenerationException;
import br.com.fiap.domain.exceptions.VideoZipNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionsTest {

    @Test
    void videoZipNotFoundException_containsVideoIdAndUserId() {
        UUID id = UUID.randomUUID();
        VideoZipNotFoundException ex = new VideoZipNotFoundException(id, "user-1");
        assertThat(ex.getMessage()).contains(id.toString());
        assertThat(ex.getMessage()).contains("user-1");
    }

    @Test
    void presignedUrlGenerationException_containsVideoIdAndCause() {
        UUID id = UUID.randomUUID();
        RuntimeException cause = new RuntimeException("S3 error");
        PresignedUrlGenerationException ex = new PresignedUrlGenerationException(id, cause);
        assertThat(ex.getMessage()).contains(id.toString());
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
