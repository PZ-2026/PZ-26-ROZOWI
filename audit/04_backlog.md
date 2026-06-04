# BlokUR — Backlog frontendu

**Data:** 2026-06-05  
**Źródła:** `audit/03_gap_analysis.md`, `audit/01_backend_inventory.md`, `audit/02_frontend_inventory.md`  
**Zakres:** **wyłącznie frontend** — dostosowanie UI, ViewModeli i warstwy sieci do **istniejącego** API backendu. **Brak nowych endpointów, zmian kontraktów i refaktorów backendu.**

**Konwencje:**
- ID: `FRONT-XXX`
- Szacunek: 1–2 dni robocze / zadanie
- Zależności: ID innych zadań frontowych lub „brak”

---

## Grupa A — Fundament (wspólne)

### FRONT-023
**Tytuł:** Wspólny mapper błędów HTTP dla warstwy serwisów  
**Moduł:** Infrastruktura / sieć  
**Rola:** WSZYSTKIE  
**Priorytet:** KRYTYCZNY  
**Zależności:** brak  
**Zakres backendu:** bez zmian — mapowanie istniejących kodów (400, 401, 403, 404, 409, 422, 429, 423).

**Opis:**
- Utworzyć np. `ApiErrorMapper` (lub rozszerzyć wzorzec z `TicketService.handleResponse`) zwracający czytelne komunikaty po polsku.
- Podłączyć w serwisach, które dziś po cichu ignorują `!isSuccessful` (komentarze, dokumenty, media).
- ViewModele: przy błędzie ustawiać snackbar / `Error` state — bez nowych endpointów.

**Kryteria akceptacji:**
- [ ] Przynajmniej `TicketCommentApiService` / `TicketDetailsViewModel` i jeden inny serwis używają wspólnego mappera.
- [ ] Przy HTTP 403/404 użytkownik widzi snackbar z komunikatem, nie pustą listę bez wyjaśnienia.
- [ ] Brak zmian w repozytorium `backend/`.

---

### FRONT-008
**Tytuł:** `ApartmentContext` — identyfikacja lokalu mieszkańca z `GET /api/buildings/tree`  
**Moduł:** Nieruchomości + Finanse  
**Rola:** MIESZKANIEC (głównie), WSZYSTKIE (odczyt)  
**Priorytet:** KRYTYCZNY  
**Zależności:** brak  
**Zakres backendu:** bez zmian — backend już filtruje drzewo wg roli w serwisie; frontend **nie wymaga** `GET /api/users/me`.

**Opis:**
- Dodać `@Singleton` serwis (np. `UserApartmentService`) cache’ujący wynik `PropertyService.getBuildingTree()` po zalogowaniu / przy wejściu w finanse.
- Logika: dla `MIESZKANIEC` wybrać lokal przypisany użytkownikowi z drzewa zwróconego przez backend (nie „pierwszy z listy” heurystycznie bez walidacji struktury).
- Jeśli drzewo puste / brak lokalu → stan `Error` z komunikatem „Brak przypisanego lokalu” (backend zwraca pustą strukturę — obsługa po stronie UI).
- Udostępnić `apartmentId: String?` przez Hilt do `FinancesViewModel`, `FinancialLedgerViewModel`.

**Kryteria akceptacji:**
- [ ] Mieszkaniec z przypisanym lokalem w backendzie dostaje stabilne `apartmentId` bez hardkodu.
- [ ] Przy braku lokalu w drzewie finanse pokazują ekran błędu, nie fałszywe 0 zł.
- [ ] Żadna zmiana w `backend/`.

---

### FRONT-024
**Tytuł:** Komponent `TicketImageThumbnail` — `GET /api/images/{id}`  
**Moduł:** Zgłoszenia / media  
**Rola:** WSZYSTKIE (wyświetlanie)  
**Priorytet:** KRYTYCZNY  
**Zależności:** brak  
**Zakres backendu:** bez zmian — endpoint już istnieje w `TicketImageController`.

**Opis:**
- Composable ładujący obraz przez Coil (lub istniejący stack) z URL `${BACKEND_URL}/api/images/{id}` + nagłówek `Authorization` (OkHttp interceptor już dodaje JWT).
- Stany: loading (placeholder), error (ikona), success.
- Opcjonalnie: tap → pełny ekran podglądu (lokalny, bez nowego API).

**Kryteria akceptacji:**
- [ ] Miniatura renderuje prawdziwy JPEG/PNG z backendu dla znanego `imageId`.
- [ ] Przy 403/404 wyświetlany jest stan błędu, nie puste miejsce.
- [ ] Brak zmian w backendzie.

---

