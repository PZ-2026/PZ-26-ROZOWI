package pl.edu.ur.blokur.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Implementacja {@link DocumentStorage} zapisująca pliki w lokalnym katalogu na dysku. Klucze
 * zwracane przez {@link #store} są zwykłymi ścieżkami systemowymi (np. {@code
 * uploads/documents/protokol-ZGL-2026-0001-1234567890.pdf}) — kompatybilne z istniejącym
 * formatem w kolumnie {@code documents.file_url}.
 */
public class LocalDiskDocumentStorage implements DocumentStorage {

    private final Path rootDirectory;

    /**
     * Tworzy storage z podanym katalogiem głównym.
     *
     * @param rootDirectory katalog bazowy (np. {@code uploads}); może być względny lub bezwzględny
     */
    public LocalDiskDocumentStorage(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public String store(String subDirectory, String fileName, byte[] content) {
        if (fileName == null || fileName.isBlank()) {
            throw new DocumentStorageException("Nazwa pliku nie może być pusta");
        }
        if (content == null) {
            throw new DocumentStorageException("Zawartość pliku nie może być null");
        }
        try {
            Path dir =
                    subDirectory != null && !subDirectory.isBlank()
                            ? rootDirectory.resolve(subDirectory)
                            : rootDirectory;
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path filePath = dir.resolve(fileName);
            Files.write(filePath, content);
            return filePath.toString();
        } catch (IOException e) {
            throw new DocumentStorageException(
                    "Nie udało się zapisać pliku " + fileName + " na dysku", e);
        }
    }

    @Override
    public Resource load(String key) {
        if (key == null || key.isBlank()) {
            throw new DocumentStorageException("Klucz pliku nie może być pusty");
        }
        Resource resource = new FileSystemResource(Paths.get(key));
        if (!resource.exists() || !resource.isReadable()) {
            throw new DocumentStorageException(
                    "Plik nie istnieje lub jest nieczytelny: " + key);
        }
        return resource;
    }

    @Override
    public boolean exists(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return Files.exists(Paths.get(key)) && Files.isReadable(Paths.get(key));
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(key));
        } catch (IOException e) {
            throw new DocumentStorageException("Nie udało się usunąć pliku: " + key, e);
        }
    }
}
