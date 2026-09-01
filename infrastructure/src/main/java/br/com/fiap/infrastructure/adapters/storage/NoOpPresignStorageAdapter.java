package br.com.fiap.infrastructure.adapters.storage;

import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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

    @Override
    public boolean verifyPresignedAccess(String s3Key, long expiresEpochSeconds, String signature) {
        log.debug("[NOOP] verifyPresignedAccess called — returning false (test stub)");
        return false;
    }

    @Override
    public InputStream loadObject(String s3Key) {
        log.debug("[NOOP] loadObject called — returning empty stream (test stub)");
        return new ByteArrayInputStream(new byte[0]);
    }
}
