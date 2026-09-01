package br.com.fiap.domain.ports.out;

import java.io.InputStream;

public interface VideoPresignStoragePort {
    boolean objectExists(String s3Key);
    String generatePresignedUrl(String s3Key, long ttlMinutes);

    /** Valida se a assinatura/expiração de um link de download previamente gerado ainda é válida. */
    boolean verifyPresignedAccess(String s3Key, long expiresEpochSeconds, String signature);

    /** Abre um stream de leitura para o objeto identificado pela chave. */
    InputStream loadObject(String s3Key);
}
