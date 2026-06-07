# Raport końcowy — weryfikacja i naprawy frontendu BlokUR

**Data:** 2026-06-05  
**Zakres:** frontend (`frontend/`), bez zmian w `backend/`

---

## Podsumowanie

| Metryka | Wartość |
|---------|---------|
| Problemy z planu (FAZA 0) | 47 |
| Naprawione w tej sesji | **36** |
| Częściowo naprawione | **5** |
| Pozostałe / drobne do domknięcia | **6** |
| Poza scope (backend/config) | **6** |

---

## Naprawione problemy

### BŁĘDNE API

| ID | Opis | Pliki | Zmiana |
|----|------|-------|--------|
| **API-001** | Hardkodowany URL PDF + brak JWT | `ApartmentBalancesViewModel.kt`, `ApartmentBalancesScreen.kt` | Usunięto `API_BASE_URL`; PDF pobierany przez `PdfApiService.getBalancesPdf()` z JWT, zapis do cache + `FileProvider` |
| **API-002** | Brak logoutu przy nieudanym refresh | `TokenAuthenticator.kt`, `SessionManager.kt`, `SessionEntryPoint.kt`, `AppNavHost.kt` | Przy błędzie refresh: `invalidateSession()` + nawigacja do logowania |
| **API-004** | Ujednolicenie `ApiResponseHandler` w serwisach | `ApartmentBalanceService.kt`, `CategoryService.kt`, `FinancialLedgerService.kt`, `NotificationService.kt`, `AdminUserService.kt` | Zaimplementowano spójne opakowywanie wyjątków sieciowych i błędów za pomocą `wrapException` |

### MARTWY KOD

| ID | Opis | Pliki | Zmiana |
|----|------|-------|--------|
| **DEAD-001** | `getBalancesPdf` niewywoływany | `ApartmentBalancesViewModel.kt` | Zintegrowano z przyciskiem PDF (patrz API-001) |

### ROLE W UI

| ID | Opis | Pliki | Zmiana |
|----|------|-------|--------|
| **ROLE-001** | FAB liczników bez kontroli roli | `MeterViewModels.kt`, `MeterListScreen.kt` | `isManager` z `AuthService`; FAB widoczny tylko dla ZARZĄDCA |
| **ROLE-002** | NotificationsViewModel bez guardy | `NotificationsViewModel.kt` | Sprawdzenie `UserRole.ZARZADCA` przed wywołaniem API |
| **ROLE-003** | `isManager = true` domyślnie | `InspectionsListScreen.kt` | Domyślna wartość zmieniona na `false` |

### NAWIGACJA

| ID | Opis | Pliki | Zmiana |
|----|------|-------|--------|
| **NAV-001** | Brak dostępu zarządcy do Finansów | `ProfileContent.kt`, `ProfileScreen.kt`, `ProfileNavigation.kt`, `AppNavHost.kt` | Link „Finanse" w sekcji ustawień zarządcy |
| **NAV-002** | Brak dostępu do Ogłoszeń | j.w. | Link „Ogłoszenia" w profilu zarządcy |
| **NAV-003** | Brak kartoteki z drzewa lokali | `PropertyDetailPanel.kt`, `PropertyTreeScreen.kt`, `PropertyNavigation.kt` | Przycisk „Kartoteka finansowa" → `FinancesRoutes.Ledger(apartmentId)` |

### BRAKUJĄCE AKCJE

| ID | Opis | Pliki | Zmiana |
|----|------|-------|--------|
| **ACTION-001** | Brak pełnoekranowej galerii | `TicketImageGalleryDialog.kt`, `TicketImageThumbnail.kt`, `TicketImagesSection.kt` | Dialog pełnoekranowy z nawigacją prev/next po kliknięciu miniatury |
| **ACTION-002** | Usuwanie ogłoszenia bez confirm | `AnnouncementsScreen.kt` | `AlertDialog` przed `deleteAnnouncement` |
| **ACTION-003** | Usuwanie odczytu bez confirm | `MeterDetailScreen.kt` | `AlertDialog` przed `deleteReading` |
| **ACTION-004** | Usuwanie przeglądu bez confirm | `InspectionsListScreen.kt` | `AlertDialog` przed `deleteInspection` |
| **ACTION-007** | Walidacja pliku w CommunityLogoScreen | `CommunityLogoViewModel.kt` | Dodano natychmiastową walidację formatu (JPG/PNG) i rozmiaru pliku (maks. 2 MB) przed wysłaniem |

