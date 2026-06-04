# BlokUR Frontend — Inwentaryzacja (Faza 1)

**Data:** 2026-06-05  
**Źródło:** analiza kodu `frontend/app/src/main/java/pl/edu/ur/blokur/`

---

## Moduł 1: Infrastruktura aplikacji i sieć

**Rola:** wspólny

### Ekrany

Brak dedykowanych ekranów — infrastruktura obsługuje całą aplikację.

| Klasa | Rola | Akcje użytkownika | Dane | Stany UI |
|-------|------|-------------------|------|----------|
| `MainActivity` | punkt wejścia | uruchomienie aplikacji | — | — |
| `AppNavHost` | root NavHost | nawigacja globalna | — | — |

### Wywołania API

| HTTP | Ścieżka | Skąd | JWT | Obsługa |
|------|---------|------|-----|---------|
| POST | `api/auth/refresh` | `TokenAuthenticator` (automatycznie przy 401) | **Nie** (klient `auth` / bare) | sukces → zapis tokenów; błąd → brak retry |

Pozostałe żądania — przez klient `"main"` z `AuthInterceptor` (Bearer z `TokenStorage`).

### Komponenty infrastruktury

| Klasa | Funkcja |
|-------|---------|
| `NetworkModule` | Dwa OkHttp: `bare` (auth) i `main` (JWT + refresh); `BuildConfig.BACKEND_URL`; 15 ApiService + logging BODY |
| `AuthInterceptor` | Nagłówek `Authorization: Bearer` jeśli token w DataStore |
| `TokenAuthenticator` | Przy 401: `POST api/auth/refresh`, rotacja tokenów, jednorazowy retry |
| `TokenStorage` | DataStore: access, refresh, role |
| `FcmModule` | `NoOpFcmTokenProvider` — brak Firebase w projekcie |

### Nawigacja

```
MainActivity → AppNavHost
  startDestination: AuthRoutes.Login
  po login → MainRoutes.Main (wszystkie role)
  logout → AuthRoutes.Login (popUpTo root inclusive)
```

---

## Moduł 2: Autentykacja i onboarding

**Rola:** wspólny (przed zalogowaniem)

### Ekrany

| Composable | Rola | Akcje | Dane wyświetlane | Loading | Error | Empty |
|------------|------|-------|------------------|---------|-------|-------|
| `LoginScreen` | wszyscy | logowanie, przejście do forgot-password | formularz email/hasło | `AuthState.Loading` | `Error`, `AccountLocked` (423) | — |
| `ForgotPasswordScreen` | wszyscy | wysłanie email resetu, powrót | email | `Loading` | `Error` | — |
| `ResetPasswordScreen` | wszyscy (link z maila) | ustawienie hasła, przejście do login | token (SavedState), hasła | `Loading` | `Error` | — |
| `AcceptInvitationScreen` | zaproszony użytkownik | ustawienie hasła, login | token, hasła | `Loading` | `Error` | — |

### Wywołania API

| HTTP | Ścieżka | ViewModel / Service | JWT | Obsługa |
|------|---------|---------------------|-----|---------|
| POST | `/api/auth/login` | `AuthViewModel` → `AuthService` | **Nie** | 401→InvalidCredentials, 423→AccountLocked, sukces→zapis tokenów + `NavigateToMain` |
| POST | `/api/auth/forgot-password` | `ForgotPasswordViewModel` → `AuthService` | **Nie** | sukces→`Success(message)`; błąd→`Error` |
| POST | `/api/auth/reset-password` | `ResetPasswordViewModel` → `AuthService` | **Nie** | sukces/błąd z body JSON |
| POST | `/api/auth/accept-invitation` | `AcceptInvitationViewModel` → `AuthService` | **Nie** | sukces/błąd z body JSON |
| POST | `/api/devices/register` | `AuthViewModel.tryRegisterFcmToken` → `DeviceService` | **Tak** (po login) | fire-and-forget; błąd tylko w logu |

### Nawigacja

