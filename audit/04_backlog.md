# Backlog i Plan Implementacji Frontend BlokUR

Poniższy backlog został zbudowany na podstawie wyników audytu Gap Analysis (część 3). Zadania podzielone są według głównych grup funkcjonalnych systemu.

## FAZA 1 — Backlog Zadań

### Grupa 1: Autoryzacja, Powiadomienia i Profil

**ID:** FRONT-001
**Tytuł:** Ekran i integracja akceptacji zaproszenia
**Moduł:** Autoryzacja i Sesja
**Rola:** MIESZKANIEC / KONSERWATOR
**Priorytet:** KRYTYCZNY
**Zależności:** brak
**Opis:**
* **Ekrany:** Stworzenie nowego ekranu `AcceptInvitationScreen` z formularzem.
* **API:** Podpięcie wywołania `POST /api/auth/accept-invitation` po wpisaniu i zatwierdzeniu hasła.
* **Stany:** Obsługa `loading` (spinner na przycisku akceptacji), `error` (niepoprawne hasło, wygasły link pokazywany w Snackbar lub na ekranie), `success` (przeniesienie do aplikacji).
* **Nawigacja:** Dodanie routingu z deeplinku do tego ekranu, a po stanie success — nawigacja na widok główny aplikacji.

**Kryteria akceptacji:**
 - [x] Stworzenie nowego ekranu `AcceptInvitationScreen` w Compose z formularzem podania hasła.
 - [x] Podpięcie metody HTTP `POST /api/auth/accept-invitation` uwzględniającej obsługę błędu 400 (słabe hasło) lub błędu wygasłego tokenu.
 - [x] Zapewnienie stanów `loading`, `error` i `success`.
 - [x] Przejście po sukcesie na widok główny aplikacji.

**ID:** FRONT-002
**Tytuł:** Naprawa rejestracji tokenu Push (FCM)
**Moduł:** Powiadomienia
**Rola:** WSZYSTKIE
**Priorytet:** WYSOKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Brak modyfikacji wizualnych, zmiany pod spodem (w AuthViewModel).
* **API:** Podpięcie wywołania `POST /api/devices` zamiast wadliwego `/api/devices/register`.
* **Stany:** Obsługa stanu `error` (ciche przechwycenie w logach, by błąd rejestracji urządzenia nie zatrzymał logowania). 
* **Nawigacja:** Brak zmian.

**Kryteria akceptacji:**
 - [x] Zmiana adnotacji Retrofit na ścieżkę `/api/devices`.
 - [x] Ciche przechwycenie błędów (bez rzucania błędem logowania na froncie) w wypadku niedziałającego serwisu Push.

**ID:** FRONT-003
**Tytuł:** Wyrejestrowanie urządzenia FCM przy wylogowaniu
**Moduł:** Powiadomienia
**Rola:** WSZYSTKIE
**Priorytet:** ŚREDNI
**Zależności:** FRONT-002
**Opis:**
* **Ekrany:** Brak nowych ekranów.
* **API:** Podpięcie `DELETE /api/devices` usuwającego wygenerowany token powiadomień.
* **Stany:** Obsługa `error` (ciche) przy wylogowywaniu.
* **Nawigacja:** Wylogowanie kończy się standardowo przekierowaniem na LoginScreen.

**Kryteria akceptacji:**
 - [x] Dopisanie operacji deaktywacji FCM w sekwencji metody `logout()`.
 - [x] Testowe sprawdzenie, czy wysłanie komunikatu z panelu nie obudzi telefonu po wylogowaniu.

**ID:** FRONT-004
**Tytuł:** Refaktoryzacja widoku Profilu Użytkownika (Makieta -> Rzeczywiste)
**Moduł:** Profil Użytkownika
**Rola:** WSZYSTKIE
**Priorytet:** ŚREDNI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja obecnego `ProfileScreen` — usunięcie makiety i wyświetlenie realnych danych użytkownika.
* **API:** Podpięcie pobierania danych logowanego użytkownika lub mapowanie stanu z sesji z `GET /api/users/me`.
* **Stany:** Obsługa stanu `loading` przy odczycie z DataStore/API, oraz `empty` (brak danych z fallbackiem na zdefiniowane wartości).
* **Nawigacja:** Brak zmian w routingu, ekran zostaje podpięty pod istniejącą pozycję w menu ustawień.

**Kryteria akceptacji:**
 - [x] Usunięte sztywno wpisane dane testowe z `ProfileViewModel`.
 - [x] Zasilanie UI zweryfikowanymi danymi konta.