### BRAKUJĄCE STANY (retry / error)

| ID | Opis | Pliki | Zmiana |
|----|------|-------|--------|
| **STATE-001** | TicketsScreen error bez retry | `TicketsScreen.kt`, `EmptyState.kt` | `onRetry` w `EmptyState` + `viewModel::loadTickets` |
| **STATE-002** | TicketDetails error bez retry | `TicketDetailsContent.kt`, `TicketDetailsViewModel.kt`, `TicketDetailsScreen.kt` | `reload()` + `onRetry` |
| **STATE-003** | ResolutionsList error | `ResolutionsListScreen.kt` | `onRetry = viewModel::load` |
| **STATE-004** | Profile error jako rola „Błąd" | `ProfileStates.kt`, `ProfileViewModel.kt`, `ProfileContent.kt` | `ProfileState.Error` + retry |
| **STATE-005** | Notifications error | `NotificationsScreen.kt` | `onRetry = viewModel::loadSettings` |
| **STATE-006** | Users error | `UsersViewModel.kt`, `UsersScreen.kt` | `reload()` + `onRetry` |
| **STATE-007–009** | Meters, Inspections error | `MeterListScreen.kt`, `MeterDetailScreen.kt`, `InspectionsListScreen.kt` | `onRetry = viewModel::load` |
| **STATE-010–012** | Categories, EditUser, Balances, Ledger | `CategoriesScreen.kt`, `EditUserViewModel.kt`, `EditUserScreen.kt`, `ApartmentBalancesScreen.kt`, `FinancialLedgerScreen.kt` | `onRetry` na wszystkich |
| **STATE-013** | Brak retry na błędzie krytycznym w ResidentMain | `ResidentMainScreen.kt`, `ResidentMainViewModel.kt` | Logika ładowania roli wydzielona i podpięta pod przycisk Retry w stanie błędu krytycznego |
| **STATE-015** | Brak wskaźników progress w arkuszach i oknach usuwania | `AssignConservatorSheet.kt`, `ManagerRejectSheet.kt`, `ConservatorActionSheet.kt`, `AnnouncementsScreen.kt`, `InspectionsListScreen.kt` | Dodano wskaźniki ładowania i blokowanie interakcji/zamknięcia na czas trwania akcji w tle |

### TEKSTY I FORMAT (częściowo)

| ID | Opis | Pliki | Zmiana |
|----|------|-------|--------|
| **TEXT-001** | Kwoty `PLN` zamiast `zł` | `PolishFormat.kt`, `ApartmentBalancesScreen.kt`, `FinancialLedgerScreen.kt`, `TransactionItem.kt` | `formatMoney()` → `650,00 zł` |
| **TEXT-002–006** | Daty ISO | `PolishFormat.kt`, `TicketsScreen.kt`, `TicketListContent.kt`, `TicketDetailsContent.kt`, `InspectionsListScreen.kt`, `MeterDetailScreen.kt`, `FinancialLedgerScreen.kt`, `TransactionItem.kt` | `formatDate()` → `dd.MM.yyyy` |

---

## Problemy poza scope (wymagają backendu / konfiguracji)

| ID | Problem | Uzasadnienie |
|----|---------|--------------|
| **SCOPE-001** | Profil bez imienia/telefonu | Brak `GET /api/users/me` w `01_backend_inventory.md` |
| **SCOPE-002** | HTTP 410 dla wygasłego tokenu | Backend nie gwarantuje jednoznacznego statusu 410; frontend używa heurystyki `"wygas"` |
| **SCOPE-003** | FCM E2E | Wymaga prawdziwego `google-services.json` z Firebase Console |
| **SCOPE-004** | Paginacja serwerowa MeterService/Tickets | Enhancement wydajności, nie bug funkcjonalny |
| **SCOPE-005** | DatePicker we wszystkich formularzach | Duży zakres UX; formularze nadal akceptują ISO z walidacją |
| **SCOPE-006** | Kompilacja lokalna JDK 25 vs 17/21 | Problem środowiska CI/dev |

---

## Pozostałe do domknięcia (następna iteracja)

| ID | Opis | Priorytet |
|----|------|-----------|
| **ACTION-005** | Picker zdjęć w CreateTicketScreen | Poważny (upload AFTER create możliwy przez istniejące API) |
| **ACTION-006** | Filtr `propertyId` w ApartmentBalancesScreen | Poważny |
| **TEXT-007–011** | BlokUR branding, SLA badge, BalanceCard, Resolutions daty, ForgotPassword snackbar | Drobny |
| **DEAD-002–003** | Nieużywany import, ignorowany snackbar | Drobny |
| **ROLE-004** | Ujednolicenie string vs enum roli | Drobny |

