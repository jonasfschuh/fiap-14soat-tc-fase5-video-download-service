package br.com.fiap.infrastructure.adapters.storage;

import br.com.fiap.domain.exceptions.PresignedUrlGenerationException;
import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

public class S3PresignedUrlAdapter implements VideoPresignStoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3PresignedUrlAdapter.class);
    private static final UUID EMPTY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3PresignedUrlAdapter(S3Client s3Client, S3Presigner s3Presigner, String bucket) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    @Override
    public boolean objectExists(String s3Key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(s3Key).build());
            log.debug("[S3] Object exists: s3://{}/{}", bucket, s3Key);
            return true;
        } catch (NoSuchKeyException e) {
            log.debug("[S3] Object not found: s3://{}/{}", bucket, s3Key);
            return false;
        } catch (AwsServiceException e) {
            if (e.statusCode() == 404) {
                log.debug("[S3] Object not found: s3://{}/{}", bucket, s3Key);
                return false;
            }
            log.error("[S3] Error checking existence of key={}: {}", s3Key, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[S3] Error checking existence of key={}: {}", s3Key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generatePresignedUrl(String s3Key, long ttlMinutes) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(ttlMinutes))
                    .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(s3Key).build())
                    .build();

            String url = s3Presigner.presignGetObject(presignRequest).url().toString();
            log.info("[S3] Presigned URL generated for key={} ttl={}min", s3Key, ttlMinutes);
            return url;
        } catch (Exception e) {
            throw new PresignedUrlGenerationException(extractVideoId(s3Key), e);
        }
    }

    private UUID extractVideoId(String s3Key) {
        try {
            String[] parts = s3Key.split("/");
            return UUID.fromString(parts[2]);
        } catch (Exception e) {
            return EMPTY_UUID;
        }
    }
}
