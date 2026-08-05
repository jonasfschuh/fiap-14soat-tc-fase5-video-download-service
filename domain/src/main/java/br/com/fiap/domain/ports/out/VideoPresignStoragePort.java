package br.com.fiap.domain.ports.out;

public interface VideoPresignStoragePort {
    boolean objectExists(String s3Key);
    String generatePresignedUrl(String s3Key, long ttlMinutes);
}