### Grupa 2: Zgłoszenia Serwisowe (Tickets)

**ID:** FRONT-005
**Tytuł:** Naprawa krytycznych metod HTTP i ścieżek akcji dla Zgłoszeń
**Moduł:** Zgłoszenia Serwisowe
**Rola:** ZARZĄDCA / KONSERWATOR
**Priorytet:** KRYTYCZNY
**Zależności:** brak
**Opis:**
* **Ekrany:** Brak (jedynie poprawa ViewModelu i Repository wywoływanych z Listy biletów i Detali biletu).
* **API:** Zmiana metod Retrofit z PATCH na POST dla `assign`, `close`, `reject`, `suspend`. Zmiana endpointów na `POST /api/tickets/{id}/start-work` i `POST /api/tickets/{id}/complete`.
* **Stany:** Przekazywanie `error` do UI w razie zwrotki np. błędu sieci 500 lub 400. `success` aktualizuje stan lokalny.
* **Nawigacja:** Odświeżenie widoku / powrót na listę po udanej akcji.

**Kryteria akceptacji:**
 - [x] Metody `assign`, `close`, `reject`, `suspend` zmienione na `POST`.
 - [x] Ścieżka dla pracy zmieniona na `POST /api/tickets/{id}/start-work`.
 - [x] Ścieżka zakończenia na `POST /api/tickets/{id}/complete`.

**ID:** FRONT-006
**Tytuł:** Paginacja i filtrowanie listy zgłoszeń (Server-Side)
**Moduł:** Zgłoszenia Serwisowe
**Rola:** WSZYSTKIE
**Priorytet:** WYSOKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja `TicketsScreen` dla wsparcia nieskończonego przewijania listy i komponentów filtrujących.
* **API:** Modyfikacja wywołania `GET /api/tickets` o parametry `@Query("page")`, `size`, `status` i wysyłanie ich na backend.
* **Stany:** `loading` (szkielet lub spinner na dole listy dopisujący itemy), `empty` (brak biletów w tej kategorii), `error` (shimmer przerywający pobieranie).
* **Nawigacja:** Pozostaje bez zmian.

**Kryteria akceptacji:**
 - [x] Interfejs API przyjmuje `@Query("page")`, `@Query("size")`, `@Query("status")` itd.
 - [x] Po dojechaniu na dół ekranu automatyczne doładowywanie starych zgłoszeń (stan loading w LazyColumn).

**ID:** FRONT-007
**Tytuł:** Widok Osi Historii Zgłoszenia (Timeline)
**Moduł:** Zgłoszenia Serwisowe
**Rola:** ZARZĄDCA / KONSERWATOR
**Priorytet:** ŚREDNI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja `TicketDetailsScreen` przez doklejenie sekcji historii (Timeline) z przejściami statusów na dole strony.
* **API:** Podpięcie wywołania `GET /api/tickets/{id}/history`.
* **Stany:** `loading` przed załadowaniem historii, `empty` dla braku akcji w historii, `error` cicho (lub ze wskaźnikiem) w bloku, by nie zepsuć całego ekranu.
* **Nawigacja:** Scroll na sam dół widoku szczegółów ujawnia nową sekcję, brak nowych tras.

**Kryteria akceptacji:**
 - [x] Interfejs użytkownika w postaci prostej listy lub stepper'a z informacjami historycznymi.
 - [x] Obsługa przypadku `empty state` (brak historii).

**ID:** FRONT-008
**Tytuł:** Poprawa obsługi błędów ładowania załączników zgłoszenia
**Moduł:** Zgłoszenia Serwisowe
**Rola:** WSZYSTKIE
**Priorytet:** NISKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja UI w `TicketDetailsScreen` oraz formularzu tworzenia `CreateTicketScreen`. 
* **API:** Implementacja akcji usuwania dla zdjęcia `DELETE /api/images/{imageId}`. Obsługa wyjątków na `GET /comments`.
* **Stany:** Obsługa `error` wyświetlającego Toast/Snackbar podczas błędu ładowania obrazów lub wysyłania komentarzy. `success` po skasowaniu obrazu w locie aktualizujący listę załączników.
* **Nawigacja:** Brak zmian.

**Kryteria akceptacji:**
 - [x] Ekran uwzględnia stany błędu i rzuca Snackbar w interfejsie.
 - [x] Dodać akcję (przycisk) usunięcia omyłkowo wgranego obrazka z poziomu UI.

### Grupa 3: Finanse

