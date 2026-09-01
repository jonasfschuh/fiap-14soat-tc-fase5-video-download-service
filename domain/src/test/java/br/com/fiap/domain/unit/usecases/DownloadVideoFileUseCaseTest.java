package br.com.fiap.domain.unit.usecases;

import br.com.fiap.domain.exceptions.PresignedUrlAccessDeniedException;
import br.com.fiap.domain.exceptions.VideoZipNotFoundException;
import br.com.fiap.domain.model.DownloadedFile;
import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import br.com.fiap.domain.usecases.DownloadVideoFileUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadVideoFileUseCaseTest {

    @Mock
    VideoPresignStoragePort storagePort;

    @Test
    void shouldReturnFileWhenSignatureValidAndZipExists() {
        UUID videoId = UUID.randomUUID();
        String expectedKey = "outputs/user-1/" + videoId + "/" + videoId + "_user-1_frames.zip";
        InputStream stream = new ByteArrayInputStream("data".getBytes());

        when(storagePort.verifyPresignedAccess(expectedKey, 123L, "sig")).thenReturn(true);
        when(storagePort.objectExists(expectedKey)).thenReturn(true);
        when(storagePort.loadObject(expectedKey)).thenReturn(stream);

        DownloadVideoFileUseCase useCase = new DownloadVideoFileUseCase(storagePort);
        DownloadedFile result = useCase.downloadFile(videoId, "user-1", 123L, "sig");

        assertThat(result.content()).isEqualTo(stream);
        assertThat(result.filename()).isEqualTo(videoId + "_user-1_frames.zip");
    }

    @Test
    void shouldThrowAccessDeniedWhenSignatureInvalid() {
        UUID videoId = UUID.randomUUID();
        when(storagePort.verifyPresignedAccess(anyString(), anyLong(), anyString())).thenReturn(false);

        DownloadVideoFileUseCase useCase = new DownloadVideoFileUseCase(storagePort);

        assertThatThrownBy(() -> useCase.downloadFile(videoId, "user-1", 123L, "bad-sig"))
                .isInstanceOf(PresignedUrlAccessDeniedException.class);
    }

    @Test
    void shouldThrowNotFoundWhenZipMissing() {
        UUID videoId = UUID.randomUUID();
        when(storagePort.verifyPresignedAccess(anyString(), anyLong(), anyString())).thenReturn(true);
        when(storagePort.objectExists(anyString())).thenReturn(false);

        DownloadVideoFileUseCase useCase = new DownloadVideoFileUseCase(storagePort);

        assertThatThrownBy(() -> useCase.downloadFile(videoId, "user-1", 123L, "sig"))
                .isInstanceOf(VideoZipNotFoundException.class);
    }
}
