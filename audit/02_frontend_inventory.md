# Inwentaryzacja frontendu BlokUR — Frontend Inventory
## Faza 1: Analiza wszystkich modułów
---

## Moduł 1: Uwierzytelnienie (Auth)
**Rola:** wspólny (wszyscy użytkownicy)

### Ekrany

#### 1.1 `LoginScreen`
- **Rola:** wspólny
- **Akcje użytkownika:**
  - Wprowadzenie e-mail i hasła
  - Kliknięcie „Zaloguj" → `viewModel.login()`
  - Przełączenie widoczności hasła
  - Przejście do ekranu „Zapomniałem hasła" → `onForgotPassword()`
- **Dane wyświetlane:** formularz email/hasło, komunikat błędu, komunikat blokady konta
- **Stany:**
  - ✅ Loading — formularz zablokowany podczas żądania
  - ✅ Error — wyświetlany komunikat błędu (złe dane, błąd sieci)
  - ✅ AccountLocked — osobny stan HTTP 423, wyświetla info o 15-minutowej blokadzie
  - ✅ Success — trigger nawigacji do głównego ekranu
  - ❌ Empty state — nie dotyczy (formularz zawsze widoczny)
- **Dodatkowe:** po sukcesie VM próbuje zarejestrować FCM token (fire-and-forget, nie blokuje nawigacji)

#### 1.2 `ForgotPasswordScreen`
- **Rola:** wspólny
- **Akcje użytkownika:**
  - Wprowadzenie adresu e-mail
  - Kliknięcie „Wyślij link" → `viewModel.submit()`
  - Powrót → `viewModel.onNavigateBack()`
- **Dane wyświetlane:** pole email, stan sukcesu z komunikatem serwera
- **Stany:**
  - ✅ Loading — podczas wysyłania
  - ✅ Success(message) — wyświetla komunikat z backendu
  - ✅ Error(message) — walidacja lokalna (pusty e-mail, niepoprawny format) + błąd serwera
  - ❌ Empty state — nie dotyczy

#### 1.3 `ResetPasswordScreen`
- **Rola:** wspólny (dostępny przez link mailowy z tokenem)
- **Akcje użytkownika:**
  - Wprowadzenie nowego hasła i potwierdzenia
  - Kliknięcie „Resetuj hasło" → `viewModel.submit()`
  - Po sukcesie: nawigacja do LoginScreen (pop entire back stack)
- **Dane wyświetlane:** dwa pola hasła, walidacja lokalnie (min. 8 znaków, zgodność)
- **Stany:**
  - ✅ Loading
  - ✅ Success(message)
  - ✅ Error(message) — walidacja lokalna + błąd serwera (wygasły/nieprawidłowy token)

### Wywołania API

| Metoda | Endpoint | Klasa/Metoda | Sukces | Błąd | JWT |
|--------|----------|-------------|--------|------|-----|
| POST | `/api/auth/login` | `AuthViewModel.login()` via `AuthService.login()` | ✅ zapisuje tokeny, emituje rolę | ✅ HTTP 401→InvalidCredentials, 423→AccountLocked | ❌ (klient "bare") |
| POST | `/api/auth/refresh` | `TokenAuthenticator.authenticate()` (auto) | ✅ zapisuje nowe tokeny, ponawia request | ✅ null → logout flow | ❌ (klient "bare") |
| POST | `/api/auth/forgot-password` | `ForgotPasswordViewModel.submit()` via `AuthService.forgotPassword()` | ✅ wyświetla komunikat | ✅ parsuje JSON błędu | ❌ (klient "bare") |
| POST | `/api/auth/reset-password` | `ResetPasswordViewModel.submit()` via `AuthService.resetPassword()` | ✅ wyświetla komunikat | ✅ parsuje JSON błędu | ❌ (klient "bare") |
| POST | `/api/devices/register` | `AuthViewModel.tryRegisterFcmToken()` via `DeviceService.registerDevice()` | ✅ loguje sukces | ✅ loguje błąd (nie blokuje) | ✅ (klient "main") |

### Nawigacja
- `AuthRoutes.Login` → start aplikacji (jeśli nie ma tokenu)
- `Login` → `ForgotPassword` (przez `onForgotPassword`)
- `ForgotPassword` → powrót do `Login` (popBackStack)
- `Login` → `ResetPassword(token)` (przez deep link / link mailowy z query param `?token=...`)
- `ResetPassword` → `Login` (popUpTo 0, inclusive = true — czyści cały backstack)
- `Login` → główna nawigacja (`onLoginSuccess(role)`) — role routing na poziomie wyżej

### Infrastruktura Auth
- **`AuthInterceptor`:** OkHttp Interceptor dodający `Authorization: Bearer <token>` do WSZYSTKICH żądań klienta "main". Token pobierany synchronicznie z DataStore przez `runBlocking`.
- **`TokenAuthenticator`:** OkHttp Authenticator — auto-refresh tokenu po HTTP 401. Pomija żądania do `/auth/**` i żądania które już były odświeżane (header `X-Token-Refreshed`).
- **`TokenStorage`:** DataStore (Preferences) — przechowuje: `access_token`, `refresh_token`, `user_role`.
- **`NetworkModule`:** Hilt module — dwa klienty OkHttp: `"bare"` (tylko logging) dla auth i `"main"` (+ AuthInterceptor + TokenAuthenticator) dla reszty. `BASE_URL` pochodzi z `BuildConfig.BACKEND_URL`.

---

## Moduł 2: Nawigacja główna (Main Shell)
**Rola:** wspólny — shell renderuje nawigację per rola

### Ekrany

#### 2.1 `ResidentMainScreen`
- **Rola:** wspólny
- **Akcje użytkownika:**
  - Zmiana zakładki przez BottomNavBar → `viewModel.onOptionClicked(option)`
  - Wylogowanie przez ikonę w TopBar → `viewModel.logout()`
- **Dane wyświetlane:**
  - Tytuł w TopBar (dynamiczny per aktywna zakładka)
  - BottomNavBar z zakładkami filtrowanymi per rola
  - `innerContent` slot (zagnieżdżony NavHost z aktywną zakładką)
- **Stany:** `ResidentMainState` — Loading, Error, ViewingAnnouncements, ViewingFinances, ViewingProfile, ViewingTickets, ViewingProperties, ViewingUsers, ViewingCategories, ViewingResolutions, ViewingInspections, ViewingNotifications, ViewingWelcome
- **Uwagi:** ViewModel przy starcie pobiera rolę z TokenStorage i ustawia odpowiednią listę zakładek

### Nawigacja per rola

| Rola | Zakładki w BottomNav |
|------|---------------------|
| MIESZKANIEC | Zgłoszenia, Finanse, Uchwały, Ogłoszenia, Profil |
| ZARZADCA | Zgłoszenia, Lokale, Uchwały, Użytkownicy, Profil |
| KONSERWATOR | Zgłoszenia, Profil |

**Dodatkowe opcje ZARZADCA** (niedostępne w BottomNav, wejście przez Profil):
- Kategorie, Przeglądy, Ustawienia powiadomień, Logo wspólnoty, Dystrybucja dokumentów

### Wywołania API
- Brak bezpośrednich wywołań API w tym module (stan roli pobierany z lokalnego DataStore przez `AuthService.getCurrentUserRole()`)

### FCM / Device
- **`FcmTokenProvider`:** abstrakcja nad `FirebaseMessaging.getInstance().getToken()` — po zalogowaniu token rejestrowany w backendzie
- **`FcmModule`:** Hilt module dostarczający `FcmTokenProvider` (z fallback `NoOpFcmTokenProvider` gdy Firebase niedostępne)

---

## Moduł 3: Zgłoszenia (Tickets)
**Rola:** MIESZKANIEC (tworzenie) / ZARZADCA (zarządzanie) / KONSERWATOR (realizacja)

### Ekrany

#### 3.1 `TicketsScreen`
- **Rola:** MIESZKANIEC / ZARZADCA / KONSERWATOR
- **Akcje użytkownika:**
  - Kliknięcie zgłoszenia → nawigacja do `TicketDetailsScreen`
  - Przycisk „Nowe zgłoszenie" → nawigacja do `CreateTicketScreen` (tylko MIESZKANIEC)
  - Filtrowanie po statusie i fraza wyszukiwania (lokalne — bez ponownego API call)
  - Odświeżenie listy
  - Przejście do Kategorii (zarządca) → `onNavigateToCategories`
  - Przejście do Użytkowników (zarządca) → `onNavigateToUsers`
- **Dane wyświetlane:** lista `TicketSummaryDto` — numer zgłoszenia, tytuł, status, kategoria, autor, przypisany, lokalizacja, czas, flaga SLA
- **Stany:**
  - ✅ Loading (`TicketsListState.Loading`)
  - ✅ Error (`TicketsListState.Error`)
  - ✅ Success — lista + możliwe pustą listę po filtrowaniu
  - ⚠️ Empty state — [do weryfikacji w TicketListContent.kt]
- **Filtrowanie:** klient-side po searchQuery (tytuł, numer, kategoria) i selectedStatus — `TicketFilterState`