**ID:** FRONT-009
**Tytuł:** Ekran Pulpitu Finansowego (FinancesScreen) bazujący na API
**Moduł:** Finanse
**Rola:** MIESZKANIEC
**Priorytet:** WYSOKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja `FinancesScreen` – zlikwidowanie twardo wpisanych danych mockupowych.
* **API:** Zastąpienie makiety prawdziwymi strzałami do `/api/apartments/{id}/transactions` lub bilansem.
* **Stany:** `loading` ładujący cały dashboard, `error` (jeśli backend finansów leży) wyświetlający przycisk "Ponów". 
* **Nawigacja:** Po kliknięciu na kafelek szczegółów przejście do LedgerScreen.

**Kryteria akceptacji:**
 - [x] Usunięte mocki kwot finansowych w `FinancesScreen`.
 - [x] Sumowanie i formatowanie balansu na podstawie realnych danych per użytkownik.

### Grupa 4: Ogłoszenia i Uchwały

**ID:** FRONT-010
**Tytuł:** Interfejs Zarządzania Ogłoszeniami dla Zarządcy (Tworzenie i Edycja)
**Moduł:** Ogłoszenia
**Rola:** ZARZĄDCA
**Priorytet:** WYSOKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Dodanie całkowicie nowego widoku `CreateAnnouncementScreen` (formularz z Tytułem, Treścią i załącznikami).
* **API:** Zaimplementowanie wywołań: `POST /api/announcements`, `PUT /api/announcements/{id}` oraz `DELETE /api/announcements/{id}` z widoku listy.
* **Stany:** Formularz z walidacją, stany zapisu `loading`, `error` w postaci czerwonego Toastu, `success`.
* **Nawigacja:** Przycisk Floating Action Button (FAB) na liście do /create i nawigacja powrotna (`popBackStack()`) po udanym zapisaniu.

**Kryteria akceptacji:**
 - [ ] Ekran dodawania z polami tekstowymi.
 - [ ] Integracja operacji Tworzenia, Edytowania i Usuwania (`DELETE`).
 - [ ] Reaktywna zmiana stanu głównej listy.

**ID:** FRONT-011
**Tytuł:** Perzystentna informacja o oddanym głosie (Uchwały)
**Moduł:** Uchwały
**Rola:** MIESZKANIEC
**Priorytet:** WYSOKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja ekranu szczegółów uchwały `ResolutionDetailsScreen`.
* **API:** Sprawdzanie zwrotki z `GET /api/resolutions/{id}` w poszukiwaniu flagi wskazującej na już oddany głos (np. `userVoted`).
* **Stany:** Obsługa stanu oddanego głosu (`success` blokada) jako zablokowanego widoku (readonly) z szarymi przyciskami radiowymi.
* **Nawigacja:** Brak zmian.

**Kryteria akceptacji:**
 - [ ] Mapowanie odpowiedzi backendu w ResolutionDTO pod UI state i zmiana zachowania przycisku oraz radio buttons.

**ID:** FRONT-012
**Tytuł:** Wskaźnik ładowania podczas pobierania PDF Uchwały
**Moduł:** Uchwały
**Rola:** WSZYSTKIE
**Priorytet:** NISKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Zmiana wyglądu przycisku pobierania na `ResolutionDetailsScreen` i `TicketDetailsScreen`.
* **API:** Wywołania generacji PDF `GET /api/resolutions/{id}/report`.
* **Stany:** `loading` renderowany jako CircularProgressIndicator w samym przycisku, oraz błędu `error` (jeśli strumień pliku polegnie).
* **Nawigacja:** Pomyślne pobranie pliku może uruchomić `Intent ACTION_VIEW` z otworzeniem zewnętrznej aplikacji PDF.

**Kryteria akceptacji:**
 - [ ] Stan w viewmodelu blokujący dwukrotne pobieranie.
 - [ ] Wizualny loader.

### Grupa 5: Drzewo Nieruchomości i Przeglądy

**ID:** FRONT-013
**Tytuł:** Narzędzia operacji kasowania w Drzewie Nieruchomości
**Moduł:** Nieruchomości i Budynki
**Rola:** ZARZĄDCA
**Priorytet:** ŚREDNI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja widoku zarządzania budynkami `PropertyTreeScreen`.
* **API:** Poprawa ścieżki na `/api/buildings`. Dodanie implementacji `DELETE /api/buildings/{id}` oraz analogicznych dla klatek i lokali.
* **Stany:** Konieczność obsługi błędów kasowania np. w przypadku powiązań (409 Conflict) oraz stanu usunięcia `success` przeładowującego drzewko.
* **Nawigacja:** Popup dialog "Czy na pewno chcesz usunąć" dla każdego itemu z drzewa.

