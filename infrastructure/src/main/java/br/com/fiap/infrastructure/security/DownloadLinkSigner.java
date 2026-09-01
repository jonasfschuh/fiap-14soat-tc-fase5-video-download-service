package br.com.fiap.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/** Assina e valida links de download temporários para o storage local (equivalente a um presigned URL de S3). */
@Component
public class DownloadLinkSigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${app.presign.secret:local-dev-insecure-secret-change-me}")
    private String secret;

    public String sign(String storageKey, long expiresEpochSeconds) {
        return hmacSha256Hex(storageKey + "|" + expiresEpochSeconds);
    }

    public boolean isValid(String storageKey, long expiresEpochSeconds, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        if (Instant.now().getEpochSecond() > expiresEpochSeconds) {
            return false;
        }
        String expected = sign(storageKey, expiresEpochSeconds);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String hmacSha256Hex(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to sign download link", e);
        }
    }
}
