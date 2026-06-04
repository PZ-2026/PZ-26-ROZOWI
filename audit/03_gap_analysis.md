# Gap Analysis — Analiza Pokrycia Frontend vs Backend

Ten dokument zawiera szczegółowe porównanie endpointów backendu z ich wywołaniami w aplikacjach klienckich frontendu.

## MODUŁ 4 — Zgłoszenia Serwisowe (Tickets)

### Analiza endpointów

1. **`POST /api/tickets`**
   - Status: ✅ POKRYTY
   - Opis: Działa poprawnie w `CreateTicketViewModel`, uwzględnia obsługę błędów (400, 403, 422).

2. **`GET /api/tickets`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Endpoint udostępnia na backendzie rozbudowane filtrowanie (po statusie, kategorii, przypisaniu, dacie). Frontend pobiera jednak pełną listę i filtruje ją po stronie klienta, co przy dużej liczbie zgłoszeń spowoduje problemy wydajnościowe.

3. **`GET /api/tickets/{id}`**
   - Status: ✅ POKRYTY
   - Opis: Działa poprawnie w `TicketDetailsViewModel`.

4. **`POST /api/tickets/{id}/assign`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Krytyczna niezgodność kontraktu. Frontend wywołuje `PATCH /api/tickets/{id}/assign`, podczas gdy backend oczekuje `POST`.

5. **`POST /api/tickets/{id}/close`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Niezgodność kontraktu. Frontend wysyła request używając metody `PATCH` zamiast `POST`.

6. **`POST /api/tickets/{id}/reject`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Niezgodność kontraktu. Frontend wysyła request używając metody `PATCH` zamiast `POST`.

7. **`POST /api/tickets/{id}/start-work`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Niezgodność kontraktu i ścieżki. Frontend odwołuje się do `PATCH /api/tickets/{id}/start`.

8. **`POST /api/tickets/{id}/suspend`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Niezgodność kontraktu. Frontend wysyła request używając metody `PATCH` zamiast `POST`.

9. **`POST /api/tickets/{id}/complete`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Niezgodność ścieżki. Frontend odwołuje się do `POST /api/tickets/{id}/completion`.

10. **`PATCH /api/tickets/{id}/status`**
    - Status: ❌ BRAK
    - Rola: ZARZĄDCA / KONSERWATOR
    - Priorytet: NISKI — Frontend używa dedykowanych endpointów (wymienionych wyżej), mimo ich błędnych kontraktów HTTP.

11. **`GET /api/tickets/{id}/history`**
    - Status: ❌ BRAK
    - Rola: ZARZĄDCA / KONSERWATOR
    - Priorytet: ŚREDNI — Brak widoku w UI, uniemożliwia przeglądanie historii przejść statusów zgłoszenia, co obniża transparentność audytu.

12. **`POST /api/tickets/{ticketId}/comments`**
    - Status: ✅ POKRYTY
    - Opis: Dodawanie komentarza zaimplementowane z uwzględnieniem komunikatu błędu.

13. **`GET /api/tickets/{ticketId}/comments`**
    - Status: ⚠️ CZĘŚCIOWO
    - Opis: Brak obsługi błędu w UI (ciche logowanie błędu, brak komunikatu dla użytkownika).

14. **`POST /api/tickets/{ticketId}/images`**
    - Status: ✅ POKRYTY
    - Opis: Obsługa multipart zaimplementowana pomyślnie.

15. **`GET /api/tickets/{ticketId}/images`**
    - Status: ⚠️ CZĘŚCIOWO
    - Opis: Błąd cichy — w przypadku niepowodzenia ładowania obrazów, użytkownik nie otrzymuje komunikatu błędu.

16. **`GET /api/images/{imageId}`**
    - Status: ✅ POKRYTY
    - Opis: Działa, zwraca surowe dane binarne zdjęć.

17. **`DELETE /api/images/{imageId}`**
    - Status: ❌ BRAK
    - Rola: MIESZKANIEC / ZARZĄDCA / KONSERWATOR
    - Priorytet: NISKI — Edge case. Użytkownik nie może usunąć omyłkowo dodanego zdjęcia.