```
AuthRoutes.Login → ForgotPassword
AuthRoutes.Login → Main (onLoginSuccess, role)
AuthRoutes.ForgotPassword → popBack
AuthRoutes.ResetPassword(token) → Login
AuthRoutes.AcceptInvitation(token) → Login
```

---

## Moduł 3: Powłoka główna i nawigacja roli

**Rola:** wspólny (po zalogowaniu)

### Ekrany

| Composable | Rola | Akcje | Dane | Loading | Error | Empty |
|------------|------|-------|------|---------|-------|-------|
| `ResidentMainScreen` | M/Z/K | zmiana zakładki bottom nav, wylogowanie | tytuł TopBar wg `ResidentMainState`, lista zakładek z `navItemsForRole` | `ResidentMainState.Loading` (init) | `Error` (stan istnieje) | — |

**Zakładki dolne (`Data.kt`):**

| Rola | Zakładki |
|------|----------|
| MIESZKANIEC | Zgłoszenia, Finanse, Uchwały, Ogłoszenia, Profil |
| ZARZĄDCA | Zgłoszenia, Lokale, Uchwały, Użytkownicy, Profil |
| KONSERWATOR | Zgłoszenia, Profil |

### Wywołania API

| HTTP | Ścieżka | ViewModel | JWT |
|------|---------|-----------|-----|
| DELETE | `/api/devices/{token}` | `ResidentMainViewModel.logout` → `DeviceService` | **Tak** (przed logout; błąd cicho ignorowany) |

Rola i zakładki: z `TokenStorage` (DataStore), bez API.

### Nawigacja

```
MainRoutes.Main
  └─ bottomNav NavHost (startDestination: ProfileRoutes.Main)
       ├─ TicketRoutes.List
       ├─ FinancesRoutes.Main
       ├─ AnnouncementsRoutes.Main
       ├─ ProfileRoutes.Main
       ├─ PropertyRoutes.Tree (Z)
       ├─ UserRoutes.List (Z)
       ├─ ResolutionRoutes.List
       ├─ … (nested graphs w AppNavHost)
```

`onOptionClicked` → `navigate` do odpowiedniej trasy z `saveState`/`restoreState`.

---

## Moduł 4: Profil użytkownika

**Rola:** wspólny (menu ZARZĄDCA rozszerzone)

### Ekrany

| Composable | Rola | Akcje | Dane | Loading | Error | Empty |
|------------|------|-------|------|---------|-------|-------|
| `ProfileScreen` | wszyscy | edycja imienia (lokalna), zapis (mock), test snackbar | name, email, phone | `ProfileState.Loading` | catch→snackbar + Data fallback | — |

**ZARZĄDCA — dodatkowe linki nawigacyjne:** ustawienia powiadomień, logo wspólnoty, dystrybucja dokumentów, przeglądy, kategorie.

### Wywołania API

Brak wywołań API — komentarz w `ProfileViewModel`: *„Backend nie posiada endpointu do pobierania swojego profilu”*.

Dane: **[HARDKODOWANE]** `name="Użytkownik"`, `email="Zalogowany"`, `phone=""`. Zapis profilu: `delay(300)` + snackbar „Zapisano zmiany” — bez API.

### Nawigacja (z ProfileScreen)

```
ProfileRoutes.Main → SettingsRoutes.Notifications (Z)
ProfileRoutes.Main → SettingsRoutes.CommunityLogo (Z)
ProfileRoutes.Main → DocumentRoutes.Distribution (Z)
ProfileRoutes.Main → InspectionRoutes.List (Z)
ProfileRoutes.Main → CategoryRoutes.List (Z)
```

---

## Moduł 5: Zgłoszenia serwisowe (tickets)

**Rola:** MIESZKANIEC + ZARZĄDCA + KONSERWATOR

### Ekrany

