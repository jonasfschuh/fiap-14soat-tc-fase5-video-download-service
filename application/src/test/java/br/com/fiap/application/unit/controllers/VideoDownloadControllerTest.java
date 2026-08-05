package br.com.fiap.application.unit.controllers;

import br.com.fiap.application.adapters.VideoDownloadController;
import br.com.fiap.domain.exceptions.VideoZipNotFoundException;
import br.com.fiap.domain.model.PresignedUrlResult;
import br.com.fiap.domain.ports.in.GenerateDownloadUrlInputPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoDownloadControllerTest {

    @Mock GenerateDownloadUrlInputPort generateDownloadUrl;

    @Test
    void shouldReturn200WithPresignedUrl() {
        UUID videoId = UUID.randomUUID();
        PresignedUrlResult result = new PresignedUrlResult(
                "https://s3.example.com/signed?token=abc",
                Instant.now().plusSeconds(900),
                videoId,
                "user-1"
        );
        when(generateDownloadUrl.generateDownloadUrl(videoId, "user-1")).thenReturn(result);

        VideoDownloadController controller = new VideoDownloadController(generateDownloadUrl);
        ResponseEntity<?> response = controller.generateDownloadUrl(videoId, "user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldPropagateVideoZipNotFoundException() {
        UUID videoId = UUID.randomUUID();
        when(generateDownloadUrl.generateDownloadUrl(videoId, "user-1"))
                .thenThrow(new VideoZipNotFoundException(videoId, "user-1"));

        VideoDownloadController controller = new VideoDownloadController(generateDownloadUrl);

        assertThatThrownBy(() -> controller.generateDownloadUrl(videoId, "user-1"))
                .isInstanceOf(VideoZipNotFoundException.class);
    }
}
