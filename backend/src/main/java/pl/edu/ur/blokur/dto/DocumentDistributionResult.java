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

    /**
     * Zwraca liczbę wygenerowanych dokumentów.
     *
     * @return liczba dokumentów
     */
    public int getDocumentsGenerated() {
        return documentsGenerated;
    }

    /**
     * Zwraca liczbę powiadomionych odbiorców.
     *
     * @return liczba odbiorców
     */
    public int getRecipientsNotified() {
        return recipientsNotified;
    }

    /**
     * Zwraca komunikat opisujący wynik dystrybucji.
     *
     * @return komunikat wynikowy
     */
    public String getMessage() {
        return message;
    }
}
