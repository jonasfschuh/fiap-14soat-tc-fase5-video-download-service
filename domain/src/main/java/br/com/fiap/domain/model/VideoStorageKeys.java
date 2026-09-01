package br.com.fiap.domain.model;

import java.util.UUID;

/** Constrói as chaves de storage e nomes de arquivo usados no fluxo de download. */
public final class VideoStorageKeys {

    private VideoStorageKeys() {
    }

    public static String buildOutputKey(String userId, UUID videoId) {
        return "outputs/" + userId + "/" + videoId + "/" + buildFileName(userId, videoId);
    }

    public static String buildFileName(String userId, UUID videoId) {
        return videoId + "_" + userId + "_frames.zip";
    }
}