#### 3.2 `TicketDetailsScreen`
- **Rola:** MIESZKANIEC (podgląd) / ZARZADCA (zarządzanie) / KONSERWATOR (realizacja)
- **Akcje dostępne per rola:**
  - **ZARZADCA:** Przypisanie konserwatora (`AssignConservatorSheet`), odrzucenie zgłoszenia (`ManagerRejectSheet`), zamknięcie zgłoszenia
  - **KONSERWATOR:** Akcje z `ConservatorActionSheet` — START (NOWE/ZAPLANOWANO → W_REALIZACJI), PAUSE (→ WSTRZYMANO), FINISH (→ ZAKONCZONE_DO_WERYFIKACJI), CLOSE_VERIFICATION (zamknięcie)
  - **Wszyscy:** Dodanie komentarza (PUBLICZNY lub WEWNETRZNY), przeglądanie komentarzy i zdjęć
  - **Zarządca:** Pobranie protokołu odbioru prac (PDF)
- **Dane wyświetlane:** `TicketDetailDto` + lista `TicketCommentDto` + lista `TicketImageDto`
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success z osobnymi flagami `isLoadingComments`
  - ✅ Komentarze i zdjęcia ładowane równolegle po załadowaniu ticketu

#### 3.3 `CreateTicketScreen`
- **Rola:** MIESZKANIEC
- **Akcje użytkownika:**
  - Wprowadzenie tytułu i opisu
  - Wybór kategorii z listy (dropdown)
  - Submit → `viewModel.submit()`
  - Powrót
- **Dane wyświetlane:** lista kategorii pobranych z API
- **Stany:**
  - ✅ Loading (ładowanie kategorii osobna flaga `_categoriesLoading`)
  - ✅ Submitting
  - ✅ Success(ticketNumber) — wyświetla komunikat z numerem zgłoszenia
  - ✅ Error — walidacja lokalna (puste pola) + błąd API (400, 403, 422)
  - ⚠️ Empty state kategorii — [brak walidacji, zachowuje pustą listę bez błędu]

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `api/tickets` | `TicketsViewModel.loadTickets()` | ✅ lista TicketSummaryDto | ✅ Error state | ✅ |
| GET | `api/tickets/{id}` | `TicketDetailsViewModel.loadTicket()` | ✅ TicketDetailDto | ✅ Error state | ✅ |
| GET | `api/categories` | `CreateTicketViewModel.loadCategories()` | ✅ lista kategorii | ⚠️ błąd cichy, pusta lista | ✅ |
| POST | `api/tickets` | `CreateTicketViewModel.submit()` | ✅ TicketDetailDto, emituje ticketNumber | ✅ 400/403/422 mapowane | ✅ |
| GET | `api/users?role=KONSERWATOR` | `TicketDetailsViewModel.loadTicket()` | ✅ lista ConservatorDto | ✅ | ✅ |
| PATCH | `api/tickets/{id}/assign` | `TicketDetailsViewModel.onAssignConservator()` | ✅ reload ticketu | ✅ ShowError event | ✅ |
| PATCH | `api/tickets/{id}/close` | `TicketDetailsViewModel.onCloseTicket()` | ✅ reload | ✅ ShowError event | ✅ |
| PATCH | `api/tickets/{id}/reject` | `TicketDetailsViewModel.onRejectTicket()` | ✅ reload | ✅ ShowError event | ✅ |
| PATCH | `api/tickets/{id}/start` | `TicketDetailsViewModel.onConservatorAction(START)` | ✅ reload | ✅ ShowError event | ✅ |
| PATCH | `api/tickets/{id}/suspend` | `TicketDetailsViewModel.onConservatorAction(PAUSE)` | ✅ reload | ✅ ShowError event | ✅ |
| POST | `api/tickets/{id}/completion` | `TicketDetailsViewModel.onConservatorAction(FINISH)` | ✅ reload | ✅ ShowError event | ✅ |
| GET | `/api/tickets/{id}/comments` | `TicketDetailsViewModel.loadComments()` | ✅ lista komentarzy | ⚠️ błąd cichy, isLoadingComments=false | ✅ |
| POST | `/api/tickets/{id}/comments` | `TicketDetailsViewModel.addComment()` | ✅ reload komentarzy | ✅ ShowError event | ✅ |
| GET | `/api/tickets/{id}/images` | `TicketDetailsViewModel.loadImages()` | ✅ lista zdjęć | ⚠️ błąd cichy | ✅ |
| POST | `/api/tickets/{id}/images` | `TicketMediaServices` (multipart) | ✅ TicketImageDto | ✅ | ✅ |
| GET | `/api/images/{id}` | `TicketImageApiService.serveImage()` | ✅ ResponseBody | ✅ | ✅ |
| POST | `/api/pdf/work-acceptance-protocol` | `TicketDetailsViewModel.downloadWorkAcceptanceProtocol()` | ✅ zapis PDF do cache, Intent VIEW | ✅ ShowError event | ✅ |

**Uwaga — filtry API:**  
`GET api/tickets` obsługuje query params: `status`, `categoryId`, `buildingId`, `staircaseId`, `assignedTo`, `dateFrom`, `dateTo`, `search` — **jednak filtrowanie w UI jest klient-side** (TicketFilterState filtruje pobraną listę lokalnie, nie przekazuje parametrów do API).

### Nawigacja
```
TicketRoutes.List
  → TicketRoutes.Details(ticketId)   [kliknięcie pozycji]
  → TicketRoutes.Create              [przycisk "Nowe zgłoszenie"]
  → CategoryRoutes.List              [zarządca — przejście do kategorii]
  → UserRoutes.List                  [zarządca — przejście do użytkowników]

TicketRoutes.Details(ticketId)
  → popBackStack                     [przycisk wstecz]

TicketRoutes.Create
  → popBackStack                     [anuluj lub po sukcesie]
```

### Komponenty pomocnicze
- **`AssignConservatorSheet`** — BottomSheet z listą konserwatorów + wybór daty
- **`ConservatorActionSheet`** — BottomSheet z akcjami konserwatora (START/PAUSE/FINISH)
- **`ManagerRejectSheet`** — BottomSheet z polem powodu odrzucenia
- **`TicketCommentsSection`** — sekcja komentarzy z formularzem dodawania
- **`TicketFilterPanel`** — panel filtrów (search + status)
- **`TicketImagesSection`** — galeria zdjęć BEFORE/AFTER
- **`TicketListItem`** — element listy z numerem, statusem, SLA badge

### Uwagi / Problemy
- [BRAK ERROR HANDLING] — błędy ładowania komentarzy i zdjęć są ciche (logowane, ale UI nie pokazuje komunikatu)
- [BRAK ERROR HANDLING] — błąd ładowania kategorii w CreateTicket jest cichy (pusta lista bez komunikatu)
- Filtrowanie zgłoszeń jest klient-side mimo że API obsługuje server-side filtering — potencjalna nieefektywność przy dużej liczbie zgłoszeń
- Protokół PDF (`/api/pdf/work-acceptance-protocol`) wywoływany z `TicketDetailsViewModel` — korzysta z osobnego `PdfApiService` wstrzykiwanego do VM

---
---

## Moduł 4: Finanse (Finances)
**Rola:** MIESZKANIEC (podgląd własnych) / ZARZADCA (pełny dostęp + import CSV + salda)

### Ekrany

#### 4.1 `FinancesScreen` (route: `FinancesRoutes.Main`)
- **Rola:** MIESZKANIEC / ZARZADCA
- **Akcje użytkownika:**
  - Nawigacja do TransactionsScreen → `onNavigateToTransactions()`
  - Nawigacja do DocumentsScreen → `onNavigateToDocuments()`
  - Nawigacja do FinancialLedgerScreen → `onNavigateToLedger()`
  - Nawigacja do ApartmentBalancesScreen → `onNavigateToBalances()` (tylko ZARZADCA)
  - Nawigacja do CsvImportScreen → `onNavigateToCsvImport()` (tylko ZARZADCA)
  - Pobieranie dokumentu PDF → `viewModel.downloadDocument(document)`
- **Dane wyświetlane:** saldo (`ApartmentBalanceDto`), lista transakcji (`List<TransactionDto>`), lista dokumentów (`List<UserDocumentDto>`)
- **Stany:**
  - ✅ Loading
  - ✅ Error (message)
  - ✅ Data (balance + transactions + documents)
  - ❌ Empty state — lista dokumentów może być pusta (brak obsługi empty state)
- **⚠️ [HARDKODOWANE]** `FinancesService.getBalance()` i `FinancesService.getTransactions()` zwracają **mockowe dane hardkodowane** — brak wywołania API. Dokumenty pobierane przez `UserDocumentService` (prawdziwe API).

#### 4.2 `TransactionsScreen` (route: `FinancesRoutes.Transactions`)
- **Rola:** MIESZKANIEC / ZARZADCA
- **Akcje użytkownika:** podgląd listy transakcji, powrót
- **Dane wyświetlane:** lista `TransactionDto` (mock)
- **Stany:** dzielony ViewModel z `FinancesScreen` (parentEntry)
- **⚠️ [HARDKODOWANE]** — dane z mockowego `FinancesService`

#### 4.3 `DocumentsScreen` (route: `FinancesRoutes.Documents`)
- **Rola:** MIESZKANIEC / ZARZADCA
- **Akcje użytkownika:** podgląd listy dokumentów, pobieranie PDF, powrót
- **Dane wyświetlane:** lista `UserDocumentDto`
- **Stany:** dzielony ViewModel z `FinancesScreen`
- **Uwaga:** dokumenty pobierane z prawdziwego API przez `UserDocumentService`

