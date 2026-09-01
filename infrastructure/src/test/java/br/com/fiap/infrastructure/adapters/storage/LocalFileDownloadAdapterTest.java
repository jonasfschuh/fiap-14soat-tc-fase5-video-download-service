package br.com.fiap.infrastructure.adapters.storage;

import br.com.fiap.domain.exceptions.PresignedUrlGenerationException;
import br.com.fiap.infrastructure.security.DownloadLinkSigner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileDownloadAdapterTest {

    private final Path testRoot = Path.of("target", "test-data", "local-storage");

    @BeforeEach
    void setUpRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/videos/x/download");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8085);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDownRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

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

        assertThat(adapter.objectExists("outputs/user-1/550e8400-e29b-41d4-a716-446655440000/550e8400-e29b-41d4-a716-446655440000_user-1_frames.zip")).isTrue();
    }

    @Test
    void generatePresignedUrl_returnsHttpDownloadLinkForExistingZip() throws IOException {
        Path zipPath = testRoot.resolve(Path.of("user-2", "550e8400-e29b-41d4-a716-446655440000.zip"));
        Files.createDirectories(zipPath.getParent());
        Files.writeString(zipPath, "zip");

        LocalFileDownloadAdapter adapter = adapterWithPath(testRoot);

        String url = adapter.generatePresignedUrl("outputs/user-2/550e8400-e29b-41d4-a716-446655440000/550e8400-e29b-41d4-a716-446655440000_user-2_frames.zip", 15L);

        assertThat(url).startsWith("http://localhost:8085/api/videos/550e8400-e29b-41d4-a716-446655440000/download/file");
        assertThat(url).contains("userId=user-2");
        assertThat(url).contains("expires=");
        assertThat(url).contains("sig=");
    }

    @Test
    void generatePresignedUrl_throwsWhenFileDoesNotExist() {
        LocalFileDownloadAdapter adapter = adapterWithPath(testRoot);

        assertThatThrownBy(() -> adapter.generatePresignedUrl("outputs/user-3/550e8400-e29b-41d4-a716-446655440000/550e8400-e29b-41d4-a716-446655440000_user-3_frames.zip", 15L))
                .isInstanceOf(PresignedUrlGenerationException.class)
                .hasCauseInstanceOf(java.nio.file.NoSuchFileException.class);
    }

    @Test
    void verifyPresignedAccess_roundTripsWithGeneratedSignature() throws IOException {
        Path zipPath = testRoot.resolve(Path.of("user-4", "550e8400-e29b-41d4-a716-446655440000.zip"));
        Files.createDirectories(zipPath.getParent());
        Files.writeString(zipPath, "zip");

        LocalFileDownloadAdapter adapter = adapterWithPath(testRoot);
        String storageKey = "outputs/user-4/550e8400-e29b-41d4-a716-446655440000/550e8400-e29b-41d4-a716-446655440000_user-4_frames.zip";

        String url = adapter.generatePresignedUrl(storageKey, 15L);
        String expires = extractQueryParam(url, "expires");
        String sig = extractQueryParam(url, "sig");

        assertThat(adapter.verifyPresignedAccess(storageKey, Long.parseLong(expires), sig)).isTrue();
        assertThat(adapter.verifyPresignedAccess(storageKey, Long.parseLong(expires), "invalid")).isFalse();
    }

    @Test
    void loadObject_returnsFileContent() throws IOException {
        Path zipPath = testRoot.resolve(Path.of("user-5", "550e8400-e29b-41d4-a716-446655440000.zip"));
        Files.createDirectories(zipPath.getParent());
        Files.writeString(zipPath, "zip-content");

        LocalFileDownloadAdapter adapter = adapterWithPath(testRoot);
        try (InputStream in = adapter.loadObject("outputs/user-5/550e8400-e29b-41d4-a716-446655440000/550e8400-e29b-41d4-a716-446655440000_user-5_frames.zip")) {
            assertThat(new String(in.readAllBytes())).isEqualTo("zip-content");
        }
    }

    private LocalFileDownloadAdapter adapterWithPath(Path rootPath) {
        DownloadLinkSigner signer = new DownloadLinkSigner();
        ReflectionTestUtils.setField(signer, "secret", "test-secret");
        LocalFileDownloadAdapter adapter = new LocalFileDownloadAdapter(signer);
        ReflectionTestUtils.setField(adapter, "processedPath", rootPath.toString());
        return adapter;
    }

    private String extractQueryParam(String url, String name) {
        String marker = name + "=";
        int start = url.indexOf(marker) + marker.length();
        int end = url.indexOf('&', start);
        return end == -1 ? url.substring(start) : url.substring(start, end);
    }
}
