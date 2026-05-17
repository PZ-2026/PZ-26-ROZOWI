package pl.edu.ur.blokur.pdflib.template.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Dane wejściowe dla raportu zestawienia sald i zaległości lokali. */
public final class BalancesReportData {

    private final List<BalanceRow> rows;

    /**
     * Tworzy raport z podanej listy wierszy.
     *
     * @param rows wiersze raportu (gotowo posortowane i przefiltrowane przez warstwę serwisową)
     */
    public BalancesReportData(List<BalanceRow> rows) {
        this.rows = rows != null ? new ArrayList<>(rows) : new ArrayList<>();
    }

    /** @return niezmienialna lista wierszy raportu */
    public List<BalanceRow> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