#### 4.4 `FinancialLedgerScreen` (route: `FinancesRoutes.Ledger(apartmentId?)`)
- **Rola:** MIESZKANIEC (własny lokal) / ZARZADCA (konkretny lokal po apartmentId)
- **Akcje użytkownika:**
  - Podgląd historii transakcji i salda
  - Dodanie transakcji (dialog `AddTransactionDialog`) — tylko ZARZADCA (`isManager = true`)
  - Powrót
- **Dane wyświetlane:** `ApartmentTransactionsDto` — `currentBalance: BigDecimal`, lista `FinancialTransactionDto`
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success (apartmentId, apartmentLabel, currentBalance, transactions, isManager)
  - ✅ Dialog `showAddDialog` — osobna flaga stanu
- **Logika apartmentId:** jeśli ZARZADCA — z `SavedStateHandle["apartmentId"]`; jeśli MIESZKANIEC — pobierany z drzewa nieruchomości przez `PropertyService`

#### 4.5 `ApartmentBalancesScreen` (route: `FinancesRoutes.Balances`)
- **Rola:** ZARZADCA
- **Akcje użytkownika:**
  - Filtrowanie sald po propertyId, minimalnym zadłużeniu, dniach zaległości
  - Zmiana sortowania (debt_asc / debt_desc)
  - Pobieranie raportu PDF przez URL (zbudowany przez `buildPdfUrl()`)
  - Odświeżenie listy
  - Powrót
- **Dane wyświetlane:** lista `ApartmentBalanceItemDto` — adres, saldo (BigDecimal), dni zaległości, data ostatniej płatności
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success(items)
  - ⚠️ Empty state — [do weryfikacji w ApartmentBalancesScreen.kt]

#### 4.6 `CsvImportScreen` (route: `FinancesRoutes.CsvImport`)
- **Rola:** ZARZADCA
- **Akcje użytkownika:**
  - Wybór pliku CSV z urządzenia (systemowy file picker)
  - Upload → `viewModel.upload()`
  - Reset → `viewModel.reset()`
  - Powrót
- **Dane wyświetlane:** nazwa wybranego pliku, wynik importu (`CsvImportResultDto` — importedCount, errorCount, lista błędów per wiersz)
- **Stany:**
  - ✅ Idle — brak pliku
  - ✅ Uploading — spinner
  - ✅ Result (importedCount, errorCount, errors)
  - ✅ Error
- **Implementacja:** czyta bajty pliku przez ContentResolver, wysyła multipart/form-data (text/csv)

#### 4.7 `AddTransactionDialog` (dialog w `FinancialLedgerScreen`)
- **Rola:** ZARZADCA
- **Akcje:** wybór typu (WPLATA/NALICZENIE/KOREKTA), kwota, opis, data, submit
- **Walidacja lokalna:** `AddTransactionFormState.isValid` — kwota != 0, opis niepusty, data niepusta

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `api/apartments/{id}/transactions` | `FinancialLedgerViewModel.load()` via `FinancialLedgerService` | ✅ ApartmentTransactionsDto | ✅ Error state | ✅ |
| POST | `api/apartments/{id}/transactions` | Dialog submit via `FinancialLedgerService.createTransaction()` | ✅ FinancialTransactionDto, odświeża listę | ✅ 400/403/404 mapowane | ✅ |
| GET | `api/admin/apartments/balances` | `ApartmentBalancesViewModel.load()` via `ApartmentBalanceService` | ✅ List<ApartmentBalanceItemDto> | ✅ Error state | ✅ |
| POST | `api/finance/import` | `CsvImportViewModel.upload()` | ✅ CsvImportResultDto | ✅ Error state + Snackbar | ✅ |
| GET | `/api/documents` | `FinancesViewModel.loadData()` via `UserDocumentService` | ✅ List<UserDocumentDto> | ✅ Error state | ✅ |
| GET | `/api/documents/{id}/download` | `FinancesViewModel.downloadDocument()` via `UserDocumentService` | ✅ PDF zapisany w cache, Intent VIEW | ✅ ShowSnackbar event | ✅ |
| GET | `/api/pdf/balances?...` | URL zbudowany przez `buildPdfUrl()` — otwierany w przeglądarce/przeglądarce PDF | — | — | ✅ |

**⚠️ [HARDKODOWANE]** — `FinancesService.getBalance()`, `getTransactions()`, `getDocuments()` — wszystkie mocki w `FinancesService`. Używane przez `FinancesScreen` (overview tab). Widok `FinancialLedgerScreen` używa prawdziwego API.

### Nawigacja
```
FinancesRoutes.Main
  → FinancesRoutes.Transactions    [przycisk "Transakcje"]
  → FinancesRoutes.Documents       [przycisk "Dokumenty"]
  → FinancesRoutes.Ledger()        [mieszkaniec — własny lokal]
  → FinancesRoutes.Balances        [zarządca — zestawienie sald]
  → FinancesRoutes.CsvImport       [zarządca — import CSV]

FinancesRoutes.Transactions  → popBackStack
FinancesRoutes.Documents     → popBackStack
FinancesRoutes.Ledger        → popBackStack
FinancesRoutes.Balances      → popBackStack
FinancesRoutes.CsvImport     → popBackStack
```

---

## Moduł 5: Ogłoszenia (Announcements)
**Rola:** MIESZKANIEC (odczyt) / ZARZADCA (tworzenie, edycja, usuwanie — przez AnnouncementService, ale brak ekranów tworzenia w UI)

### Ekrany

#### 5.1 `AnnouncementsScreen` (route: `AnnouncementsRoutes.Main`)
- **Rola:** MIESZKANIEC / ZARZADCA (odczyt)
- **Akcje użytkownika:**
  - Przeglądanie listy ogłoszeń
  - Kliknięcie „Pobierz załącznik" → `viewModel.downloadAttachment(announcementId, title)`
  - Odświeżenie listy → `viewModel.loadAnnouncements()`
- **Dane wyświetlane:** lista `AnnouncementDto` — id, typ, tytuł, treść, autor, targetType, URL załącznika, plannedDate, createdAt
- **Stany:**
  - ✅ Loading
  - ✅ Empty (osobny stan `AnnouncementsState.Empty` gdy lista pusta)
  - ✅ Success(announcements)
  - ✅ Error — Snackbar z komunikatem
- **Pobieranie załącznika:** zapisywany w `cache/announcements/`, otwierany przez FileProvider + Intent ACTION_VIEW

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `/api/announcements` | `AnnouncementsViewModel.loadAnnouncements()` | ✅ List<AnnouncementDto> | ✅ Error state + Snackbar | ✅ |
| GET | `/api/announcements/{id}/attachment` | `AnnouncementsViewModel.downloadAttachment()` | ✅ PDF → cache → Intent | ✅ ShowError event | ✅ |

**Uwaga — CRUD ogłoszeń:** `AnnouncementService` udostępnia `createAnnouncement()`, `updateAnnouncement()`, `deleteAnnouncement()` ale **brak ekranów UI** dla tych operacji w warstwie widoków (brak CreateAnnouncementScreen, EditAnnouncementScreen). API jest zdefiniowane po stronie frontendu (serwis + API interface), ale niedostępne z poziomu aplikacji.

### Nawigacja
```
AnnouncementsRoutes.Main  ← punkt wejścia z BottomNav
  (brak podekranów)
```

---

## Moduł 6: Uchwały (Resolutions)
**Rola:** MIESZKANIEC (przeglądanie + głosowanie) / ZARZADCA (tworzenie + przeglądanie wyników + raport PDF)

### Ekrany

#### 6.1 `ResolutionsListScreen` (route: `ResolutionRoutes.List`)
- **Rola:** MIESZKANIEC / ZARZADCA
- **Akcje użytkownika:**
  - Przeglądanie listy uchwał
  - Kliknięcie uchwały → nawigacja do `ResolutionDetailScreen`
  - (ZARZADCA) Przycisk „Nowa uchwała" → otwiera `CreateResolutionDialog`
- **Dane wyświetlane:** lista `ResolutionDto` — id, tytuł, opis, endDate, buildingId, authorName, flaga isActive
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success (lista uchwał)
  - ✅ Empty state — [w ResolutionsListViewModel]
  - ✅ Dialog tworzenia — osobna flaga `showCreateDialog`
  - ✅ Rola użytkownika dostępna w stanie (`isManager`)

#### 6.2 `ResolutionDetailScreen` (route: `ResolutionRoutes.Detail(resolutionId)`)
- **Rola:** MIESZKANIEC (głosowanie) / ZARZADCA (podgląd wyników + raport)
- **Akcje użytkownika:**
  - Wybór opcji głosowania → `viewModel.selectOption(optionId)`
  - Oddanie głosu → `viewModel.castVote()`
  - (ZARZADCA) Pobranie raportu PDF → `viewModel.downloadReport()`
  - Powrót
- **Dane wyświetlane:** `ResolutionDetailDto` — tytuł, opis, endDate, opcje (`List<ResolutionOptionDto>`), wyniki (`List<ResolutionOptionResultDto>?`), totalVotes
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success — selectedOptionId, isVoting, hasVoted, isManager, isDownloadingReport
  - ✅ Po głosowaniu: `hasVoted = true`, reload wyników
  - ✅ Blokada wielokrotnego głosowania (sprawdzenie `hasVoted || isVoting`)