## Grupa B — Zgłoszenia (9 zadań)

### FRONT-001
**Tytuł:** Galeria zdjęć na ekranie szczegółów zgłoszenia  
**Moduł:** Zgłoszenia  
**Rola:** MIESZKANIEC / ZARZĄDCA / KONSERWATOR  
**Priorytet:** KRYTYCZNY  
**Zależności:** FRONT-024, FRONT-023  
**Zakres backendu:** `GET /api/tickets/{id}/images`, `GET /api/images/{id}` — bez zmian.

**Opis:**
- Przebudować `TicketImagesSection`: zamiast emoji 📷 użyć `TicketImageThumbnail` per `TicketImageDto.id`.
- Zachować podział BEFORE / AFTER według `imageType` z API.
- Loading całej sekcji podczas `getImagesForTicket`; empty gdy lista pusta.

**Kryteria akceptacji:**
- [ ] Użytkownik widzi miniatury zdjęć już wgranych przez inne role.
- [ ] Sekcja nie wyświetla się jako pusta przy niepustej odpowiedzi API.
- [ ] Brak wywołań do nieistniejących endpointów.

---

### FRONT-002
**Tytuł:** Upload zdjęć do zgłoszenia (`POST /api/tickets/{id}/images`)  
**Moduł:** Zgłoszenia  
**Rola:** KONSERWATOR (wymagane), MIESZKANIEC (opcjonalnie jeśli backend pozwala — ta sama ścieżka API)  
**Priorytet:** KRYTYCZNY  
**Zależności:** FRONT-001  
**Zakres backendu:** multipart `file` + `image_type` (`BEFORE`|`AFTER`) — zgodnie z `TicketImageController`.

**Opis:**
- W `TicketDetailsViewModel` wstrzyknąć `TicketMediaServices` (już istnieje).
- FAB lub przyciski „Dodaj zdjęcie przed/po” widoczne dla KONSERWATORA wg `ticket.status` (np. `W_REALIZACJI`, `ZAKONCZONE_DO_WERYFIKACJI`).
- Photo Picker → `uploadImage(ticketId, file, imageType)` → reload listy; loading overlay podczas uploadu; błąd przez FRONT-023.

**Kryteria akceptacji:**
- [ ] Konservator może wgrać co najmniej jedno zdjęcie AFTER i zobaczyć je w galerii po odświeżeniu.
- [ ] Przy odrzuceniu przez backend (403/400) snackbar z komunikatem.
- [ ] Brak zmian w backendzie.

---

### FRONT-003
**Tytuł:** Usunięcie martwego „Usuń zdjęcie” (`DELETE /api/images/{id}`)  
**Moduł:** Zgłoszenia  
**Rola:** WSZYSTKIE  
**Priorytet:** WYSOKI  
**Zależności:** FRONT-001  
**Zakres backendu:** **nie dodawać** DELETE — backend nie eksponuje tego endpointu; usunąć wywołanie z `TicketImageApiService` / UI.

**Opis:**
- Usunąć `deleteImage` z Retrofit i `TicketDetailsViewModel.deleteImage`.
- Usunąć `IconButton` usuwania z `TicketImagesSection`.
- Jeśli produktowo wymagane usuwanie w przyszłości — osobny ticket **backendowy** (poza tym backlogiem).

**Kryteria akceptacji:**
- [ ] Żaden request DELETE na `/api/images/` nie jest wysyłany z aplikacji.
- [ ] UI nie sugeruje usuwania zdjęć.
- [ ] Brak zmian w backendzie.

---

### FRONT-004
**Tytuł:** Wznowienie zgłoszenia — `PATCH /api/tickets/{id}/status`  
**Moduł:** Zgłoszenia  
**Rola:** ZARZĄDCA  
**Priorytet:** WYSOKI  
**Zależności:** brak (TicketDetails istnieje)  
**Zakres backendu:** `TicketStatusChangeRequest` — walidacja w `TicketStateMachine` po stronie serwera; frontend tylko wysyła dozwolony status docelowy.

**Opis:**
- Dodać `changeStatus` do `TicketService` (owinięcie istniejącego Retrofit).
- Dla `TicketStatus.WSTRZYMANO`: FAB „Wznów” → dialog potwierdzenia → `PATCH` z celem np. `W_REALIZACJI` lub zgodnie z dokumentacją backendu / dozwolonymi przejściami (odczyt z `TicketDetailDto.allowedNextStatuses` jeśli jest w DTO, inaczej stała zgodna z enum backendu).
- **Nie** otwierać `AssignConservatorSheet` przy wznowieniu.
- Obsługa 409/422 z FRONT-023.

