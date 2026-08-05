package br.com.fiap.infrastructure.adapters.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpPresignStorageAdapterTest {

    private final NoOpPresignStorageAdapter adapter = new NoOpPresignStorageAdapter();

    @Test
    void objectExists_returnsFalse() {
        assertThat(adapter.objectExists("any/key")).isFalse();
    }

    @Test
    void generatePresignedUrl_returnsStubUrl() {
        String url = adapter.generatePresignedUrl("outputs/user/uuid/frames.zip", 15L);
        assertThat(url).contains("localhost:4566");
        assertThat(url).contains("frames.zip");
    }
}
