package pl.edu.ur.blokur.domain.model

/**
 * Role użytkownika w systemie Blokur.
 *
 * Wartości odpowiadają dosłownie temu, co backend zwraca w polu `role`
 * w odpowiedzi na POST /api/auth/login oraz POST /api/auth/refresh.
 */
enum class UserRole {
    /** Zwykły mieszkaniec lokalu – dostęp do własnych finansów, zgłoszeń i ogłoszeń. */
    MIESZKANIEC,

    /** Konserwator – widzi zgłoszenia serwisowe przypisane do niego. */
    KONSERWATOR,

    /** Zarządca – pełny dostęp do systemu (backend zwraca `"ZARZADCA"`). */
    ZARZADCA
}
