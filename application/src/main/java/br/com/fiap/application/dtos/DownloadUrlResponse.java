package br.com.fiap.application.dtos;

import java.time.Instant;
import java.util.UUID;

public record DownloadUrlResponse(
        String url,
        Instant expiresAt,
        UUID videoId,
        String userId
) {}
