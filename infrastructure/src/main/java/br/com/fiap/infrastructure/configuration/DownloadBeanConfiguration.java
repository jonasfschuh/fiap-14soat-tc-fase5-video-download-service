package br.com.fiap.infrastructure.configuration;

import br.com.fiap.domain.ports.in.GenerateDownloadUrlInputPort;
import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import br.com.fiap.domain.usecases.GenerateDownloadUrlUseCase;
import br.com.fiap.infrastructure.adapters.storage.NoOpPresignStorageAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownloadBeanConfiguration {

    @Value("${app.presign.ttl-minutes:15}")
    private long ttlMinutes;

    @Bean
    public GenerateDownloadUrlInputPort generateDownloadUrlInputPort(VideoPresignStoragePort storagePort) {
        return new GenerateDownloadUrlUseCase(storagePort, ttlMinutes);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "local-test")
    public VideoPresignStoragePort noOpPresignStoragePort() {
        return new NoOpPresignStorageAdapter();
    }
}
