package pl.edu.ur.blokur.dto;

import java.util.List;
import lombok.Data;

/** Reprezentuje wynik masowego importu transakcji finansowych z pliku CSV. */
@Data
public class CsvImportResultDto {

    private int importedCount;
    private int errorCount;
    private List<CsvImportErrorDto> errors;

    public CsvImportResultDto(int importedCount, int errorCount, List<CsvImportErrorDto> errors) {
        this.importedCount = importedCount;
        this.errorCount = errorCount;
        this.errors = errors;
    }
}