| Composable | Rola | Akcje | Dane | Loading | Error | Empty |
|------------|------|-------|------|---------|-------|-------|
| `TicketsScreen` | M/Z/K | lista, filtry (status, search), paginacja, nawigacja do szczegółów/tworzenia | `TicketSummaryDto`, rola | `TicketsListState.Loading` | `Error` | brak dedykowanego `Empty` — pusta lista w `Success` |
| `TicketDetailsScreen` | M/Z/K | przypisanie (Z), odrzucenie (Z), zamknięcie (Z), start/suspend/complete (K), komentarze, zdjęcia, PDF protokół | szczegóły, historia, komentarze, obrazy | `Loading`, `isLoadingComments` | `Error`, snackbar | — |
| `CreateTicketScreen` | MIESZKANIEC | formularz nowego zgłoszenia | kategorie, pola | `Submitting` | `Error` | — |

**Dialogi/sheets:** `AssignConservatorSheet`, `ManagerRejectSheet`, `ConservatorActionSheet`

### Wywołania API

| HTTP | Ścieżka | ViewModel | JWT | Obsługa |
|------|---------|-----------|-----|---------|
| GET | `/api/tickets` | `TicketsViewModel` | Tak | `page`/`size` wysyłane — backend **[WYMAGA WERYFIKACJI]** ignoruje paginację |
| GET | `/api/tickets/{id}` | `TicketDetailsViewModel` | Tak | błąd→`Error` |
| POST | `/api/tickets` | `CreateTicketViewModel` | Tak | sukces/błąd |
| GET | `/api/categories` | `CreateTicketViewModel` | Tak (publiczny na backendzie) | lista kategorii |
| GET | `/api/users?role=KONSERWATOR` | `TicketDetailsViewModel` | Tak | lista konserwatorów |
| PATCH | `/api/tickets/{id}/assign` | `TicketDetailsViewModel` | Tak | |
| PATCH | `/api/tickets/{id}/close` | `TicketDetailsViewModel` | Tak | |
| PATCH | `/api/tickets/{id}/reject` | `TicketDetailsViewModel` | Tak | |
| PATCH | `/api/tickets/{id}/start` | `TicketDetailsViewModel` | Tak | |
| PATCH | `/api/tickets/{id}/suspend` | `TicketDetailsViewModel` | Tak | |
| POST | `/api/tickets/{id}/completion` | `TicketDetailsViewModel` | Tak | |
| GET | `/api/tickets/{id}/comments` | `TicketDetailsViewModel` | Tak | nieudane→pusta lista + opcjonalny `ShowError` |
| POST | `/api/tickets/{id}/comments` | `TicketDetailsViewModel` | Tak | |
| GET/POST | `/api/tickets/{id}/images` | `TicketDetailsViewModel` | Tak | |
| GET | `/api/images/{id}` | `TicketDetailsViewModel` (serve) | Tak | |
| POST | `/api/pdf/work-acceptance-protocol` | `TicketDetailsViewModel` | Tak | generowanie PDF |

### Nawigacja

```
TicketRoutes.List → Details(ticketId)
TicketRoutes.List → Create (MIESZKANIEC)
TicketRoutes.List → CategoryRoutes.List / UserRoutes.List (skróty w UI)
Details → popBack
Create → popBack
```

---

## Moduł 6: Ogłoszenia

**Rola:** MIESZKANIEC (odczyt) + ZARZĄDCA (CRUD)

### Ekrany

| Composable | Rola | Akcje | Dane | Loading | Error | Empty |
|------------|------|-------|------|---------|-------|-------|
| `AnnouncementsScreen` | M+Z | lista, pobierz załącznik, usuń (Z), FAB→create (Z) | `AnnouncementDto` | `Loading` | `Error` + snackbar | `Empty` |
| `CreateAnnouncementScreen` | Z | tworzenie ogłoszenia multipart | formularz + PDF | w ViewModel | snackbar | — |

### Wywołania API

| HTTP | Ścieżka | ViewModel | JWT |
|------|---------|-----------|-----|
| GET | `/api/announcements` | `AnnouncementsViewModel` | Tak |
| GET | `/api/announcements/{id}/attachment` | `AnnouncementsViewModel.downloadAttachment` | Tak |
| DELETE | `/api/announcements/{id}` | `AnnouncementsViewModel` (Z) | Tak |
| POST | `/api/announcements` | `CreateAnnouncementViewModel` | Tak |
| PUT | `/api/announcements/{id}` | `CreateAnnouncementViewModel` | Tak |