18. **`GET /api/categories`**
    - Status: ⚠️ CZĘŚCIOWO
    - Opis: Wykorzystywane przez menedżera oraz przez mieszkańca przy tworzeniu zgłoszenia. W ekranie `CreateTicketScreen` w razie błędu ładowania wyświetlana jest pusta lista bez powiadomienia użytkownika o błędzie.

19. **`GET /api/admin/categories`**
    - Status: ❌ BRAK
    - Rola: ZARZĄDCA
    - Priorytet: ŚREDNI — Frontend w widoku zarządcy używa ogólnodostępnego `GET /api/categories` z pominięciem specjalistycznego widoku dla adminów (który może zwracać np. dezaktywowane kategorie).

20. **`POST /api/admin/categories`**
    - Status: ✅ POKRYTY
    - Opis: Działa wraz z weryfikacją błędów 400 i 409.

21. **`PUT /api/admin/categories/{id}`**
    - Status: ✅ POKRYTY
    - Opis: Edycja działa i obsługuje statusy błędów.

22. **`PATCH /api/admin/categories/{id}/sla`**
    - Status: ✅ POKRYTY
    - Opis: Modyfikacja czasu SLA działa i obsługuje błędy.

23. **`DELETE /api/admin/categories/{id}`**
    - Status: ⚠️ CZĘŚCIOWO
    - Opis: Niezgodność metody na warstwie interfejsów API. Frontend wysyła żądanie dezaktywacji na adres `PATCH /api/admin/categories/{id}/deactivate` (prawdopodobnie stary kontrakt API).

## MODUŁ 3 — Nieruchomości i Struktura Budynku

### Analiza endpointów

1. **`GET /api/properties`**
   - Status: ✅ POKRYTY
   - Opis: Wywoływane dla formularza wyboru przy tworzeniu budynku.

2. **`GET /api/properties/{id}`**
   - Status: ❌ BRAK
   - Rola: ZARZĄDCA
   - Priorytet: NISKI — Frontend opiera się na pełnym drzewie, nie wykorzystując osobnego odpytywania o szczegóły jednej nieruchomości.

3. **`POST /api/properties`**
   - Status: ✅ POKRYTY
   - Opis: Zaimplementowane w `PropertyTreeViewModel` (tryb ADD).

4. **`PUT /api/properties/{id}`**
   - Status: ✅ POKRYTY
   - Opis: Zaimplementowane w `PropertyTreeViewModel` (tryb EDIT).

5. **`POST /api/properties/{id}/logo`**
   - Status: ✅ POKRYTY
   - Opis: Wgrywanie pliku logo jest podpięte w `CommunityLogoScreen` w osobnym widoku ustawień.

6. **`GET /api/buildings`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Frontend wywołuje endpoint pod adresem `/api/buildings/tree` zamiast `/api/buildings` (niezgodność ścieżki wpisanej w kliencie względem dokumentacji).

7. **`POST /api/buildings`**
   - Status: ✅ POKRYTY
   - Opis: Tworzenie budynku jest zaimplementowane.

8. **`PUT /api/buildings/{id}`**
   - Status: ✅ POKRYTY
   - Opis: Edycja działa.

9. **`DELETE /api/buildings/{id}`**
   - Status: ❌ BRAK
   - Rola: ZARZĄDCA
   - Priorytet: ŚREDNI — Logika serwisowa w interfejsie API istnieje, ale w widoku UI (`PropertyTreeScreen`) brakuje przycisku/akcji do usunięcia węzła z drzewa.

10. **`POST /api/buildings/{buildingId}/staircases`**
    - Status: ✅ POKRYTY
    - Opis: Dodawanie klatek schodowych działa.

11. **`PUT /api/buildings/{buildingId}/staircases/{staircaseId}`**
    - Status: ✅ POKRYTY
    - Opis: Edycja klatek zaimplementowana.

12. **`DELETE /api/buildings/{buildingId}/staircases/{staircaseId}`**
    - Status: ❌ BRAK
    - Rola: ZARZĄDCA
    - Priorytet: ŚREDNI — Podobnie jak dla budynków, brakuje wywołania usunięcia z poziomu UI.

