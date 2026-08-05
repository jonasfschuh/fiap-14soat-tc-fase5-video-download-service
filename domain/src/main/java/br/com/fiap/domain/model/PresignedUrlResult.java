package br.com.fiap.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PresignedUrlResult(
        String url,
        Instant expiresAt,
        UUID videoId,
        String userId
) {}
