package pl.edu.ur.blokur.pdflib;

import com.itextpdf.layout.Document;

/**
 * Szablon dokumentu PDF. Każda konkretna implementacja opisuje jeden typ raportu lub protokołu
 * (np. {@code WorkAcceptanceProtocolTemplate}, {@code BalancesReportTemplate}).
 *
 * <p>Implementacje są bezstanowe od strony iText — otrzymują otwarty {@link Document} i dodają do
 * niego elementy. Zarządzanie cyklem życia dokumentu (otwieranie, zamykanie, zapis do bajtów)
 * realizuje {@link PdfGenerator}.
 */
public interface PdfTemplate {

    /**
     * Wypełnia podany dokument zawartością raportu lub protokołu.
     *
     * @param document otwarty dokument iText, do którego należy dodać elementy
     */
    void render(Document document);
}
