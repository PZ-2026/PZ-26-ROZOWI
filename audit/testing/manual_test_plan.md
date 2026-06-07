# Plan testowania manualnego — BlokUR

**Wersja:** 1.0 | **Data:** 2026-06-07 | **Tester:** ___________

## Legenda statusów zgłoszeń
- `NOWE` — zgłoszenie zarejestrowane, brak konserwatora
- `W_REALIZACJI` — przypisany konserwator, prace w toku
- `WSTRZYMANO` — praca wstrzymana (przez konserwatora)
- `ZAKONCZONE_DO_WERYFIKACJI` — konserwator zgłosił zakończenie, czeka na zamknięcie przez zarządcę
- `ZAMKNIETE` — zarządca zatwierdził i zamknął
- `ODRZUCONE` — zarządca odrzucił zgłoszenie

## Kluczowe konta testowe (skrót)
| Email | Hasło | Rola |
|-------|-------|------|
| `admin1@blokur.pl` | `haslo123` | ZARZĄDCA |
| `mock.zarzadca1@blokur.pl` | `Haslo123` | ZARZĄDCA |
| `hydraulik@blokur.pl` | `haslo123` | KONSERWATOR |
| `mock.hydraulik@blokur.pl` | `Haslo123` | KONSERWATOR |
| `jan.kowalski@gmail.com` | `haslo123` | MIESZKANIEC (Lokal 1, Budynek A) |
| `mock.mieszkaniec1@test.pl` | `Haslo123` | MIESZKANIEC (Lokal 1, Blok 1) |
| `mock.inactive@test.pl` | `Haslo123` | MIESZKANIEC (konto DEZAKTYWOWANE) |

---

# GRUPA AUTH — Testy logowania

---

## TEST-001 — Logowanie poprawnymi danymi jako MIESZKANIEC

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Backend uruchomiony. Aplikacja zainstalowana. Użytkownik niezalogowany (ekran logowania).

