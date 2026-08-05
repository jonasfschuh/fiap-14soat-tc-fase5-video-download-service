package br.com.fiap.infrastructure.configuration;

import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DownloadBeanConfigurationTest {

    @Mock VideoPresignStoragePort storagePort;

    @Test
    void shouldCreateNoOpPresignStoragePort() {
        DownloadBeanConfiguration config = new DownloadBeanConfiguration();
        assertThat(config.noOpPresignStoragePort()).isNotNull();
    }

    @Test
    void shouldCreateNoOpInputPort() {
        DownloadBeanConfiguration config = new DownloadBeanConfiguration();
        ReflectionTestUtils.setField(config, "ttlMinutes", 15L);
        VideoPresignStoragePort noOp = config.noOpPresignStoragePort();
        assertThat(config.noOpGenerateDownloadUrlInputPort(noOp)).isNotNull();
    }
}
