package pl.edu.ur.blokur.dto;

import java.util.List;

/** Reprezentuje wynik masowego importu transakcji finansowych z pliku CSV. */
public class CsvImportResultDto {

    private int importedCount;
    private int errorCount;
    private List<CsvImportErrorDto> errors;

    public CsvImportResultDto() {}

    public CsvImportResultDto(int importedCount, int errorCount, List<CsvImportErrorDto> errors) {
        this.importedCount = importedCount;
        this.errorCount = errorCount;
        this.errors = errors;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<CsvImportErrorDto> getErrors() {
        return errors;
    }

    public void setErrors(List<CsvImportErrorDto> errors) {
        this.errors = errors;
    }
}
