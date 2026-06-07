package pl.edu.ur.blokur.models;

/**
 * Typ zasięgu przeglądu technicznego. Określa, na jakim poziomie hierarchii nieruchomości
 * obowiązuje dany przegląd.
 */
public enum ScopeType {

    /** Zasięg dla całej nieruchomości (wspólnoty mieszkaniowej). */
    NIERUCHOMOSC,

    /** Zasięg dla pojedynczego budynku. */
    BUDYNEK,

    /** Zasięg dla pojedynczej klatki schodowej. */
    KLATKA
}
