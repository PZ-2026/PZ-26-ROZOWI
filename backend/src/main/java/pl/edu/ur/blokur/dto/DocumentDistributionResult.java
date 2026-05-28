package pl.edu.ur.blokur.dto;

/** Wynik operacji dystrybucji dokumentów do mieszkańców. */
public class DocumentDistributionResult {

    private final int documentsGenerated;
    private final int recipientsNotified;
    private final String message;

    public DocumentDistributionResult(
            int documentsGenerated, int recipientsNotified, String message) {
        this.documentsGenerated = documentsGenerated;
        this.recipientsNotified = recipientsNotified;
        this.message = message;
    }

    public int getDocumentsGenerated() {
        return documentsGenerated;
    }

    public int getRecipientsNotified() {
        return recipientsNotified;
    }

    public String getMessage() {
        return message;
    }
}