**Kryteria akceptacji:**
- [ ] Zarządca wznawia zgłoszenie WSTRZYMANO bez ponownego przypisania konserwatora.
- [ ] Przy niedozwolonym przejściu użytkownik widzi komunikat błędu z API.
- [ ] Brak zmian w backendzie.

---

### FRONT-005
**Tytuł:** Komentarze — poprawna obsługa POST/GET  
**Moduł:** Zgłoszenia  
**Rola:** WSZYSTKIE  
**Priorytet:** ŚREDNI  
**Zależności:** FRONT-023  
**Zależności backend:** `TicketCommentController` bez zmian.

**Opis:**
- `addComment`: sprawdzać `response.isSuccessful`, przy błędzie snackbar, nie odświeżać listy.
- Dodać `isSendingComment` w stanie UI (disable przycisku Send).
- `loadComments`: przy !successful → snackbar „Nie udało się załadować komentarzy”, opcjonalnie zachować poprzednią listę.

**Kryteria akceptacji:**
- [ ] Nieudany POST nie znika z UI bez komunikatu.
- [ ] Podczas wysyłki widać stan ładowania.
- [ ] Brak zmian w backendzie.

---

### FRONT-006
**Tytuł:** Lista zgłoszeń — paginacja i błędy doładowania  
**Moduł:** Zgłoszenia  
**Rola:** WSZYSTKIE  
**Priorytet:** ŚREDNI  
**Zależności:** brak  
**Zakres backendu:** `GET /api/tickets` **nie obsługuje** `page`/`size` — frontend dostosowuje się (usuwa mylący infinite scroll lub ładuje raz całość).

**Opis:**
- W `TicketsViewModel`: po pierwszym sukcesie ustawić `hasReachedEnd = true` (backend zwraca pełną listę).
- Usunąć lub ukryć trigger `loadNextPage` jeśli nie ma paginacji po stronie serwera.
- Przy błędzie pierwszego ładowania: stan `Error` (już jest); przy hipotetycznym retry — snackbar.
- **Nie** wysyłać PR do backendu o paginację.

**Kryteria akceptacji:**
- [ ] Użytkownik nie widzi wiecznego „ładowania kolejnej strony” przy pustej odpowiedzi.
- [ ] Lista pokazuje wszystkie zgłoszenia zwrócone jednym GET.
- [ ] Brak zmian w backendzie.

---

### FRONT-007
**Tytuł:** FAB „Nowe zgłoszenie” tylko dla MIESZKAŃCA  
**Moduł:** Zgłoszenia  
**Rola:** MIESZKANIEC / ZARZĄDCA  
**Priorytet:** WYSOKI  
**Zależności:** brak  
**Zakres backendu:** `POST /api/tickets` — rola MIESZKANIEC w `@PreAuthorize`; frontend respektuje kontrakt.

**Opis:**
- W `TicketsScreen`: `showFab = role == MIESZKANIEC` (z `TicketsListState.currentUserRole` / `UserRole`).
- ZARZĄDCA i KONSERWATOR: brak FAB; zarządca zarządza istniejącymi zgłoszeniami.

**Kryteria akceptacji:**
- [ ] Mieszkaniec widzi FAB i może utworzyć zgłoszenie.
- [ ] Zarządca nie widzi FAB i nie dostaje 403 przy przypadkowym kliknięciu.
- [ ] Brak zmian w backendzie.

---

### FRONT-022
**Tytuł:** Rozszerzone filtry listy zgłoszeń (query params istniejące w API)  
**Moduł:** Zgłoszenia  
**Rola:** ZARZĄDCA  
**Priorytet:** NISKI  
**Zależności:** FRONT-006  
**Zakres backendu:** `GET /api/tickets` już akceptuje `categoryId`, `buildingId`, `staircaseId`, `assignedTo`, `dateFrom`, `dateTo` — tylko podpięcie w UI.

**Opis:**
- Rozszerzyć `TicketFilterPanel` o pola zgodne z query w `TicketApiService`.
- Dane słownikowe: kategorie z `GET /api/categories`, budynki z `GET /api/buildings/tree`, konserwatorzy z `GET /api/users?role=KONSERWATOR`.
- Przekazać parametry w `TicketsViewModel.loadTickets`.

**Kryteria akceptacji:**
- [ ] Zarządca może filtrować po kategorii i budynku; lista odświeża się z API.
- [ ] Brak nowych parametrów po stronie backendu.

---

## Grupa C — Finanse (2 zadania)

### FRONT-009
**Tytuł:** Hub finansów mieszkańca — `GET /api/apartments/{id}/transactions`  
**Moduł:** Finanse  
**Rola:** MIESZKANIEC  
**Priorytet:** KRYTYCZNY  
**Zależności:** FRONT-008  
**Zakres backendu:** istniejący `FinancialTransactionController.getTransactions`.

