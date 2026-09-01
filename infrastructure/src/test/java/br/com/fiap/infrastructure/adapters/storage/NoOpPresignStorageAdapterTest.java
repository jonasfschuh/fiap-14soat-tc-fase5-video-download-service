package br.com.fiap.infrastructure.adapters.storage;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpPresignStorageAdapterTest {

    private final NoOpPresignStorageAdapter adapter = new NoOpPresignStorageAdapter();

    @Test
    void objectExists_returnsFalse() {
        assertThat(adapter.objectExists("any/key")).isFalse();
    }

    @Test
    void generatePresignedUrl_returnsStubUrl() {
        String url = adapter.generatePresignedUrl("outputs/user/uuid/uuid_user_frames.zip", 15L);
        assertThat(url).contains("localhost:4566");
        assertThat(url).contains("_frames.zip");
    }

    @Test
    void verifyPresignedAccess_returnsFalse() {
        assertThat(adapter.verifyPresignedAccess("any/key", 0L, "sig")).isFalse();
    }

    @Test
    void loadObject_returnsEmptyStream() throws Exception {
        try (InputStream in = adapter.loadObject("any/key")) {
            assertThat(in.readAllBytes()).isEmpty();
        }
    }
}
