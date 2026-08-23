package br.com.fiap.domain.usecases;

import br.com.fiap.domain.exceptions.VideoZipNotFoundException;
import br.com.fiap.domain.model.PresignedUrlResult;
import br.com.fiap.domain.ports.in.GenerateDownloadUrlInputPort;
import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class GenerateDownloadUrlUseCase implements GenerateDownloadUrlInputPort {

    private static final Logger log = LoggerFactory.getLogger(GenerateDownloadUrlUseCase.class);

    private final VideoPresignStoragePort storagePort;
    private final long ttlMinutes;

    public GenerateDownloadUrlUseCase(VideoPresignStoragePort storagePort, long ttlMinutes) {
        this.storagePort = storagePort;
        this.ttlMinutes = ttlMinutes;
    }

    @Override
    public PresignedUrlResult generateDownloadUrl(UUID videoId, String userId) {
        String outputKey = buildOutputKey(userId, videoId);

        log.info("[DOWNLOAD] Checking existence of key={} for videoId={} userId={}", outputKey, videoId, userId);

        if (!storagePort.objectExists(outputKey)) {
            log.warn("[DOWNLOAD] ZIP not found for videoId={} userId={}", videoId, userId);
            throw new VideoZipNotFoundException(videoId, userId);
        }

        String presignedUrl = storagePort.generatePresignedUrl(outputKey, ttlMinutes);
        Instant expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES);

        log.info("[DOWNLOAD] Presigned URL generated for videoId={} ttlMinutes={}", videoId, ttlMinutes);

        return new PresignedUrlResult(presignedUrl, expiresAt, videoId, userId);
    }

    private String buildOutputKey(String userId, UUID videoId) {
        return "outputs/" + userId + "/" + videoId + "/" + videoId + "_" + userId + "_frames.zip";
    }
}
