package pl.edu.ur.blokur.pdflib.template.data;

/** Dane do wygenerowania zawiadomienia o zmianie stawek opłat (PDF). */
public class RateChangeNotificationData {

    private final String subject;
    private final String body;
    private final String effectiveDate;
    private final String communityName;

    /**
     * @param subject tytuł zawiadomienia
     * @param body treść zawiadomienia
     * @param effectiveDate data wejścia w życie zmian (format YYYY-MM-DD)
     * @param communityName nazwa wspólnoty (wyświetlana w nagłówku)
     */
    public RateChangeNotificationData(
            String subject, String body, String effectiveDate, String communityName) {
        this.subject = subject;
        this.body = body;
        this.effectiveDate = effectiveDate;
        this.communityName = communityName;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public String getCommunityName() {
        return communityName;
    }
}
