# Analiza błędów manualnych aplikacji BlokUR

Dokument zawiera szczegółową analizę techniczną błędów wykrytych podczas testowania manualnego (TEST-011, TEST-012 oraz TEST-020). Analiza obejmuje diagnozę przyczyn źródłowych, określenie warstwy występowania błędu, stopień pewności oraz powiązania z innymi obszarami systemu.

---

## Bug 11 (TEST-011): Błąd bezpieczeństwa przy wchodzeniu w szczegóły zgłoszenia przez mieszkańca

### 1. Diagnoza przyczyny źródłowej (Root Cause Analysis)
Błąd objawia się rzuceniem wyjątku bezpieczeństwa na backendzie:
`Request processing failed: java.lang.SecurityException: Brak dostępu do cudzego zgłoszenia` w metodzie `TicketImageService.validateViewAccess`.

Przyczyną jest **niespójność reguł autoryzacji** pomiędzy pobieraniem szczegółów zgłoszenia (`TicketService.getById`) a pobieraniem zdjęć powiązanych ze zgłoszeniem (`TicketImageService.validateViewAccess` wywoływanym przy `GET /api/tickets/{id}/images`):
*   **Odczyt zgłoszenia:** W `TicketService.getById` mieszkaniec ma przyznawany dostęp do zgłoszeń powiązanych z jego lokalem, klatką schodową lub budynkiem (metoda `isTicketVisibleForResident`). Pozwala to mieszkańcom na podgląd zgłoszeń wspólnych (np. dotyczących klatki schodowej zgłoszonych przez sąsiadów).
*   **Odczyt zdjęć:** W `TicketImageService.validateViewAccess`, dla roli `MIESZKANIEC` zaimplementowano zbyt restrykcyjną regułę, wymagającą aby użytkownik był bezpośrednim *autorem* zgłoszenia:
    ```java
    if (!ticket.getAuthor().getId().equals(user.getId())) {
        throw new SecurityException("Brak dostępu do cudzego zgłoszenia.");
    }
    ```
Gdy mieszkaniec próbuje wejść w szczegóły zgłoszenia wspólnego (np. `ZGL/2026/002` zgłoszonego przez innego lokatora), szczegóły zgłoszenia ładują się poprawnie, ale żądanie pobrania zdjęć kończy się błędem HTTP 500/403, co uniemożliwia wyświetlenie całego ekranu na frontendzie.

### 2. Warstwa błędu
*   **BACKEND** (Logika biznesowa / Spójność reguł autoryzacji).

### 3. Pewność diagnozy
*   **100%** (kod autoryzacji zdjęć bezpośrednio zaprzecza regułom widoczności zgłoszeń).

### 4. Powiązanie z lukami
*   Niespójność w regułach dostępu i brak jednolitego systemu sprawdzania uprawnień dla zasobów powiązanych ze zgłoszeniem (zdjęcia, komentarze).

### 5. Błędy pochodne
Ten sam problem autoryzacji (zbyt restrykcyjne wymaganie bycia autorem) występuje w:
*   `POST /api/tickets/{id}/images` (dodawanie zdjęć przez mieszkańca do wspólnego zgłoszenia).
*   `GET /api/images/{id}` (bezpośrednie pobieranie pliku graficznego).

---

## Bug 12 (TEST-012): Nowe zgłoszenie nie odświeża się automatycznie oraz niespójny format numeru

### 1. Diagnoza przyczyny źródłowej (Root Cause Analysis)
Usterka składa się z dwóch niezależnych problemów w różnych warstwach systemu:

*   **Problem 1: Brak automatycznego odświeżenia listy**
    Po pomyślnym utworzeniu zgłoszenia i powrocie wstecz z formularza tworzenia zgłoszenia (`CreateTicketViewModel`) do listy zgłoszeń (`TicketsViewModel`), lista nie aktualizuje się automatycznie. Użytkownik musi ręcznie wykonać gest "pull-to-refresh". Wynika to z faktu, że w komponencie listy nie zaimplementowano reaktywnego przeładowania ani inwalidacji cache po powrocie z formularza zapisu.
*   **Problem 2: Niespójny format numeru zgłoszenia ("format się różni")**
    Generator na backendzie (`TicketNumberGenerator.java`) tworzy numery w formacie `ZGL-RRRR-NNNN` (np. `ZGL-2026-0001` z myślnikami i 4-cyfrowym numerem sekwencyjnym). Z kolei w bazodanowych seedach (`V4__sample_data.sql`) oraz specyfikacji testów manualnych stosowany jest format `ZGL/RRRR/NNN` (np. `ZGL/2026/001` z ukośnikami i 3-cyfrowym numerem).

