package br.com.fiap.domain.unit.usecases;

import br.com.fiap.domain.exceptions.VideoZipNotFoundException;
import br.com.fiap.domain.model.PresignedUrlResult;
import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import br.com.fiap.domain.usecases.GenerateDownloadUrlUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateDownloadUrlUseCaseTest {

    @Mock
    VideoPresignStoragePort storagePort;

    @Test
    void shouldGeneratePresignedUrlWhenZipExists() {
        UUID videoId = UUID.randomUUID();
        String expectedKey = "outputs/user-1/" + videoId + "/frames.zip";

        when(storagePort.objectExists(expectedKey)).thenReturn(true);
        when(storagePort.generatePresignedUrl(eq(expectedKey), eq(15L)))
                .thenReturn("https://s3.example.com/signed-url?X-Amz-Signature=abc");

        GenerateDownloadUrlUseCase useCase = new GenerateDownloadUrlUseCase(storagePort, 15L);
        PresignedUrlResult result = useCase.generateDownloadUrl(videoId, "user-1");

        assertThat(result.url()).contains("signed-url");
        assertThat(result.videoId()).isEqualTo(videoId);
        assertThat(result.userId()).isEqualTo("user-1");
        assertThat(result.expiresAt()).isNotNull();
        verify(storagePort).objectExists(expectedKey);
        verify(storagePort).generatePresignedUrl(expectedKey, 15L);
    }

    @Test
    void shouldThrowNotFoundWhenZipDoesNotExist() {
        UUID videoId = UUID.randomUUID();
        when(storagePort.objectExists(anyString())).thenReturn(false);

        GenerateDownloadUrlUseCase useCase = new GenerateDownloadUrlUseCase(storagePort, 15L);

        assertThatThrownBy(() -> useCase.generateDownloadUrl(videoId, "user-1"))
                .isInstanceOf(VideoZipNotFoundException.class)
                .hasMessageContaining(videoId.toString());

        verify(storagePort, never()).generatePresignedUrl(anyString(), anyLong());
    }

    @Test
    void shouldBuildCorrectS3KeyFromUserIdAndVideoId() {
        UUID videoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String expectedKey = "outputs/user-abc/550e8400-e29b-41d4-a716-446655440000/frames.zip";

        when(storagePort.objectExists(expectedKey)).thenReturn(true);
        when(storagePort.generatePresignedUrl(eq(expectedKey), anyLong())).thenReturn("https://url");

        GenerateDownloadUrlUseCase useCase = new GenerateDownloadUrlUseCase(storagePort, 15L);
        useCase.generateDownloadUrl(videoId, "user-abc");

        verify(storagePort).objectExists(expectedKey);
    }
}
