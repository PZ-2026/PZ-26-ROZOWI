package pl.edu.ur.blokur.models;

/**
 * Enum reprezentujący możliwe stany zgłoszenia w systemie Blokur.
 *
 * <p>Cykl życia zgłoszenia: NOWE → ZAPLANOWANO → W_REALIZACJI → ZAKONCZONE →
 * ZAMKNIETE. Alternatywne zakończenia: WSTRZYMANO (oczekiwanie) lub ODRZUCONE.
 */
public enum TicketStatus {

    /** Zgłoszenie właśnie złożone, oczekuje na przyjęcie. */
    NOWE,

    /** Wizyta lub prace zostały zaplanowane. */
    ZAPLANOWANO,

    /** Prace są aktualnie wykonywane. */
    W_REALIZACJI,

    /** Realizacja wstrzymana (np. oczekiwanie na części). */
    WSTRZYMANO,

    /** Prace zakończone, czeka na weryfikację przez zarządcę lub mieszkańca. */
    ZAKONCZONE,

    /** Zgłoszenie zweryfikowane i zamknięte. */
    ZAMKNIETE,

    /** Zgłoszenie odrzucone jako niezasadne lub duplikat. */
    ODRZUCONE
}
