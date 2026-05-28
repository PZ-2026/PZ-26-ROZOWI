package pl.edu.ur.blokur.pdflib.template.data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Pojedyncza pozycja rozliczeniowa w rocznym rozliczeniu kosztów lokalu. */
public class AnnualSettlementRow {

    private final LocalDate date;
    private final String type;
    private final String description;
    private final BigDecimal amount;

    /**
     * @param date data transakcji
     * @param type typ operacji (np. WPLATA, NALICZENIE, KOREKTA)
     * @param description opis transakcji
     * @param amount kwota (dodatnia = wpłata, ujemna = naliczenie/korekta)
     */
    public AnnualSettlementRow(LocalDate date, String type, String description, BigDecimal amount) {
        this.date = date;
        this.type = type;
        this.description = description;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
