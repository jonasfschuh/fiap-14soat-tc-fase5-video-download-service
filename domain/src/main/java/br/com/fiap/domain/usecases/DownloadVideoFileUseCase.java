package br.com.fiap.domain.usecases;

import br.com.fiap.domain.exceptions.PresignedUrlAccessDeniedException;
import br.com.fiap.domain.exceptions.VideoZipNotFoundException;
import br.com.fiap.domain.model.DownloadedFile;
import br.com.fiap.domain.model.VideoStorageKeys;
import br.com.fiap.domain.ports.in.DownloadVideoFileInputPort;
import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DownloadVideoFileUseCase implements DownloadVideoFileInputPort {

    private static final Logger log = LoggerFactory.getLogger(DownloadVideoFileUseCase.class);

    private final VideoPresignStoragePort storagePort;

    public DownloadVideoFileUseCase(VideoPresignStoragePort storagePort) {
        this.storagePort = storagePort;
    }

    @Override
    public DownloadedFile downloadFile(UUID videoId, String userId, long expiresEpochSeconds, String signature) {
        String outputKey = VideoStorageKeys.buildOutputKey(userId, videoId);

        if (!storagePort.verifyPresignedAccess(outputKey, expiresEpochSeconds, signature)) {
            log.warn("[DOWNLOAD] Invalid or expired presigned link for videoId={} userId={}", videoId, userId);
            throw new PresignedUrlAccessDeniedException(videoId);
        }

        if (!storagePort.objectExists(outputKey)) {
            log.warn("[DOWNLOAD] ZIP not found for videoId={} userId={}", videoId, userId);
            throw new VideoZipNotFoundException(videoId, userId);
        }

        return new DownloadedFile(
                storagePort.loadObject(outputKey),
                VideoStorageKeys.buildFileName(userId, videoId)
        );
    }
}
