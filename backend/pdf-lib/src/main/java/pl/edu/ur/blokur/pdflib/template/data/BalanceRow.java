package pl.edu.ur.blokur.pdflib.template.data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Pojedynczy wiersz raportu sald — dane jednego lokalu. */
public final class BalanceRow {

    private final String address;
    private final BigDecimal balance;
    private final LocalDate lastPaymentDate;
    private final Long daysOverdue;

    /**
     * Tworzy wiersz raportu sald.
     *
     * @param address adres lokalu (np. budynek + klatka + numer)
     * @param balance saldo w PLN (dodatnie = nadpłata, ujemne = zaległość)
     * @param lastPaymentDate data ostatniej wpłaty lub {@code null} gdy brak wpłat
     * @param daysOverdue liczba dni zalegania lub {@code null} gdy brak zaległości
     */
    public BalanceRow(
            String address, BigDecimal balance, LocalDate lastPaymentDate, Long daysOverdue) {
        this.address = address;
        this.balance = balance;
        this.lastPaymentDate = lastPaymentDate;
        this.daysOverdue = daysOverdue;
    }

    /** @return adres lokalu */
    public String getAddress() {
        return address;
    }

    /** @return saldo lokalu w PLN */
    public BigDecimal getBalance() {
        return balance;
    }

    /** @return data ostatniej wpłaty (może być null) */
    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    /** @return liczba dni zalegania (może być null) */
    public Long getDaysOverdue() {
        return daysOverdue;
    }
}