### Nawigacja

```
AnnouncementsRoutes.Main → Create (Z, FAB)
```

---

## Moduł 7: Finanse i dokumenty mieszkańca

**Rola:** MIESZKANIEC + ZARZĄDCA

### 7a — Przegląd (`FinancesScreen`, `TransactionsScreen`, `DocumentsScreen`)

| Composable | Rola | Akcje | Dane | Loading | Error | Empty |
|------------|------|-------|------|---------|-------|-------|
| `FinancesScreen` | M+Z | hub: transakcje, dokumenty, kartoteka, salda (Z), import CSV (Z) | saldo, transakcje, dokumenty | `FinancesState.Loading` | `Error` | — |
| `TransactionsScreen` | M+Z | lista transakcji (parent VM) | transakcje z `FinancesViewModel` | dziedziczy | dziedziczy | — |
| `DocumentsScreen` | M+Z | lista dokumentów, pobierz PDF | `UserDocumentDto` | dziedziczy | snackbar przy błędzie | — |

**Uwaga:** `FinancesService` (mock saldo/transakcje) — **nie jest używany** przez `FinancesViewModel` (używa `FinancialLedgerService` + `UserDocumentService`).

### 7b — Kartoteka, salda, import

| Composable | Rola | API |
|------------|------|-----|
| `FinancialLedgerScreen` | M+Z | GET/POST `/api/apartments/{id}/transactions` |
| `ApartmentBalancesScreen` | Z | GET `/api/admin/apartments/balances` |
| `CsvImportScreen` | Z | POST `/api/finance/import` |
| `AddTransactionDialog` | Z | POST transakcji (przez Ledger VM) |

### 7c — Dokumenty użytkownika

| HTTP | Ścieżka | ViewModel | JWT |
|------|---------|-----------|-----|
| GET | `/api/documents` | `FinancesViewModel` | Tak |
| GET | `/api/documents/{id}/download` | `FinancesViewModel.downloadDocument` | Tak |

### Nawigacja

```
FinancesRoutes.Main → Transactions / Documents / Ledger / Balances / CsvImport
```

---

## Moduł 8: Uchwały i głosowania

**Rola:** MIESZKANIEC + ZARZĄDCA

### Ekrany

| Composable | Rola | Akcje | Loading | Error | Empty |
|------------|------|-------|---------|-------|-------|
| `ResolutionsListScreen` | M+Z | lista, dialog tworzenia (Z) | Tak | Tak | — |
| `ResolutionDetailScreen` | M+Z | głosowanie (M), raport PDF (Z) | Tak | Tak | — |

### Wywołania API

| HTTP | Ścieżka | ViewModel | JWT |
|------|---------|-----------|-----|
| GET | `/api/resolutions` | `ResolutionsListViewModel` | Tak |
| POST | `/api/resolutions` | `ResolutionsListViewModel` (Z) | Tak |
| GET | `/api/resolutions/{id}` | `ResolutionDetailViewModel` | Tak |
| POST | `/api/resolutions/{id}/vote` | `ResolutionDetailViewModel` (M) | Tak |
| GET | `/api/resolutions/{id}/report` | `ResolutionDetailViewModel` (Z) | Tak |

### Nawigacja

```
ResolutionRoutes.List → Detail(id)
```

---

## Moduł 9: Nieruchomości — drzewo budynków

**Rola:** ZARZĄDCA

### Ekrany

| Composable | Akcje | API (PropertyService) | Stany |
|------------|-------|----------------------|-------|
| `PropertyTreeScreen` | CRUD property/building/staircase/apartment, nawigacja do liczników | GET/POST/PUT/DELETE na `/api/buildings/tree`, `/api/properties`, `/api/buildings`, `/api/staircases`, `/api/apartments` | `PropertyTreeState.Loading/Error/Success` |

