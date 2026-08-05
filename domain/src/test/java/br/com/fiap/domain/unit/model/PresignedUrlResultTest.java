package br.com.fiap.domain.unit.model;

import br.com.fiap.domain.model.PresignedUrlResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PresignedUrlResultTest {

    @Test
    void shouldCreateWithAllFields() {
        UUID id = UUID.randomUUID();
        Instant expires = Instant.now().plusSeconds(900);
        PresignedUrlResult result = new PresignedUrlResult("https://s3.example.com/signed", expires, id, "user-1");

        assertThat(result.url()).isEqualTo("https://s3.example.com/signed");
        assertThat(result.expiresAt()).isEqualTo(expires);
        assertThat(result.videoId()).isEqualTo(id);
        assertThat(result.userId()).isEqualTo("user-1");
    }
}