#### 6.3 `CreateResolutionDialog` (dialog w `ResolutionsListScreen`)
- **Rola:** ZARZADCA
- **Akcje:** wprowadzenie tytułu, opisu, daty zakończenia, opcji (dynamiczne dodawanie/usuwanie), wybór budynku, submit
- **Walidacja lokalna:** `ResolutionFormState.isValid` (w ViewModel)
- **Stany:** `isSubmitting`

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `api/resolutions` | `ResolutionsListViewModel.load()` | ✅ List<ResolutionDto> | ✅ Error state | ✅ |
| POST | `api/resolutions` | `ResolutionsListViewModel.submitCreate()` | ✅ Response<Void> 201, reload listy | ✅ Snackbar event | ✅ |
| GET | `api/resolutions/{id}` | `ResolutionDetailViewModel.load()` / `loadAfterVote()` | ✅ ResolutionDetailDto | ✅ Error state | ✅ |
| POST | `api/resolutions/{id}/vote` | `ResolutionDetailViewModel.castVote()` | ✅ Response<Void> 204, reload szczegółów | ✅ Snackbar event | ✅ |
| GET | `api/resolutions/{id}/report` | `ResolutionDetailViewModel.downloadReport()` | ✅ PDF → cache/pdfs/ → Intent VIEW | ✅ Snackbar event | ✅ |

### Nawigacja
```
ResolutionRoutes.List
  → ResolutionRoutes.Detail(resolutionId)  [kliknięcie uchwały]
  [otwiera CreateResolutionDialog]         [przycisk "+", tylko ZARZADCA]

ResolutionRoutes.Detail
  → popBackStack                           [przycisk wstecz]
```

### Uwagi
- Brak mechanizmu sprawdzania czy MIESZKANIEC już głosował (w session/persistentnie) — flaga `hasVoted` jest tylko w pamięci VM, resetuje się po wyjściu z ekranu
- [BRAK LOADING STATE] — brak stanu ładowania raportu w UI (jest flaga `isDownloadingReport` w stanie, ale wymaga weryfikacji czy UI ją konsumuje)

---
---

## Moduł 7: Lokale i Liczniki (Properties + Meters)
**Rola:** ZARZADCA (pełny CRUD drzewa nieruchomości + liczniki) / MIESZKANIEC (liczniki własnego lokalu)

### Ekrany

#### 7.1 `PropertyTreeScreen` (route: `PropertyRoutes.Tree`)
- **Rola:** ZARZADCA
- **Akcje użytkownika:**
  - Przeglądanie drzewa hierarchii: Nieruchomość → Budynek → Klatka → Lokal
  - Kliknięcie węzła → wyświetlenie `PropertyDetailPanel` (szczegóły)
  - Tryb ADD — dodawanie nowych węzłów na każdym poziomie:
    - Dodaj Nieruchomość (`AddTarget.PROPERTY`)
    - Dodaj Budynek (`AddTarget.BUILDING`) — wymaga wybranego węzła
    - Dodaj Klatkę (`AddTarget.STAIRCASE`) — wymaga wybranego budynku
    - Dodaj Lokal (`AddTarget.APARTMENT`) — wymaga wybranej klatki
  - Tryb EDIT — edycja wybranego węzła (formularz per typ)
  - Nawigacja do liczników lokalu → `onNavigateToMeters(apartmentId)`
- **Dane wyświetlane:** drzewo `List<BuildingTreeNodeDto>` z zagnieżdżonymi StaircaseNodeDto i ApartmentNodeDto
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success (drzewo + wybrany węzeł `SelectedNode`)
  - ✅ Tryb formularza: FormMode.VIEW / FormMode.ADD / FormMode.EDIT
  - ✅ Błąd formularza (`_formError`)
  - ✅ Flaga `_isSaving` podczas zapisu
  - ⚠️ Empty state — [brak dedykowanego stanu dla pustego drzewa]

#### 7.2 `PropertyDetailPanel` (composable w PropertyTreeScreen)
- **Rola:** ZARZADCA
- **Dane wyświetlane:** szczegóły wybranego węzła (różne pola w zależności od typu: Property/Building/Staircase/Apartment)
- Wyświetla saldo lokalu (`currentBalance: BigDecimal?`)

#### 7.3 `MeterListScreen` (route: `MeterRoutes.List(apartmentId)`)
- **Rola:** ZARZADCA / MIESZKANIEC
- **Akcje użytkownika:**
  - Przeglądanie listy liczników lokalu
  - Kliknięcie licznika → nawigacja do `MeterDetailScreen`
  - (ZARZADCA) Dodanie nowego licznika → dialog `CreateMeterDialog`
  - (ZARZADCA) Dezaktywacja licznika
  - Powrót
- **Dane wyświetlane:** lista `MeterResponseDto` — id, numer seryjny, typ medium (ZIMNA_WODA/CIEPLA_WODA/GAZ/CIEPLO), data instalacji, aktywność
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success
  - ✅ Empty state — brak liczników

#### 7.4 `MeterDetailScreen` (route: `MeterRoutes.Detail(apartmentId, meterId, serialNumber, mediumType)`)
- **Rola:** ZARZADCA / MIESZKANIEC
- **Akcje użytkownika:**
  - Przeglądanie historii odczytów
  - Dodanie odczytu → dialog `CreateMeterReadingDialog`
  - Edycja odczytu → dialog edycji (osobny `editFormState`)
  - Usunięcie odczytu → `viewModel.deleteReading(readingId)`
  - Powrót
- **Dane wyświetlane:** lista `MeterReadingResponseDto` przefiltrowana po `meterId`, sortowana malejąco po dacie — id, wartość (Double + displayValue), data odczytu, data utworzenia, kto zapisał
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success(readings)
  - ✅ Dialog `showCreateDialog` / `editingReading`
  - ✅ Flaga `isSubmitting` w formularzu
- **Uwaga techniczna:** API zwraca odczyty dla CAŁEGO apartamentu, VM filtruje po `meterId` po stronie klienta

#### 7.5 `CreateMeterDialog` (dialog w MeterListScreen)
- **Rola:** ZARZADCA
- **Akcje:** numer seryjny, typ medium (dropdown), data instalacji, submit

#### 7.6 `CreateMeterReadingDialog` (dialog w MeterDetailScreen)
- **Rola:** ZARZADCA / MIESZKANIEC
- **Akcje:** wartość odczytu (BigDecimal, dopuszcza przecinek), data odczytu, submit
- **Walidacja:** `isValid` — wartość parsowalna do BigDecimal, data niepusta

### Wywołania API (Properties)

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `/api/buildings/tree` | `PropertyTreeViewModel.loadTree()` | ✅ List<BuildingTreeNodeDto> | ✅ Error state | ✅ |
| GET | `/api/properties` | `PropertyTreeViewModel` (dla formularza budynku) | ✅ List<PropertyResponseDto> | ✅ | ✅ |
| POST | `/api/properties` | `PropertyTreeViewModel.saveNew()` przy AddTarget.PROPERTY | ✅ PropertyResponseDto | ✅ formError | ✅ |
| PUT | `/api/properties/{id}` | `PropertyTreeViewModel.saveEdit()` przy SelectedNode.Property | ✅ | ✅ formError | ✅ |
| POST | `/api/buildings` | `PropertyTreeViewModel.saveNew()` przy AddTarget.BUILDING | ✅ BuildingResponseDto | ✅ formError | ✅ |
| PUT | `/api/buildings/{id}` | `PropertyTreeViewModel.saveEdit()` przy SelectedNode.Building | ✅ | ✅ formError | ✅ |
| DELETE | `/api/buildings/{id}` | `PropertyTreeViewModel` (jeśli zaimplementowane w UI) | — | — | ✅ |
| POST | `/api/buildings/{buildingId}/staircases` | `saveNew()` AddTarget.STAIRCASE | ✅ StaircaseResponseDto | ✅ | ✅ |
| PUT | `/api/buildings/{buildingId}/staircases/{id}` | `saveEdit()` SelectedNode.Staircase | ✅ | ✅ | ✅ |
| DELETE | `/api/buildings/{buildingId}/staircases/{id}` | — | — | — | ✅ |
| POST | `/api/staircases/{staircaseId}/apartments` | `saveNew()` AddTarget.APARTMENT | ✅ ApartmentResponseDto | ✅ | ✅ |
| PUT | `/api/staircases/{staircaseId}/apartments/{id}` | `saveEdit()` SelectedNode.Apartment | ✅ | ✅ | ✅ |
| DELETE | `/api/staircases/{staircaseId}/apartments/{id}` | — | — | — | ✅ |

### Wywołania API (Meters)

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `/api/apartments/{id}/meters` | `MeterListViewModel.load()` | ✅ List<MeterResponseDto> | ✅ Error state | ✅ |
| POST | `/api/apartments/{id}/meters` | Dialog submit via `MeterListViewModel` | ✅ MeterResponseDto | ✅ Snackbar | ✅ |
| PATCH | `/api/meters/{id}/deactivate` | `MeterListViewModel.deactivate()` | ✅ MeterResponseDto | ✅ Snackbar | ✅ |
| GET | `/api/apartments/{id}/meter-readings?page=0&size=100` | `MeterDetailViewModel.load()` | ✅ PaginatedResponse<MeterReadingResponseDto> | ✅ Error state | ✅ |
| POST | `/api/apartments/{id}/meter-readings` | `MeterDetailViewModel.submitCreate()` | ✅ MeterReadingResponseDto | ✅ Snackbar | ✅ |
| PUT | `/api/meter-readings/{id}` | `MeterDetailViewModel.submitUpdate()` | ✅ MeterReadingResponseDto | ✅ Snackbar | ✅ |
| DELETE | `/api/meter-readings/{id}` | `MeterDetailViewModel.deleteReading()` | ✅ (204) | ✅ Snackbar | ✅ |