### Nawigacja

```
PropertyRoutes.Tree → MeterRoutes.List(apartmentId)
```

---

## Moduł 10: Liczniki i odczyty

**Rola:** ZARZĄDCA (+ odczyt wg backendu)

### Ekrany

| Composable | Dialogi | API |
|------------|---------|-----|
| `MeterListScreen` | `CreateMeterDialog` | GET/POST `/api/apartments/{id}/meters`, PATCH deactivate |
| `MeterDetailScreen` | `CreateMeterReadingDialog` | GET/POST readings, DELETE/PUT reading |

### Nawigacja

```
MeterRoutes.List(apartmentId) → Detail(...)
```

---

## Moduł 11: Użytkownicy (administracja)

**Rola:** ZARZĄDCA

### Ekrany

| Composable | Dialogi | API |
|------------|---------|-----|
| `UsersScreen` | `CreateUserDialog` | GET/POST `/api/admin/users` |
| `EditUserScreen` | — | PATCH `/api/admin/users/{id}`, PATCH deactivate |

### Nawigacja

```
UserRoutes.List → Edit(id)
```

---

## Moduł 12: Kategorie zgłoszeń (SLA)

**Rola:** ZARZĄDCA

### Ekrany

| Composable | API |
|------------|-----|
| `CategoriesScreen` + `CategoryFormDialog` | GET `/api/categories` (TicketApi); POST/PUT/PATCH `/api/admin/categories/*` |

### Nawigacja

```
CategoryRoutes.List (z Profilu lub TicketsScreen)
```

---

## Moduł 13: Przeglądy techniczne

**Rola:** ZARZĄDCA (CRUD) + wszyscy (GET)

### Ekrany

| Composable | API |
|------------|-----|
| `InspectionsListScreen` + `CreateInspectionDialog` | GET/POST/PUT/DELETE `/api/inspections` |

Stany: `Loading`, `Error`, `Success` w `InspectionsListViewModel`.

---

## Moduł 14: Dystrybucja dokumentów i PDF (admin)

**Rola:** ZARZĄDCA

### Ekrany

| Composable | API (DocumentApiService) |
|------------|--------------------------|
| `DocumentDistributionScreen` | POST `/api/admin/documents/rate-change`, POST `/api/admin/documents/annual-settlement` |
| `CommunityLogoScreen` | PATCH `/api/properties/{id}/logo` |

Dodatkowo: GET `/api/pdf/balances` — `ApartmentBalancesViewModel` / `PdfApiService`

### Nawigacja

```
Profile → DocumentRoutes.Distribution
Profile → SettingsRoutes.CommunityLogo
FinancesRoutes.Balances → PDF (opcjonalnie save)
```

---

## Moduł 15: Powiadomienia PUSH (konfiguracja globalna)

**Rola:** ZARZĄDCA

### Ekrany

| Composable | API | Uwagi |
|------------|-----|-------|
| `NotificationsScreen` | GET/PATCH `/api/admin/notifications/settings` | `NotificationsViewModel` — Loading/Error/Success |
| `NotificationSettingsScreen` | **brak API** | **[HARDKODOWANE]** `defaultToggles()`; TODO WIP zapis |

---

## Moduł 16: Urządzenia FCM

**Rola:** wspólny

### Wywołania API

| HTTP | Ścieżka | Skąd | JWT |
|------|---------|------|-----|
| POST | `/api/devices/register` | `AuthViewModel` po login | Tak |
| DELETE | `/api/devices/{token}` | `ResidentMainViewModel` logout | Tak |

`FcmTokenProvider` = `NoOpFcmTokenProvider` → token null → rejestracja często pomijana.

---

## Weryfikacja kompletności (Faza 2)

### Pokrycie ekranów `*Screen.kt` (30/30)

Wszystkie ekrany z Fazy 0 przypisane do modułów 1–16. Brak modułu „Pozostałe”.

Dialogi (8) udokumentowane w modułach nadrzędnych.

### Podsumowanie