### 2. Warstwa błędu
*   **FRONTEND** (Zarządzanie stanem UI / przepływ nawigacji).
*   **BACKEND** (Niespójność formatu danych ze specyfikacją i seedami).

### 3. Pewność diagnozy
*   **100%** (potwierdzone w kodzie generatora na backendzie oraz w kodzie nawigacji i ViewModelu na frontendzie).

### 4. Powiązanie z lukami
*   Luka w spójności formatów danych pomiędzy dokumentacją, seedami i kodem produkcyjnym generatora.

### 5. Błędy pochodne
*   Ryzyko błędów parsowania lub filtrowania zgłoszeń na frontendzie/backendzie, jeśli inne moduły (np. wyszukiwarka) oczekują ściśle określonego formatu z ukośnikami.

---

## Bug 20 (TEST-020): Brak przypisanego lokalu w profilu mieszkańca oraz błędne saldo / brak transakcji

### 1. Diagnoza przyczyny źródłowej (Root Cause Analysis)
Błąd wynika z **poważnej luki architektonicznej (braku integracji)**. Frontend aplikacji nie posiada dedykowanego endpointu do pobierania profilu zalogowanego użytkownika (`GET /api/users/me`), przez co nie zna powiązań mieszkańca z lokalami w sposób bezpośredni.

Aby to obejść, zaimplementowano ryzykowną heurystykę w `UserApartmentService.resolveForResident()`:
1.  Pobierana jest lista zgłoszeń użytkownika (`GET /api/tickets`).
2.  Odczytywane są szczegóły pierwszego (najnowszego) zgłoszenia z listy (`GET /api/tickets/{id}`).
3.  Z pola `apartmentId` szczegółów zgłoszenia wyciągany jest identyfikator lokalu, który jest następnie cache'owany.

Heurystyka ta zawodzi w dwóch scenariuszach opisanych przez testera:
*   **Pierwsze wejście (błąd braku przypisanego lokalu):**
    Jeśli pierwszym zgłoszeniem na liście jest zgłoszenie wspólne (klatkowe lub budynkowe, np. `ZGL/2026/002` dotyczące przepalonej żarówki), to nie jest ono powiązane z żadnym konkretnym lokalem mieszkańca — jego pole `apartmentId` wynosi `null`. Wtedy `resolveForResident()` rzuca wyjątek `UserApartmentException("Brak przypisanego lokalu...")`, a użytkownik widzi ekran błędu.
*   **Drugie wejście (saldo 0,00 zł i brak transakcji):**
    Po utworzeniu nowego zgłoszenia lokalowego w `TEST-012`, to nowe zgłoszenie (posiadające poprawny `apartmentId` lokalu 1) staje się najnowszym zgłoszeniem na liście. Przy ponownym wejściu na ekran Finansów, heurystyka pobiera to zgłoszenie, odczytuje poprawny `apartmentId` i zapisuje go w cache. Następnie wysyłane jest żądanie pobrania transakcji `GET /api/apartments/{apartmentId}/transactions`.
    
    Jednak w tym momencie ujawnia się kolejna niespójność typów transakcji:
    W bazie danych w seedzie `V111` transakcje wstawiane są z typem `'NALEZNOSC'`, podczas gdy frontend (`FinancesDtos.kt` oraz `TransactionItem.kt`) i walidacja backendu (`FinancialTransactionService.java`) obsługują wyłącznie typy: `WPLATA`, `NALICZENIE`, `KOREKTA`. W rezultacie transakcje o nieznanym typie `'NALEZNOSC'` mogą nie być poprawnie mapowane lub sumowane, co prowadzi do wyświetlenia wyzerowanego salda i pustej listy w UI.

### 2. Warstwa błędu
*   **LUKA INTEGRACYJNA** (Brak endpointu profilu użytkownika na backendzie i błędna heurystyka frontendu).
*   **KONTRAKT / BAZA DANYCH** (Niezgodność typów transakcji `'NALEZNOSC'` vs `'NALICZENIE'`).

### 3. Pewność diagnozy
*   **Wysoka/100%** (heurystyka w `UserApartmentService.kt` wprost zależy od listy zgłoszeń, a typ `'NALEZNOSC'` w seedach SQL różni się od dopuszczalnych typów transakcji w kodzie).

### 4. Powiązanie z lukami
*   Brak endpointu `/api/users/me` (wymieniony w `audit/03_gap_analysis.md`).
*   Niespójność definicji słownikowych typów transakcji w bazie danych i kodzie aplikacji.

### 5. Błędy pochodne
*   Niemożność korzystania z modułu finansów przez mieszkańców, którzy nie utworzyli żadnego zgłoszenia lokalowego (zawsze otrzymają błąd braku lokalu).
*   Błędne obliczenia bilansu finansowego lokali z powodu niespójnych typów transakcji w bazie danych.
