package pl.edu.ur.blokur.models;

/**
 * Typ wyliczeniowy określający rodzaj komentarza do zgłoszenia. PUBLICZNY - widoczny dla wszystkich
 * (mieszkaniec, zarządca, konserwator). WEWNETRZNY - widoczny tylko dla personelu (zarządca,
 * konserwator).
 */
public enum TicketCommentType {
    PUBLICZNY,
    WEWNETRZNY
}
