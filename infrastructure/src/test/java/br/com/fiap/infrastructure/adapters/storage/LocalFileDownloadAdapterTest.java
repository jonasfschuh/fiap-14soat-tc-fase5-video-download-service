package br.com.fiap.infrastructure.adapters.storage;

import br.com.fiap.domain.exceptions.PresignedUrlGenerationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileDownloadAdapterTest {

    private final Path testRoot = Path.of("target", "test-data", "local-storage");

    @AfterEach
    void cleanUp() throws IOException {
        if (Files.notExists(testRoot)) {
            return;
        }

        try (var paths = Files.walk(testRoot)) {
            paths.sorted((left, right) -> right.getNameCount() - left.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    void objectExists_returnsTrueForUserVideoZip() throws IOException {
        Path zipPath = testRoot.resolve(Path.of("user-1", "550e8400-e29b-41d4-a716-446655440000.zip"));
        Files.createDirectories(zipPath.getParent());
        Files.writeString(zipPath, "zip");

        LocalFileDownloadAdapter adapter = adapterWithPath(testRoot);

        assertThat(adapter.objectExists("outputs/user-1/550e8400-e29b-41d4-a716-446655440000/frames.zip")).isTrue();
    }

    @Test
    void generatePresignedUrl_returnsFileUriForExistingZip() throws IOException {
        Path zipPath = testRoot.resolve(Path.of("user-2", "550e8400-e29b-41d4-a716-446655440000.zip"));
        Files.createDirectories(zipPath.getParent());
        Files.writeString(zipPath, "zip");

        LocalFileDownloadAdapter adapter = adapterWithPath(testRoot);

        String url = adapter.generatePresignedUrl("outputs/user-2/550e8400-e29b-41d4-a716-446655440000/frames.zip", 15L);

        assertThat(url).isEqualTo(zipPath.toAbsolutePath().normalize().toUri().toString());
    }

    @Test
    void generatePresignedUrl_throwsWhenFileDoesNotExist() {
        LocalFileDownloadAdapter adapter = adapterWithPath(testRoot);

        assertThatThrownBy(() -> adapter.generatePresignedUrl("outputs/user-3/550e8400-e29b-41d4-a716-446655440000/frames.zip", 15L))
                .isInstanceOf(PresignedUrlGenerationException.class)
                .hasCauseInstanceOf(java.nio.file.NoSuchFileException.class);
    }

    private LocalFileDownloadAdapter adapterWithPath(Path rootPath) {
        LocalFileDownloadAdapter adapter = new LocalFileDownloadAdapter();
        ReflectionTestUtils.setField(adapter, "processedPath", rootPath.toString());
        return adapter;
    }
}