**Kryteria akceptacji:**
 - [ ] Implementacja wizualnej akcji usunięcia każdego z węzłów hierarchii.
 - [ ] Komunikat ostrzegawczy z potwierdzeniem operacji "Usuń".

**ID:** FRONT-014
**Tytuł:** Przeglądy w zasięgu Nieruchomości
**Moduł:** Przeglądy Techniczne
**Rola:** ZARZĄDCA
**Priorytet:** ŚREDNI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja `CreateInspectionScreen`.
* **API:** Podczas akcji zapisu, przekazanie z widoku parametrów do `POST /api/inspections`.
* **Stany:** Usuwa z UI blokujący "TODO state". Pomyślne utworzenie to `success`.
* **Nawigacja:** Po zamknięciu poprawne wyjście z modalu.

**Kryteria akceptacji:**
 - [ ] Zmiana logiki tworzenia inspekcji pod zasięg Property na wstrzyknięcie poprawnego PropertyID ze stanu lub drzewa.

### Grupa 6: Użytkownicy i Liczniki

**ID:** FRONT-015
**Tytuł:** Paginacja użytkowników oraz wyszukiwarka
**Moduł:** Zarządzanie Użytkownikami
**Rola:** ZARZĄDCA
**Priorytet:** WYSOKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Przepisanie komponentu listy w sekcji `AdminUsersScreen`.
* **API:** Transformacja `GET /api/admin/users` do zapytania sparametryzowanego.
* **Stany:** Obsługa ładowania na szukajce oraz scrollowaniu `loading`. 
* **Nawigacja:** Brak zmian.

**Kryteria akceptacji:**
 - [ ] Opóźnienie na inpucie szukajki w UI omija spam zapytań (debounce np. 500ms).

**ID:** FRONT-016
**Tytuł:** Narzędzia edycji i permanentnego usuwania kont
**Moduł:** Zarządzanie Użytkownikami
**Rola:** ZARZĄDCA
**Priorytet:** WYSOKI
**Zależności:** brak
**Opis:**
* **Ekrany:** Stworzenie nowego podwidoku dla Admina `EditUserScreen` / `UserDetailsScreen`.
* **API:** Podpięcie `PUT /api/admin/users/{id}` do nadpisywania oraz `DELETE /api/admin/users/{id}` (permanent).
* **Stany:** Posiadanie stanów `empty` braku profilu, `error` problemów z aktualizacją, oraz wyszarzenie ekranu przy permanentnym usunięciu `loading`.
* **Nawigacja:** Z listy kliknięcie w kontener przenosi nas na `/users/{id}` z przyciskami aktualizacji.

**Kryteria akceptacji:**
 - [ ] Stworzenie UI dla edycji użytkownika.
 - [ ] Zapewnienie stanów ładowania i notyfikacji o sukcesie akcji.

**ID:** FRONT-017
**Tytuł:** Paginacja historii Odczytów Liczników i url ścieżki
**Moduł:** Liczniki
**Rola:** WSZYSTKIE
**Priorytet:** ŚREDNI
**Zależności:** brak
**Opis:**
* **Ekrany:** Modyfikacja sekcji detali w widoku `MetersScreen`.
* **API:** Korekta statycznego `?size=100` na standardową integrację parametrami z `GET /api/apartments/{apartmentId}/meter-readings`. Zmiana adresu przy deaktywacji.
* **Stany:** Infinite scroll obsługujący dolne dobieranie danych z bazy `loading`.
* **Nawigacja:** Brak zmian.

**Kryteria akceptacji:**
 - [ ] Skorygowane adnotacje URL (dodanie ścieżki ze wstrzykiwanym kluczem).

---

## FAZA 2 — Roadmapa implementacji

### Etap 1: Fundament (Krytyczne poprawki i blokery)
*Zadania w tym etapie są warunkiem przejścia do testowania aplikacji przez nowych użytkowników (Zarządcę i Konserwatorów).*
1. **FRONT-001** (Accept Invitation Screen) - Blokuje w ogóle wejście do aplikacji połowie zaproszonych użytkowników.
2. **FRONT-005** (Naprawa metod HTTP w Tickets) - Blokuje całkowicie rdzeń działania aplikacji (nie można odrzucać, kończyć zgłoszeń).
3. **FRONT-002** (FCM Push Fix) - Krytyczne narzędzie dla użyteczności mobilnej, błędne uderzenia blokują system alertów.

