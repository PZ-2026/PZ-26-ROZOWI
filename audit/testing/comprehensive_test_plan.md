# Kompleksowy Plan Testów Całej Aplikacji BlokUR (100% Pokrycia Funkcjonalnego)

Niniejszy dokument stanowi kompletny plan testowania wszystkich modułów i funkcjonalności systemu BlokUR. Każdy scenariusz opisuje cel, wymagane role, kroki wykonania oraz oczekiwane rezultaty na poziomie API i interfejsu użytkownika (UI).

---

## 1. Moduł: Autentykacja, Sesje i Bezpieczeństwo (Auth)

### Scenariusz 1.1: Logowanie i autoryzacja (M/Z/K)
*   **Cel:** Weryfikacja poprawnego logowania użytkowników o różnych rolach.
*   **Kroki:**
    1. Otwórz aplikację, wprowadź poprawny email i hasło.
    2. Kliknij "Zaloguj".
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/auth/login` zwraca kod 200 z `token`, `refreshToken` i `role`.
    *   **UI:** Ekran logowania przekierowuje na odpowiedni ekran główny (np. `ResidentMainScreen` dla Mieszkańca).

### Scenariusz 1.2: Błędne logowanie i blokada konta (Auth-Lock)
*   **Cel:** Weryfikacja zabezpieczenia przed atakiem brute-force.
*   **Kroki:**
    1. Wprowadź poprawny email, ale błędne hasło 3 razy z rzędu.
    2. Spróbuj zalogować się ponownie poprawnym hasłem.
*   **Oczekiwane rezultaty:**
    *   **API:** Trzecia i kolejne próby zwracają kod 423 Locked.
    *   **UI:** Komunikat na ekranie informuje o zablokowaniu konta na 15 minut.

### Scenariusz 1.3: Resetowanie hasła (Forgot-Password)
*   **Cel:** Weryfikacja procedury odzyskiwania dostępu.
*   **Kroki:**
    1. Na ekranie logowania kliknij "Zapomniałem hasła".
    2. Podaj email i kliknij "Wyślij".
    3. (Symulacja maila) Uruchom link z tokenem resetującym.
    4. Wpisz nowe hasło i zatwierdź.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/auth/forgot-password` zwraca 200. `POST /api/auth/reset-password` zwraca 200.
    *   **UI:** Ekran resetowania potwierdza zmianę i kieruje do logowania.

### Scenariusz 1.4: Automatyczne odświeżanie tokena (JWT Refresh)
*   **Cel:** Weryfikacja działania `TokenAuthenticator`.
*   **Kroki:**
    1. Zaloguj się. Zaczekaj na wygaśnięcie access tokena (lub ręcznie zmodyfikuj czas w bazie/kodzie).
    2. Wykonaj dowolną akcję wymagającą autoryzacji (np. odśwież listę zgłoszeń).
*   **Oczekiwane rezultaty:**
    *   **API:** Klient automatycznie wykonuje `POST /api/auth/refresh`, zapisuje nowy token i ponawia oryginalne żądanie bez udziału użytkownika.

### Scenariusz 1.5: Limit żądań (Rate Limiting)
*   **Cel:** Zabezpieczenie przed przeciążeniem.
*   **Kroki:**
    1. Wyślij szybko (w pętli) ponad 60 zapytań w ciągu minuty na `/api/auth/login`.
*   **Oczekiwane rezultaty:**
    *   **API:** Serwer zwraca kod 429 Too Many Requests z nagłówkiem `Retry-After`.

---

## 2. Moduł: Administracja Użytkownikami (Admin)

