package pl.edu.ur.blokur.dto;

import lombok.Data;

/** Reprezentuje błąd pojedynczego wiersza podczas importu pliku CSV. */
@Data
public class CsvImportErrorDto {

    private int line;
    private String message;

    public CsvImportErrorDto(int line, String message) {
        this.line = line;
        this.message = message;
    }
}