### Etap 2: Przepływy krytyczne (End-to-End Features)
*Skompletowanie głównych zarysów funkcji systemu, gdzie brakowało kluczowych klocków.*
1. **FRONT-009** (Rzeczywiste Finanse z Dashboardu) - Likwiduje drastyczne wprowadzanie w błąd dla lokatora używając fake'owych danych.
2. **FRONT-010** (Tworzenie i Zarządzanie Ogłoszeniami Zarządca) - Domyka kompletny flow ogłoszeniowy.
3. **FRONT-011** (Persystencja hasVoted Uchwały) - Zamyka blokowanie podwójnego logowania na formularzach ankiet.
4. **FRONT-016** (Edycja kont użytkowników dla Admina).

### Etap 3: Optymalizacja wydajności (Server-Side)
*Przebudowa punktów zatykających się przy skalowaniu systemu z małej liczby do setek wpisów.*
1. **FRONT-006** (Paginacja listy Tickets).
2. **FRONT-015** (Paginacja Użytkowników).
3. **FRONT-017** (Paginacja Odczytów Liczników).

### Etap 4: Polerowanie i Edge Cases (UX)
*Funkcje uzupełniające dla poprawienia transparentności obsługi.*
1. Opcje usuwania węzłów nieruchomości (**FRONT-013**).
2. Oś czasu napraw na Zgłoszeniach (**FRONT-007**).
3. Obsługa loadingu dla PDF i błędów sieci w formularzach komentarzy (**FRONT-008**, **FRONT-012**).
4. Profil Read/Edit dla użytkownika oraz wyrejestrowywanie tokenów FCM z RAMu przy wyjściu (**FRONT-003**, **FRONT-004**).

---

## FAZA 3 — Ryzyka techniczne i ocena konieczności refaktoryzacji

Po przejrzeniu architektury projektowej z inwentaryzacji (02_frontend_inventory.md), mapujemy obszary wysokiego ryzyka:

1. **Brak generycznej (centralnej) obsługi błędów sieciowych**
   - **Ryzyko:** Jeśli każda metoda (jak dotąd) samodzielnie łapie HTTP 400, 401, 500 z try-catch i ukrywa je w postaci "silent fail" lub wypluwa Snackbar tylko na jednym ekranie, dodanie dziesiątek zadań (np. ekranów zaproszenia, ogłoszeń) mocno zduplikuje ten brzydki wzorzec. Brak też globalnego wylogowania po utracie autoryzacji `401 Unauthorized`.
   - **Decyzja:** Wymaga to **refaktoru (utworzenia BaseViewModel lub globalnego ErrorHandlera) PRZED lub w TRAKCIE Etapu 1**, dla zachowania spójności.

2. **Hardkodowany Bazowy URL (BaseURL w DI)**
   - **Ryzyko:** Przy pracy deweloperskiej lokalnie (np. na emulatorze 10.0.2.2 vs serwer hostowany w chmurze) każda zmiana endpointu będzie obarczona commitami zmieniającymi produkcyjny URL.
   - **Decyzja:** Przepisać wdrożenie Retrofit na użycie `BuildConfig.BASE_URL`. Można to zrobić w 10 minut i **wymaga to natychmiastowego wdrożenia przed resztą nowości**.

3. **Inconsistent Architecture - "Brak warstwy Repository w kilku elementach"**
   - **Ryzyko:** Czasem, np. podczas dodawania zgłoszeń czy pobierania kategorii, użyto prostego injecta na widoku. Niesie to ze sobą ryzyko pęknięcia testowalności. 
   - **Decyzja:** Przenosić do ViewModel i Repository przyrostowo **w TRAKCIE implementacji poszczególnych zadań**. Nie wymaga wcześniejszego uderzenia wielkim refaktorem.

4. **Stany UI (UiState)**
   - **Ryzyko:** W niektórych starych ViewModelach stany są oparte wprost na zmiennych val (flagi boolean `isLoading` itp.), miast centralnego `sealed class UiState`.
   - **Decyzja:** Odejście od przestarzałej i podatnej na błędy formy, nowe i aktualizowane moduły (np. w Faza 1 - Zgłoszenia, Ogłoszenia) przejdą refaktoring pod silne stany domenowe równolegle, nie jako bloker.

5. **Tokenowanie JWT**
   - **Ryzyko:** Brak odnawiania tokenów, albo problem po re-autentykacji.
   - **Decyzja:** Authenticator z Retrofit jest odnotowany w inwentarzu jako poprawnie skonfigurowany. Będziemy jednak musieli zweryfikować czy w przypadku błędu z odświeżenia aplikacja wykonuje logout. Jest to rzecz optymalizacyjna, do korekty w Etapie 4.