### Scenariusz 2.1: Zapraszanie i tworzenie użytkownika (Zarządca)
*   **Cel:** Tworzenie nowego konta w systemie.
*   **Kroki:**
    1. Zaloguj się jako Zarządca. Przejdź do: Profil -> Użytkownicy -> "+" (Dodaj).
    2. Wprowadź dane: Imię, Nazwisko, Email, Rola (MIESZKANIEC), przypisz Lokal.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/admin/users` zwraca 201 i wysyła maila z tokenem zaproszenia.
    *   **UI:** Nowy użytkownik pojawia się na liście ze statusem "Nieaktywny".

### Scenariusz 2.2: Akceptacja zaproszenia przez użytkownika
*   **Cel:** Aktywacja nowo utworzonego konta.
*   **Kroki:**
    1. Jako zaproszony użytkownik kliknij w link aktywacyjny z maila.
    2. Ustaw hasło i zatwierdź.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/auth/accept-invitation` zwraca 200.
    *   **UI:** Użytkownik może pomyślnie zalogować się nowym hasłem.

### Scenariusz 2.3: Edycja i dezaktywacja użytkownika (Zarządca)
*   **Cel:** Zarządzanie kontem aktywnego użytkownika.
*   **Kroki:**
    1. Jako Zarządca kliknij użytkownika na liście, zmień telefon lub przypisany lokal.
    2. Kliknij "Dezaktywuj".
*   **Oczekiwane rezultaty:**
    *   **API:** `PATCH /api/admin/users/{id}` (edycja) zwraca 200. `PATCH /api/admin/users/{id}/deactivate` (dezaktywacja) zwraca 204.
    *   **UI:** Status użytkownika na liście zmienia się na "Nieaktywny".

---

## 3. Moduł: Nieruchomości i Struktura Lokali (Properties)

### Scenariusz 3.1: Tworzenie struktury nieruchomości (Zarządca)
*   **Cel:** Konfiguracja nowej wspólnoty mieszkaniowej.
*   **Kroki:**
    1. Zaloguj się jako Zarządca. Przejdź do sekcji Lokale (Drzewo nieruchomości).
    2. Dodaj Wspólnotę (Property), następnie Budynek (Building), Klatkę (Staircase) i Lokal (Apartment).
*   **Oczekiwane rezultaty:**
    *   **API:** Kolejno `POST /api/properties` (201), `POST /api/buildings` (201), `POST /api/buildings/{id}/staircases` (201), `POST /api/staircases/{id}/apartments` (201).
    *   **UI:** Drzewo nieruchomości wyświetla pełną dodaną strukturę.

### Scenariusz 3.2: Wgrywanie logo wspólnoty (Zarządca)
*   **Cel:** Personalizacja wizualna wspólnoty.
*   **Kroki:**
    1. Wejdź w Profil -> Logo wspólnoty. Wybierz plik graficzny (PNG/JPG).
*   **Oczekiwane rezultaty:**
    *   **API:** `PATCH /api/properties/{id}/logo` zwraca 200.
    *   **UI:** Nowe logo wyświetla się w nagłówku aplikacji.

---

## 4. Moduł: Zgłoszenia Usterki (Tickets)

### Scenariusz 4.1: Pełny cykl obsługi zgłoszenia (M → Z → K → M)
*   **Cel:** Przetestowanie maszyny stanów zgłoszenia.
*   **Kroki:**
    1. **Mieszkaniec:** Tworzy zgłoszenie (`POST /api/tickets`).
    2. **Zarządca:** Przypisuje Konserwatora i datę (`PATCH /api/tickets/{id}/assign`).
    3. **Konserwator:** Rozpoczyna prace (`PATCH /api/tickets/{id}/start`).
    4. **Konserwator:** Wstrzymuje prace z notatką (`PATCH /api/tickets/{id}/suspend`).
    5. **Konserwator:** Wznawia i kończy prace (`POST /api/tickets/{id}/completion`).
    6. **Zarządca/Mieszkaniec:** Zamyka zgłoszenie (`PATCH /api/tickets/{id}/close`).
*   **Oczekiwane rezultaty:**
    *   Statusy przechodzą kolejno: `NOWE` -> `ZAPLANOWANO` -> `W_REALIZACJI` -> `WSTRZYMANO` -> `W_REALIZACJI` -> `ZAKONCZONE_DO_WERYFIKACJI` -> `ZAMKNIETE`.
    *   Zarządca może pobrać protokół odbioru PDF (`POST /api/pdf/work-acceptance-protocol`).

