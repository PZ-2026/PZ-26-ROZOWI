package pl.edu.ur.blokur.dto;

/** Reprezentuje błąd pojedynczego wiersza podczas importu pliku CSV. */
public class CsvImportErrorDto {

    private int line;
    private String message;

    public CsvImportErrorDto() {}

    public CsvImportErrorDto(int line, String message) {
        this.line = line;
        this.message = message;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
