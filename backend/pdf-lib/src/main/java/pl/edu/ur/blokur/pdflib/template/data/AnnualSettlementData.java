package pl.edu.ur.blokur.pdflib.template.data;

import java.math.BigDecimal;
import java.util.List;

/** Dane do wygenerowania rocznego rozliczenia kosztów lokalu (PDF). */
public class AnnualSettlementData {

    private final String apartmentAddress;
    private final int year;
    private final BigDecimal openingBalance;
    private final BigDecimal closingBalance;
    private final List<AnnualSettlementRow> rows;
    private final String note;
    private final String communityName;

    /**
     * @param apartmentAddress adres lokalu (wyświetlany w nagłówku dokumentu)
     * @param year rok rozliczeniowy
     * @param openingBalance saldo na początku roku
     * @param closingBalance saldo na końcu roku
     * @param rows lista pozycji rozliczeniowych (wpłaty, naliczenia, korekty)
     * @param note dodatkowe uwagi zarządcy (może być null)
     * @param communityName nazwa wspólnoty (wyświetlana w nagłówku)
     */
    public AnnualSettlementData(
            String apartmentAddress,
            int year,
            BigDecimal openingBalance,
            BigDecimal closingBalance,
            List<AnnualSettlementRow> rows,
            String note,
            String communityName) {
        this.apartmentAddress = apartmentAddress;
        this.year = year;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.rows = rows;
        this.note = note;
        this.communityName = communityName;
    }

    public String getApartmentAddress() {
        return apartmentAddress;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public List<AnnualSettlementRow> getRows() {
        return rows;
    }

    public String getNote() {
        return note;
    }

    public String getCommunityName() {
        return communityName;
    }
}