**Opis:**
- W `FinancesViewModel.loadData`: dla `MIESZKANIEC` pobrać `apartmentId` z `ApartmentContext`, wywołać `FinancialLedgerService.getTransactions`.
- Wyświetlić `currentBalance` i skróconą listę transakcji na `FinancesScreen` (jak dla zarządcy).
- Stany: Loading, Error (brak lokalu / błąd sieci), Success, Empty (brak transakcji).

**Kryteria akceptacji:**
- [ ] Mieszkaniec na zakładce Finanse widzi prawdziwe saldo z API, nie stałe 0 zł.
- [ ] Przejście do kartoteki nadal działa.
- [ ] Brak zmian w backendzie.

---

### FRONT-010
**Tytuł:** Kartoteka finansowa — `ApartmentContext` zamiast pierwszego lokalu z drzewa  
**Moduł:** Finanse  
**Rola:** MIESZKANIEC / ZARZĄDCA  
**Priorytet:** KRYTYCZNY  
**Zależności:** FRONT-008, FRONT-009  
**Zakres backendu:** bez zmian.

**Opis:**
- `FinancialLedgerViewModel`: dla mieszkańca używać `UserApartmentService.apartmentId`; dla zarządcy — `navApartmentId` z nawigacji (już jest) lub wybór z drzewa.
- Usunąć logikę `tree.first().staircases.first().apartments.first()`.

**Kryteria akceptacji:**
- [ ] Kartoteka mieszkańca pokazuje transakcje ** jego** lokalu zgodnie z danymi backendu.
- [ ] Zarządca otwierający lokal z drzewa nadal widzi właściwy `apartmentId`.
- [ ] Brak zmian w backendzie.

---

## Grupa D — Profil (2 zadania)

### FRONT-011
**Tytuł:** Profil — dane z sesji (TokenStorage / JWT), bez fałszywego zapisu  
**Moduł:** Profil  
**Rola:** WSZYSTKIE  
**Priorytet:** WYSOKI  
**Zależności:** brak  
**Zakres backendu:** **świadomie bez** `GET /api/users/me` — backend nie ma tego endpointu; nie planować jego dodania w tym backlogu.

**Opis:**
- `ProfileViewModel`: usunąć hardkod `„Użytkownik”` / `„Zalogowany”`.
- Wyświetlać: `role` z `TokenStorage`, email jeśli zapisany przy logowaniu (rozszerzyć `TokenStorage.saveTokens` o opcjonalny email z `LoginRequest` — tylko DataStore, bez API).
- Pola imię/telefon: **read-only** z komunikatem „Edycja profilu będzie dostępna po rozszerzeniu systemu” lub ukryć przycisk zapisu.
- Usunąć `delay(300)` fake save i dialog sukcesu zapisu.

**Kryteria akceptacji:**
- [ ] Po zalogowaniu profil pokazuje rolę i email (jeśli dostępny z loginu).
- [ ] Użytkownik nie może „zapisać” fikcyjnych zmian z sukcesem.
- [ ] Brak zmian w backendzie.

---

### FRONT-026
**Tytuł:** Usunięcie elementów developerskich z profilu  
**Moduł:** Profil  
**Rola:** WSZYSTKIE  
**Priorytet:** NISKI  
**Zależności:** FRONT-011  
**Zakres backendu:** bez zmian.

**Opis:**
- Usunąć przycisk „test snackbar” / `sendTestNotification` z `ProfileContent`.
- Posprzątać martwe eventy w `ProfileViewModel`.

**Kryteria akceptacji:**
- [ ] Profil nie zawiera kontrolek testowych widocznych dla użytkownika końcowego.
- [ ] Brak zmian w backendzie.

---

## Grupa E — Powiadomienia (4 zadania)

### FRONT-012
**Tytuł:** Nawigacja profilu → prawdziwe ustawienia PUSH (`NotificationsScreen`)  
**Moduł:** Powiadomienia  
**Rola:** ZARZĄDCA  
**Priorytet:** KRYTYCZNY  
**Zależności:** brak  
**Zakres backendu:** `GET/PATCH /api/admin/notifications/settings` — już zaimplementowane w backendzie i `NotificationsViewModel`.

**Opis:**
- W `ProfileNavigation` / `ProfileScreen`: `onNavigateToNotificationSettings` → nawiguj do `NotificationRoutes.Settings` (istniejący graf), **nie** do `SettingsRoutes.Notifications`.
- Upewnić się, że `NotificationsScreen` ma przycisk wstecz (`popBackStack`).
- Opcjonalnie: usunąć duplikat trasy w `settingsGraph` dla powiadomień.

