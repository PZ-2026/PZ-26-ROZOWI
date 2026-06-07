# Scenariusze testów przepływowych (Flow-Based Test Plan) aplikacji BlokUR

Niniejszy dokument zastępuje dotychczasowy uproszczony plan testów manualnych. Nowe scenariusze opierają się na kompletnych ścieżkach użytkowników (User Flows), angażując role Mieszkańca, Zarządcy i Konserwatora. Uwzględniają one weryfikację poprawnego działania poprawek błędów TEST-011, TEST-012 oraz TEST-020.

---

## Flow A: Cykl życia zgłoszenia serwisowego (Mieszkaniec → Zarządca → Konserwator → Mieszkaniec)

**Cel:** Weryfikacja tworzenia zgłoszenia, automatycznej reaktywności listy, formatu numerów, przypisywania zadań i poprawnego przejścia statusów.

### Krok 1: Tworzenie zgłoszenia (Mieszkaniec)
*   **Dane testowe:** Użytkownik `jan.kowalski@gmail.com`
*   **Akcja:** Wejdź na ekran zgłoszeń, kliknij przycisk tworzenia nowego zgłoszenia, wybierz kategorię (np. Hydraulika), wprowadź tytuł i opis, a następnie zatwierdź.
*   **Oczekiwany rezultat:** 
    1.  Po kliknięciu "Zapisz", aplikacja automatycznie wraca do ekranu listy zgłoszeń.
    2.  Nowo utworzone zgłoszenie pojawia się natychmiast na liście (bez konieczności pull-to-refresh).
    3.  Numer zgłoszenia ma format zgodny ze specyfikacją: `ZGL/RRRR/NNN` (np. `ZGL/2026/003`).

### Krok 2: Przypisanie konserwatora (Zarządca)
*   **Dane testowe:** Użytkownik `zarzadca1@blokur.pl`
*   **Akcja:** Zaloguj się jako Zarządca. Wejdź na listę zgłoszeń, znajdź nowo utworzone zgłoszenie `ZGL/RRRR/NNN`, przejdź do szczegółów i kliknij "Przypisz konserwatora". Wybierz dostępnego konserwatora, ustaw datę planowanej wizyty i zatwierdź.
*   **Oczekiwany rezultat:** Status zgłoszenia zmienia się na `ZAPLANOWANO`. Data wizyty i przypisany konserwator są widoczne w szczegółach.

### Krok 3: Realizacja i zakończenie prac (Konserwator)
*   **Dane testowe:** Przypisany konserwator (np. `konserwator1@blokur.pl`)
*   **Akcja:** 
    1.  Zaloguj się jako Konserwator. Znajdź zgłoszenie na swojej liście, wejdź w szczegóły i kliknij "Rozpocznij prace" (status zmienia się na `W_REALIZACJI`).
    2.  Po zakończeniu prac kliknij "Zgłoś zakończenie", wpisz opis wykonanych prac i opcjonalnie dodaj zdjęcie z galerii/aparatu.
*   **Oczekiwany rezultat:** Status zgłoszenia zmienia się na `ZAKONCZONE_DO_WERYFIKACJI`.

### Krok 4: Weryfikacja i zamknięcie (Mieszkaniec & Zarządca)
*   **Akcja:** Zaloguj się ponownie jako Mieszkaniec (`jan.kowalski@gmail.com`). Wejdź w szczegóły zgłoszenia.
*   **Oczekiwany rezultat:** Mieszkaniec widzi szczegóły zgłoszenia, opis wykonanych prac oraz wgrane przez konserwatora zdjęcia bez żadnych wyjątków bezpieczeństwa (brak błędu `SecurityException`).

---

## Flow B: Finanse i rozliczenia lokalu (Zarządca → Mieszkaniec)

**Cel:** Weryfikacja pobierania danych finansowych bezpośrednio z profilu (bez heurystyk zgłoszeniowych), poprawnej interpretacji salda i mapowania typów transakcji.

### Krok 1: Masowy import transakcji (Zarządca)
*   **Dane testowe:** Użytkownik `zarzadca1@blokur.pl` + przygotowany plik CSV z transakcjami (zawierający transakcje typu `'NALICZENIE'` o wartości dodatniej, np. 350.00 zł, oraz typu `'WPLATA'`, np. 350.00 zł dla lokalu 1 w Budynku A).
*   **Akcja:** Przejdź do panelu finansowego zarządcy, wybierz import CSV, wskaż plik i zatwierdź import.
*   **Oczekiwany rezultat:** Import kończy się sukcesem. Aplikacja informuje o liczbie pomyślnie zaimportowanych rekordów.

