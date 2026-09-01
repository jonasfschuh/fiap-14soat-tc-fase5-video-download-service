package br.com.fiap.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadLinkSignerTest {

    private final DownloadLinkSigner signer = newSigner();

    @Test
    void isValid_returnsTrueForMatchingSignatureBeforeExpiry() {
        long expires = Instant.now().plusSeconds(60).getEpochSecond();
        String sig = signer.sign("outputs/user-1/key.zip", expires);

        assertThat(signer.isValid("outputs/user-1/key.zip", expires, sig)).isTrue();
    }

    @Test
    void isValid_returnsFalseWhenExpired() {
        long expires = Instant.now().minusSeconds(60).getEpochSecond();
        String sig = signer.sign("outputs/user-1/key.zip", expires);

        assertThat(signer.isValid("outputs/user-1/key.zip", expires, sig)).isFalse();
    }

    @Test
    void isValid_returnsFalseForTamperedKey() {
        long expires = Instant.now().plusSeconds(60).getEpochSecond();
        String sig = signer.sign("outputs/user-1/key.zip", expires);

        assertThat(signer.isValid("outputs/user-2/key.zip", expires, sig)).isFalse();
    }

    @Test
    void isValid_returnsFalseForNullOrBlankSignature() {
        long expires = Instant.now().plusSeconds(60).getEpochSecond();

        assertThat(signer.isValid("outputs/user-1/key.zip", expires, null)).isFalse();
        assertThat(signer.isValid("outputs/user-1/key.zip", expires, " ")).isFalse();
    }

    private DownloadLinkSigner newSigner() {
        DownloadLinkSigner s = new DownloadLinkSigner();
        ReflectionTestUtils.setField(s, "secret", "test-secret");
        return s;
    }
}
