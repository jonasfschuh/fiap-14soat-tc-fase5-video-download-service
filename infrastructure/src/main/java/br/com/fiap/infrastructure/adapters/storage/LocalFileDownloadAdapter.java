package br.com.fiap.infrastructure.adapters.storage;

import br.com.fiap.domain.exceptions.PresignedUrlGenerationException;
import br.com.fiap.domain.ports.out.VideoPresignStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Adapter de download de vídeo processado a partir do PersistentVolume local. */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileDownloadAdapter implements VideoPresignStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalFileDownloadAdapter.class);
    private static final UUID EMPTY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Value("${app.storage.local.processed-path:/app/videos/processed}")
    private String processedPath;

    @Override
    public boolean objectExists(String storageKey) {
        boolean exists = resolveExistingPath(storageKey) != null;
        log.debug("[LOCAL] objectExists key={} exists={}", storageKey, exists);
        return exists;
    }

    @Override
    public String generatePresignedUrl(String storageKey, long ttlMinutes) {
        try {
            Path filePath = resolveExistingPath(storageKey);
            if (filePath == null) {
                throw new NoSuchFileException(resolvePrimaryPath(storageKey).toString());
            }

            String url = filePath.toUri().toString();
            log.info("[LOCAL] URL de arquivo local gerada para key={} path={}", storageKey, filePath);
            return url;
        } catch (Exception e) {
            throw new PresignedUrlGenerationException(extractVideoId(storageKey), e);
        }
    }

    private Path resolveExistingPath(String storageKey) {
        return candidatePaths(storageKey).stream()
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
    }

    private Path resolvePrimaryPath(String storageKey) {
        return candidatePaths(storageKey).getFirst();
    }

    private List<Path> candidatePaths(String storageKey) {
        Path basePath = Paths.get(processedPath).toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();

        String normalizedKey = storageKey.replace('\\', '/');
        String[] parts = normalizedKey.split("/");
        if (parts.length >= 4 && "outputs".equals(parts[0]) && parts[parts.length - 1].endsWith("_frames.zip")) {
            candidates.add(safeResolve(basePath, parts[1] + "/" + parts[2] + ".zip"));
        }

        candidates.add(safeResolve(basePath, normalizedKey));

        if (normalizedKey.startsWith("outputs/")) {
            normalizedKey = normalizedKey.substring("outputs/".length());
            candidates.add(safeResolve(basePath, normalizedKey));
        }
        return candidates;
    }

    private Path safeResolve(Path basePath, String relativePath) {
        Path resolved = basePath.resolve(relativePath).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid storage key: " + relativePath);
        }
        return resolved;
    }

    private UUID extractVideoId(String storageKey) {
        try {
            String[] parts = storageKey.replace('\\', '/').split("/");
            return UUID.fromString(parts[2]);
        } catch (Exception e) {
            return EMPTY_UUID;
        }
    }
}