### Nawigacja
```
PropertyRoutes.Tree  ← zakładka "Lokale" (tylko ZARZADCA)
  → MeterRoutes.List(apartmentId)   [kliknięcie lokalu → nawigacja do liczników]

MeterRoutes.List(apartmentId)
  → MeterRoutes.Detail(apartmentId, meterId, serialNumber, mediumType)
  → popBackStack

MeterRoutes.Detail
  → popBackStack
```

**Uwaga:** `MeterRoutes.List` dostępny też dla MIESZKAŃCA przez `PropertyRoutes.Tree` (gdy ZARZADCA wchodzi do lokalu) i potencjalnie przez Finanse/Profil (po apartmentId z drzewa).

---

## Moduł 8: Użytkownicy (Users)
**Rola:** ZARZADCA

### Ekrany

#### 8.1 `UsersScreen` (route: `UserRoutes.List`)
- **Rola:** ZARZADCA
- **Akcje użytkownika:**
  - Przeglądanie listy użytkowników
  - Wyszukiwanie (klient-side po imieniu/nazwisku/email) — `viewModel.onSearchChanged()`
  - Przycisk „Dodaj użytkownika" → dialog `CreateUserDialog`
  - Deaktywacja konta użytkownika → `viewModel.deactivateUser(id, name)`
  - Powrót → `onNavigateBack()`
- **Dane wyświetlane:** lista `AdminUserDto` — firstName, lastName, email, phone, role, active, createdAt, apartmentId; filtrowana per `searchQuery`
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success(users, searchQuery) — filtracja klient-side
  - ✅ Dialog: `showDialog` flaga
  - ⚠️ Empty state — [do weryfikacji w UsersScreen.kt]

#### 8.2 `CreateUserDialog` (dialog w UsersScreen)
- **Rola:** ZARZADCA
- **Akcje:** imię, nazwisko, e-mail, rola (dropdown: MIESZKANIEC/ZARZADCA/KONSERWATOR), wybór lokalu (cascadowe: budynek → klatka → lokal) gdy rola=MIESZKANIEC, submit
- **Stany formularza (`NewUserFormState`):**
  - `isLoadingBuildings` — ładowanie drzewa budynków przy otwarciu dialogu
  - `buildingsError` — błąd ładowania struktury
  - `isSubmitting`
  - `isValid` — firstName, lastName, email niepuste + jeśli MIESZKANIEC to apartament wybrany
- **Walidacja:** lokalna przez `isValid`; po submit: 400 (złe dane), 404 (brak lokalu), 409 (e-mail zajęty)

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `api/admin/users` | `UsersViewModel.loadUsers()` | ✅ List<AdminUserDto> | ✅ Error state | ✅ |
| POST | `api/admin/users` | `UsersViewModel.submitCreateUser()` | ✅ AdminUserDto, prepend do listy | ✅ Snackbar (400/404/409) | ✅ |
| PATCH | `api/admin/users/{id}/deactivate` | `UsersViewModel.deactivateUser()` | ✅ aktualiz. w liście (active=false) | ✅ Snackbar | ✅ |
| GET | `/api/buildings/tree` | `UsersViewModel.loadBuildingTree()` (przy otwarciu dialogu) | ✅ drzewo w formularzu | ✅ buildingsError w formularzu | ✅ |

**Uwaga:** `AdminUserApiService` definiuje też `PATCH api/admin/users/{id}` (updateUser) — **nie jest używane przez żaden ViewModel** (brak edycji użytkownika w UI).

### Nawigacja
```
UserRoutes.List  ← wejście z TicketsNavigation (zarządca) lub BottomNav
  → popBackStack  [przycisk wstecz]
```

---

## Moduł 9: Kategorie (Categories)
**Rola:** ZARZADCA

### Ekrany

#### 9.1 `CategoriesScreen` (route: `CategoryRoutes.List`)
- **Rola:** ZARZADCA
- **Akcje użytkownika:**
  - Przeglądanie listy kategorii
  - Przycisk „Dodaj kategorię" → dialog `CategoryFormDialog` (tryb CREATE)
  - Kliknięcie edycji kategorii → dialog `CategoryFormDialog` (tryb EDIT)
  - Deaktywacja kategorii → `viewModel.deactivateCategory(id, name)` (soft delete)
  - Ustawienie SLA → `viewModel.setSla(id, hours)`
  - Powrót
- **Dane wyświetlane:** lista `CategoryDto` — id, name, slaHours (opcjonalne)
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success(categories)
  - ✅ Flaga `isSubmitting` w stanie (blokuje przycisk submit)
  - ⚠️ Empty state — [do weryfikacji]

#### 9.2 `CategoryFormDialog` (dialog w CategoriesScreen)
- **Rola:** ZARZADCA
- **Akcje:** nazwa kategorii, submit (create lub update)
- **Walidacja lokalna:** nazwa niepusta

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `api/categories` | `CategoriesViewModel.loadCategories()` via `CategoryService.getCategories()` via `TicketApiService` | ✅ List<CategoryDto> | ✅ Error state | ✅ |
| POST | `api/admin/categories` | `CategoriesViewModel.createCategory()` | ✅ AdminCategoryDto, prepend do listy | ✅ Snackbar (400/409) | ✅ |
| PUT | `api/admin/categories/{id}` | `CategoriesViewModel.updateCategory()` | ✅ AdminCategoryDto, update w liście | ✅ Snackbar (400/404/409) | ✅ |
| PATCH | `api/admin/categories/{id}/deactivate` | `CategoriesViewModel.deactivateCategory()` | ✅ usuwa z listy | ✅ Snackbar | ✅ |
| PATCH | `api/admin/categories/{id}/sla` | `CategoriesViewModel.setSla()` | ✅ update slaHours w liście | ✅ Snackbar | ✅ |

**Uwaga architektoniczna:** `GET /api/categories` jest wstrzyknięty przez `TicketApiService` do `CategoryService` (reużycie — ten sam endpoint używany w `CreateTicketViewModel`).

### Nawigacja
```
CategoryRoutes.List  ← wejście z TicketsNavigation (zarządca) lub ProfileNavigation
  → popBackStack
```

---
---

## Moduł 10: Przeglądy techniczne (Inspections)
**Rola:** ZARZADCA (tworzenie / edycja / usuwanie) / KONSERWATOR (podgląd)

### Ekrany

#### 10.1 `InspectionsListScreen` (route: `InspectionRoutes.List`)
- **Rola:** ZARZADCA / KONSERWATOR
- **Akcje użytkownika:**
  - Przeglądanie listy przeglądów technicznych
  - (ZARZADCA) Przycisk „Dodaj przegląd" → dialog `CreateInspectionDialog`
  - (ZARZADCA) Kliknięcie edycji → dialog `EditInspectionDialog`
  - (ZARZADCA) Usunięcie przeglądu → `viewModel.deleteInspection(id)`
  - Odświeżenie listy
- **Dane wyświetlane:** lista `InspectionResponseDto` — id, tytuł, opis, scheduledAt, scopeType (NIERUCHOMOSC/BUDYNEK/KLATKA), scopeId, createdByName, createdAt, computed `isUpcoming`
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success(inspections, currentUserRole, isManager)
  - ✅ Dialog CREATE: `showCreateDialog` + `CreateInspectionFormState`
  - ✅ Dialog EDIT: `editingInspection` + `editFormState`
  - ⚠️ Empty state — [do weryfikacji w InspectionsListScreen.kt]

#### 10.2 `CreateInspectionDialog` (dialog w InspectionsListScreen)
- **Rola:** ZARZADCA
- **Akcje:** tytuł, opis (opcjonalny), data i czas (`scheduledAt`), typ zasięgu (dropdown: NIERUCHOMOSC/BUDYNEK/KLATKA), id zasięgu (dropdown z pobranych wartości)
- **Dynamiczne ładowanie zasięgu:** `loadScopesForForm(ScopeType)` — przy zmianie typu zasięgu pobiera z drzewa budynków odpowiednie opcje
- **Walidacja:** `isValid` — tytuł i scopeId niepuste, scheduledAt niepuste
- **⚠️ TODO:** ScopeType.NIERUCHOMOSC — brak implementacji (komentarz `TODO: pobranie ID nieruchomości`)

#### 10.3 `EditInspectionDialog` (dialog w InspectionsListScreen)
- **Rola:** ZARZADCA
- **Uwaga:** przy edycji scopeType jest zablokowany na BUDYNEK (hardkodowane); przy update używa oryginalnych scopeType i scopeId z obiektu

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `api/inspections` | `InspectionsListViewModel.load()` | ✅ List<InspectionResponseDto> | ✅ Error state | ✅ |
| POST | `api/inspections` | `InspectionsListViewModel.submitCreate()` | ✅ InspectionResponseDto, reload listy | ✅ Snackbar | ✅ |
| PUT | `api/inspections/{id}` | `InspectionsListViewModel.submitUpdate()` | ✅ InspectionResponseDto, reload listy | ✅ Snackbar | ✅ |
| DELETE | `api/inspections/{id}` | `InspectionsListViewModel.deleteInspection()` | ✅ (204), reload listy | ✅ Snackbar | ✅ |
| GET | `/api/buildings/tree` | `InspectionsListViewModel.loadScopesForForm()` | ✅ budynki/klatki do dropdown | ⚠️ cichy błąd (brak obsługi) | ✅ |