| Metryka | Wartość |
|---------|---------|
| **Ekrany (`*Screen.kt`)** | **30** |
| **Dialogi / sheets** | **11** |
| **Wywołania API (unikalne ścieżki w Retrofit)** | **~52** |
| **ViewModels** | **29** |

### Ekrany per rola

| Rola | Ekrany (szacunek dostępu) |
|------|---------------------------|
| **Przed logowaniem** | 4 (auth) |
| **MIESZKANIEC** | ~15 (nav: tickets, finances, resolutions, announcements, profile + szczegóły) |
| **ZARZĄDCA** | ~28 (wszystkie oprócz create ticket; + admin ekrany z profilu/finances) |
| **KONSERWATOR** | ~4 (tickets lista/szczegóły, profile) |

### Lista unikalnych ścieżek API wywoływanych z frontendu

```
POST   api/auth/login
POST   api/auth/refresh
POST   api/auth/forgot-password
POST   api/auth/reset-password
POST   api/auth/accept-invitation
GET    api/tickets
GET    api/tickets/{id}
POST   api/tickets
PATCH  api/tickets/{id}/assign
PATCH  api/tickets/{id}/close
PATCH  api/tickets/{id}/reject
PATCH  api/tickets/{id}/start
PATCH  api/tickets/{id}/suspend
POST   api/tickets/{id}/completion
PATCH  api/tickets/{id}/status
GET    api/tickets/{id}/comments
POST   api/tickets/{id}/comments
POST   api/tickets/{id}/images
GET    api/tickets/{id}/images
GET    api/images/{id}
GET    api/categories
GET    api/users
GET    api/announcements
POST   api/announcements
PUT    api/announcements/{id}
DELETE api/announcements/{id}
GET    api/announcements/{id}/attachment
GET    api/apartments/{id}/transactions
POST   api/apartments/{id}/transactions
GET    api/admin/apartments/balances
POST   api/finance/import
GET    api/documents
GET    api/documents/{id}/download
GET    api/resolutions
POST   api/resolutions
GET    api/resolutions/{id}
POST   api/resolutions/{id}/vote
GET    api/resolutions/{id}/report
GET    api/buildings/tree
GET    api/properties
POST   api/properties
PUT    api/properties/{id}
PATCH  api/properties/{id}/logo
POST   api/buildings
PUT    api/buildings/{id}
DELETE api/buildings/{id}
POST   api/buildings/{id}/staircases
PUT    api/buildings/{id}/staircases/{id}
DELETE api/buildings/{id}/staircases/{id}
POST   api/staircases/{id}/apartments
PUT    api/staircases/{id}/apartments/{id}
DELETE api/staircases/{id}/apartments/{id}
GET    api/apartments/{id}/meters
POST   api/apartments/{id}/meters
PATCH  api/meters/{id}/deactivate
GET    api/apartments/{id}/meter-readings
POST   api/apartments/{id}/meter-readings
GET    api/meter-readings/{id}
PUT    api/meter-readings/{id}
DELETE api/meter-readings/{id}
GET    api/admin/users
POST   api/admin/users
PATCH  api/admin/users/{id}
PATCH  api/admin/users/{id}/deactivate
POST   api/admin/categories
PUT    api/admin/categories/{id}
PATCH  api/admin/categories/{id}/sla
PATCH  api/admin/categories/{id}/deactivate
GET    api/inspections
POST   api/inspections
PUT    api/inspections/{id}
DELETE api/inspections/{id}
POST   api/admin/documents/rate-change
POST   api/admin/documents/annual-settlement
GET    api/pdf/balances
POST   api/pdf/work-acceptance-protocol
GET    api/admin/notifications/settings
PATCH  api/admin/notifications/settings/{eventType}
POST   api/devices/register
DELETE api/devices/{token}
```

**Endpointy backendu NIE wywoływane z frontendu (przykłady):** brak dedykowanego `GET /api/users/me`; `FinancesService` mock niepodłączony.

---

**STATUS: Faza 1 i Faza 2 zakończone.**
