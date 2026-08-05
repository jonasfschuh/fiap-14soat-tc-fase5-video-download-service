package br.com.fiap.domain.exceptions;

import java.util.UUID;

public class PresignedUrlGenerationException extends RuntimeException {
    public PresignedUrlGenerationException(UUID videoId, Throwable cause) {
        super("Failed to generate presigned URL for videoId: " + videoId, cause);
    }
}