13. **`POST /api/staircases/{staircaseId}/apartments`**
    - Status: ✅ POKRYTY
    - Opis: Dodawanie lokali zaimplementowane.

14. **`PUT /api/staircases/{staircaseId}/apartments/{apartmentId}`**
    - Status: ✅ POKRYTY
    - Opis: Edycja lokali w drzewie zaimplementowana.

15. **`DELETE /api/staircases/{staircaseId}/apartments/{apartmentId}`**
    - Status: ❌ BRAK
    - Rola: ZARZĄDCA
    - Priorytet: ŚREDNI — Brak wywołania operacji usuwania z UI.

## MODUŁ 7 — Liczniki i Odczyty Liczników

### Analiza endpointów

1. **`GET /api/apartments/{apartmentId}/meters`**
   - Status: ✅ POKRYTY
   - Opis: Działa w `MeterListViewModel`.

2. **`POST /api/apartments/{apartmentId}/meters`**
   - Status: ✅ POKRYTY
   - Opis: Dodawanie licznika zaimplementowane.

3. **`PATCH /api/apartments/{apartmentId}/meters/{meterId}/deactivate`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Niezgodność struktury URL. Frontend używa ścieżki `/api/meters/{id}/deactivate`, pomijając `apartmentId`, czego wymaga kontrakt backendu.

4. **`GET /api/apartments/{apartmentId}/meter-readings`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Frontend wywołuje ten endpoint z na sztywno przypisanymi parametrami paginacji (`?page=0&size=100`). Spowoduje to brak wyświetlania odczytów przekraczających 100 wpisów. Ponadto UI pobiera dane dla całego lokalu i filtruje licznik po stronie klienta, zamiast korzystać z endpointu odfiltrowującego dane per licznik (jeśli dostępny) lub nie ma optymalizacji.

5. **`POST /api/apartments/{apartmentId}/meter-readings`**
   - Status: ✅ POKRYTY
   - Opis: Dodawanie nowego odczytu jest w pełni wspierane.

6. **`GET /api/meter-readings/{id}`**
   - Status: ❌ BRAK
   - Rola: ZARZĄDCA / MIESZKANIEC
   - Priorytet: NISKI — UI wyświetla szczegóły na podstawie listy załadowanych odczytów, przez co dedykowane zapytanie o pojedynczy szczegół odczytu nie jest wywoływane.

7. **`PUT /api/meter-readings/{id}`**
   - Status: ✅ POKRYTY
   - Opis: Aktualizacja wybranej wartości odczytu zaimplementowana z formularzem edycji.

8. **`DELETE /api/meter-readings/{id}`**
   - Status: ✅ POKRYTY
   - Opis: Usuwanie odczytu (204) jest zaimplementowane wraz ze snacbarem informacyjnym.

## MODUŁ 2 — Zarządzanie Użytkownikami

### Analiza endpointów

1. **`GET /api/admin/users`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Działa i wyświetla listę, ale filtrowanie (po frazie) działa po stronie klienta (z pamięci RAM). Może to rodzić obciążenie przy dużej ilości kont.

2. **`POST /api/admin/users`**
   - Status: ✅ POKRYTY
   - Opis: Tworzenie konta, włącznie z kaskadowym przypisywaniem mieszkań (drzewo budynków), jest zaimplementowane. Posiada weryfikację błędów API.

3. **`PUT /api/admin/users/{id}`**
   - Status: ❌ BRAK
   - Rola: ZARZĄDCA
   - Priorytet: KRYTYCZNY — Brak interfejsu w panelu UI do zaktualizowania profilu/danych/numeru użytkownika. Ponadto sam serwis API po stronie frontendu ma błędnie zdefiniowaną metodę `PATCH api/admin/users/{id}`. 

4. **`PATCH /api/admin/users/{id}/deactivate`**
   - Status: ✅ POKRYTY
   - Opis: Operacja dezaktywacji jest obsługiwana pomyślnie.