**Kryteria akceptacji:**
- [ ] Zarządca z profilu widzi listę zdarzeń z API i może przełączać `enabled` (PATCH).
- [ ] Zmiany są widoczne po ponownym wejściu (reload z GET).
- [ ] Brak zmian w backendzie.

---

### FRONT-013
**Tytuł:** Wycofanie hardkodowanego `NotificationSettingsScreen`  
**Moduł:** Powiadomienia  
**Rola:** ZARZĄDCA  
**Priorytet:** WYSOKI  
**Zależności:** FRONT-012  
**Zakres backendu:** bez zmian.

**Opis:**
- Usunąć lub oznaczyć `@Deprecated` `NotificationSettingsViewModel` + ekran z `defaultToggles()`.
- Usunąć composable `SettingsRoutes.Notifications` jeśli nieużywany.
- Brak „TODO: endpoint per user” wymagającego backendu — admin settings są globalne dla zarządcy (zgodnie z API).

**Kryteria akceptacji:**
- [ ] W aplikacji nie ma ekranu z fałszywymi przełącznikami powiadomień.
- [ ] Jedyna ścieżka ustawień PUSH dla zarządcy korzysta z API admin.
- [ ] Brak zmian w backendzie.

---

### FRONT-014
**Tytuł:** Integracja Firebase FCM — zastąpienie `NoOpFcmTokenProvider`  
**Moduł:** Powiadomienia / urządzenia  
**Rola:** WSZYSTKIE  
**Priorytet:** WYSOKI  
**Zależności:** brak  
**Zakres backendu:** `POST /api/devices/register`, `DELETE /api/devices/{token}` — bez zmian; wymaga `google-services.json` w module `app` (konfiguracja Firebase po stronie klienta).

**Opis:**
- Dodać zależności Firebase Messaging w `build.gradle`.
- Implementacja `FcmTokenProvider` pobierająca token FCM.
- Obsługa braku Google Play Services — graceful fallback (log + pominięcie rejestracji, bez crasha).
- **Nie** zmieniać `DeviceController` ani payloadu `DeviceRegistrationRequest`.

**Kryteria akceptacji:**
- [ ] Po logowaniu na urządzeniu z GMS wysyłany jest `POST /api/devices/register` z niepustym `fcmToken`.
- [ ] `NoOpFcmTokenProvider` nie jest domyślny w buildzie produkcyjnym.
- [ ] Brak zmian w backendzie.

---

### FRONT-015
**Tytuł:** Rejestracja i wyrejestrowanie urządzenia — weryfikacja E2E  
**Moduł:** Powiadomienia / urządzenia  
**Rola:** WSZYSTKIE  
**Priorytet:** ŚREDNI  
**Zależności:** FRONT-014  
**Zakres backendu:** bez zmian.

**Opis:**
- `AuthViewModel.tryRegisterFcmToken`: rejestrować tylko gdy token != null; logować błąd 404.
- `ResidentMainViewModel.logout`: przed `clearTokens` wywołać `DELETE /api/devices/{token}` jeśli token zapisany lokalnie (DataStore).
- Zapisać ostatni FCM token w DataStore przy rejestracji (do DELETE przy logout).

**Kryteria akceptacji:**
- [ ] Logout wysyła DELETE gdy rejestracja się powiodła.
- [ ] Błąd DELETE nie blokuje wylogowania (jak dziś — cicho lub log).
- [ ] Brak zmian w backendzie.

---

## Grupa F — Ogłoszenia (1 zadanie)

### FRONT-016
**Tytuł:** Edycja ogłoszenia — `PUT /api/announcements/{id}`  
**Moduł:** Ogłoszenia  
**Rola:** ZARZĄDCA  
**Priorytet:** WYSOKI  
**Zależności:** brak (lista + create istnieją)  
**Zakres backendu:** `AnnouncementController.updateAnnouncement` — multipart jak POST.

**Opis:**
- Dodać `EditAnnouncementScreen` (lub tryb edycji w istniejącym formularzu) z prefill z `AnnouncementDto`.
- Nawigacja: long press / ikona edycji na `AnnouncementsScreen` (tylko ZARZĄDCA).
- `AnnouncementService.updateAnnouncement` — owinięcie istniejącego PUT w Retrofit.
- Stany: Loading, Error, Success → popBack + odświeżenie listy.

**Kryteria akceptacji:**
- [ ] Zarządca edytuje tytuł/treść ogłoszenia; po zapisie lista pokazuje zmiany.
- [ ] Opcjonalna zamiana załącznika PDF zgodnie z kontraktem multipart backendu.
- [ ] Brak zmian w backendzie.

