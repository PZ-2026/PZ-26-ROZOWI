# Plan napraw błędów w wariantach dla aplikacji BlokUR

Niniejszy dokument przedstawia propozycje poprawek dla błędów manualnych (TEST-011, TEST-012, TEST-020) w dwóch wariantach: minimalistycznym (Quick-Fix) oraz docelowym (Robust / Clean Architecture).

---

## Bug 11: Autoryzacja zdjęć zgłoszenia (TEST-011)

### Wariant A: Minimalistyczny (Quick-Fix)
*   **Backend:** Modyfikacja metody `validateViewAccess` w klasie `TicketImageService.java`. Zamiast sprawdzać, czy zalogowany użytkownik jest autorem zgłoszenia (`ticket.getAuthor().getId().equals(user.getId())`), użyjemy istniejącej w `TicketService.java` logiki widoczności zgłoszeń dla mieszkańca.
*   **Implementacja:**
    Sprawdzamy, czy użytkownik ma przypisany lokal, a następnie czy zgłoszenie dotyczy jego lokalu, klatki lub budynku:
    ```java
    if (user.getUserApartments().isEmpty()) {
        throw new SecurityException("Brak dostępu — mieszkaniec nie ma przypisanego lokalu");
    }
    var apt = user.getUserApartments().get(0).getApartment();
    var residentApartmentId = apt != null ? apt.getId() : null;
    var residentStaircaseId = (apt != null && apt.getStaircase() != null) ? apt.getStaircase().getId() : null;
    var residentBuildingId = (apt != null && apt.getStaircase() != null && apt.getStaircase().getBuilding() != null)
            ? apt.getStaircase().getBuilding().getId() : null;

    boolean hasAccess = isTicketVisibleForResident(ticket, residentApartmentId, residentStaircaseId, residentBuildingId);
    if (!hasAccess) {
        throw new SecurityException("Brak dostępu do zgłoszenia.");
    }
    ```
    *Uwaga:* Metoda `isTicketVisibleForResident` zostanie przeniesiona lub zduplikowana w `TicketImageService`.

### Wariant B: Docelowy (Robust)
*   **Backend:** Wydzielenie logiki autoryzacji zgłoszeń do dedykowanego komponentu bezpieczeństwa (np. klasy `TicketSecurityHelper` lub metody w `TicketRepository`). Komponent ten będzie reużywany w `TicketService`, `TicketImageService` oraz `TicketCommentService`. Unikamy dzięki temu duplikacji kodu sprawdzania uprawnień mieszkańców, konserwatorów i zarządców do konkretnego zgłoszenia.

---

## Bug 12: Odświeżanie listy i format numeru (TEST-012)

### Wariant A: Minimalistyczny (Quick-Fix)
*   **Frontend (Odświeżanie):** W widoku Compose `TicketsScreen.kt` wywołujemy przeładowanie listy zgłoszeń (`viewModel.loadTickets()`) w bloku `LaunchedEffect` po powrocie na ten ekran (np. rejestrując powrót w cyklu życia widoku).
*   **Backend (Format numeru):** Zmiana szablonu formatowania w klasie `TicketNumberGenerator.java` z `ZGL-%d-%04d` na `ZGL/%d/%03d` (lub `%04d`), tak aby generowane numery zgłoszeń były zgodne z ukośnikami stosowanymi w bazie danych i specyfikacji testów.

### Wariant B: Docelowy (Robust)
*   **Frontend (Odświeżanie):** Wprowadzenie reaktywnego przepływu danych za pomocą `SharedFlow` lub `StateFlow` współdzielonego między `CreateTicketViewModel` a `TicketsViewModel`. Gdy zgłoszenie zostanie pomyślnie utworzone, emitowane jest zdarzenie dodania zgłoszenia, na które subskrybuje się widok listy, co automatycznie inwaliduje cache i wymusza pobranie nowej listy bez konieczności przeładowywania całego widoku.
*   **Backend & Baza danych (Format numeru):** Pełne ujednolicenie formatu w całym projekcie: aktualizacja testów jednostkowych generatora (`TicketNumberGeneratorTest.java`), modyfikacja generatora, aktualizacja seedów Flyway (`V4__sample_data.sql` i innych) oraz dostosowanie ewentualnych filtrów regex na backendzie.

---

## Bug 20: Profil mieszkańca, saldo i transakcje (TEST-020)

### Wariant A: Minimalistyczny (Quick-Fix)
*   **Frontend (Ustalanie lokalu):** Usprawnienie heurystyki w `UserApartmentService.resolveForResident()`. Zamiast bezwarunkowo pobierać szczegóły pierwszego zgłoszenia z listy (`tickets.first()`), szukamy na pobranej liście zgłoszeń pierwszego takiego, które ma przypisany lokal (tj. `locationLabel` pasujący do formatu numeru lokalu, lub szczegóły zgłoszenia zwracają nie-null `apartmentId`):
    ```kotlin
    val tickets = ticketService.getTickets()
    // Szukamy pierwszego zgłoszenia, którego szczegóły zawierają nie-null apartmentId
    val firstApartmentTicket = tickets.firstOrNull { ticket ->
        val detail = ticketService.getTicketById(ticket.id)
        detail?.apartmentId != null
    } ?: throw UserApartmentException("Brak przypisanego lokalu w profilu użytkownika.")
    ```
*   **Frontend & Backend (Typ transakcji):** 
    *   W `TransactionItem.kt` na frontendzie dodajemy obsługę typu `'NALEZNOSC'` w mapperze `toPresentation()` i traktujemy go tak samo jak `'NALICZENIE'`.
    *   Na backendzie w `FinancialTransactionService.java` rozszerzamy listę `allowedTypes` o typ `'NALEZNOSC'`.

### Wariant B: Docelowy (Robust)
*   **Backend (Profil użytkownika):** Dodanie dedykowanego endpointu profilu zalogowanego użytkownika `GET /api/users/me` (lub `/api/profile`), który będzie zwracał pełny profil użytkownika wraz z listą powiązanych lokali (obiektów `Apartment` pobranych z relacji `UserApartment`).
*   **Frontend (Usuwanie heurystyki):** Pobieramy profil mieszkańca po zalogowaniu za pomocą nowego endpointu. Zapisujemy przypisany `apartmentId` (lub ich listę, jeśli mieszkaniec ma wiele lokali) w `AuthService` lub lokalnym cache sesji. Finanse pobierają dane bezpośrednio dla tego ID, eliminując całkowicie heurystykę opartą na zgłoszeniach z `UserApartmentService.kt`.
*   **Baza danych (Spójność typów transakcji):** Utworzenie nowej migracji SQL (np. `V118__fix_transaction_types.sql`), która zamienia wszystkie typy `'NALEZNOSC'` na `'NALICZENIE'` w tabeli `financial_transactions`, ujednolicając słownik transakcji w bazie danych z kodem backendu i frontendu.