5. **`DELETE /api/admin/users/{id}`**
   - Status: ❌ BRAK
   - Rola: ZARZĄDCA
   - Priorytet: ŚREDNI — Aplikacja mobilna wspiera wyłącznie operację soft-delete (dezaktywację), całkowite usunięcie (tzw. hard delete) nie jest w ogóle podpięte.

6. **`GET /api/users`**
   - Status: ✅ POKRYTY
   - Opis: Wykorzystane z sukcesem w komponencie biletów `TicketDetailsViewModel` do pobrania listy konserwatorów poprzez `?role=KONSERWATOR`.

## MODUŁ 1 — Uwierzytelnianie i Sesja

### Analiza endpointów

1. **`POST /api/auth/login`**
   - Status: ✅ POKRYTY
   - Opis: Pełna obsługa logowania wraz z komunikatami błędów.

2. **`POST /api/auth/refresh`**
   - Status: ✅ POKRYTY
   - Opis: Działa automatycznie po stronie sieciowej (Authenticator).

3. **`POST /api/auth/forgot-password`**
   - Status: ✅ POKRYTY
   - Opis: Zaimplementowane w `ForgotPasswordViewModel`.

4. **`POST /api/auth/reset-password`**
   - Status: ✅ POKRYTY
   - Opis: Zaimplementowane z uwzględnieniem deeplinkowania (`ResetPasswordScreen`).

5. **`POST /api/auth/accept-invitation`**
   - Status: ❌ BRAK
   - Rola: MIESZKANIEC / KONSERWATOR
   - Priorytet: KRYTYCZNY — Brak interfejsu i wywołania dla akceptacji zaproszenia po utworzeniu użytkownika przez Zarządcę. Oznacza to, że osoby zaproszone do systemu nie mogą skutecznie utworzyć hasła i rozpocząć sesji korzystając z aplikacji mobilnej.

## MODUŁ 5 — Ogłoszenia

### Analiza endpointów

1. **`GET /api/announcements`**
   - Status: ✅ POKRYTY
   - Opis: Działa poprawnie w liście po stronie mieszkańca oraz zarządcy.

2. **`GET /api/announcements/{id}/attachment`**
   - Status: ✅ POKRYTY
   - Opis: Otwieranie/pobieranie załączników PDF działa poprzez `Intent ACTION_VIEW`.

3. **`POST /api/announcements`**
   - Status: ❌ BRAK
   - Rola: ZARZĄDCA
   - Priorytet: WYSOKI — Klasa serwisowa posiada kontrakt, ale nie istnieją żadne ekrany tworzenia (`CreateAnnouncementScreen`), więc zarządca z poziomu aplikacji nie doda ogłoszenia.

4. **`PUT /api/announcements/{id}`**
   - Status: ❌ BRAK
   - Rola: ZARZĄDCA
   - Priorytet: ŚREDNI — Podobnie jak przy tworzeniu, interfejs graficzny edycji nie został przygotowany.

5. **`DELETE /api/announcements/{id}`**
   - Status: ❌ BRAK
   - Rola: ZARZĄDCA
   - Priorytet: ŚREDNI — Brak integracji usuwania ogłoszenia z poziomu UI na liście.

## MODUŁ 6 — Uchwały i Głosowania

### Analiza endpointów

1. **`GET /api/resolutions`**
   - Status: ✅ POKRYTY
   - Opis: Lista uchwał jest ładowana poprawnie.

2. **`GET /api/resolutions/{id}`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Działa poprawnie, jednak backend nie zwraca wprost informacji czy dany mieszkaniec już oddał głos. Frontend zapisuje flagę `hasVoted` wyłącznie w pamięci ulotnej widoku, co skutkuje brakiem blokady głosowania po ponownym wejściu w ten sam ekran (aż do odrzucenia przez backend).

3. **`POST /api/resolutions`**
   - Status: ✅ POKRYTY
   - Opis: Tworzenie zaimplementowane wraz z uwzględnieniem opcji wyboru (dynamicznych).

4. **`POST /api/resolutions/{id}/vote`**
   - Status: ✅ POKRYTY
   - Opis: Oddawanie głosu jest podpięte, po błędzie wyświetla Snackbar.

