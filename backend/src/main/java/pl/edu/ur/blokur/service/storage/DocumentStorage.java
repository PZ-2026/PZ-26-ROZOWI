package pl.edu.ur.blokur.service.storage;

import org.springframework.core.io.Resource;

/**
 * Abstrakcja warstwy fizycznego przechowywania plików dokumentów. Pozwala wymiennie używać dysku
 * lokalnego (środowisko deweloperskie, on-prem) lub chmurowego storage (AWS S3) bez zmian w
 * warstwie serwisowej.
 *
 * <p>Konkretna implementacja jest wybierana przez {@link DocumentStorageConfig} na podstawie
 * właściwości {@code storage.type} (wartości: {@code local}, {@code s3}).
 */
public interface DocumentStorage {

    /**
     * Zapisuje zawartość pliku w storage.
     *
     * @param subDirectory logiczny podkatalog (np. {@code "documents"}, {@code "announcements"})
     * @param fileName nazwa pliku z rozszerzeniem (powinna być unikalna w obrębie podkatalogu)
     * @param content zawartość pliku jako tablica bajtów
     * @return klucz lub URL do zapisanego zasobu — wartość przeznaczona do zapisu w kolumnie
     *     {@code documents.file_url}; format zależy od implementacji
     * @throws DocumentStorageException w przypadku błędu zapisu (brak uprawnień, brak miejsca,
     *     błąd I/O)
     */
    String store(String subDirectory, String fileName, byte[] content);

    /**
     * Ładuje zawartość pliku spod podanego klucza/URL.
     *
     * @param key klucz lub URL zwrócony wcześniej przez {@link #store}
     * @return zasób Springowy do odczytu (np. do streamingu HTTP)
     * @throws DocumentStorageException gdy zasób nie istnieje lub jest nieczytelny
     */
    Resource load(String key);

    /**
     * Sprawdza, czy pod podanym kluczem istnieje plik.
     *
     * @param key klucz lub URL
     * @return {@code true} gdy plik istnieje i jest czytelny
     */
    boolean exists(String key);

    /**
     * Usuwa plik spod podanego klucza/URL.
     *
     * @param key klucz lub URL
     * @throws DocumentStorageException gdy operacja się nie powiedzie (poza brakiem pliku — to
     *     traktujemy jako idempotentne)
     */
    void delete(String key);
}