---

## Grupa G — Autentykacja (2 zadania)

### FRONT-017
**Tytuł:** Obsługa HTTP 429 (rate limit) na ekranach auth  
**Moduł:** Autentykacja  
**Rola:** WSZYSTKIE  
**Priorytet:** ŚREDNI  
**Zależności:** FRONT-023  
**Zakres backendu:** `RateLimitFilter` już zwraca 429 + `Retry-After` — frontend tylko odczytuje nagłówek.

**Opis:**
- W `AuthService` (login, forgot, reset): case 429 → wyjątek z komunikatem zawierającym `Retry-After` jeśli present.
- ViewModele: `AuthState.Error` / snackbar z tekstem „Zbyt wiele prób. Spróbuj za X min.”

**Kryteria akceptacji:**
- [ ] Przy symulacji 429 użytkownik widzi zrozumiały komunikat, nie „Błąd 500”.
- [ ] Brak zmian w backendzie.

---

### FRONT-018
**Tytuł:** Reset hasła — obsługa wygasłego tokenu  
**Moduł:** Autentykacja  
**Rola:** WSZYSTKIE  
**Priorytet:** ŚREDNI  
**Zależności:** FRONT-023  
**Zakres backendu:** bez zmian — jeśli backend zwraca 400/410 przez `IllegalArgumentException` / global handler, mapować kod z odpowiedzi; **nie** wymuszać nowego statusu w backendzie.

**Opis:**
- `ResetPasswordViewModel` / `AuthService.resetPassword`: mapować body `{message}` przy 400.
- Jeśli w odpowiedzi jest kod wskazujący wygaśnięcie — CTA „Poproś o nowy link” → nawigacja do `ForgotPassword`.
- `AcceptInvitationViewModel`: analogiczna obsługa (ten sam token flow).

**Kryteria akceptacji:**
- [ ] Wygaśnięty token resetu nie kończy się generycznym „Błąd serwera” bez dalszego kroku.
- [ ] Brak zmian w backendzie.

---

## Grupa H — Liczniki (1 zadanie)

### FRONT-019
**Tytuł:** Dezaktywacja licznika — `PATCH /api/meters/{id}/deactivate`  
**Moduł:** Liczniki  
**Rola:** ZARZĄDCA  
**Priorytet:** ŚREDNI  
**Zależności:** brak  
**Zakres backendu:** endpoint istnieje w `MeterController`.

**Opis:**
- W `MeterListScreen`: menu kontekstowe / ikona na aktywnym liczniku → potwierdzenie → `MeterService.deactivate(meterId)`.
- Po sukcesie odświeżyć listę; status „Nieaktywny” w UI.
- Brak nawigacji dla mieszkańca/konservatora w tym zadaniu (osobna decyzja produktowa).

**Kryteria akceptacji:**
- [ ] Zarządca dezaktywuje licznik; znika z listy aktywnych lub pokazuje status nieaktywny.
- [ ] Błąd 403 wyświetla snackbar.
- [ ] Brak zmian w backendzie.

---

## Grupa I — Nieruchomości (1 zadanie)

### FRONT-020
**Tytuł:** Wybór wspólnoty przy logo — `GET /api/properties/{id}`  
**Moduł:** Nieruchomości / ustawienia  
**Rola:** ZARZĄDCA  
**Priorytet:** NISKI  
**Zależności:** brak  
**Zakres backendu:** `PropertyController.getById` — już istnieje; podpiąć istniejący Retrofit `getPropertyById`.

**Opis:**
- `CommunityLogoViewModel`: jeśli `getProperties()` zwraca >1 rekord, dropdown wyboru; po wyborze opcjonalnie `getPropertyById` dla szczegółów/logo URL.
- Upload logo bez zmian (`PATCH /logo`).

**Kryteria akceptacji:**
- [ ] Przy wielu wspólnotach zarządca wybiera właściwą przed uploadem logo.
- [ ] Brak zmian w backendzie.

---

## Grupa J — Przeglądy (1 zadanie)

### FRONT-021
**Tytuł:** Podgląd przeglądów dla mieszkańca (read-only)  
**Moduł:** Przeglądy  
**Rola:** MIESZKANIEC  
**Priorytet:** ŚREDNI  
**Zależności:** brak  
**Zakres backendu:** `GET /api/inspections` — backend filtruje wg roli w serwisie; frontend tylko dodaje link.

