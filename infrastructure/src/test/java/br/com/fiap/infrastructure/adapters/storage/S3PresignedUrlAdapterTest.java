package br.com.fiap.infrastructure.adapters.storage;

import br.com.fiap.domain.exceptions.PresignedUrlGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3PresignedUrlAdapterTest {

    @Mock S3Client s3Client;
    @Mock S3Presigner s3Presigner;

    @Test
    void objectExists_returnsTrueWhenHeadObjectSucceeds() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        S3PresignedUrlAdapter adapter = new S3PresignedUrlAdapter(s3Client, s3Presigner, "my-bucket");
        assertThat(adapter.objectExists("outputs/user/uuid/frames.zip")).isTrue();
    }

    @Test
    void objectExists_returnsFalseWhenNoSuchKey() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        S3PresignedUrlAdapter adapter = new S3PresignedUrlAdapter(s3Client, s3Presigner, "my-bucket");
        assertThat(adapter.objectExists("outputs/user/uuid/frames.zip")).isFalse();
    }

    @Test
    void objectExists_returnsFalseOnGenericException() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(new RuntimeException("S3 unavailable"));

        S3PresignedUrlAdapter adapter = new S3PresignedUrlAdapter(s3Client, s3Presigner, "my-bucket");
        assertThat(adapter.objectExists("outputs/user/uuid/frames.zip")).isFalse();
    }

    @Test
    void generatePresignedUrl_returnsUrlString() throws MalformedURLException {
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URL("https://s3.amazonaws.com/bucket/key?sig=abc"));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        S3PresignedUrlAdapter adapter = new S3PresignedUrlAdapter(s3Client, s3Presigner, "my-bucket");
        String url = adapter.generatePresignedUrl("outputs/user/550e8400-e29b-41d4-a716-446655440000/frames.zip", 15L);

        assertThat(url).contains("s3.amazonaws.com");
    }

    @Test
    void generatePresignedUrl_throwsPresignedUrlGenerationExceptionOnError() {
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("Presigner error"));

        S3PresignedUrlAdapter adapter = new S3PresignedUrlAdapter(s3Client, s3Presigner, "my-bucket");

        assertThatThrownBy(() -> adapter.generatePresignedUrl(
                "outputs/user/550e8400-e29b-41d4-a716-446655440000/frames.zip", 15L))
                .isInstanceOf(PresignedUrlGenerationException.class);
    }
}