5. **`GET /api/resolutions/{id}/report`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Endpoint podpięty, jednak aplikacja mobilna nie informuje o stanie ładowania dla pobierania raportu (brak widocznego wskaźnika w UI podczas generowania PDF).

## MODUŁ 8 — Finanse i Transakcje

### Analiza endpointów

1. **`GET /api/apartments/{apartmentId}/transactions`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Zaimplementowane na ekranie `FinancialLedgerScreen` używając realnego API, jednak główny ekran `FinancesScreen` (zakładka przeglądu) korzysta wyłącznie z twardo zakodowanych (hardcoded) danych (mock `FinancesService`).

2. **`POST /api/apartments/{apartmentId}/transactions`**
   - Status: ✅ POKRYTY
   - Opis: Ręczne dodawanie transakcji przez zarządcę (np. WPLATA, NALICZENIE) działa w 100%.

3. **`POST /api/finance/import`**
   - Status: ✅ POKRYTY
   - Opis: Zaimplementowany ekran wgrywania CSV (`CsvImportScreen`) działa pomyślnie.

4. **`GET /api/admin/apartments/balances`**
   - Status: ✅ POKRYTY
   - Opis: Rozliczenia zbiorcze i filtrowanie działają po stronie zarządcy z odpowiednim mapowaniem błędów.

## MODUŁ 9 — Dokumenty

### Analiza endpointów

1. **`GET /api/documents`**
   - Status: ✅ POKRYTY
   - Opis: Działa, lista pobierana prawidłowo w `FinancesViewModel`.

2. **`GET /api/documents/{id}/download`**
   - Status: ✅ POKRYTY
   - Opis: Pobieranie zaimplementowane pomyślnie.

3. **`POST /api/admin/documents/rate-change`**
   - Status: ✅ POKRYTY
   - Opis: Wykorzystywane przez moduł dystrybucji dokumentów (Zarządca).

4. **`POST /api/admin/documents/annual-settlement`**
   - Status: ✅ POKRYTY
   - Opis: Wykorzystywane przez moduł dystrybucji dokumentów (Zarządca).

## MODUŁ 10 — PDF (Generowanie)

### Analiza endpointów

1. **`POST /api/pdf/work-acceptance-protocol`**
   - Status: ✅ POKRYTY
   - Opis: Zaimplementowane w zgłoszeniach serwisowych do pobierania protokołu odbioru prac.

2. **`GET /api/pdf/balances-report`**
   - Status: ✅ POKRYTY
   - Opis: Wykorzystywane przy eksporcie sald mieszkańców na ekranie `ApartmentBalancesScreen`.

## MODUŁ 11 — Przeglądy Techniczne

### Analiza endpointów

1. **`GET /api/inspections`**
   - Status: ✅ POKRYTY
   - Opis: Lista przeglądów poprawnie pobierana.

2. **`POST /api/inspections`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Tworzenie zaimplementowane dla klatek i budynków, jednak w wypadku wybrania zasięgu NIERUCHOMOŚĆ (Property) brakuje implementacji po stronie widoku formularza (`TODO: pobranie ID nieruchomości`), co uniemożliwia utworzenie takiego przeglądu.

3. **`PUT /api/inspections/{id}`**
   - Status: ✅ POKRYTY
   - Opis: Aktualizacja przeglądu zaimplementowana.

4. **`DELETE /api/inspections/{id}`**
   - Status: ✅ POKRYTY
   - Opis: Zaimplementowano usuwanie z komunikatem potwierdzenia.

## MODUŁ 12 — Powiadomienia Push i Urządzenia

### Analiza endpointów

1. **`POST /api/devices`**
   - Status: ⚠️ CZĘŚCIOWO
   - Opis: Niezgodność struktury URI. Frontend odwołuje się do `POST /api/devices/register` w `AuthViewModel.tryRegisterFcmToken()`, zamiast do oczekiwanego przez serwer `/api/devices`. Rejestracja urządzeń nie powiedzie się.

