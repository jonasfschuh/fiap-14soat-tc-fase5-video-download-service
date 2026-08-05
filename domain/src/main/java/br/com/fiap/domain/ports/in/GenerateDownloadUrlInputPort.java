package br.com.fiap.domain.ports.in;

import br.com.fiap.domain.model.PresignedUrlResult;

import java.util.UUID;

public interface GenerateDownloadUrlInputPort {
    PresignedUrlResult generateDownloadUrl(UUID videoId, String userId);
}
