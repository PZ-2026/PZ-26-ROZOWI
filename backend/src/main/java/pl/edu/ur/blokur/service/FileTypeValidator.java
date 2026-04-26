package pl.edu.ur.blokur.service;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Walidator typów plików oparty na magic bytes (sygnaturach binarnych). Nie opiera się na
 * rozszerzeniu ani deklarowanym Content-Type — odczytuje pierwsze bajty strumienia pliku.
 */
@Component
public class FileTypeValidator {

    static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};
    static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46};

    /**
     * Waliduje, że przesłany plik jest obrazem JPEG lub PNG.
     *
     * @param file przesłany plik
     * @throws ResponseStatusException HTTP 415 gdy plik nie jest JPEG ani PNG
     */
    public void validateImage(MultipartFile file) {
        byte[] header = readHeader(file, 4);
        if (!startsWith(header, JPEG_MAGIC) && !startsWith(header, PNG_MAGIC)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Niedozwolony typ pliku. Dozwolone formaty: JPEG, PNG.");
        }
    }

    /**
     * Waliduje, że przesłany plik jest dokumentem PDF.
     *
     * @param file przesłany plik
     * @throws ResponseStatusException HTTP 415 gdy plik nie jest PDF
     */
    public void validatePdf(MultipartFile file) {
        byte[] header = readHeader(file, 4);
        if (!startsWith(header, PDF_MAGIC)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Niedozwolony typ pliku. Dozwolony format: PDF.");
        }
    }

    /**
     * Waliduje, że przesłany plik nie jest plikiem binarnym (akceptuje pliki tekstowe CSV). Odrzuca
     * pliki zaczynające się od znanych sygnatur binarnych (JPEG, PNG, PDF).
     *
     * @param file przesłany plik
     * @throws ResponseStatusException HTTP 415 gdy plik zaczyna się od sygnatury binarnej
     */
    public void validateCsv(MultipartFile file) {
        byte[] header = readHeader(file, 4);
        if (startsWith(header, JPEG_MAGIC)
                || startsWith(header, PNG_MAGIC)
                || startsWith(header, PDF_MAGIC)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Niedozwolony typ pliku. Dozwolony format: CSV (plik tekstowy).");
        }
    }

    private byte[] readHeader(MultipartFile file, int length) {
        try (InputStream is = file.getInputStream()) {
            return is.readNBytes(length);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Nie można odczytać przesłanego pliku.");
        }
    }

    boolean startsWith(byte[] data, byte[] magic) {
        if (data.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }
}
