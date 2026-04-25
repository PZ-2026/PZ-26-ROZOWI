package pl.edu.ur.blokur.models;

/**
 * Typ wyliczeniowy określający zasięg ogłoszenia. WSZYSCY - ogłoszenie globalne dla wszystkich
 * mieszkańców. NIERUCHOMOSC - skierowane do konkretnego lokalu. BUDYNEK - skierowane do konkretnego
 * budynku. KLATKA - skierowane do konkretnej klatki schodowej.
 */
public enum AnnouncementTargetType {
    WSZYSCY,
    NIERUCHOMOSC,
    BUDYNEK,
    KLATKA
}
