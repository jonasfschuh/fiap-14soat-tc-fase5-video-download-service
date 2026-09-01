package br.com.fiap.domain.ports.in;

import br.com.fiap.domain.model.DownloadedFile;

import java.util.UUID;

public interface DownloadVideoFileInputPort {
    DownloadedFile downloadFile(UUID videoId, String userId, long expiresEpochSeconds, String signature);
}
