package br.com.fiap.infrastructure.adapters.storage;

import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpPresignStorageAdapter implements VideoPresignStoragePort {

    private static final Logger log = LoggerFactory.getLogger(NoOpPresignStorageAdapter.class);

    @Override
    public boolean objectExists(String s3Key) {
        log.debug("[NOOP] objectExists called for key={} — returning false (test stub)", s3Key);
        return false;
    }

    @Override
    public String generatePresignedUrl(String s3Key, long ttlMinutes) {
        log.debug("[NOOP] generatePresignedUrl called — returning stub URL");
        return "http://localhost:4566/fiap-video-uploads/" + s3Key + "?X-Amz-Signature=test-stub";
    }
}
