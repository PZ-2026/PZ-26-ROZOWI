package pl.edu.ur.blokur.service.storage;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracja Springa wybierająca implementację {@link DocumentStorage} na podstawie właściwości
 * {@code storage.type}.
 *
 * <ul>
 *   <li>{@code storage.type=local} (domyślne) — {@link LocalDiskDocumentStorage}
 *   <li>{@code storage.type=s3} — {@link S3DocumentStorage} (stub, do dokończenia)
 * </ul>
 */
@Configuration
public class DocumentStorageConfig {

    /**
     * Bean dla lokalnego storage na dysku. Aktywny domyślnie lub gdy {@code storage.type=local}.
     *
     * @param uploadDir katalog bazowy zapisu z właściwości {@code app.upload.dir}
     * @return implementacja {@link LocalDiskDocumentStorage}
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
    public DocumentStorage localDocumentStorage(@Value("${app.upload.dir}") String uploadDir) {
        return new LocalDiskDocumentStorage(Paths.get(uploadDir));
    }

    /**
     * Bean dla chmurowego storage S3. Aktywny gdy {@code storage.type=s3}.
     *
     * @param bucket nazwa bucketa z właściwości {@code storage.s3.bucket}
     * @param region region AWS z właściwości {@code storage.s3.region}
     * @return implementacja {@link S3DocumentStorage}
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public DocumentStorage s3DocumentStorage(
            @Value("${storage.s3.bucket:}") String bucket,
            @Value("${storage.s3.region:}") String region) {
        return new S3DocumentStorage(bucket, region);
    }
}