### Scenariusz 4.2: Komentarze i autoryzacja zdjęć w zgłoszeniach (TEST-011)
*   **Cel:** Weryfikacja uprawnień i komunikacji w zgłoszeniu.
*   **Kroki:**
    1. Utwórz zgłoszenie wspólne (klatkowe) jako Mieszkaniec A. Dodaj zdjęcie i komentarz.
    2. Zaloguj się jako Mieszkaniec B z tej samej klatki. Wejdź w szczegóły tego zgłoszenia.
*   **Oczekiwane rezultaty:**
    *   **API:** `GET /api/tickets/{id}/images` oraz `GET /api/tickets/{id}/comments` zwracają status 200 (brak błędu autoryzacji).
    *   **UI:** Mieszkaniec B widzi komentarze i zdjęcia wgrane przez Mieszkańca A.

### Scenariusz 4.3: Kategorie zgłoszeń i SLA (Zarządca)
*   **Cel:** Weryfikacja zarządzania czasem SLA.
*   **Kroki:**
    1. Jako Zarządca przejdź do: Profil -> Kategorie zgłoszeń.
    2. Utwórz nową kategorię, ustaw limit SLA (np. 48 godzin) i zatwierdź.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/admin/categories` (201), `PATCH /api/admin/categories/{id}/sla` (204).
    *   **UI:** Nowa kategoria pojawia się przy tworzeniu zgłoszenia przez mieszkańca.

---

## 5. Moduł: Liczniki i Odczyty Mediów (Meters)

### Scenariusz 5.1: Dodawanie i dezaktywacja licznika (Zarządca)
*   **Cel:** Weryfikacja ewidencji urządzeń pomiarowych.
*   **Kroki:**
    1. Przejdź do Lokale -> wybierz Lokal -> Liczniki -> Kliknij "+" (Dodaj).
    2. Wprowadź numer seryjny, typ (CIEPLA_WODA), datę i zatwierdź.
    3. Dezaktywuj licznik.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/apartments/{id}/meters` (201), `PATCH /api/meters/{id}/deactivate` (200).
    *   **UI:** Licznik pojawia się na liście jako aktywny, a po dezaktywacji zmienia status na nieaktywny.

### Scenariusz 5.2: Wprowadzanie i edycja odczytów (Z/K)
*   **Cel:** Rejestracja zużycia mediów.
*   **Kroki:**
    1. Jako Konserwator wejdź w licznik lokalu, dodaj nowy odczyt (wartość, data).
    2. Jako Zarządca wejdź w historię odczytów tego licznika i zmień wartość odczytu.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/apartments/{id}/meter-readings` (201), `PUT /api/meter-readings/{id}` (200).
    *   **UI:** Zużycie i historia odczytów są poprawnie wyliczane i formatowane.

---

## 6. Moduł: Finanse i Rozliczenia Lokalu (Finances)

### Scenariusz 6.1: Zestawienie finansowe mieszkańca (TEST-020)
*   **Cel:** Weryfikacja poprawności sald i transakcji.
*   **Kroki:**
    1. Zaloguj się jako Mieszkaniec (konto bez zgłoszeń serwisowych).
    2. Przejdź do zakładki Finanse.
*   **Oczekiwane rezultaty:**
    *   **API:** `GET /api/users/me` zwraca `apartmentId` zalogowanego użytkownika. `GET /api/apartments/{id}/transactions` zwraca historię.
    *   **UI:** Zakładka Finanse ładuje się poprawnie (brak komunikatu o braku lokalu). Saldo wyświetla się poprawnie, a transakcje mają właściwe kolory (np. naliczenie na czerwono).

### Scenariusz 6.2: Ręczne dodawanie transakcji i masowy import CSV (Zarządca)
*   **Cel:** Obsługa księgowa lokali.
*   **Kroki:**
    1. Jako Zarządca przejdź do Finanse -> Zestawienie sald. Wybierz import CSV.
    2. Zaimportuj plik z transakcjami.
    3. Dodaj ręczną transakcję (Wpłata) dla wybranego lokalu.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/finance/import` (200), `POST /api/apartments/{id}/transactions` (201).
    *   **UI:** Salda lokali zostają natychmiast zaktualizowane w zestawieniu.

