package br.com.fiap.infrastructure.configuration;

import br.com.fiap.domain.ports.in.GenerateDownloadUrlInputPort;
import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import br.com.fiap.domain.usecases.GenerateDownloadUrlUseCase;
import br.com.fiap.infrastructure.adapters.storage.NoOpPresignStorageAdapter;
import br.com.fiap.infrastructure.adapters.storage.S3PresignedUrlAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class DownloadBeanConfiguration {

    @Value("${app.storage.s3.bucket:fiap-video-uploads}")
    private String bucket;

    @Value("${app.presign.ttl-minutes:15}")
    private long ttlMinutes;

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
    public VideoPresignStoragePort s3PresignStoragePort(S3Client s3Client, S3Presigner s3Presigner) {
        return new S3PresignedUrlAdapter(s3Client, s3Presigner, bucket);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3", matchIfMissing = false)
    public GenerateDownloadUrlInputPort generateDownloadUrlInputPort(VideoPresignStoragePort storagePort) {
        return new GenerateDownloadUrlUseCase(storagePort, ttlMinutes);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "local-test", matchIfMissing = true)
    public VideoPresignStoragePort noOpPresignStoragePort() {
        return new NoOpPresignStorageAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "local-test", matchIfMissing = true)
    public GenerateDownloadUrlInputPort noOpGenerateDownloadUrlInputPort(VideoPresignStoragePort storagePort) {
        return new GenerateDownloadUrlUseCase(storagePort, ttlMinutes);
    }
}
