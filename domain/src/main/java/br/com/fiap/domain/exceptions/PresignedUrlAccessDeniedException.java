package br.com.fiap.domain.exceptions;

import java.util.UUID;

public class PresignedUrlAccessDeniedException extends RuntimeException {
    public PresignedUrlAccessDeniedException(UUID videoId) {
        super("Presigned download link is invalid or expired for videoId: " + videoId);
    }
}