### Krok 2: Sprawdzenie salda w panelu zarządcy (Zarządca)
*   **Akcja:** W panelu finansowym zarządcy wejdź w zestawienie sald lokali. Znajdź Budynek A, Klatkę 1, lokal 1.
*   **Oczekiwany rezultat:** Saldo lokalu odzwierciedla zaimportowane transakcje (naliczenia obciążają saldo, wpłaty je pokrywają).

### Krok 3: Weryfikacja finansów z perspektywy mieszkańca (Mieszkaniec)
*   **Dane testowe:** Użytkownik `jan.kowalski@gmail.com` (Mieszkaniec lokalu 1)
*   **Scenariusz A (Użytkownik bez zgłoszeń):** Uruchom test na czystej bazie, gdzie Jan Kowalski nie ma żadnych zgłoszeń serwisowych w systemie.
*   **Scenariusz B (Użytkownik ze zgłoszeniem wspólnym):** Uruchom test w sytuacji, gdy Jan Kowalski ma na swojej liście tylko jedno zgłoszenie wspólne (klatkowe, np. `ZGL/2026/002` z `apartment_id = null`).
*   **Akcja:** Zaloguj się jako Jan Kowalski i przejdź do zakładki Finanse.
*   **Oczekiwany rezultat:**
    1.  W obu scenariuszach ekran Finansów ładuje się poprawnie (brak komunikatu o braku przypisanego lokalu dzięki pobraniu ID bezpośrednio z profilu `/api/users/me`).
    2.  Wyświetla się aktualne saldo lokalu (np. 150,50 zł lub zaktualizowane po imporcie).
    3.  Lista transakcji ładuje się poprawnie. Transakcje typu `NALICZENIE` są oznaczone kolorem czerwonym (obciążenie), a transakcje typu `WPLATA` kolorem zielonym (uznanie). Brak nieznanych typów (typu `'NALEZNOSC'`), które byłyby pomijane w prezentacji UI.

---

## Flow C: Bezpieczeństwo i widoczność zgłoszeń wspólnych (Mieszkaniec A → Mieszkaniec B)

**Cel:** Weryfikacja, czy mieszkańcy mogą przeglądać zgłoszenia dotyczące ich klatki schodowej lub budynku oraz załączone zdjęcia bez naruszenia zasad bezpieczeństwa.

### Krok 1: Utworzenie zgłoszenia wspólnego ze zdjęciem (Mieszkaniec A)
*   **Dane testowe:** Użytkownik `anna.nowak@gmail.com` (Mieszkaniec lokalu 6 w Klatce 2 Budynku A).
*   **Akcja:** Zaloguj się jako Anna Nowak. Utwórz zgłoszenie dotyczące wspólnej przestrzeni budynku (np. "Uszkodzone drzwi wejściowe do Budynku A"). Wgraj zdjęcie obrazujące uszkodzenie.
*   **Oczekiwany rezultat:** Zgłoszenie zostaje pomyślnie utworzone. Backend zapisuje zgłoszenie jako powiązane z budynkiem A i klatką 2, ale z `apartment_id = null` (przestrzeń wspólna).

### Krok 2: Odczyt zgłoszenia wspólnego i zdjęć (Mieszkaniec B)
*   **Dane testowe:** Użytkownik `piotr.wisniewski@wp.pl` (Mieszkaniec lokalu 7 w Klatce 2 Budynku A).
*   **Akcja:** Zaloguj się jako Piotr Wiśniewski. Wejdź na listę zgłoszeń, znajdź zgłoszenie wspólne utworzone przez Annę Nowak ("Uszkodzone drzwi wejściowe"), wejdź w jego szczegóły.
*   **Oczekiwany rezultat:** 
    1.  Piotr widzi to zgłoszenie na liście, ponieważ mieszka w tej samej klatce/budynku.
    2.  Po wejściu w szczegóły, dane zgłoszenia oraz wgrane przez Annę Nowak zdjęcia ładują się poprawnie.
    3.  Backend nie rzuca błędu `SecurityException` (mieszkaniec ma uprawnienia do podglądu zdjęć w zgłoszeniach, które są dla niego widoczne na poziomie budynku/klatki).
