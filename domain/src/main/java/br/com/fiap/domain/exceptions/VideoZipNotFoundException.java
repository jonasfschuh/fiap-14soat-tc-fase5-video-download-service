package br.com.fiap.domain.exceptions;

import java.util.UUID;

public class VideoZipNotFoundException extends RuntimeException {
    public VideoZipNotFoundException(UUID videoId, String userId) {
        super("ZIP not found in storage for videoId: " + videoId
              + " and userId: " + userId
              + ". The video may still be processing, failed, or does not belong to this user.");
    }
}