---

## 7. Moduł: Dystrybucja Dokumentów (Documents)

### Scenariusz 7.1: Generowanie i pobieranie dokumentów (Z → M)
*   **Cel:** Weryfikacja masowej dystrybucji pism (roczne rozliczenie, podwyżka stawek).
*   **Kroki:**
    1. Jako Zarządca przejdź do: Profil -> Dystrybucja dokumentów.
    2. Wybierz "Roczne rozliczenie", wpisz kwotę/stawki i kliknij "Dystrybuuj".
    3. Zaloguj się jako Mieszkaniec, przejdź do: Finanse -> Dokumenty i pobierz najnowszy PDF.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/admin/documents/annual-settlement` (200), `GET /api/documents` (200), `GET /api/documents/{id}/download` zwraca strumień PDF.
    *   **UI:** Mieszkaniec widzi wygenerowany dokument i może go zapisać na urządzeniu.

---

## 8. Moduł: Ogłoszenia (Announcements)

### Scenariusz 8.1: Zarządzanie ogłoszeniami z załącznikami (Zarządca)
*   **Cel:** Publikacja ogłoszeń dla mieszkańców.
*   **Kroki:**
    1. Jako Zarządca przejdź do Ogłoszenia -> "+". Wpisz tytuł, treść, wybierz zasięg (np. Budynek A) i dodaj plik PDF jako załącznik.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/announcements` zwraca 201.
    *   **UI:** Ogłoszenie pojawia się na liście. Mieszkańcy Budynku A widzą ogłoszenie i mogą pobrać PDF. Mieszkańcy Budynku B go nie widzą.

---

## 9. Moduł: Uchwały i Głosowania (Resolutions)

### Scenariusz 9.1: Tworzenie uchwały i głosowanie (Z → M)
*   **Cel:** Przeprowadzenie głosowania nad uchwałą wspólnoty.
*   **Kroki:**
    1. Jako Zarządca przejdź do Uchwały -> Dodaj. Ustaw opcje (ZA / PRZECIW), zasięg i termin.
    2. Jako Mieszkaniec wejdź w Uchwały, wybierz nową uchwałę i oddaj głos.
    3. Spróbuj oddać głos ponownie.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/resolutions` (201), `POST /api/resolutions/{id}/vote` (204). Druga próba głosowania zwraca kod 409 Conflict.
    *   **UI:** Wyniki głosowania (procenty) aktualizują się na żywo w widoku Zarządcy.

---

## 10. Moduł: Przeglądy Techniczne (Inspections)

### Scenariusz 10.1: Terminarz przeglądów (Zarządca)
*   **Cel:** Planowanie obowiązkowych kontroli budowlanych.
*   **Kroki:**
    1. Jako Zarządca wejdź w Profil -> Przeglądy techniczne -> Dodaj.
    2. Ustaw typ (GAZOWY), datę wizyty i budynek.
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/inspections` zwraca 201.
    *   **UI:** Przegląd pojawia się na liście przeglądów u wszystkich mieszkańców tego budynku.

---

## 11. Moduł: Powiadomienia PUSH i Urządzenia (Notifications)

### Scenariusz 11.1: Rejestracja tokenu i konfiguracja zdarzeń
*   **Cel:** Dostarczanie powiadomień na urządzenie.
*   **Kroki:**
    1. Zaloguj się w aplikacji na telefonie/emulatorze.
    2. Jako Zarządca wejdź w: Profil -> Ustawienia powiadomień i wyłącz zdarzenie "Nowe Zgłoszenie".
*   **Oczekiwane rezultaty:**
    *   **API:** `POST /api/devices/register` wysyła token FCM (204). `PATCH /api/admin/notifications/settings/{eventType}` wyłącza wysyłkę (200).