2. **`DELETE /api/devices`**
   - Status: ❌ BRAK
   - Rola: MIESZKANIEC / ZARZĄDCA / KONSERWATOR
   - Priorytet: ŚREDNI — Pod kątem bezpieczeństwa, po wylogowaniu się użytkownika z telefonu, aplikacja nie wysyła żądania usunięcia swojego FCM Tokena z backendu. Może to poskutkować dalszym wysyłaniem powiadomień.

3. **`GET /api/admin/notifications/settings`**
   - Status: ✅ POKRYTY
   - Opis: Działa.

4. **`PATCH /api/admin/notifications/settings/{eventType}`**
   - Status: ✅ POKRYTY
   - Opis: Zaimplementowane z tzw. *optimistic update* w interfejsie.

---

## FAZA 2 — Analiza UX i Nawigacji (End-to-End)

### Ocena przepływów z perspektywy ról

#### 1. MIESZKANIEC
Z perspektywy Mieszkańca, kluczowe ścieżki wyglądają następująco:
- **Logowanie i autoryzacja:** Nowy użytkownik zaproszony przez zarządcę do systemu natrafia na tzw. "ścianę" – w aplikacji brakuje wywołania punktu końcowego `accept-invitation`. Może się zalogować tylko, jeśli wcześniej aktywował konto inną drogą (np. przez platformę web).
- **Zgłoszenia:** Może swobodnie zgłaszać problemy (z obsługą zdjęć i kategorii), lecz z racji braku implementacji widoku historii zgłoszenia nie ma klarownego wglądu w proces rozwiązywania problemu.
- **Finanse:** Przepływ ten jest mylący, bowiem na stronie głównej Finansów UI dostarcza "mockowane", twardo zapisane dane. Poprawne i rzeczywiste informacje odczyta dopiero przechodząc do Księgi Głównej (Ledger).
- **Komunikacja:** Mieszkaniec może bez trudu wyświetlać bieżące ogłoszenia oraz odbierać raporty PDF z ankiet.

#### 2. ZARZĄDCA
- **Zarządzanie zgłoszeniami:** Podstawowy przepływ odrzucenia/zakończenia bądź delegowania zgłoszeń jest **CAŁKOWICIE ZABLOKOWANY** ze względu na krytyczną niezgodność kontraktu HTTP (Frontend wysyła `PATCH`, Backend oczekuje `POST`). Zapytania zwrócą błąd 405 Method Not Allowed.
- **Dokumenty i finanse:** W pełni działające procesy zarządzania finansami, importu dokumentów oraz generacji masowej sald i raportów PDF.
- **Ogłoszenia:** Przepływ niekompletny – zarządca pozbawiony jest w UI narzędzi do dodawania, edytowania i usuwania ogłoszeń. Może jedynie takowe wyświetlić.
- **Przeglądy i struktura:** Bardzo dobrze obudowane zarządzanie drzewem (nieruchomości-lokale), aczkolwiek usunięcie węzła bądź przypisanie testu przeglądu bezpośrednio pod korzeń nieruchomości "urywa się" w oknie z twardym komunikatem `TODO`.

#### 3. KONSERWATOR
- Podobnie jak w kwestii zarządcy, całe działanie konserwatora opiera się na przyjmowaniu i zmienianiu statusu usterek. Błędne podpięcie metod modyfikujących status (`PATCH` w miejsce `POST`) uniemożliwia konserwatorowi raportowanie czasu wykonania zgłoszenia oraz posuwania w przód maszyny stanowej. Upload załączników do usterek jest wspierany pomyślnie.

### Ślepe uliczki w nawigacji i urwane przepływy
1. **Widok profilu użytkownika (`ProfileScreen`)** — interfejs wspiera wklepywanie nowego "imienia/nazwiska", posiada okienko autoryzacji zmiany, aczkolwiek oparty jest jedynie o symulowane "delay(300)". Backend nie udostępnia endpointu `PUT /api/users/me`, w związku z czym flow aktualizacji profilu nie zadziała.
2. **Kopia powiadomień ustawień (`NotificationSettingsScreen`) w profilu** — ekran nie dokonuje wywołań do realnego serwera w odróżnieniu od bliźniaczego panelu po stronie Zarządcy.
3. Przeglądy zablokowane dla poziomu całościowej Nieruchomości z komentarzem TODO w kodzie.
4. Brak opcji jakiegokolwiek kasowania struktury mieszkaniowej i poszczególnych budynków. Opcje dostępne są na poziomie API, nie po stronie interfejsu.