**Kroki:**
1. Uruchom aplikację — powinien pojawić się ekran logowania z polami Email i Hasło.
2. W polu Email wpisz: `jan.kowalski@gmail.com`
3. W polu Hasło wpisz: `haslo123`
4. Stuknij przycisk „Zaloguj się".
5. Poczekaj na zakończenie ładowania (przycisk powinien być disabled, tekst zmienia się na „Logowanie…").

**Oczekiwany rezultat:**
- Ekran logowania znika.
- Pojawia się główny ekran aplikacji z dolną nawigacją zawierającą zakładki: **Zgłoszenia, Finanse, Uchwały, Ogłoszenia, Profil** (5 pozycji).
- Aktywna zakładka to Profil (startDestination).
- Brak żadnego komunikatu błędu.

**Możliwe błędy:**
- Jeśli po stuknięciu „Zaloguj się" nic się nie dzieje lub pojawia się „Błąd logowania" → BUG: logowanie nie działa dla prawidłowych danych.
- Jeśli po zalogowaniu widać zakładki ZARZĄDCY (Lokale, Użytkownicy) zamiast zakładek MIESZKAŃCA → BUG: błędne przypisanie roli.
- Jeśli pojawia się 5 zakładek z innymi nazwami → BUG: błędna nawigacja dla roli MIESZKANIEC.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-002 — Logowanie poprawnymi danymi jako ZARZĄDCA

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Ekran logowania, użytkownik niezalogowany.

**Kroki:**
1. W polu Email wpisz: `admin1@blokur.pl`
2. W polu Hasło wpisz: `haslo123`
3. Stuknij „Zaloguj się".

**Oczekiwany rezultat:**
- Pojawia się główny ekran z dolną nawigacją: **Zgłoszenia, Lokale, Uchwały, Użytkownicy, Profil** (5 pozycji).
- Zakładka „Lokale" i „Użytkownicy" są widoczne — to odróżnia rolę ZARZĄDCA od MIESZKAŃCA.

**Możliwe błędy:**
- Jeśli dolna nawigacja pokazuje „Finanse" lub „Ogłoszenia" zamiast „Lokale"/„Użytkownicy" → BUG: błędna rola po zalogowaniu.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-003 — Logowanie poprawnymi danymi jako KONSERWATOR

**Rola:** KONSERWATOR  
**Konto testowe:** `hydraulik@blokur.pl` / `haslo123`  
**Warunki wstępne:** Ekran logowania.

**Kroki:**
1. Wpisz email: `hydraulik@blokur.pl`, hasło: `haslo123`.
2. Stuknij „Zaloguj się".

**Oczekiwany rezultat:**
- Pojawia się główny ekran z dolną nawigacją zawierającą **tylko 2 zakładki: Zgłoszenia, Profil**.
- Brak zakładek Finanse, Ogłoszenia, Lokale, Użytkownicy.

**Możliwe błędy:**
- Jeśli widoczna jest więcej niż 2 zakładki → BUG: nieprawidłowy zestaw zakładek dla roli KONSERWATOR.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-004 — Logowanie błędnym hasłem — komunikat błędu

**Rola:** (dowolna)  
**Konto testowe:** `jan.kowalski@gmail.com` / `ZleHaslo999`  
**Warunki wstępne:** Ekran logowania.

**Kroki:**
1. Wpisz email: `jan.kowalski@gmail.com`
2. Wpisz hasło: `ZleHaslo999` (celowo błędne)
3. Stuknij „Zaloguj się".

**Oczekiwany rezultat:**
- Aplikacja pozostaje na ekranie logowania.
- Pojawia się komunikat błędu (inline przy polu hasła LUB snackbar) informujący o nieprawidłowych danych.
- Komunikat powinien być po polsku (np. „Nieprawidłowy email lub hasło").
- Pola Email i Hasło są ponownie aktywne (można wpisać nowe dane).

**Możliwe błędy:**
- Jeśli aplikacja przenosi do głównego ekranu → KRYTYCZNY BUG: logowanie bez autoryzacji.
- Jeśli komunikat błędu jest po angielsku lub zawiera surowy JSON → BUG: brak lokalizacji błędów.
- Jeśli po 3 próbach nie pojawia się informacja o blokadzie konta (backend blokuje po 3 nieudanych próbach na 15 min) → BUG: brak obsługi HTTP 423.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-005 — Reset hasła — flow z emailem

**Rola:** (dowolna)  
**Konto testowe:** `jan.kowalski@gmail.com`  
**Warunki wstępne:** Ekran logowania. Serwer email skonfigurowany (lub możliwość podejrzenia logów backendu).

**Kroki:**
1. Na ekranie logowania stuknij link „Zapomniałem hasła".
2. Pojawia się ekran „Zapomniane hasło" z polem Email.
3. Wpisz: `jan.kowalski@gmail.com`
4. Stuknij przycisk wysyłania (np. „Wyślij link").
5. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- Pojawia się komunikat potwierdzający wysłanie emaila (np. „Jeśli konto istnieje, email zostanie wysłany").
- Komunikat jest **taki sam niezależnie od tego, czy email istnieje** (backend zawsze zwraca 200 — celowe zabezpieczenie przed enumeration).
- Link „Wróć" lub „Wróć do logowania" działa i przenosi z powrotem na ekran logowania.

**Możliwe błędy:**
- Jeśli aplikacja informuje wprost „Email nie istnieje" → BUG: ujawnianie informacji o kontach.
- Jeśli przycisk wysyłania nie jest disabled podczas ładowania → drobny BUG UX.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-006 — Wylogowanie i ponowne logowanie

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Użytkownik jest zalogowany (zaloguj się wg TEST-001).

**Kroki:**
1. Przejdź do zakładki **Profil** (ostatnia pozycja w dolnej nawigacji).
2. Znajdź i stuknij przycisk wylogowania (ikona lub napis „Wyloguj" w prawym górnym rogu TopBar).
3. Potwierdź wylogowanie jeśli pojawi się dialog.
4. Poczekaj na przekierowanie.
5. Ponownie zaloguj się danymi: `jan.kowalski@gmail.com` / `haslo123`.

**Oczekiwany rezultat:**
- Po wylogowaniu aplikacja wraca do ekranu logowania. Nie można cofnąć się do poprzedniego ekranu (przycisk Back nie powinien działać).
- Po ponownym zalogowaniu pojawia się ekran główny z zakładkami MIESZKAŃCA — identyczny jak po pierwszym logowaniu.

**Możliwe błędy:**
- Jeśli po wylogowaniu można cofnąć się przyciskiem Back do poprzedniego ekranu → KRYTYCZNY BUG: brak czyszczenia stosu nawigacji.
- Jeśli po ponownym zalogowaniu wyświetlane są dane z poprzedniej sesji (np. stare zgłoszenia bez odświeżenia) → BUG: cache sesji.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________


---

# GRUPA MIESZKANIEC — Zgłoszenia

---

## TEST-010 — Wyświetlenie listy zgłoszeń jako MIESZKANIEC

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Użytkownik zalogowany (wg TEST-001).

**Kroki:**
1. Stuknij zakładkę **Zgłoszenia** w dolnej nawigacji.
2. Poczekaj na załadowanie listy.

**Oczekiwany rezultat:**
- Widoczna jest lista zgłoszeń należących do Jana Kowalskiego.
- Na liście powinno być widoczne przynajmniej zgłoszenie **ZGL/2026/001 — „Wyciek pod zlewem"** ze statusem **W_REALIZACJI**.
- Każdy element listy pokazuje: numer zgłoszenia, tytuł, status (badge/etykieta), datę.
- Daty wyświetlane są w formacie **dd.MM.yyyy** (np. `07.06.2026`), NIE w formacie ISO `2026-06-07`.
- Widoczny jest przycisk FAB (duży okrągły przycisk) „+" lub „Utwórz zgłoszenie" w prawym dolnym rogu.
- Brak przycisku edycji/usunięcia na elementach listy (mieszkaniec nie może zarządzać zgłoszeniami innych).

**Możliwe błędy:**
- Jeśli lista jest pusta mimo istniejących zgłoszeń w bazie → BUG: filtrowanie zgłoszeń nie działa lub brak połączenia z backendem.
- Jeśli daty wyświetlane są w formacie ISO (`2026-03-15`) zamiast `15.03.2026` → BUG UX (poważny, znany z audytu).
- Jeśli widoczne są zgłoszenia innych użytkowników → KRYTYCZNY BUG: brak izolacji danych.
- Jeśli FAB jest niewidoczny dla MIESZKAŃCA → BUG: błędna kontrola roli.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-011 — Otwarcie szczegółów istniejącego zgłoszenia

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany, na liście zgłoszeń (wg TEST-010). Widoczne ZGL/2026/001.

**Kroki:**
1. Stuknij element **ZGL/2026/001 — „Wyciek pod zlewem"** na liście.
2. Poczekaj na załadowanie ekranu szczegółów.
3. Przejrzyj wszystkie sekcje: nagłówek, status, komentarze, zdjęcia.

**Oczekiwany rezultat:**
- Wyświetla się ekran szczegółów z danymi:
  - Numer: **ZGL/2026/001**
  - Tytuł: **Wyciek pod zlewem**
  - Status: **W_REALIZACJI** (wyświetlony po polsku, np. „W realizacji")
  - Kategoria: **Hydraulika**
  - Opis: „Woda kapie spod syfonu w kuchni."
- W sekcji komentarzy widoczny jest **jeden komentarz publiczny**: „Będę jutro około 10:00. Proszę o zapewnienie dostępu do kuchni." (autor: Marian Rura / hydraulik).
- Komentarz **wewnętrzny** (admin: „pies może być agresywny") **NIE jest widoczny** dla MIESZKAŃCA.
- Widoczna jest sekcja zdjęć (może być pusta lub z miniaturami).
- Brak przycisków zarządzania (przypisz/odrzuć/zamknij) — te są tylko dla ZARZĄDCY.
- Widoczny przycisk „Wróć" (←) w górnym pasku.

**Możliwe błędy:**
- Jeśli widoczny jest komentarz wewnętrzny „pies może być agresywny" → KRYTYCZNY BUG: wyciek danych wewnętrznych.
- Jeśli opis zgłoszenia jest inny niż w seedzie → BUG: złe mapowanie danych.
- Jeśli sekcja komentarzy pokazuje błąd zamiast listy → BUG: ładowanie komentarzy.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-012 — Utworzenie nowego zgłoszenia z kategorią i opisem

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany, na liście zgłoszeń.

**Kroki:**
1. Stuknij przycisk FAB „+" / „Utwórz zgłoszenie".
2. Pojawia się formularz nowego zgłoszenia.
3. W polu **Tytuł** wpisz: `Test zgłoszenia manualnego`
4. W polu **Opis** wpisz: `To jest zgłoszenie testowe stworzone podczas testów manualnych.`
5. Wybierz kategorię: **Hydraulika** z listy/dropdown.
6. Stuknij przycisk „Zgłoś usterkę" lub „Wyślij".
7. Poczekaj na zakończenie operacji.

**Oczekiwany rezultat:**
- Po sukcesie pojawia się dialog potwierdzenia lub snackbar „Zgłoszenie zostało utworzone".
- Aplikacja powraca do listy zgłoszeń.
- Na liście widoczne jest nowe zgłoszenie „Test zgłoszenia manualnego" ze statusem **NOWE**.
- Nowe zgłoszenie ma automatycznie nadany numer w formacie `ZGL/2026/XXX`.

**Możliwe błędy:**
- Jeśli przycisk „Zgłoś usterkę" jest aktywny gdy tytuł/opis jest pusty → BUG: brak walidacji.
- Jeśli po wysłaniu aplikacja pokazuje błąd 403 → KRYTYCZNY BUG: backend nie akceptuje zgłoszeń od mieszkańca (lub problem z JWT).
- Jeśli po sukcesie lista nie odświeża się → BUG: brak aktualizacji stanu po tworzeniu.
- Jeśli kategoria nie ładuje się (pusta lista dropdown) → BUG: GET /api/categories nie działa.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-013 — Dodanie komentarza do zgłoszenia

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany, na ekranie szczegółów ZGL/2026/001 (wg TEST-011).

**Kroki:**
1. Przewiń ekran szczegółów do sekcji **Komentarze**.
2. W polu tekstowym komentarza wpisz: `To jest komentarz testowy od mieszkańca.`
3. Upewnij się, że przełącznik „Wewnętrzny" (jeśli istnieje) jest wyłączony / niewidoczny dla roli MIESZKANIEC.
4. Stuknij przycisk wysyłania (np. ikonka papierowego samolotu lub „Wyślij").
5. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- Komentarz „To jest komentarz testowy od mieszkańca." pojawia się na liście komentarzy.
- Autor komentarza wyświetlany jako Jan Kowalski lub email `jan.kowalski@gmail.com`.
- Pole komentarza zostaje wyczyszczone po wysłaniu.
- Brak przełącznika „Wewnętrzny" lub jest on ukryty/niewidoczny — mieszkaniec nie może dodawać komentarzy wewnętrznych.

**Możliwe błędy:**
- Jeśli komentarz nie pojawia się po wysłaniu (ale błąd nie jest pokazany) → BUG: ciche niepowodzenie HTTP (znany bug z audytu — brak sprawdzenia `response.isSuccessful()`).
- Jeśli widoczny jest przełącznik „Wewnętrzny" dla MIESZKAŃCA → BUG UX: błędna kontrola widoczności.
- Jeśli pole nie czyści się po wysłaniu → drobny BUG UX.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-014 — Wyświetlenie zdjęć przy zgłoszeniu

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany, na ekranie szczegółów ZGL/2026/001.

**Kroki:**
1. Przewiń ekran szczegółów do sekcji **Zdjęcia**.
2. Sprawdź co widać w sekcji zdjęć.
3. Jeśli widoczne są miniatury — spróbuj stuknąć jedną z nich.
4. Jeśli sekcja jest pusta — zapisz to jako wynik.

**Oczekiwany rezultat:**
- Sekcja zdjęć jest widoczna (może być pusta jeśli nikt nie dodał zdjęć do ZGL/2026/001 w seedach).
- Jeśli zdjęcia istnieją: widoczne są miniatury obrazów (nie emoji 📷 zamiast obrazu).
- Po stuknięciu miniatury otwiera się pełnoekranowy podgląd zdjęcia z możliwością przejścia prev/next.

**Możliwe błędy:**
- Jeśli zamiast miniatur widać tylko emoji 📷 lub tekst „[zdjęcie]" → BUG: obrazy nie ładują się przez endpoint `GET /api/images/{id}` (znany krytyczny bug z audytu).
- Jeśli stuknięcie miniatury nic nie robi (brak pełnoekranowego podglądu) → BUG UX: brak galerii pełnoekranowej (zgłoszone jako naprawione w 06_final_report ACTION-001 — weryfikujemy czy faktycznie działa).

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

# GRUPA MIESZKANIEC — Finanse i reszta

---

## TEST-020 — Wyświetlenie salda i transakcji

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany. Użytkownik ma lokal 1 w Budynku A, saldo +150,50 zł, 3 transakcje w seedzie V111.

**Kroki:**
1. Stuknij zakładkę **Finanse** w dolnej nawigacji.
2. Sprawdź co widać na ekranie głównym finansów.
3. Jeśli widoczne jest saldo — zanotuj kwotę.
4. Jeśli widoczny jest link/przycisk „Transakcje" lub „Kartoteka" — stuknij go.
5. Sprawdź listę transakcji.

**Oczekiwany rezultat:**
- Ekran finansów pokazuje saldo lokalu Jana Kowalskiego: **+150,50 zł** (lub w pobliżu tej wartości jeśli dane testowe były uzupełniane).
- Kwota wyświetlana jest w formacie polskim: `150,50 zł` (przecinek, nie kropka; „zł", nie „PLN").
- Na liście transakcji widoczne są 3 transakcje z seeda V111:
  - `Czynsz 04/2026` — 450,00 zł — 01.04.2026 — typ NALEŻNOŚĆ
  - `Przelew Czynsz 04/2026` — 450,00 zł — 05.04.2026 — typ WPŁATA
  - `Rozliczenie wody 03/2026` — 12,50 zł — 10.04.2026 — typ NALEŻNOŚĆ
- Daty transakcji w formacie `dd.MM.yyyy`.

**Możliwe błędy:**
- Jeśli ekran finansów pokazuje **0,00 zł** i pustą listę bez żadnego wywołania API → KRYTYCZNY BUG: znany z audytu (FinancesScreen hub nie ładuje transakcji dla mieszkańca).
- Jeśli kwoty wyświetlane są jako `150.50 PLN` (kropka, PLN) → BUG UX: brak polskiego formatowania walut.
- Jeśli widoczne są transakcje innych użytkowników → KRYTYCZNY BUG: brak izolacji danych.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-021 — Pobranie dokumentu PDF

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany. Muszą istnieć dokumenty PDF przypisane do lokalu (np. wygenerowane przez zarządcę — jeśli brak, zanotuj i pomiń test lub poproś zarządcę o wygenerowanie dokumentu przez TEST-053).

**Kroki:**
1. Stuknij zakładkę **Finanse**.
2. Stuknij opcję **Dokumenty** (przycisk lub zakładka w module finansów).
3. Na liście dokumentów stuknij przycisk „Pobierz" lub ikonę PDF przy pierwszym dokumencie.
4. Poczekaj na pobranie.

**Oczekiwany rezultat:**
- System pobiera plik PDF i otwiera go w zewnętrznej przeglądarce PDF (systemowy Intent ACTION_VIEW).
- Plik PDF jest czytelny i zawiera dane dokumentu.
- Brak błędu autoryzacji (JWT jest prawidłowo dołączany do żądania pobierania).

**Możliwe błędy:**
- Jeśli lista dokumentów jest pusta → Nie bug (brak danych w seedach); poproś zarządcę o dystrybucję.
- Jeśli pobieranie kończy się błędem 401/403 → BUG: JWT nie dołączany przy pobieraniu dokumentu.
- Jeśli plik otwiera się pusty lub uszkodzony → BUG: problem z serwowaniem pliku.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-022 — Lista uchwał i oddanie głosu

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany. W seedzie istnieje uchwała „Fundusz remontowy 2026" dla Budynku A (Solaris) z 3 opcjami: Za / Przeciw / Wstrzymuję się. Termin głosowania aktywny (+14 dni od seeda).

**Kroki:**
1. Stuknij zakładkę **Uchwały**.
2. Sprawdź listę uchwał — powinna być widoczna uchwała „Fundusz remontowy 2026".
3. Stuknij uchwałę „Fundusz remontowy 2026".
4. Na ekranie szczegółów sprawdź opcje głosowania: Za / Przeciw / Wstrzymuję się.
5. Zaznacz opcję **„Za"**.
6. Stuknij przycisk „Oddaj głos".
7. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- Na liście widoczna jest co najmniej uchwała **„Fundusz remontowy 2026"** (budynek A).
- Ekran szczegółów pokazuje tytuł, opis i 3 opcje głosowania.
- Opcje są wybieralne (RadioButton lub podobny komponent).
- Po oddaniu głosu pojawia się potwierdzenie (snackbar lub zmiana stanu w UI).
- Przycisk „Oddaj głos" jest zablokowany podczas wysyłania.
- Po zagłosowaniu **nie można zagłosować ponownie** (opcje są zablokowane lub pojawia się info o oddanym głosie).

**Możliwe błędy:**
- Jeśli uchwała dla Budynku A nie jest widoczna dla Jana Kowalskiego (mieszkańca Budynku A) → BUG: filtrowanie uchwał po budynku nie działa.
- Jeśli można oddać głos wielokrotnie → KRYTYCZNY BUG: brak ochrony przed wielokrotnym głosowaniem (backend powinien zwrócić 409).
- Jeśli uchwała „Monitoring w windzie" (Budynek B) jest widoczna dla Jana Kowalskiego (Budynek A) → BUG: błędna izolacja uchwał po budynku.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-023 — Lista ogłoszeń i otwarcie załącznika

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany. W seedzie istnieje kilka ogłoszeń (globalne i dla Budynku A).

**Kroki:**
1. Stuknij zakładkę **Ogłoszenia**.
2. Sprawdź listę ogłoszeń.
3. Odnotuj które ogłoszenia są widoczne.
4. Jeśli któreś ogłoszenie ma przycisk „Pobierz załącznik" lub ikonę PDF — stuknij go.
5. Sprawdź czy plik PDF się otwiera.

**Oczekiwany rezultat:**
- Na liście widoczne są ogłoszenia skierowane do: WSZYSCY (np. „Modernizacja oświetlenia", „Piknik sąsiedzki") oraz skierowane do Budynku A (np. „Przegląd kominiarski").
- Ogłoszenia skierowane wyłącznie do innych budynków (np. Rezydencji Parkowej) **NIE są widoczne**.
- Każde ogłoszenie pokazuje: tytuł, treść, datę.
- Brak przycisku „Usuń" i FAB „+" — mieszkaniec tylko czyta ogłoszenia.
- Jeśli ogłoszenie ma załącznik PDF: po stuknięciu „Pobierz załącznik" PDF otwiera się poprawnie.

**Możliwe błędy:**
- Jeśli lista jest całkowicie pusta → BUG: GET /api/announcements nie działa.
- Jeśli widoczne są ogłoszenia innych budynków (np. z Kamienicy Różanej gdy nie dotyczy Jana) → BUG: brak filtrowania.
- Jeśli stuknięcie pobierania załącznika nie powoduje żadnej reakcji → BUG: download attachment nie działa.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________


---

# GRUPA KONSERWATOR

---

## TEST-030 — Lista zgłoszeń (tylko przypisane)

**Rola:** KONSERWATOR  
**Konto testowe:** `hydraulik@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany jako Marian Rura (hydraulik). W seedzie przypisane: ZGL/2026/001 (Wyciek pod zlewem, W_REALIZACJI).

**Kroki:**
1. Stuknij zakładkę **Zgłoszenia** (jedyna zakładka poza Profilem).
2. Poczekaj na załadowanie listy.
3. Przejrzyj widoczne zgłoszenia.

**Oczekiwany rezultat:**
- Na liście widoczne jest zgłoszenie **ZGL/2026/001 — „Wyciek pod zlewem"** ze statusem **W_REALIZACJI**.
- Zgłoszenia nieprzypisane do Mariana Rury (np. ZGL/2026/002 przypisany do elektryka) **NIE są widoczne**.
- Brak przycisku FAB tworzenia zgłoszenia — konserwator nie może tworzyć zgłoszeń.
- Brak zakładek Finanse, Ogłoszenia, Lokale — konserwator ma tylko Zgłoszenia i Profil.

**Możliwe błędy:**
- Jeśli widoczne są zgłoszenia przypisane do innych konserwatorów → KRYTYCZNY BUG: brak izolacji danych konserwatora.
- Jeśli lista jest pusta mimo przypisanego ZGL/2026/001 → BUG: filtrowanie po `assigned_to` nie działa.
- Jeśli widoczny jest FAB tworzenia → BUG: błędna kontrola roli w UI.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-031 — Zmiana statusu zgłoszenia na „W realizacji"

**Rola:** KONSERWATOR  
**Konto testowe:** `mock.hydraulik@blokur.pl` / `Haslo123`  
**Warunki wstępne:** Zalogowany jako Stanisław Wróbel. W seedzie przypisane: ZGL/TEST/006 (Przeciek z dachu, W_REALIZACJI), ZGL/TEST/011 (Wymiana uszczelki, ZAKONCZONE_DO_WERYFIKACJI), ZGL/TEST/012 (Przegląd gazowy, ZAKONCZONE_DO_WERYFIKACJI).

**Uwaga:** ZGL/TEST/006 jest już w statusie W_REALIZACJI. Żeby przetestować akcję START potrzebne jest zgłoszenie w statusie NOWE przypisane do tego konserwatora. Poproś zarządcę (`mock.zarzadca1@blokur.pl`) o przypisanie ZGL/TEST/001 do `mock.hydraulik@blokur.pl` (wg TEST-041), a następnie wróć do tego testu.

**Kroki (po przypisaniu ZGL/TEST/001 do mock.hydraulik):**
1. Na liście zgłoszeń stuknij **ZGL/TEST/001 — „Brak ciepłej wody"** (status NOWE).
2. Znajdź przycisk/opcję „Rozpocznij pracę" lub otwórz menu akcji (np. sheet lub przycisk w dolnej części ekranu).
3. Stuknij **„Rozpocznij pracę"** lub „Start".
4. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- Status zgłoszenia zmienia się na **W_REALIZACJI**.
- W historii zgłoszenia pojawia się wpis o zmianie statusu.
- Ekran szczegółów odświeża się pokazując nowy status.

**Możliwe błędy:**
- Jeśli przycisk „Rozpocznij pracę" nie jest widoczny dla konserwatora na zgłoszeniu NOWE → BUG: brak UI dla akcji START.
- Jeśli po stuknięciu nic się nie dzieje → BUG: PATCH /api/tickets/{id}/start nie działa.
- Jeśli status nie zmienił się po odświeżeniu → BUG: błąd persystencji stanu.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-032 — Dodanie zdjęcia AFTER do zgłoszenia

**Rola:** KONSERWATOR  
**Konto testowe:** `mock.hydraulik@blokur.pl` / `Haslo123`  
**Warunki wstępne:** Zalogowany. Zgłoszenie ZGL/TEST/006 lub ZGL/TEST/011 w statusie W_REALIZACJI lub ZAKONCZONE_DO_WERYFIKACJI. Na urządzeniu dostępne jest dowolne zdjęcie w galerii.

**Kroki:**
1. Otwórz szczegóły zgłoszenia ZGL/TEST/006 — „Przeciek z dachu".
2. Przewiń do sekcji **Zdjęcia**.
3. Znajdź przycisk „Dodaj zdjęcie po pracach" lub „+" w sekcji zdjęć.
4. Stuknij przycisk.
5. Wybierz dowolne zdjęcie z galerii urządzenia.
6. Poczekaj na upload.

**Oczekiwany rezultat:**
- Po wybraniu zdjęcia pojawia się wskaźnik postępu (loading podczas upload).
- Po zakończeniu upload zdjęcie pojawia się jako miniatura w sekcji zdjęć.
- Typ zdjęcia oznaczony jest jako „PO PRACACH" (AFTER).
- Miniatura jest klikalny (otwiera pełnoekranowy podgląd).

**Możliwe błędy:**
- Jeśli przycisk „Dodaj zdjęcie po pracach" jest niewidoczny dla konserwatora → KRYTYCZNY BUG: brak UI upload zdjęć (znany z audytu — weryfikujemy czy naprawione).
- Jeśli po wybraniu zdjęcia nic się nie dzieje → KRYTYCZNY BUG: POST /api/tickets/{id}/images niepodpięty do UI.
- Jeśli upload kończy się błędem 403 → BUG: brak uprawnień dla konserwatora do dodawania zdjęć.
- Jeśli zdjęcie pojawia się jako emoji 📷 zamiast miniaturki → BUG: GET /api/images/{id} nie działa.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-033 — Zakończenie zgłoszenia przez konserwatora

**Rola:** KONSERWATOR  
**Konto testowe:** `mock.hydraulik@blokur.pl` / `Haslo123`  
**Warunki wstępne:** Zalogowany. Zgłoszenie ZGL/TEST/006 w statusie W_REALIZACJI i przypisane do tego konserwatora.

**Kroki:**
1. Otwórz szczegóły ZGL/TEST/006 — „Przeciek z dachu".
2. Znajdź akcję zakończenia pracy (np. przycisk „Zakończ pracę", sheet akcji konserwatora).
3. Stuknij „Zakończ pracę" / „Finish".
4. Jeśli pojawi się pole opisu wykonanej pracy — wpisz: `Naprawiono uszczelnienie dachu. Przeciek wyeliminowany.`
5. Potwierdź zakończenie.
6. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- W dolnym arkuszu akcji (ConservatorActionSheet) podczas zapisywania widać wskaźnik ładowania, a przyciski i możliwość zamknięcia arkusza (poprzez kliknięcie poza nim) są zablokowane aż do ukończenia operacji.
- Status zgłoszenia zmienia się na **ZAKONCZONE_DO_WERYFIKACJI**.
- Pojawia się informacja o wykonanej pracy.
- Zgłoszenie znika z listy aktywnych zadań konserwatora (lub zmienia kolor/oznaczenie).
- W historii zgłoszenia widoczny jest wpis o zakończeniu przez konserwatora.

**Możliwe błędy:**
- Jeśli przycisk „Zakończ pracę" nie jest dostępny → BUG: brak UI dla akcji FINISH.
- Jeśli pole opisu jest wymagane ale przycisk jest aktywny mimo pustego opisu → BUG: brak walidacji.
- Jeśli status po zakończeniu to ZAMKNIETE zamiast ZAKONCZONE_DO_WERYFIKACJI → BUG: błędna maszyna stanów.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________


---

# GRUPA ZARZĄDCA — Zgłoszenia

---

## TEST-040 — Lista wszystkich zgłoszeń z filtrami

**Rola:** ZARZĄDCA  
**Konto testowe:** `mock.zarzadca1@blokur.pl` / `Haslo123`  
**Warunki wstępne:** Zalogowany. W seedzie istnieje 18 zgłoszeń łącznie (3 z V4 + 15 z V99).

**Kroki:**
1. Stuknij zakładkę **Zgłoszenia**.
2. Poczekaj na załadowanie listy.
3. Sprawdź ile zgłoszeń jest widocznych (zarządca widzi wszystkie).
4. Otwórz panel filtrów (np. przycisk „Filtry" lub ikonka filtra).
5. Wybierz filtr statusu: **NOWE**.
6. Zastosuj filtr.
7. Sprawdź wynik — powinno być 5 zgłoszeń NOWE z V99 (ZGL/TEST/001–005) + ewentualnie ZGL/2026/002.
8. Wyczyść filtr. Wpisz w wyszukiwarkę: `wyciek`.
9. Sprawdź wynik — powinno pojawić się ZGL/2026/001 lub ZGL/TEST/011.

**Oczekiwany rezultat:**
- Zarządca widzi **wszystkie zgłoszenia** (min. 18 z seedów, plus ewentualnie nowe z TEST-012).
- Filtr NOWE zwraca zgłoszenia: ZGL/TEST/001, 002, 003, 004, 005 (oraz ZGL/2026/002 jeśli nie zmieniony).
- Wyszukiwarka tekstowa `wyciek` zwraca ZGL/2026/001 „Wyciek pod zlewem".
- Filtry działają natychmiastowo lub po stuknięciu „Zastosuj".

**Możliwe błędy:**
- Jeśli zarządca widzi tylko swoje zgłoszenia (tak jak mieszkaniec) → KRYTYCZNY BUG: brak uprawnień zarządcy.
- Jeśli filtr statusu nie zwęża listy → BUG: filtrowanie po statusie nie działa.
- Jeśli wyszukiwarka nie filtruje po tytule → BUG: wyszukiwanie pełnotekstowe nie działa.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-041 — Przypisanie konserwatora do zgłoszenia

**Rola:** ZARZĄDCA  
**Konto testowe:** `mock.zarzadca1@blokur.pl` / `Haslo123`  
**Warunki wstępne:** Zalogowany. Zgłoszenie ZGL/TEST/001 — „Brak ciepłej wody" w statusie NOWE, bez przypisanego konserwatora.

**Kroki:**
1. Otwórz szczegóły ZGL/TEST/001 — „Brak ciepłej wody".
2. Znajdź przycisk/FAB „Przypisz konserwatora".
3. Stuknij go — powinien otworzyć się sheet/dialog z listą konserwatorów i polem daty wizyty.
4. Ustaw datę planowanej wizyty na jutro (np. `2026-06-08`).
5. Ustaw godzinę: `10:00`.
6. Z listy konserwatorów wybierz **Stanisław Wróbel** (`mock.hydraulik@blokur.pl`).
7. Stuknij „Zatwierdź" lub „Przypisz".
8. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- W dolnym arkuszu przypisywania (AssignConservatorSheet) podczas zapisywania widać wskaźnik ładowania, a przyciski i możliwość zamknięcia arkusza są zablokowane do momentu ukończenia operacji sieciowej.
- Status zgłoszenia zmienia się na **W_REALIZACJI** (lub pozostaje NOWE z przypisanym konserwatorem, zależy od logiki backendu).
- W szczegółach zgłoszenia pojawia się imię i nazwisko konserwatora: **Stanisław Wróbel**.
- Planowana data wizyty jest wyświetlana.
- Konserwator `mock.hydraulik@blokur.pl` po zalogowaniu widzi to zgłoszenie na swojej liście.

**Możliwe błędy:**
- Jeśli lista konserwatorów jest pusta → BUG: GET /api/users?role=KONSERWATOR nie działa.
- Jeśli nie można ustawić daty wizyty → BUG: brak pola daty w AssignConservatorSheet.
- Jeśli po przypisaniu status nie zmienił się → BUG: PATCH /api/tickets/{id}/assign nie działa.
- Jeśli w liście konserwatorów brak Stanisława Wróbla → BUG: użytkownicy z V99 nie zwracani przez API.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-042 — Odrzucenie zgłoszenia z powodem

**Rola:** ZARZĄDCA  
**Konto testowe:** `mock.zarzadca1@blokur.pl` / `Haslo123`  
**Warunki wstępne:** Zalogowany. Zgłoszenie ZGL/TEST/002 — „Uszkodzona poręcz na klatce" w statusie NOWE.

**Kroki:**
1. Otwórz szczegóły ZGL/TEST/002 — „Uszkodzona poręcz na klatce".
2. Znajdź przycisk „Odrzuć" lub opcję odrzucenia.
3. Stuknij — powinien otworzyć się sheet/dialog z polem powodu odrzucenia.
4. Wpisz powód: `Zgłoszenie jest duplikatem — kwestia naprawy poręczy jest już obsługiwana.`
5. Stuknij „Odrzuć" / „Potwierdź".
6. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- W dolnym arkuszu odrzucania (ManagerRejectSheet) podczas zapisywania widać wskaźnik ładowania, a przyciski i możliwość zamknięcia arkusza są zablokowane.
- Status ZGL/TEST/002 zmienia się na **ODRZUCONE**.
- W szczegółach zgłoszenia widoczny jest powód odrzucenia.
- Przycisk „Odrzuć" jest disabled dopóki pole powodu jest puste (walidacja).

**Możliwe błędy:**
- Jeśli można odrzucić bez podania powodu (przycisk aktywny gdy pole puste) → BUG: brak walidacji wymaganego pola.
- Jeśli po odrzuceniu status nadal pokazuje NOWE → BUG: PATCH /api/tickets/{id}/reject nie działa.
- Jeśli sheet zamyka się bez wysłania żądania → BUG: ciche niepowodzenie.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-043 — Zamknięcie zgłoszenia i pobranie protokołu PDF

**Rola:** ZARZĄDCA  
**Konto testowe:** `mock.zarzadca1@blokur.pl` / `Haslo123`  
**Warunki wstępne:** Zalogowany. Zgłoszenie ZGL/TEST/011 — „Wymiana uszczelki pod zlewem" w statusie ZAKONCZONE_DO_WERYFIKACJI (jest w seedzie).

**Kroki:**
1. Otwórz szczegóły ZGL/TEST/011 — „Wymiana uszczelki pod zlewem".
2. Sprawdź status: ZAKONCZONE_DO_WERYFIKACJI.
3. Znajdź przycisk „Zamknij" lub FAB zamknięcia.
4. Stuknij „Zamknij zgłoszenie".
5. Poczekaj na zmianę statusu.
6. Po zamknięciu sprawdź czy pojawia się przycisk/FAB „Pobierz protokół PDF".
7. Stuknij „Pobierz protokół PDF".
8. Poczekaj na wygenerowanie i otwarcie PDF.

**Oczekiwany rezultat:**
- Status zgłoszenia zmienia się na **ZAMKNIETE**.
- Po zamknięciu pojawia się opcja pobrania protokołu odbioru prac (PDF).
- PDF otwiera się poprawnie w systemowej przeglądarce PDF.
- Dokument zawiera dane zgłoszenia: numer ZGL/TEST/011, tytuł, daty, dane konserwatora.

**Możliwe błędy:**
- Jeśli przycisk „Zamknij" nie jest dostępny dla zgłoszenia ZAKONCZONE_DO_WERYFIKACJI → BUG: błędna maszyna stanów lub brak UI.
- Jeśli po zamknięciu brak opcji PDF → BUG: FAB PDF nie pojawia się po zamknięciu.
- Jeśli PDF generuje się błędem 500 → BUG: problem z generatorem PDF na backendzie.
- Jeśli plik PDF nie otwiera się (błąd FileProvider) → BUG: problem z udostępnianiem pliku na Androidzie.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-044 — Wznowienie zgłoszenia ze statusu WSTRZYMANO

**Rola:** ZARZĄDCA  
**Konto testowe:** `mock.zarzadca1@blokur.pl` / `Haslo123`  
**Warunki wstępne:** Zalogowany. Potrzebne jest zgłoszenie w statusie WSTRZYMANO. Brak takiego w seedach — należy najpierw: (1) przypisać konserwatora do ZGL/TEST/003 wg TEST-041, (2) zalogować się jako konserwator i wywołać akcję WSTRZYMAJ/PAUSE na tym zgłoszeniu.

**Kroki (po stworzeniu zgłoszenia w statusie WSTRZYMANO):**
1. Otwórz szczegóły zgłoszenia w statusie WSTRZYMANO.
2. Znajdź opcję „Wznów" lub przycisk zmiany statusu.
3. Stuknij „Wznów zgłoszenie".
4. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- Status zmienia się z WSTRZYMANO na **W_REALIZACJI**.
- W historii zgłoszenia pojawia się wpis o wznowieniu.

**Możliwe błędy:**
- Jeśli przycisk „Wznów" otwiera dialog przypisania konserwatora zamiast zmieniać status → ZNANY BUG z audytu (gap_analysis: PATCH /api/tickets/{id}/status nie podpięty do UI). Zanotuj jako BUG.
- Jeśli brak jakiejkolwiek opcji dla zgłoszenia WSTRZYMANO → BUG: brak UI dla wznowienia.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________


---

# GRUPA ZARZĄDCA — Administracja

---

## TEST-050 — Przeglądanie drzewa nieruchomości

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany.

**Kroki:**
1. Stuknij zakładkę **Lokale** w dolnej nawigacji.
2. Poczekaj na załadowanie drzewa nieruchomości.
3. Sprawdź strukturę: wspólnota → budynki → klatki → lokale.
4. Rozwiń gałąź **Budynek A (Solaris)** → **Klatka 1**.
5. Sprawdź lokale: 1, 2, 3, 4, 5.
6. Stuknij **Lokal 1** — sprawdź czy widać: saldo +150,50 zł, piętro 0, powierzchnia 45,50 m², typ WŁASNOŚCIOWY.

**Oczekiwany rezultat:**
- Drzewo pokazuje co najmniej 3 budynki (Budynek A, Budynek B, Rezydencja Parkowa z seeda V4).
- Budynek A, Klatka 1 zawiera 5 lokali (1–5).
- Lokal 1 ma: saldo **+150,50 zł**, piętro **0**, pow. **45,50 m²**, typ **WŁASNOŚCIOWY**.
- Saldo wyświetlane jako `150,50 zł` (format polski).
- Przycisk „Kartoteka finansowa" przy lokalu prowadzi do ekranu kartoteki tego lokalu.

**Możliwe błędy:**
- Jeśli drzewo jest puste → BUG: GET /api/buildings/tree nie działa.
- Jeśli saldo wyświetlane jako `150.50 PLN` → BUG: brak formatowania walut.
- Jeśli brak przycisku „Kartoteka finansowa" → BUG (naprawiony wg final_report NAV-003 — weryfikujemy).

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-051 — Tworzenie nowego użytkownika (zaproszenie)

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany. Dostęp do konfiguracji SMTP backendu (email musi wyjść).

**Kroki:**
1. Z dolnej nawigacji stuknij zakładkę **Użytkownicy**.
2. Stuknij przycisk FAB „+" — „Nowe konto".
3. Wypełnij formularz:
   - Imię: `Testowy`
   - Nazwisko: `Użytkownik`
   - Email: `testowy.uzytkownik.blokur@gmail.com` (lub inny aktywny email)
   - Rola: **MIESZKANIEC**
   - Lokal: wybierz **Budynek A (Solaris) → Klatka 1 → Lokal 3** (wolny lokal z saldem 0)
4. Stuknij „Utwórz" / „Zaproś".
5. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- Pojawia się potwierdzenie że zaproszenie zostało wysłane (snackbar lub dialog).
- Nowy użytkownik pojawia się na liście użytkowników.
- Na podany email przychodzi wiadomość z linkiem do akceptacji zaproszenia.
- Nie można stworzyć drugiego użytkownika z tym samym emailem (409 Conflict).

**Możliwe błędy:**
- Jeśli formularz nie waliduje wymaganego lokalu dla roli MIESZKANIEC → BUG: brak walidacji.
- Jeśli API zwraca 409 (email zajęty) ale aplikacja nie pokazuje błędu → BUG: brak obsługi 409.
- Jeśli email z zaproszeniem nie przychodzi → Problem z konfiguracją SMTP (poza scope aplikacji mobilnej).

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-052 — Edycja i dezaktywacja użytkownika

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany, na liście użytkowników.

**Kroki — Edycja:**
1. Na liście użytkowników znajdź **Piotr Wiśniewski** (`piotr.wisniewski@wp.pl`).
2. Stuknij go — przejdź do ekranu edycji.
3. Zmień numer telefonu na: `+48 123 456 789`
4. Stuknij „Zapisz".
5. Poczekaj na odpowiedź.

**Kroki — Dezaktywacja:**
6. Wróć do listy użytkowników.
7. Znajdź użytkownika którego chcesz dezaktywować (NIE dezaktywuj głównych kont testowych — użyj np. użytkownika stworzonego w TEST-051).
8. Stuknij jego kafelek → wejdź w edycję → znajdź przycisk „Dezaktywuj".
9. Potwierdź dezaktywację w dialogu.
10. Poczekaj na odpowiedź.

**Oczekiwany rezultat:**
- Po edycji telefon Piotra Wiśniewskiego jest zaktualizowany i widoczny na liście.
- Ekran edycji pokazuje snackbar sukcesu lub nawiguje z powrotem.
- Po dezaktywacji użytkownik jest oznaczony jako nieaktywny na liście (lub znika z listy aktywnych).
- Dezaktywowany użytkownik nie może się zalogować (backend zwróci 423 lub 401).

**Możliwe błędy:**
- Jeśli po zapisaniu telefonu dane nie zmieniają się → BUG: PATCH /api/admin/users/{id} nie działa.
- Jeśli dezaktywacja nie pokazuje dialogu potwierdzenia → BUG UX: brak alertu przed destrukcyjną akcją.
- Jeśli dialog dezaktywacji zamyka się od razu bez feedbacku (wiemy z audytu że tak może być) → BUG UX (STATE-015).

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-053 — Wyświetlenie sald lokali

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany.

**Kroki:**
1. Z zakładki **Profil** stuknij „Finanse" (link dodany wg naprawy NAV-001 z final_report).
2. Na ekranie finansów stuknij „Salda lokali" lub „Balances".
3. Poczekaj na załadowanie listy sald.
4. Sprawdź listę — filtruj po minimalnym długu: wpisz `1000` w polu min. zaległości.
5. Zastosuj filtr.

**Oczekiwany rezultat:**
- Lista sald pokazuje lokale z ich saldami.
- Widoczne są lokale z ujemnymi saldami, np.:
  - Lokal 5 (Budynek A, Klatka 1): **-1200,00 zł** (największy dług z seeda V4)
  - Lokal 2 (Klatka 1): **-20,00 zł**
- Salda wyświetlane w formacie `1 200,00 zł` (format polski, ze spacją jako separatorem tysięcy).
- Filtr po minimalnej zaległości 1000 zwraca tylko Lokal 5 (-1200 zł).

**Możliwe błędy:**
- Jeśli link „Finanse" w Profilu zarządcy nie istnieje → BUG (naprawiony wg NAV-001 — weryfikujemy).
- Jeśli salda wyświetlane jako `1200.00 PLN` → BUG: brak formatowania walut.
- Jeśli filtr min. zaległości nie działa → BUG: parametry zapytania API /api/admin/apartments/balances.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-054 — Tworzenie ogłoszenia

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany.

**Kroki:**
1. Z zakładki **Profil** stuknij „Ogłoszenia" (link dodany wg naprawy NAV-002 z final_report).
2. Na ekranie ogłoszeń stuknij FAB „+" — „Nowe ogłoszenie".
3. Wypełnij formularz:
   - Tytuł: `Testowe ogłoszenie zarządcy`
   - Treść: `To jest ogłoszenie stworzone podczas testów manualnych dnia 07.06.2026.`
   - Typ: OGLOSZENIE
   - Zasięg: WSZYSCY (lub All)
4. (Opcjonalnie) Dodaj załącznik PDF — jeśli dostępny mały plik PDF na urządzeniu.
5. Stuknij „Utwórz" / „Opublikuj".

**Oczekiwany rezultat:**
- Ogłoszenie pojawia się na liście ogłoszeń.
- Widoczne jest dla wszystkich zalogowanych użytkowników.
- Tytuł i treść są poprawnie wyświetlane.

**Możliwe błędy:**
- Jeśli link „Ogłoszenia" w Profilu zarządcy nie istnieje → BUG (naprawiony wg NAV-002 — weryfikujemy).
- Jeśli POST /api/announcements zwraca błąd → BUG: tworzenie ogłoszeń nie działa.
- Jeśli ogłoszenie nie pojawia się dla mieszkańca (TEST-023) → BUG: filtrowanie zasięgu ogłoszeń.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-055 — Edycja ogłoszenia

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany. Istniejące ogłoszenie (np. stworzone w TEST-054).

**Kroki:**
1. Na liście ogłoszeń znajdź ogłoszenie „Testowe ogłoszenie zarządcy".
2. Stuknij ikonę edycji (ołówek lub przycisk „Edytuj").
3. Zmień treść na: `Zaktualizowana treść ogłoszenia — test edycji.`
4. Stuknij „Zapisz".

**Oczekiwany rezultat:**
- Ogłoszenie wyświetla zaktualizowaną treść.
- PUT /api/announcements/{id} wywołane poprawnie.

**Możliwe błędy:**
- Jeśli brak przycisku edycji przy ogłoszeniu → BUG (znany z audytu: PUT /api/announcements/{id} zdefiniowany w Retrofit ale bez ekranu edycji — weryfikujemy czy naprawione w najnowszej wersji).
- Jeśli edycja otwiera ekran tworzenia z pustymi polami zamiast wypełnionymi danymi obecnego ogłoszenia → BUG: brak pre-populacji formularza.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-056 — Konfiguracja ustawień PUSH

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany.

**Kroki:**
1. Z zakładki **Profil** stuknij „Ustawienia powiadomień" lub „Powiadomienia PUSH".
2. Poczekaj na załadowanie ekranu.
3. Sprawdź czy widoczna jest lista typów powiadomień (np. TICKET_CREATED, TICKET_ASSIGNED, itp.) z przełącznikami włącz/wyłącz.
4. Przełącz jeden typ powiadomień z ON na OFF.
5. Poczekaj na zapis (inline progress lub snackbar).

**Oczekiwany rezultat:**
- Ekran pokazuje listę typów zdarzeń z przełącznikami (Switch).
- Każdy Switch ma etykietę opisującą typ zdarzenia po polsku.
- Zmiana przełącznika wywołuje PATCH /api/admin/notifications/settings/{eventType} i zapisuje stan.
- Inline progress pojawia się podczas zmiany (isUpdating).

**Możliwe błędy:**
- Jeśli stuknięcie „Powiadomienia" otwiera ekran z hardkodowanymi togglemi bez API (znany bug NAV-003 z audytu) → BUG (naprawiony wg final_report — weryfikujemy).
- Jeśli lista jest pusta → BUG: GET /api/admin/notifications/settings nie działa.
- Jeśli przełącznik nie reaguje na zmianę → BUG: PATCH nie wywołany.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-057 — Konfiguracja logo wspólnoty (z walidacją)

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany. Posiadanie na urządzeniu pliku testowego PDF (lub pliku graficznego > 2 MB) oraz prawidłowego pliku graficznego (JPG/PNG < 2 MB).

**Kroki:**
1. Z zakładki **Profil** stuknij „Logo wspólnoty" (lub przejdź do ustawień logo).
2. Spróbuj wybrać/przesłać plik PDF.
3. Sprawdź, czy aplikacja odrzuca plik z odpowiednim komunikatem o błędzie formatu.
4. Spróbuj wybrać/przesłać plik JPG/PNG o rozmiarze większym niż 2 MB.
5. Sprawdź, czy aplikacja odrzuca plik z odpowiednim komunikatem o błędzie rozmiaru.
6. Wybierz poprawny plik graficzny (JPG/PNG < 2 MB) i zatwierdź przesłanie.

**Oczekiwany rezultat:**
- Próba wyboru pliku PDF lub pliku > 2 MB skutkuje natychmiastowym wyświetleniem błędu (np. snackbar lub komunikat o błędzie walidacji) bez wysyłania zapytania do API.
- Wybranie poprawnego pliku przesyła go do API i aktualizuje logo wspólnoty na ekranie.

**Możliwe błędy:**
- Jeśli plik PDF lub zbyt duży plik zostaje zaakceptowany i wysłany do serwera (zwracając błąd serwera 400/500 zamiast walidacji lokalnej) → BUG: brak walidacji klienta.
- Jeśli brak jakiejkolwiek informacji o błędnym formacie/rozmiarze pliku → BUG UX.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-058 — Usuwanie ogłoszenia

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany. Istniejące ogłoszenie na liście (np. stworzone w TEST-054).

**Kroki:**
1. Przejdź do listy ogłoszeń.
2. Znajdź ogłoszenie „Testowe ogłoszenie zarządcy".
3. Stuknij ikonę usuwania (kosz) przy tym ogłoszeniu.
4. W oknie potwierdzenia usunięcia stuknij przycisk „Usuń" i obserwuj interakcję.

**Oczekiwany rezultat:**
- Pojawia się okno dialogowe z pytaniem o potwierdzenie usunięcia.
- Po kliknięciu „Usuń" w dialogu pojawia się wskaźnik ładowania (progress indicator), a przyciski „Usuń" oraz „Anuluj" stają się nieaktywne (disabled) na czas trwania usuwania.
- Ogłoszenie znika z listy po pomyślnym zakończeniu operacji.

**Możliwe błędy:**
- Jeśli w oknie dialogowym podczas usuwania nie widać wskaźnika ładowania, a przyciski są aktywne (co pozwala na podwójne kliknięcie i błąd API) → BUG UX.
- Jeśli ogłoszenie nie znika z listy mimo braku błędów → BUG: brak odświeżenia listy.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-059 — Przeglądanie i usuwanie przeglądu (Inspections)

**Rola:** ZARZĄDCA  
**Konto testowe:** `admin1@blokur.pl` / `haslo123`  
**Warunki wstępne:** Zalogowany. Istniejące przeglądy w bazie danych (np. „Przegląd roczny budynku", „Kontrola gaśnic", „Audit energetyczny" z seedów).

**Kroki:**
1. Z zakładki **Profil** przejdź do sekcji „Przeglądy techniczne" lub „Inspekcje".
2. Sprawdź czy widzisz 3 przeglądy z seeda.
3. Znajdź przegląd „Audit energetyczny" i stuknij ikonę usuwania (kosz).
4. W oknie potwierdzenia usunięcia kliknij przycisk „Usuń" i obserwuj interakcję.

**Oczekiwany rezultat:**
- Lista przeglądów ładuje się poprawnie, pokazując szczegółowe informacje z seedów.
- Dialog potwierdzenia usunięcia pojawia się prawidłowo.
- Podczas procesu usuwania wewnątrz okna dialogowego widać wskaźnik ładowania, a przyciski interakcji stają się zablokowane.
- Przegląd zostaje pomyślnie usunięty i znika z listy.

**Możliwe błędy:**
- Jeśli lista przeglądów nie ładuje się (błąd pobierania) → BUG: GET /api/inspections nie działa.
- Jeśli brak wskaźnika ładowania lub przyciski w dialogu usuwania nie są blokowane podczas operacji → BUG UX.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

# GRUPA BŁĘDÓW I EDGE CASE

---

## TEST-060 — Brak internetu podczas ładowania listy zgłoszeń

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany. Aplikacja wyświetla listę zgłoszeń (wczytana wcześniej).

**Kroki:**
1. Przejdź do zakładki Zgłoszenia i poczekaj na załadowanie listy.
2. Włącz tryb samolotowy (wyłącz Wi-Fi i dane mobilne) na urządzeniu.
3. Stuknij przycisk „Odśwież" (jeśli widoczny w panelu filtrów) lub opuść ekran i wróć.
4. Poczekaj na próbę odświeżenia.
5. Sprawdź stan ekranu.
6. Stuknij „Spróbuj ponownie" jeśli pojawi się taki przycisk.
7. Przywróć połączenie internetowe.
8. Stuknij „Spróbuj ponownie" ponownie.

**Oczekiwany rezultat:**
- Po utracie połączenia pojawia się stan błędu z komunikatem (np. „Brak połączenia z internetem" lub „Błąd sieci").
- Widoczny jest przycisk **„Spróbuj ponownie"** (naprawiony wg STATE-001 w final_report).
- Po przywróceniu połączenia i stuknięciu „Spróbuj ponownie" — lista odświeża się poprawnie.

**Możliwe błędy:**
- Jeśli po błędzie sieci aplikacja zawiesza się lub crashuje → KRYTYCZNY BUG.
- Jeśli brak przycisku „Spróbuj ponownie" w stanie błędu → BUG (naprawiony wg STATE-001 — weryfikujemy).
- Jeśli komunikat błędu zawiera surowy stack trace lub techniczny opis → BUG: brak lokalizacji błędów.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-061 — Próba dostępu do zasobu innej roli

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany jako mieszkaniec.

**Kroki:**
1. Zaloguj się jako `jan.kowalski@gmail.com` / `haslo123` (MIESZKANIEC).
2. Sprawdź dolną nawigację — czy widoczna jest zakładka „Lokale" lub „Użytkownicy"? (NIE powinna).
3. Sprawdź zakładkę Profil — czy widoczne są linki „Finanse zarządcy", „Ustawienia powiadomień PUSH", „Logo wspólnoty"? (NIE powinny).
4. Sprawdź ekran szczegółów zgłoszenia ZGL/2026/001 — czy widoczne są przyciski „Przypisz", „Odrzuć", „Zamknij"? (NIE powinny — to akcje zarządcy).

**Oczekiwany rezultat:**
- Zakładki „Lokale" i „Użytkownicy" są **niewidoczne** w dolnej nawigacji.
- Linki administracyjne w Profilu są **niewidoczne**.
- Przyciski zarządzania zgłoszeniem (przypisz/odrzuć/zamknij) są **niewidoczne** na ekranie szczegółów.
- Mieszkaniec widzi tylko: Zgłoszenia, Finanse, Uchwały, Ogłoszenia, Profil.

**Możliwe błędy:**
- Jeśli widoczna jest zakładka „Lokale" lub „Użytkownicy" → KRYTYCZNY BUG: błędna kontrola roli w nawigacji.
- Jeśli widoczne są przyciski zarządzania (przypisz/odrzuć) → KRYTYCZNY BUG: brak kontroli roli na ekranie szczegółów.
- Jeśli próba wywołania admin-endpointu przez UI zwraca 403 ale bez komunikatu → BUG UX: brak obsługi 403.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-062 — Wysłanie formularza z pustymi polami

**Rola:** MIESZKANIEC  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Zalogowany. Formularz tworzenia zgłoszenia otwarty (wg TEST-012, krok 1–2).

**Kroki:**
1. Otwórz formularz nowego zgłoszenia (FAB na liście zgłoszeń).
2. Pozostaw pola **Tytuł** i **Opis** puste.
3. Nie wybieraj kategorii.
4. Sprawdź stan przycisku „Zgłoś usterkę".
5. Jeśli przycisk jest aktywny — stuknij go.

**Oczekiwany rezultat:**
- Przycisk „Zgłoś usterkę" jest **disabled** (wyszarzony, niestukany) gdy wymagane pola są puste.
- Ewentualnie: po stuknięciu (jeśli aktywny) pojawia się komunikat walidacji pod pustymi polami.
- Żądanie do API **NIE jest wysyłane** gdy formularz jest nieprawidłowy.

**Możliwe błędy:**
- Jeśli przycisk jest aktywny mimo pustych pól i wysyłanie zwraca błąd 400 → BUG: brak walidacji po stronie klienta (tylko server-side).
- Jeśli komunikat walidacji jest po angielsku → BUG: brak lokalizacji.
- Jeśli aplikacja zawiesza się po próbie wysłania pustego formularza → KRYTYCZNY BUG: crash.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

## TEST-063 — Błąd krytyczny przy ładowaniu roli na starcie (obsługa Retry)

**Rola:** (dowolna)  
**Konto testowe:** `jan.kowalski@gmail.com` / `haslo123`  
**Warunki wstępne:** Użytkownik jest już zalogowany w aplikacji. Brak połączenia internetowego przed uruchomieniem aplikacji.

**Kroki:**
1. Wyłącz połączenie sieciowe (Wi-Fi oraz dane komórkowe) na urządzeniu.
2. Zamknij całkowicie aplikację BlokUR (ubij proces).
3. Uruchom aplikację ponownie.
4. Sprawdź zachowanie ekranu głównego (ResidentMainScreen).
5. Włącz ponownie połączenie sieciowe.
6. Stuknij przycisk „Spróbuj ponownie" (Retry) na ekranie błędu.

**Oczekiwany rezultat:**
- Aplikacja po uruchomieniu bez sieci nie wywala się (brak crashu).
- Pojawia się stan błędu krytycznego informujący o braku możliwości wczytania danych roli/nawigacji.
- Widoczny jest dedykowany przycisk „Spróbuj ponownie" (Retry).
- Po przywróceniu sieci i kliknięciu przycisku retry, rola użytkownika jest poprawnie ładowana, a aplikacja przechodzi do odpowiedniego ekranu głównego (np. Profil mieszkańca).

**Możliwe błędy:**
- Jeśli aplikacja crashuje (np. NullPointerException przy próbie nawigacji bez wczytanej roli) → KRYTYCZNY BUG.
- Jeśli brak przycisku „Spróbuj ponownie" na ekranie błędu krytycznego lub jego kliknięcie nic nie robi → BUG.

**Pole na wynik:** [ ] PASS  [ ] FAIL  
**Notatki:** ___________

---

# SZABLON RAPORTU BŁĘDU

Skopiuj poniższy szablon dla każdego znalezionego błędu i wypełnij go:

---

```
BUG-XXX — [krótki tytuł błędu]

Test: TEST-XXX
Rola / konto: [email użytkownika testowego]
Priorytet: KRYTYCZNY / WYSOKI / ŚREDNI / NISKI

Kroki do odtworzenia:
1. [pierwsza akcja]
2. [kolejna akcja]
3. [akcja wyzwalająca błąd]

Oczekiwany rezultat:
[co powinno się wydarzyć zgodnie ze scenariuszem]

Rzeczywisty rezultat:
[co faktycznie się stało — opis tego co widać na ekranie]

Screenshot / nagranie: [ścieżka do pliku lub "brak"]

Dodatkowe info:
- Wersja Android: [np. Android 14]
- Urządzenie: [np. Samsung Galaxy A54 lub Emulator API 34]
- Czas wystąpienia: [np. 2026-06-07 09:35]
- Czy błąd powtarzalny: [TAK / NIE / CZASAMI]
```

---

## Priorytety błędów

| Priorytet | Definicja |
|-----------|-----------|
| **KRYTYCZNY** | Aplikacja nie uruchamia się / crash / nieautoryzowany dostęp do danych / logowanie bez uwierzytelnienia |
| **WYSOKI** | Główna funkcjonalność nie działa (tworzenie zgłoszeń, zmiana statusu, brak transakcji) |
| **ŚREDNI** | Funkcja działa ale z nieprawidłowym wynikiem (złe formatowanie walut/dat, brak komunikatu błędu) |
| **NISKI** | Drobne problemy UX (literówki, brak spinnera, niespójne marginesy) |

---

## Znane ograniczenia — NIE zgłaszaj jako bug

| Zachowanie | Powód |
|------------|-------|
| Profil pokazuje „—" jako imię użytkownika | Brak endpointu GET /api/users/me w backendzie (SCOPE-001) |
| Powiadomienia PUSH nie przychodzą | Brak google-services.json / Firebase (SCOPE-003) |
| Daty w formularzach wpisywane ręcznie (format ISO) | DatePicker nie wdrożony w tej iteracji (SCOPE-005) |
| Backend nie paginuje listy zgłoszeń | Enhancement — nie bug funkcjonalny (SCOPE-004) |

---

*Plan testowania manualnego BlokUR — wersja 1.0 — 2026-06-07*