**Opis:**
- W `ProfileContent` (sekcja mieszkańca, nie zarządcy): link „Przeglądy w budynku”.
- Reużyć `InspectionsListScreen` w trybie read-only: ukryć FAB/dodawanie/edycję/usuwanie gdy `role != ZARZADCA`.
- Ten sam `InspectionsListViewModel` + GET bez nowych endpointów.

**Kryteria akceptacji:**
- [ ] Mieszkaniec widzi listę przeglądów z API (jeśli backend zwraca dla jego budynku).
- [ ] Brak przycisków CRUD dla mieszkańca.
- [ ] Brak zmian w backendzie.

---

## Grupa K — Polish (2 zadania)

### FRONT-028
**Tytuł:** Spójne stany Empty na listach (zgłoszenia, finanse)  
**Moduł:** UX / wspólne komponenty  
**Rola:** WSZYSTKIE  
**Priorytet:** NISKI  
**Zależności:** FRONT-006, FRONT-009  
**Zakres backendu:** bez zmian.

**Opis:**
- Użyć istniejącego `EmptyState` na `TicketsScreen` gdy `Success` i `tickets.isEmpty()`.
- Hub finansów: empty gdy brak transakcji ale sukces API.

**Kryteria akceptacji:**
- [ ] Pusta lista zgłoszeń nie jest białym ekranem bez komunikatu.
- [ ] Brak zmian w backendzie.

---

### FRONT-030
**Tytuł:** Dokumentacja ograniczeń API w README frontendu  
**Moduł:** Dokumentacja  
**Rola:** —  
**Priorytet:** NISKI  
**Zależności:** brak  
**Zakres backendu:** bez zmian — opis tego, czego **nie ma** w API (profil, DELETE zdjęć, paginacja tickets).

**Opis:**
- Krótki `frontend/README.md` lub sekcja w istniejącym README: lista świadomych limitów backendu i jak UI je obsługuje.
- Pomaga uniknąć ponownego dodawania `DELETE /api/images` w Retrofit.

**Kryteria akceptacji:**
- [ ] README wymienia brak endpointu profilu i DELETE obrazów.
- [ ] Brak zmian w kodzie backendu (plik tylko w `frontend/`).

---

## Podsumowanie liczbowe

| Grupa | Zadań |
|-------|-------|
| A Fundament | 3 |
| B Zgłoszenia | 8 |
| C Finanse | 2 |
| D Profil | 2 |
| E Powiadomienia | 4 |
| F Ogłoszenia | 1 |
| G Auth | 2 |
| H Liczniki | 1 |
| I Nieruchomości | 1 |
| J Przeglądy | 1 |
| K Polish | 2 |
| **Razem** | **27** |

Wszystkie zadania: **tylko `frontend/`** (+ opcjonalnie `google-services.json`).

---

## FAZA 2 — Roadmapa implementacji

### Zasady kolejności
1. **Zależności techniczne** (fundament przed feature’ami).
2. **Kompletność E2E per rola:** KONSERWATOR → MIESZKANIEC → ZARZĄDCA (zgodnie z akceptacją).
3. **Zero zmian backendu** — jeśli API nie wspiera funkcji (np. usuwanie zdjęć), UI się dostosowuje lub ukrywa akcję.

### Etap 1 — Fundament (≈ 1 tydzień)
| Kolejność | ID | Tytuł |
|-----------|-----|-------|
| 1 | FRONT-023 | Mapper błędów API |
| 2 | FRONT-008 | ApartmentContext |
| 3 | FRONT-024 | TicketImageThumbnail |
| 4 | FRONT-007 | FAB tylko MIESZKANIEC |
| 5 | FRONT-011 | Profil bez hardkodu |
| 6 | FRONT-012 | Nav → NotificationsScreen (API) |

**Wyjście etapu:** stabilne błędy sieci, lokal mieszkańca, podstawa pod media i finanse, zarządca widzi prawdziwe ustawienia PUSH.

### Etap 2 — Przepływy krytyczne E2E (≈ 2 tygodnie)

**Ścieżka KONSERWATOR (najpierw):**
| Kolejność | ID |
|-----------|-----|
| 7 | FRONT-001 |
| 8 | FRONT-002 |
| 9 | FRONT-003 |
| 10 | FRONT-005 |

**Ścieżka MIESZKANIEC (równolegle po FRONT-008):**
| Kolejność | ID |
|-----------|-----|
| 7 | FRONT-009 |
| 8 | FRONT-010 |
| 9 | FRONT-021 (opcjonalnie w tym sprincie) |

**Ścieżka ZARZĄDCA:**
| Kolejność | ID |
|-----------|-----|
| 7 | FRONT-004 |
| 8 | FRONT-016 |
| 9 | FRONT-013 |
| 10 | FRONT-014 → FRONT-015 |

