package pl.edu.ur.blokur.service.storage;

import org.springframework.core.io.Resource;

/**
 * Szkielet implementacji {@link DocumentStorage} dla AWS S3. Klasa istnieje, aby pokazać że
 * abstrakcja jest gotowa pod podpięcie chmurowego storage — pełna implementacja (z AWS SDK,
 * konfiguracją credentials i podpisanymi URL-ami) będzie przedmiotem osobnego zadania.
 *
 * <p>Aktywowane przez właściwość {@code storage.type=s3}.
 */
public class S3DocumentStorage implements DocumentStorage {

    private final String bucket;
    private final String region;

    /**
     * Tworzy stub S3 z podanym bucketem i regionem.
     *
     * @param bucket nazwa bucketa S3
     * @param region region AWS (np. {@code eu-central-1})
     */
    public S3DocumentStorage(String bucket, String region) {
        this.bucket = bucket;
        this.region = region;
    }

    /** @return nazwa bucketa S3 (do późniejszego użycia) */
    public String getBucket() {
        return bucket;
    }

    /** @return region AWS (do późniejszego użycia) */
    public String getRegion() {
        return region;
    }

    @Override
    public String store(String subDirectory, String fileName, byte[] content) {
        throw new UnsupportedOperationException(
                "S3 storage nie jest jeszcze zaimplementowany — wymagane dodanie AWS SDK i"
                        + " credentials. Włącz storage.type=local lub zaimplementuj S3.");
    }

    @Override
    public Resource load(String key) {
        throw new UnsupportedOperationException(
                "S3 storage nie jest jeszcze zaimplementowany — pobieranie plików.");
    }

    @Override
    public boolean exists(String key) {
        throw new UnsupportedOperationException(
                "S3 storage nie jest jeszcze zaimplementowany — sprawdzanie istnienia.");
    }

    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException(
                "S3 storage nie jest jeszcze zaimplementowany — usuwanie plików.");
    }
}