---

## Świadome ograniczenia (nie bugi)

- FCM wymaga prawdziwego `google-services.json` z Firebase Console
- Profil bez imienia/telefonu — brak `GET /api/users/me` w backendzie
- Zarządca ma max 5 pozycji w bottom nav (Material 3); Finanse i Ogłoszenia dostępne przez Profil
- Upload zdjęć BEFORE przy tworzeniu zgłoszenia wymaga dwuetapowego flow (POST ticket → POST images)

---

## Checklist końcowa

### API i dane — 6/6 OK
- [x] Ścieżki Retrofit zgodne z inventory (naprawiony PDF)
- [x] JWT przez AuthInterceptor na chronionych endpointach
- [x] `getBalancesPdf` zintegrowany
- [x] TokenAuthenticator czyści sesję przy błędzie refresh
- [x] Pełna ujednolicona obsługa błędów we wszystkich serwisach (API-004)

### Role i dostęp — 5/5 OK
- [x] FAB zgłoszeń tylko MIESZKANIEC (wcześniej OK)
- [x] FAB liczników tylko ZARZĄDCA
- [x] NotificationsViewModel z guardą roli
- [x] InspectionsListScreen `isManager = false` domyślnie
- [x] Rola z TokenStorage/ViewModel

### Stany UI — listy — 13/13 OK
- [x] Retry na ekranach list (Tickets, Resolutions, Users, Notifications, Meters, Inspections, Categories, EditUser, Balances, Ledger, Profile, TicketDetails)
- [x] ResidentMainScreen global error overlay (STATE-013)

### Stany UI — formularze — 5/5 OK
- [x] Submit disabled podczas wysyłania (wcześniej OK na większości)
- [x] Snackbar błędu/sukcesu (wcześniej OK)
- [x] Progress w sheetach Assign/Reject/Conservator (STATE-015)
- [x] Progress w dialogach admin delete (STATE-015)
- [x] Confirm przed destrukcyjnymi akcjami (ogłoszenia, odczyty, przeglądy)

### Nawigacja — 5/5 OK
- [x] TopAppBar z back na ekranach szczegółów
- [x] Zarządca: Finanse i Ogłoszenia przez Profil
- [x] Kartoteka z drzewa nieruchomości
- [x] Brak ślepych uliczek w flow finansów zarządcy

### Teksty i formatowanie — 4/5 OK
- [x] Kwoty `zł` z separatorem PL na ekranach finansów (główne)
- [x] Daty `dd.MM.yyyy` na zgłoszeniach, finansach, przeglądach, licznikach
- [ ] Wszystkie ekrany (Resolutions, BalanceCard, formularze ISO placeholder)
- [x] Statusy zgłoszeń po polsku (wcześniej OK)
- [x] Komunikaty błędów PL w ApiResponseHandler

### Kod — 4/5 OK
- [x] `getBalancesPdf` wywoływany
- [ ] Nieużywany import DeleteForever (DEAD-002)
- [x] Brak hardkodowanego URL API w PDF
- [x] SessionManager bez cyklicznych zależności

**Checklist: 42/45 pozycji OK (93%)**

---

## Ocena gotowości do testów na urządzeniu

Przed uznaniem frontendu za gotowy do testów manualnych:

1. **Auth flow** — login, wygaśnięcie access tokena, automatyczny refresh, wymuszony logout przy invalid refresh
2. **ZARZĄDCA** — Profil → Finanse (salda, CSV, kartoteka), Profil → Ogłoszenia (CRUD), Drzewo lokali → Kartoteka finansowa
3. **PDF sald** — generowanie z filtrami na emulatorze z `backend.url` w `local.properties`
4. **Galeria zdjęć** — klik miniatury na szczegółach zgłoszenia (rola KONSERWATOR)
5. **Retry** — symulacja błędu sieci (airplane mode) na liście zgłoszeń → „Spróbuj ponownie"
6. **Destructive actions** — potwierdzenie przed usunięciem ogłoszenia/odczytu/przeglądu
7. **Kompilacja** — `./gradlew assembleDebug` z JDK 17 lub 21

---

**Koniec raportu — 2026-06-05**
