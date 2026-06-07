package pl.edu.ur.blokur.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

/** Testy jednostkowe dla {@link LocalDiskDocumentStorage} — używa {@link TempDir} z JUnit 5. */
@DisplayName("LocalDiskDocumentStorage — storage na dysku lokalnym")
class LocalDiskDocumentStorageTest {

    @TempDir Path tempRoot;

    private LocalDiskDocumentStorage storage;

    @BeforeEach
    void setUp() {
        storage = new LocalDiskDocumentStorage(tempRoot);
    }

    @Nested
    @DisplayName("store")
    class Store {

        @Test
        @DisplayName("zapisuje plik i tworzy podkatalog jeśli nie istnieje")
        void shouldStoreFileAndCreateSubdirectory() throws IOException {
            byte[] content = "%PDF-1.4 test content".getBytes();

            String url = storage.store("documents", "test-protokol.pdf", content);

            assertThat(url).isNotNull();
            Path savedFile = Path.of(url);
            assertThat(Files.exists(savedFile)).isTrue();
            assertThat(Files.readAllBytes(savedFile)).isEqualTo(content);
            assertThat(savedFile.getParent().getFileName().toString()).isEqualTo("documents");
        }

        @Test
        @DisplayName("zapisuje plik w katalogu bazowym gdy subDirectory jest puste")
        void shouldStoreInRootWhenSubdirectoryBlank() throws IOException {
            String url = storage.store("", "root.pdf", new byte[] {1, 2, 3});

            assertThat(Files.exists(Path.of(url))).isTrue();
            assertThat(Path.of(url).getParent()).isEqualTo(tempRoot);
        }

        @Test
        @DisplayName("rzuca wyjątek przy pustej nazwie pliku")
        void shouldThrowOnBlankFileName() {
            assertThatThrownBy(() -> storage.store("documents", "  ", new byte[] {}))
                    .isInstanceOf(DocumentStorageException.class)
                    .hasMessageContaining("Nazwa pliku");
        }

        @Test
        @DisplayName("rzuca wyjątek przy null content")
        void shouldThrowOnNullContent() {
            assertThatThrownBy(() -> storage.store("documents", "a.pdf", null))
                    .isInstanceOf(DocumentStorageException.class)
                    .hasMessageContaining("Zawartość");
        }
    }

    @Nested
    @DisplayName("load")
    class Load {

        @Test
        @DisplayName("ładuje istniejący plik")
        void shouldLoadExistingFile() throws IOException {
            byte[] content = "abc".getBytes();
            String url = storage.store("documents", "x.pdf", content);

            Resource resource = storage.load(url);

            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
            assertThat(resource.getContentAsByteArray()).isEqualTo(content);
        }

        @Test
        @DisplayName("rzuca wyjątek gdy plik nie istnieje")
        void shouldThrowOnMissingFile() {
            assertThatThrownBy(() -> storage.load("/nonexistent/foo.pdf"))
                    .isInstanceOf(DocumentStorageException.class)
                    .hasMessageContaining("nieczytelny");
        }

        @Test
        @DisplayName("rzuca wyjątek na pusty klucz")
        void shouldThrowOnBlankKey() {
            assertThatThrownBy(() -> storage.load(""))
                    .isInstanceOf(DocumentStorageException.class);
        }
    }

    @Nested
    @DisplayName("exists")
    class Exists {

        @Test
        @DisplayName("zwraca true dla istniejącego pliku")
        void shouldReturnTrueForExisting() {
            String url = storage.store("d", "a.pdf", new byte[] {1});
            assertThat(storage.exists(url)).isTrue();
        }

        @Test
        @DisplayName("zwraca false dla nieistniejącego")
        void shouldReturnFalseForMissing() {
            assertThat(storage.exists(tempRoot.resolve("brak.pdf").toString())).isFalse();
        }

        @Test
        @DisplayName("zwraca false dla null/blank")
        void shouldReturnFalseForBlank() {
            assertThat(storage.exists(null)).isFalse();
            assertThat(storage.exists("")).isFalse();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("usuwa istniejący plik")
        void shouldDeleteExisting() {
            String url = storage.store("d", "del.pdf", new byte[] {1});
            assertThat(Files.exists(Path.of(url))).isTrue();

            storage.delete(url);

            assertThat(Files.exists(Path.of(url))).isFalse();
        }

        @Test
        @DisplayName("nie rzuca przy braku pliku (idempotentność)")
        void shouldNotThrowOnMissingFile() {
            storage.delete(tempRoot.resolve("nieistnieje.pdf").toString());
            // brak wyjątku = sukces
        }
    }
}