---

## FAZA 3 — Podsumowanie i Metryki

### Metryki pokrycia API (w oparciu o sumaryczną liczbę 85 zadeklarowanych endpointów)
- Liczba endpointów **✅ POKRYTYCH**: **49** (ok. 57.6%)
- Liczba endpointów **⚠️ CZĘŚCIOWO POKRYTYCH**: **20** (ok. 23.5%)
- Liczba endpointów **❌ BRAK**: **16** (ok. 18.8%)

### Top 10 najbardziej krytycznych braków w integracji
*(Od najwyższego priorytetu po najniższe, decydujące o zdatności do wdrożenia)*

1. **Zepsute zarządzenie procesem Zgłoszeń (Krytyczne HTTP Methods)** — frontend odpytuje endpointy akcji (`assign`, `close`, `reject`, `start`, `suspend`) za pomocą metody `PATCH` oraz stosuje błędną ścieżkę do `completion`, co skutkuje trwałym paraliżem głównego feature’a aplikacji.
2. **Brak API dla zaproszeń kont** — brak obsługi `/api/auth/accept-invitation` uniemożliwia zaproszonym lokatorom i nowym konserwatorom dołączenie do instancji.
3. **Hardkodowane dane (Fake data) dla głównego pulpitu Finansów** — udawanie integracji w okienkach zbiorczych może drastycznie wprowadzić mieszkańców w błąd.
4. **Rozbieżne rejestrowanie powiadomień Push (FCM Token)** — frontend wysyła klucz Firebase pod zły adres (`/api/devices/register`), przez co klienci mobilni nie otrzymają natywnych alertów, dodatkowo nie kasują kluczy po wylogowaniu.
5. **Brak opcji redakcyjnych dla ogłoszeń** — z poziomu apki menedżerowie wspólnot nie są w stanie wysłać przypomnienia czy opublikować ogłoszenia.
6. **Brak wsparcia w aktualizacji danych swojego profilu** — profil wizualny to fałszywa pusta makieta.
7. **Pobieranie listy lokali jako Drzewa (`/api/buildings/tree`)** — powołując się na adres endpointu (złe URI wstrzyknięte).
8. **Stan uchwał przetrzymywany lokalnie w RAM (Flaga hasVoted)** — Mieszkaniec wchodzi w glosowanie, oddaje je, wychodzi i widzi formularz do glosowania od nowa (błąd logiki biznesowej widoku).
9. **Stała paginacja w odczytach liczników** — zakodowanie `?page=0&size=100` poskutkuje utratą danych historii na etapie produkcji po dłuższym działaniu aplikacji.
10. **Brak "hard delete" dla całego węzła mieszkaniowego** — na wypadek drobnego błędu przy dodawaniu struktury bloków, zarządca musi radzić sobie z poziomu bazodanowego z racji ucięcia tego po stronie mobilnej.

### Ogólna ocena stanu frontendu
Frontend BlokUR to solidnie zbudowany projekt oparty na czystej, warstwowej architekturze w Compose. Pomimo bardzo spójnego szkieletu i przemyślanych wizualnie ekranów projekt znajduje się bliżej fazy "wczesnej wersji Beta" niż wariantu produkcyjnego. 
Część funkcji funkcjonuje bez zarzutów (np. pobieranie statystyk uchwał, raportów PDF, wczytywanie drzewa hierarchii lokali), jednak w aplikacji pojawiają się **dziury integracyjne dyskwalifikujące jej release**. 

Największą wadą są zaszyte makiety (hardcoded stubs) z poprzednich iteracji, o których zapomniano w momencie przypinania prawdziwego API, oraz literówki w definiowanych REST metodach, niszczące fundamentalne użycie dla 2 z 3 ról (Zarządcy oraz Konserwatora). Projekt po załataniu wymienionych "Ślepych Uliczek" będzie niezwykle potężnym narzędziem w systemie.