**Wyjście etapu:** KONSERWATOR z foto; MIESZKANIEC z finansami; ZARZĄDCA z wznowieniem zgłoszeń, edycją ogłoszeń, FCM.

### Etap 3 — Uzupełnienia (≈ 1 tydzień)
| ID | Tytuł |
|----|-------|
| FRONT-006 | Paginacja listy zgłoszeń |
| FRONT-017 | Auth 429 |
| FRONT-018 | Token wygasły |
| FRONT-019 | Dezaktywacja licznika |
| FRONT-026 | Cleanup profilu |

### Etap 4 — Polish (≈ 3–5 dni)
| ID | Tytuł |
|----|-------|
| FRONT-022 | Filtry zgłoszeń ZARZĄDCA |
| FRONT-020 | Wybór wspólnoty logo |
| FRONT-028 | Empty states |
| FRONT-030 | README limitów API |

### Mapa zależności (krytyczna ścieżka)

```
FRONT-023 ─┬─► FRONT-005
FRONT-024 ─► FRONT-001 ─► FRONT-002
           └► FRONT-003
FRONT-008 ─► FRONT-009 ─► FRONT-010
FRONT-012 ─► FRONT-013
FRONT-014 ─► FRONT-015
```

---

## FAZA 3 — Ryzyka techniczne (perspektywa frontendu)

| Ryzyko | Opis | Refaktor PRZED nowymi feature? | Uwagi „bez backendu” |
|--------|------|-------------------------------|----------------------|
| **R1: Niespójna obsługa błędów** | Część VM używa `runCatching`, inne ignoruje `!isSuccessful` | **TAK** — FRONT-023 przed masowym podpinaniem API | Nie wymaga zmian backendu |
| **R2: Brak `GET /users/me`** | Profil nie może pokazać imienia/telefonu z API | **NIE** — FRONT-011 obejście sesją; ewentualny endpoint to osobny projekt backendowy **poza scope** | |
| **R3: ApartmentId mieszkańca** | Heurystyka „pierwszy lokal” jest błędna przy wielu lokalach w drzewie | **TAK** — FRONT-008 przed FRONT-009/010 | Polegać na filtrze backendu w `getBuildingTree()` |
| **R4: DELETE zdjęć w Retrofit** | Martwy kontrakt w `TicketImageApiService` | **TAK** — FRONT-003 wcześnie; nie dodawać endpointu w backendzie | |
| **R5: Dwa ekrany powiadomień** | Myląca nawigacja i duplikat kodu | **TAK** — FRONT-012/013 przed FCM | Admin API już jest |
| **R6: NoOp FCM** | Push nigdy nie zadziała bez Firebase w `app` | **RÓWNOLEGLE** z FRONT-014 — wymaga konfiguracji projektu Firebase (klient), nie zmiany Java API | `DeviceController` bez zmian |
| **R7: Coil + autoryzowany URL obrazów** | `GET /api/images/{id}` wymaga JWT — zwykły URL w Coil może nie mieć nagłówka | **TAK** — FRONT-024: custom `ImageLoader` / OkHttp z interceptorem | |
| **R8: Paginacja tickets** | Frontend i backend rozjechane | **TAK** — FRONT-006: dostosować UI do pełnej listy | Nie wysyłać PR o paginację w tym zakresie |
| **R9: Architektura OK** | Hilt + ViewModel + Retrofit już spójne — **brak** refaktoru Activity/Fragment | **NIE** | |
| **R10: JWT w interceptorze** | `AuthInterceptor` + `TokenAuthenticator` działają | **NIE** | |
| **R11: Hardkod URL** | `BuildConfig.BACKEND_URL` — OK | **NIE** | |
| **R12: `TicketMediaServices` poza Hilt** | Może wymagać modułu DI przed FRONT-002 | **RÓWNOLEGLE** — dodać provider w `NetworkModule` | Bez zmian backendu |
| **R13: TokenExpiredException / 410** | Backend może nie mapować 410 na accept-invitation | **NIE** — FRONT-018 obsługuje kody faktycznie zwracane; nie zmieniać `AuthController` | |

### Rekomendacja przed startem kodowania
1. Zmergować **FRONT-023, FRONT-008, FRONT-024, FRONT-003, FRONT-012** jako pierwszy PR.
2. Potem **ścieżka KONSERWATOR** (001, 002) — największa luka biznesowa.
3. Równoległy PR **finanse** (009, 010) dla mieszkańca.
4. **Żaden PR** w tym planie nie dotyka `backend/src/main/java`.

---

**STATUS: Backlog i roadmapa gotowe (2026-06-05).**
