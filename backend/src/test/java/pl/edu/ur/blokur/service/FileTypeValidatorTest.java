package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("FileTypeValidator — walidacja magic bytes")
class FileTypeValidatorTest {

    private FileTypeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileTypeValidator();
    }

    @Nested
    @DisplayName("validateImage")
    class ValidateImage {

        @Test
        @DisplayName("akceptuje plik JPEG")
        void acceptsJpeg() {
            byte[] content = buildContent(FileTypeValidator.JPEG_MAGIC, 20);
            MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content);

            assertThatCode(() -> validator.validateImage(file)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("akceptuje plik PNG")
        void acceptsPng() {
            byte[] content = buildContent(FileTypeValidator.PNG_MAGIC, 20);
            MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", content);

            assertThatCode(() -> validator.validateImage(file)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("odrzuca plik PDF jako obraz — HTTP 415")
        void rejectsPdf() {
            byte[] content = buildContent(FileTypeValidator.PDF_MAGIC, 20);
            MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", content);

            assertThatThrownBy(() -> validator.validateImage(file))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("415");
        }

        @Test
        @DisplayName("odrzuca plik tekstowy jako obraz — HTTP 415")
        void rejectsText() {
            MockMultipartFile file =
                    new MockMultipartFile("file", "data.csv", "text/plain", "col1,col2\n1,2".getBytes());

            assertThatThrownBy(() -> validator.validateImage(file))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("415");
        }
    }

    @Nested
    @DisplayName("validatePdf")
    class ValidatePdf {

        @Test
        @DisplayName("akceptuje plik PDF")
        void acceptsPdf() {
            byte[] content = buildContent(FileTypeValidator.PDF_MAGIC, 20);
            MockMultipartFile file =
                    new MockMultipartFile("file", "doc.pdf", "application/pdf", content);

            assertThatCode(() -> validator.validatePdf(file)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("odrzuca JPEG jako PDF — HTTP 415")
        void rejectsJpeg() {
            byte[] content = buildContent(FileTypeValidator.JPEG_MAGIC, 20);
            MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content);

            assertThatThrownBy(() -> validator.validatePdf(file))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("415");
        }

        @Test
        @DisplayName("odrzuca PNG jako PDF — HTTP 415")
        void rejectsPng() {
            byte[] content = buildContent(FileTypeValidator.PNG_MAGIC, 20);
            MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", content);

            assertThatThrownBy(() -> validator.validatePdf(file))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("415");
        }
    }

    @Nested
    @DisplayName("validateCsv")
    class ValidateCsv {

        @Test
        @DisplayName("akceptuje plik tekstowy CSV")
        void acceptsCsvText() {
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "data.csv", "text/csv", "id,amount\n1,100.00".getBytes());

            assertThatCode(() -> validator.validateCsv(file)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("odrzuca JPEG jako CSV — HTTP 415")
        void rejectsJpeg() {
            byte[] content = buildContent(FileTypeValidator.JPEG_MAGIC, 20);
            MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content);

            assertThatThrownBy(() -> validator.validateCsv(file))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("415");
        }

        @Test
        @DisplayName("odrzuca PDF jako CSV — HTTP 415")
        void rejectsPdf() {
            byte[] content = buildContent(FileTypeValidator.PDF_MAGIC, 20);
            MockMultipartFile file =
                    new MockMultipartFile("file", "doc.pdf", "application/pdf", content);

            assertThatThrownBy(() -> validator.validateCsv(file))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("415");
        }
    }

    @Nested
    @DisplayName("startsWith (helper)")
    class StartsWith {

        @Test
        @DisplayName("zwraca true gdy data zaczyna się od magic")
        void returnsTrueForMatch() {
            byte[] magic = {0x01, 0x02};
            byte[] data = {0x01, 0x02, 0x03, 0x04};

            assertThatCode(() -> validator.startsWith(data, magic)).doesNotThrowAnyException();
            assert validator.startsWith(data, magic);
        }

        @Test
        @DisplayName("zwraca false gdy data krótsza niż magic")
        void returnsFalseWhenDataShorter() {
            byte[] magic = {0x01, 0x02, 0x03};
            byte[] data = {0x01};

            assert !validator.startsWith(data, magic);
        }
    }

    private byte[] buildContent(byte[] magic, int totalLength) {
        byte[] content = new byte[totalLength];
        System.arraycopy(magic, 0, content, 0, magic.length);
        return content;
    }
}