### Nawigacja
```
InspectionRoutes.List  ← wejście z ProfileNavigation (zarządca)
  → popBackStack
```

---

## Moduł 11: Powiadomienia — konfiguracja (Notifications)
**Rola:** ZARZADCA (konfiguracja ustawień notyfikacji push dla wspólnoty)

### Ekrany

#### 11.1 `NotificationsScreen` (route: `NotificationRoutes.Settings`)
- **Rola:** ZARZADCA
- **Akcje użytkownika:**
  - Podgląd listy typów zdarzeń powiadomień
  - Toggle włącz/wyłącz per typ zdarzenia → `viewModel.toggleSetting(eventType, enabled)`
  - Odświeżenie listy
- **Dane wyświetlane:** lista `NotificationConfigDto` — eventType, label, enabled
- **Stany:**
  - ✅ Loading
  - ✅ Error
  - ✅ Success(settings, updatingEventType) — `updatingEventType` blokuje konkretny toggle podczas aktualizacji
  - ✅ Optimistic update: natychmiastowe odbicie w UI po sukcesie

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `api/admin/notifications/settings` | `NotificationsViewModel.loadSettings()` | ✅ List<NotificationConfigDto> | ✅ Error state | ✅ |
| PATCH | `api/admin/notifications/settings/{eventType}` | `NotificationsViewModel.toggleSetting()` | ✅ NotificationConfigDto, update listy | ✅ Snackbar + rollback UI | ✅ |

### Nawigacja
```
NotificationRoutes.Settings  ← wejście z ProfileNavigation (zarządca)
  → popBackStack
```

---

## Moduł 12: Profil (Profile)
**Rola:** wspólny — wyświetla wszystkim, zarządcy dostęp do dodatkowych sekcji admin

### Ekrany

#### 12.1 `ProfileScreen` (route: `ProfileRoutes.Main`)
- **Rola:** wspólny
- **Akcje użytkownika (MIESZKANIEC/KONSERWATOR):**
  - Edycja nazwy / danych (WIP — tylko pole name, brak wywołania API)
  - Kliknięcie „Zapisz" → dialog potwierdzenia → `viewModel.confirmSave()` (mock delay, brak API)
- **Akcje użytkownika (ZARZADCA — dodatkowe linki):**
  - Przejście do Kategorii
  - Przejście do Przeglądów
  - Przejście do Ustawień powiadomień
  - Przejście do Logo wspólnoty
  - Przejście do Dystrybucji dokumentów
- **Dane wyświetlane:** lokalna flaga `isManager`, pola profilu (WIP)
- **Stany:** `ProfileState.Data(name, isSaving)` — brak stanów Loading/Error/Success (tylko lokalne)
- **⚠️ [WIP]** — brak integracji z API (brak GET profilu, brak PUT profilu). `confirmSave()` jest mockowe (`delay(300)` + Snackbar).

### Wywołania API
Brak — ProfileViewModel nie wywołuje żadnego endpointu API. Rola pobierana przez `authService.getCurrentUserRole()` (lokalnie z DataStore).

### Nawigacja
```
ProfileRoutes.Main  ← zakładka "Profil" w BottomNav (wszyscy)
  → CategoryRoutes.List          [zarządca]
  → InspectionRoutes.List        [zarządca]
  → NotificationRoutes.Settings  [zarządca]
  → SettingsRoutes.Logo          [zarządca]
  → DocumentRoutes.Distribution  [zarządca]
```

---

## Moduł 13: Ustawienia (Settings)
**Rola:** ZARZADCA

### Ekrany

#### 13.1 `NotificationSettingsScreen` (route: `SettingsRoutes.Notifications` — używany przez profil zarządcy)

> **⚠️ UWAGA: To NIE jest ten sam ekran co Moduł 11.** To jest odrębny, lokalny ekran ustawień z **hardkodowaną listą toggleów** (`defaultToggles()`), **NIE połączony z API**. Wyświetla banner "WIP — Funkcja w przygotowaniu".

- **Rola:** ZARZADCA
- **Akcje użytkownika:** Toggle per typ zdarzenia — zmiany przechowywane tylko w pamięci VM
- **Stany:** `NotificationSettingsState(toggles, isSaving)` — tylko in-memory
- **⚠️ [WIP / HARDKODOWANE]** — `NotificationSettingsViewModel` nie wysyła żadnych żądań do API. Wszystko in-memory.

#### 13.2 `CommunityLogoScreen` (route: `SettingsRoutes.Logo`)
- **Rola:** ZARZADCA
- **Akcje użytkownika:**
  - Wybór nieruchomości (dropdown)
  - Wybór pliku logo (systemowy file picker — image/*)
  - Upload → `viewModel.uploadLogo(propertyId, uri)`
  - Podgląd aktualnego logo
- **Dane wyświetlane:** lista `PropertyResponseDto`, aktualny logoPath (URL), podgląd wybranego pliku
- **Stany:**
  - ✅ ładowanie listy nieruchomości
  - ✅ Loading (upload)
  - ✅ Success — wyświetlenie nowego logo
  - ✅ Error — Snackbar
- **Format:** multipart/form-data z pliku wybranego przez ContentResolver

### Wywołania API (Settings)

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| GET | `/api/properties` | `CommunityLogoViewModel.loadProperties()` | ✅ List<PropertyResponseDto> | ✅ | ✅ |
| PATCH | `/api/properties/{id}/logo` | `CommunityLogoViewModel.uploadLogo()` | ✅ PropertyResponseDto (z nowym logoPath) | ✅ Snackbar | ✅ |

### Nawigacja
```
SettingsRoutes.Logo           ← ProfileRoutes.Main (zarządca)
SettingsRoutes.Notifications  ← ProfileRoutes.Main (zarządca) [WIP — lokalny screen]
  → popBackStack
```

---

## Moduł 14: Dystrybucja dokumentów (Documents)
**Rola:** ZARZADCA

### Ekrany

#### 14.1 `DocumentDistributionScreen` (route: `DocumentRoutes.Distribution`)
- **Rola:** ZARZADCA
- **Akcje użytkownika:**
  - Przełączanie zakładek (TabRow): `RATE_CHANGE` (Zmiana stawek) / `ANNUAL_SETTLEMENT` (Roczne rozliczenie)
  - **RATE_CHANGE tab:**
    - Tytuł zawiadomienia (`rateChangeSubject`)
    - Treść zawiadomienia (`rateChangeBody`)
    - Data wejścia w życie (`rateChangeEffectiveDate`)
    - Zakres odbiorców: ALL / BUILDING (+ targetId) / APARTMENT (+ targetId)
    - Submit → `viewModel.sendRateChange()`
  - **ANNUAL_SETTLEMENT tab:**
    - Rok rozliczenia (`settlementYear` — INT)
    - Notatka opcjonalna (`settlementNote`)
    - Zakres odbiorców: ALL / BUILDING / APARTMENT
    - Submit → `viewModel.sendAnnualSettlement()`
  - Powrót
- **Dane wyświetlane:** wynik dystrybucji (`DocumentDistributionResultDto` — documentsGenerated, recipientsNotified, message)
- **Stany:**
  - ✅ Idle
  - ✅ isSubmitting — blokuje przycisk
  - ✅ lastSentTab — oznacza zakładkę po sukcesie
  - ✅ Error — Snackbar

### Wywołania API

| Metoda | Endpoint | ViewModel/Service | Sukces | Błąd | JWT |
|--------|----------|------------------|--------|------|-----|
| POST | `/api/admin/documents/rate-change` | `DocDistributionViewModel.sendRateChange()` | ✅ DocumentDistributionResultDto | ✅ Snackbar | ✅ |
| POST | `/api/admin/documents/annual-settlement` | `DocDistributionViewModel.sendAnnualSettlement()` | ✅ DocumentDistributionResultDto | ✅ Snackbar | ✅ |

### Nawigacja
```
DocumentRoutes.Distribution  ← ProfileRoutes.Main (zarządca)
  → popBackStack
```

---

## Moduł 15: Infrastruktura sieciowa (Network Infrastructure)
**Rola:** wspólny (warstwa techniczna)

### Komponenty

#### 15.1 `NetworkModule` (Hilt @Module, SingletonComponent)
- Dwa osobne klienty OkHttp:
  - `"bare"` — tylko logging, używany dla `AuthApiService` (login, refresh, forgot-password, reset-password)
  - `"main"` — z `AuthInterceptor` + `TokenAuthenticator`, używany dla wszystkich pozostałych API serwisów
- `BASE_URL` z `BuildConfig.BACKEND_URL` (konfigurowana per variant)
- Timeouty: 30 sekund (connect + read)
- Rejestruje wszystkie API serwisy: `TicketApiService`, `PropertyApiService`, `FinancialApiService`, `ResolutionApiService`, `InspectionApiService`, `MeterApiService`, `NotificationApiService`, `DocumentApiService`, `AnnouncementApiService`, `TicketCommentApiService`, `TicketImageApiService`, `UserDocumentApiService`, `DeviceApiService`, `PdfApiService`

#### 15.2 `AuthInterceptor` (OkHttp Interceptor)
- Dodaje `Authorization: Bearer <accessToken>` do każdego żądania klienta `"main"`
- Token pobierany synchronicznie przez `runBlocking { tokenStorage.getAccessToken() }`
- **⚠️ Potencjalny problem:** `runBlocking` w main thread może powodować ANR przy wolnym DataStore

#### 15.3 `TokenAuthenticator` (OkHttp Authenticator)
- Automatyczne odświeżanie access tokenu po HTTP 401
- Zabezpieczenia:
  - Sprawdza header `X-Token-Refreshed` — zapobiega pętli
  - Pomija żądania do ścieżek `/auth/**`
  - Zwraca `null` (logout flow) gdy brak refresh tokenu lub refresh nieudany
- Używa klienta `"bare"` do odświeżania (bez JWT)

#### 15.4 `TokenStorage` (DataStore Preferences, @Singleton)
- Klucze: `access_token`, `refresh_token`, `user_role`
- Metody: `getAccessToken()`, `getRefreshToken()`, `getUserRole()`, `saveTokens()`, `clearTokens()`

#### 15.5 `FcmTokenProvider` / `FcmModule`
- Interfejs `FcmTokenProvider.getToken(): String?`
- Aktualnie: `NoOpFcmTokenProvider` zawsze zwraca `null` (Firebase Messaging **nie jest dodany** do projektu)
- Komentarz w kodzie wskazuje 3 kroki potrzebne do włączenia Firebase

#### 15.6 `DeviceApiService` + `DeviceService`
- `POST /api/devices/register` — rejestracja FCM token (wywoływana po zalogowaniu, fire-and-forget)
- `DELETE /api/devices/{token}` — wyrejestrowanie tokenu (po wylogowaniu)
- `DeviceService.registerDevice()` i `unregisterDevice()` działają na `Dispatchers.IO`, błędy logowane (nie przerywają flow)

---

## Podsumowanie technicznych długów (Technical Debt) — skumulowane ze wszystkich modułów

| Typ | Moduł | Plik | Opis |
|-----|-------|------|------|
| ⚠️ HARDKODOWANE | 4 | `FinancesService.kt` | `getBalance()`, `getTransactions()`, `getDocuments()` — mock danych (FinancesScreen overview) |
| ⚠️ WIP | 12 | `ProfileViewModel.kt` | Brak integracji z API (GET/PUT profilu), `confirmSave()` to `delay(300)` |
| ⚠️ WIP | 13 | `NotificationSettingsViewModel.kt` | In-memory toggles, brak API; ekran wyświetla baner "WIP" |
| ⚠️ TODO | 10 | `InspectionsViewModels.kt` | ScopeType.NIERUCHOMOSC — pusta implementacja w `loadScopesForForm()` |
| ⚠️ CICHY BŁĄD | 3 | `TicketDetailsViewModel.kt` | Błędy ładowania komentarzy i zdjęć są ciche (brak UI feedback) |
| ⚠️ CICHY BŁĄD | 3 | `CreateTicketViewModel.kt` | Błąd ładowania kategorii cichy — pusta lista bez komunikatu |
| ⚠️ BRAK UI | 5 | `AnnouncementService.kt` | `createAnnouncement()`, `updateAnnouncement()`, `deleteAnnouncement()` — serwis istnieje, brak ekranów UI |
| ⚠️ BRAK EDYCJI | 8 | `AdminUserApiService.kt` | `PATCH /api/admin/users/{id}` (updateUser) — brak ViewModel/ekranu |
| ⚠️ KLIENT-SIDE | 3 | `TicketsViewModel.kt` | Filtrowanie zgłoszeń klient-side mimo server-side API query params |
| ⚠️ KLIENT-SIDE | 3 | `MeterDetailViewModel.kt` | Filtrowanie odczytów po `meterId` klient-side (API zwraca wszystkie dla apartamentu) |
| ⚠️ RUNBLOCKING | 15 | `AuthInterceptor.kt` | `runBlocking` w `intercept()` — ryzyko ANR przy wolnym DataStore |
| ⚠️ FCM WYŁĄCZONE | 15 | `FcmModule.kt` | Firebase Messaging niezintegrowane — `NoOpFcmTokenProvider` zawsze null |
| ⚠️ BRAK HASŁA | 6 | `ResolutionDetailViewModel.kt` | `hasVoted` in-memory — reset po wyjściu z ekranu, brak persistentnej blokady podwójnego głosu |

---

## FAZA 2 — Weryfikacja kompletności

### Sprawdzenie pokrycia ekranów

Wszystkie znalezione ekrany (`*Screen.kt`) zostały przeanalizowane:

| # | Ekran | Moduł | Rola |
|---|-------|-------|------|
| 1 | `LoginScreen` | 1 Auth | wspólny |
| 2 | `ForgotPasswordScreen` | 1 Auth | wspólny |
| 3 | `ResetPasswordScreen` | 1 Auth | wspólny |
| 4 | `ResidentMainScreen` | 2 Main Shell | wspólny |
| 5 | `TicketsScreen` | 3 Tickets | wspólny |
| 6 | `TicketDetailsScreen` | 3 Tickets | wspólny |
| 7 | `CreateTicketScreen` | 3 Tickets | MIESZKANIEC |
| 8 | `FinancesScreen` | 4 Finances | MIESZKANIEC/ZARZADCA |
| 9 | `TransactionsScreen` | 4 Finances | MIESZKANIEC/ZARZADCA |
| 10 | `DocumentsScreen` | 4 Finances | MIESZKANIEC/ZARZADCA |
| 11 | `FinancialLedgerScreen` | 4 Finances | MIESZKANIEC/ZARZADCA |
| 12 | `ApartmentBalancesScreen` | 4 Finances | ZARZADCA |
| 13 | `CsvImportScreen` | 4 Finances | ZARZADCA |
| 14 | `AnnouncementsScreen` | 5 Announcements | MIESZKANIEC/ZARZADCA |
| 15 | `ResolutionsListScreen` | 6 Resolutions | MIESZKANIEC/ZARZADCA |
| 16 | `ResolutionDetailScreen` | 6 Resolutions | MIESZKANIEC/ZARZADCA |
| 17 | `PropertyTreeScreen` | 7 Properties | ZARZADCA |
| 18 | `MeterListScreen` | 7 Meters | ZARZADCA/MIESZKANIEC |
| 19 | `MeterDetailScreen` | 7 Meters | ZARZADCA/MIESZKANIEC |
| 20 | `UsersScreen` | 8 Users | ZARZADCA |
| 21 | `CategoriesScreen` | 9 Categories | ZARZADCA |
| 22 | `InspectionsListScreen` | 10 Inspections | ZARZADCA/KONSERWATOR |
| 23 | `NotificationsScreen` | 11 Notifications | ZARZADCA |
| 24 | `ProfileScreen` | 12 Profile | wspólny |
| 25 | `NotificationSettingsScreen` | 13 Settings | ZARZADCA |
| 26 | `CommunityLogoScreen` | 13 Settings | ZARZADCA |
| 27 | `DocumentDistributionScreen` | 14 Documents | ZARZADCA |

**Łączna liczba ekranów: 27**

### Sprawdzenie pokrycia ViewModeli

| ViewModel | Moduł | Pokryty |
|-----------|-------|---------|
| `AuthViewModel` | 1 Auth | ✅ |
| `ForgotPasswordViewModel` | 1 Auth | ✅ |
| `ResetPasswordViewModel` | 1 Auth | ✅ |
| `ResidentMainViewModel` | 2 Main Shell | ✅ |
| `TicketsViewModel` | 3 Tickets | ✅ |
| `TicketDetailsViewModel` | 3 Tickets | ✅ |
| `CreateTicketViewModel` | 3 Tickets | ✅ |
| `FinancesViewModel` | 4 Finances | ✅ |
| `FinancialLedgerViewModel` | 4 Finances | ✅ |
| `ApartmentBalancesViewModel` | 4 Finances | ✅ |
| `CsvImportViewModel` | 4 Finances | ✅ |
| `AnnouncementsViewModel` | 5 Announcements | ✅ |
| `ResolutionsListViewModel` | 6 Resolutions | ✅ |
| `ResolutionDetailViewModel` | 6 Resolutions | ✅ |
| `PropertyTreeViewModel` | 7 Properties | ✅ |
| `MeterListViewModel` | 7 Meters | ✅ |
| `MeterDetailViewModel` | 7 Meters | ✅ |
| `UsersViewModel` | 8 Users | ✅ |
| `CategoriesViewModel` | 9 Categories | ✅ |
| `InspectionsListViewModel` | 10 Inspections | ✅ |
| `NotificationsViewModel` | 11 Notifications | ✅ |
| `ProfileViewModel` | 12 Profile | ✅ |
| `NotificationSettingsViewModel` | 13 Settings | ✅ |
| `CommunityLogoViewModel` | 13 Settings | ✅ |
| `DocDistributionViewModel` | 14 Documents | ✅ |

**Łączna liczba ViewModeli: 25**

### Sprawdzenie pokrycia API Service'ów

| API Service | Moduł | Endpointy |
|-------------|-------|-----------|
| `AuthApiService` | 1 Auth / 15 Infra | POST /login, POST /refresh, POST /forgot-password, POST /reset-password |
| `DeviceApiService` | 1 Auth / 15 Infra | POST /api/devices/register, DELETE /api/devices/{token} |
| `TicketApiService` | 3 Tickets | GET/POST /api/tickets, GET/PATCH /api/tickets/{id}, PATCH assign/close/reject/start/suspend, POST completion, GET categories, GET conservators |
| `TicketCommentApiService` | 3 Tickets | GET/POST /api/tickets/{id}/comments |
| `TicketImageApiService` | 3 Tickets | POST/GET /api/tickets/{id}/images, GET /api/images/{id} |
| `FinancialApiService` | 4 Finances | GET/POST /api/apartments/{id}/transactions, GET /api/admin/apartments/balances, POST /api/finance/import |
| `UserDocumentApiService` | 4 Finances | GET /api/documents, GET /api/documents/{id}/download |
| `AnnouncementApiService` | 5 Announcements | GET/POST /api/announcements, PUT/DELETE /api/announcements/{id}, GET /api/announcements/{id}/attachment |
| `ResolutionApiService` | 6 Resolutions | GET /api/resolutions, GET/POST /api/resolutions/{id}, POST /api/resolutions/{id}/vote, GET /api/resolutions/{id}/report |
| `PropertyApiService` | 7 Properties | GET /api/buildings/tree, CRUD /api/properties, CRUD /api/buildings, CRUD staircases, CRUD apartments |
| `MeterApiService` | 7 Meters | GET/POST /api/apartments/{id}/meters, PATCH /api/meters/{id}/deactivate, GET/POST /api/apartments/{id}/meter-readings, GET/PUT/DELETE /api/meter-readings/{id} |
| `AdminUserApiService` | 8 Users | GET/POST /api/admin/users, PATCH /api/admin/users/{id}, PATCH /api/admin/users/{id}/deactivate |
| `CategoryApiService` | 9 Categories | POST /api/admin/categories, PUT /api/admin/categories/{id}, PATCH .../sla, PATCH .../deactivate |
| `InspectionApiService` | 10 Inspections | GET/POST /api/inspections, PUT/DELETE /api/inspections/{id} |
| `NotificationApiService` | 11 Notifications | GET /api/admin/notifications/settings, PATCH .../settings/{eventType} |
| `DocumentApiService` | 13 Settings / 14 Documents | POST /api/admin/documents/rate-change, POST /api/admin/documents/annual-settlement, PATCH /api/properties/{id}/logo |
| `PdfApiService` | 3 Tickets / 4 Finances | GET /api/pdf/balances, POST /api/pdf/work-acceptance-protocol |

**Łączna liczba API Service interfejsów: 17**

---

### Podsumowanie końcowe Fazy 1

#### Statystyki ogólne

| Kategoria | Liczba |
|-----------|--------|
| **Ekrany (Composable screens)** | **27** |
| **ViewModele** | **25** |
| **API Service interfejsy (Retrofit)** | **17** |
| **Serwisy logiczne (Service/Repository)** | **12** |
| **Pliki DTO** | **11** |
| **Moduły funkcjonalne** | **15** |

#### Ekrany per rola

| Rola | Ekrany | Lista |
|------|--------|-------|
| **wspólny** | 5 | LoginScreen, ForgotPasswordScreen, ResetPasswordScreen, ResidentMainScreen, ProfileScreen |
| **MIESZKANIEC** | 2 | CreateTicketScreen (jedyny wyłącznie M) |
| **MIESZKANIEC / ZARZADCA** | 8 | TicketsScreen, TicketDetailsScreen, FinancesScreen, TransactionsScreen, DocumentsScreen, FinancialLedgerScreen, AnnouncementsScreen, ResolutionsListScreen, ResolutionDetailScreen |
| **ZARZADCA** | 10 | ApartmentBalancesScreen, CsvImportScreen, PropertyTreeScreen, UsersScreen, CategoriesScreen, NotificationsScreen, NotificationSettingsScreen, CommunityLogoScreen, DocumentDistributionScreen |
| **ZARZADCA / KONSERWATOR** | 1 | InspectionsListScreen |
| **ZARZADCA / MIESZKANIEC** | 2 | MeterListScreen, MeterDetailScreen |

#### Kompletny rejestr wywołanych endpointów

```
# AUTH
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/forgot-password
POST   /api/auth/reset-password

# DEVICES (FCM)
POST   /api/devices/register
DELETE /api/devices/{token}

# TICKETS
GET    /api/tickets
POST   /api/tickets
GET    /api/tickets/{id}
PATCH  /api/tickets/{id}/assign
PATCH  /api/tickets/{id}/close
PATCH  /api/tickets/{id}/reject
PATCH  /api/tickets/{id}/start
PATCH  /api/tickets/{id}/suspend
POST   /api/tickets/{id}/completion
GET    /api/tickets/{id}/comments
POST   /api/tickets/{id}/comments
GET    /api/tickets/{id}/images
POST   /api/tickets/{id}/images
GET    /api/images/{id}
GET    /api/categories
GET    /api/users?role=KONSERWATOR

# FINANCES
GET    /api/apartments/{id}/transactions
POST   /api/apartments/{id}/transactions
GET    /api/admin/apartments/balances
POST   /api/finance/import

# DOCUMENTS (user)
GET    /api/documents
GET    /api/documents/{id}/download

# ANNOUNCEMENTS
GET    /api/announcements
POST   /api/announcements
PUT    /api/announcements/{id}
DELETE /api/announcements/{id}
GET    /api/announcements/{id}/attachment

# RESOLUTIONS
GET    /api/resolutions
POST   /api/resolutions
GET    /api/resolutions/{id}
POST   /api/resolutions/{id}/vote
GET    /api/resolutions/{id}/report

# PROPERTIES
GET    /api/buildings/tree
GET    /api/properties
GET    /api/properties/{id}
POST   /api/properties
PUT    /api/properties/{id}
PATCH  /api/properties/{id}/logo
POST   /api/buildings
PUT    /api/buildings/{id}
DELETE /api/buildings/{id}
POST   /api/buildings/{buildingId}/staircases
PUT    /api/buildings/{buildingId}/staircases/{id}
DELETE /api/buildings/{buildingId}/staircases/{id}
POST   /api/staircases/{id}/apartments
PUT    /api/staircases/{staircaseId}/apartments/{id}
DELETE /api/staircases/{staircaseId}/apartments/{id}

# METERS
GET    /api/apartments/{id}/meters
POST   /api/apartments/{id}/meters
PATCH  /api/meters/{id}/deactivate
GET    /api/apartments/{id}/meter-readings
POST   /api/apartments/{id}/meter-readings
GET    /api/meter-readings/{id}
PUT    /api/meter-readings/{id}
DELETE /api/meter-readings/{id}

# USERS (admin)
GET    /api/admin/users
POST   /api/admin/users
PATCH  /api/admin/users/{id}
PATCH  /api/admin/users/{id}/deactivate

# CATEGORIES (admin)
POST   /api/admin/categories
PUT    /api/admin/categories/{id}
PATCH  /api/admin/categories/{id}/sla
PATCH  /api/admin/categories/{id}/deactivate

# INSPECTIONS
GET    /api/inspections
POST   /api/inspections
PUT    /api/inspections/{id}
DELETE /api/inspections/{id}

# NOTIFICATIONS (admin)
GET    /api/admin/notifications/settings
PATCH  /api/admin/notifications/settings/{eventType}

# DOCUMENTS (admin)
POST   /api/admin/documents/rate-change
POST   /api/admin/documents/annual-settlement

# PDF
GET    /api/pdf/balances
POST   /api/pdf/work-acceptance-protocol
```

**Łączna liczba unikalnych endpointów: 71**

#### Zbiorczy rejestr długów technicznych

| Priorytet | Typ | Moduł | Opis |
|-----------|-----|-------|------|
| 🔴 | [HARDKODOWANE] | 4 Finances | `FinancesService.getBalance()` / `getTransactions()` — mock, brak API |
| 🔴 | [HARDKODOWANE] | 12 Profile | `ProfileViewModel.confirmSave()` — `delay(300)` zamiast API |
| 🔴 | [WIP] | 13 Settings | `NotificationSettingsViewModel` — in-memory toggles, brak API, UI wyświetla "WIP" |
| 🟡 | [TODO] | 10 Inspections | `ScopeType.NIERUCHOMOSC` — pusta implementacja w `loadScopesForForm()` |
| 🟡 | [BRAK UI] | 5 Announcements | Create/Update/Delete ogłoszeń — serwis gotowy, brak ekranów |
| 🟡 | [BRAK UI] | 8 Users | `PATCH /api/admin/users/{id}` (updateUser) — brak ViewModelu i ekranu edycji |
| 🟡 | [BRAK ERROR HANDLING] | 3 Tickets | Błędy ładowania komentarzy i zdjęć do zgłoszenia — ciche, brak UI feedback |
| 🟡 | [BRAK ERROR HANDLING] | 3 Tickets | Błąd ładowania kategorii w CreateTicket — cichy (pusta lista bez komunikatu) |
| 🟠 | [BRAK LOADING STATE] | 6 Resolutions | Flaga `isDownloadingReport` istnieje w stanie, wymaga weryfikacji czy UI ją konsumuje |
| 🟠 | [KLIENT-SIDE FILTER] | 3 Tickets | Filtrowanie po statusie/frazie — klient-side mimo server-side query params w API |
| 🟠 | [KLIENT-SIDE FILTER] | 7 Meters | Filtrowanie odczytów po `meterId` — klient-side (API zwraca wszystkie dla apartamentu) |
| 🟠 | [FCM WYŁĄCZONE] | 15 Infra | `NoOpFcmTokenProvider` zawsze null — Firebase Messaging niezintegrowane |
| 🟠 | [RUNBLOCKING] | 15 Infra | `AuthInterceptor.intercept()` używa `runBlocking` — ryzyko ANR |
| 🟠 | [BRAK PERSISTENCJI] | 6 Resolutions | `hasVoted` in-memory — reset po wyjściu z ekranu, możliwe podwójne głosowanie |
