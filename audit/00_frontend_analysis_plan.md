# BlokUR Frontend — Plan analizy (Faza 0)

**Data:** 2026-06-05  
**Źródło:** rekurencyjny przegląd `frontend/` (Android, Kotlin, Jetpack Compose)  
**Pakiet główny:** `pl.edu.ur.blokur`  
**Moduł aplikacji:** `frontend/app/`

---

## Struktura katalogów `frontend/`

```
frontend/
├── app/src/main/java/pl/edu/ur/blokur/
│   ├── MainActivity.kt              # jedyna Activity
│   ├── AppNavHost.kt                # globalny NavHost
│   ├── dtos/                        # 14 plików modeli API
│   ├── services/                    # Retrofit ApiService + warstwa Service + DI
│   └── ui/
│       ├── components/              # współdzielone komponenty UI
│       ├── navigation/              # AppRoute (interfejs tras)
│       ├── theme/                   # Material theme
│       └── views/                   # feature modules (screens, viewmodels, navigation)
└── app/src/main/res/                # zasoby Android
```

**Stack UI:** Jetpack Compose (brak Fragmentów), Navigation Compose z `@Serializable` routes, Hilt DI, Retrofit + OkHttp, DataStore (tokeny).

**Brak:** klas `Repository` / `DataSource` — rolę repozytorium pełnią klasy `*Service.kt` opakowujące `*ApiService`.

---

## Inwentaryzacja klas (Faza 0)

### Activity

| Klasa | Opis |
|-------|------|
| `MainActivity` | `ComponentActivity`, `setContent { AppNavHost() }` |

Brak innych Activity / Fragmentów.

---

### Ekrany (`*Screen.kt`) — 30 plików

| Plik | Uwagi |
|------|--------|
| `auth/screens/LoginScreen.kt` | |
| `auth/screens/ForgotPasswordScreen.kt` | |
| `auth/screens/ResetPasswordScreen.kt` | |
| `auth/screens/AcceptInvitationScreen.kt` | |
| `main/screens/ResidentMainScreen.kt` | powłoka z bottom nav (wszystkie role) |
| `profile/screens/ProfileScreen.kt` | |
| `tickets/screens/TicketsScreen.kt` | |
| `tickets/screens/TicketDetailsScreen.kt` | |
| `tickets/screens/CreateTicketScreen.kt` | |
| `announcements/screens/AnnouncementsScreen.kt` | |
| `announcements/screens/CreateAnnouncementScreen.kt` | |
| `finances/screens/FinancesScreen.kt` | |
| `finances/screens/TransactionsScreen.kt` | |
| `finances/screens/DocumentsScreen.kt` | |
| `finances/screens/FinancialLedgerScreen.kt` | |
| `finances/screens/ApartmentBalancesScreen.kt` | |
| `finances/screens/CsvImportScreen.kt` | |
| `resolutions/screens/ResolutionsListScreen.kt` | |
| `resolutions/screens/ResolutionDetailScreen.kt` | |
| `properties/screens/PropertyTreeScreen.kt` | |
| `users/screens/UsersScreen.kt` | |
| `users/screens/EditUserScreen.kt` | |
| `categories/screens/CategoriesScreen.kt` | |
| `inspections/screens/InspectionsListScreen.kt` | |
| `meters/screens/MeterListScreen.kt` | |
| `meters/screens/MeterDetailScreen.kt` | |
| `documents/screens/DocumentDistributionScreen.kt` | |
| `settings/screens/NotificationSettingsScreen.kt` | |
| `settings/screens/CommunityLogoScreen.kt` | |
| `notifications/screens/NotificationsScreen.kt` | |

### Dialogi / arkusze (UI modalne — nie pełne trasy NavHost)

| Plik | Typ |
|------|-----|
| `users/screens/CreateUserDialog.kt` | Dialog |
| `users/screens/EditUserScreen.kt` | ekran (trasa w UsersNavigation) |
| `categories/screens/CategoryFormDialog.kt` | Dialog |
| `resolutions/screens/CreateResolutionDialog.kt` | Dialog |
| `inspections/screens/CreateInspectionDialog.kt` | Dialog |
| `meters/screens/CreateMeterDialog.kt` | Dialog |
| `meters/screens/CreateMeterReadingDialog.kt` | Dialog |
| `finances/screens/AddTransactionDialog.kt` | Dialog |
| `tickets/components/AssignConservatorSheet.kt` | Bottom sheet |
| `tickets/components/ManagerRejectSheet.kt` | Bottom sheet |
| `tickets/components/ConservatorActionSheet.kt` | Bottom sheet |
| `components/AlertDialog.kt` | współdzielony dialog |

### ViewModels (`@HiltViewModel`) — 29 klas

| Klasa | Plik |
|-------|------|
| `AuthViewModel` | `auth/viewmodels/AuthViewModel.kt` |
| `ForgotPasswordViewModel` | `auth/viewmodels/ForgotPasswordViewModel.kt` |
| `ResetPasswordViewModel` | `auth/viewmodels/ResetPasswordViewModel.kt` |
| `AcceptInvitationViewModel` | `auth/viewmodels/AcceptInvitationViewModel.kt` |
| `ResidentMainViewModel` | `main/viewmodels/ResidentMainViewModel.kt` |
| `ProfileViewModel` | `profile/viewmodels/ProfileViewModel.kt` |
| `TicketsViewModel` | `tickets/viewmodels/TicketsViewModel.kt` |
| `TicketDetailsViewModel` | `tickets/viewmodels/TicketDetailsViewModel.kt` |
| `CreateTicketViewModel` | `tickets/viewmodels/CreateTicketViewModel.kt` |
| `AnnouncementsViewModel` | `announcements/viewmodels/AnnouncementsViewModel.kt` |
| `CreateAnnouncementViewModel` | `announcements/viewmodels/CreateAnnouncementViewModel.kt` |
| `FinancesViewModel` | `finances/viewmodels/FinancesViewModel.kt` |
| `FinancialLedgerViewModel` | `finances/viewmodels/FinancialLedgerViewModel.kt` |
| `ApartmentBalancesViewModel` | `finances/viewmodels/ApartmentBalancesViewModel.kt` |
| `CsvImportViewModel` | `finances/viewmodels/CsvImportViewModel.kt` |
| `ResolutionsListViewModel` | `resolutions/viewmodels/ResolutionViewModels.kt` |
| `ResolutionDetailViewModel` | `resolutions/viewmodels/ResolutionViewModels.kt` |
| `PropertyTreeViewModel` | `properties/viewmodels/PropertyTreeViewModel.kt` |
| `UsersViewModel` | `users/viewmodels/UsersViewModel.kt` |
| `EditUserViewModel` | `users/viewmodels/EditUserViewModel.kt` |
| `CategoriesViewModel` | `categories/viewmodels/CategoriesViewModel.kt` |
| `InspectionsListViewModel` | `inspections/viewmodels/InspectionsViewModels.kt` |
| `MeterListViewModel` | `meters/viewmodels/MeterViewModels.kt` |
| `MeterDetailViewModel` | `meters/viewmodels/MeterViewModels.kt` |
| `DocDistributionViewModel` | `documents/viewmodels/DocDistributionViewModel.kt` |
| `NotificationSettingsViewModel` | `settings/viewmodels/NotificationSettingsViewModel.kt` |
| `CommunityLogoViewModel` | `settings/viewmodels/CommunityLogoViewModel.kt` |
| `NotificationsViewModel` | `notifications/viewmodels/NotificationsViewModel.kt` |

---

### Warstwa sieciowa — Retrofit `*ApiService` (16 interfejsów)

| Interfejs | Główne ścieżki API |
|-----------|-------------------|
| `AuthApiService` | `/api/auth/*` (login, refresh, forgot, reset, accept-invitation) |
| `TicketApiService` | `/api/tickets`, `/api/categories`, `/api/users` |
| `TicketCommentApiService` | `/api/tickets/{id}/comments` |
| `TicketImageApiService` | `/api/tickets/{id}/images`, `/api/images/{id}` |
| `CategoryApiService` | `/api/admin/categories` |
| `AdminUserApiService` | `/api/admin/users` |
| `PropertyApiService` | `/api/properties`, `/api/buildings`, `/api/staircases` |
| `FinancialApiService` | `/api/apartments/{id}/transactions`, `/api/admin/apartments/balances`, `/api/finance/import` |
| `UserDocumentApiService` | `/api/documents` |
| `DocumentApiService` | `/api/admin/documents/*`, `/api/properties/{id}/logo`, `/api/pdf/*` |
| `AnnouncementApiService` | `/api/announcements` |
| `ResolutionApiService` | `/api/resolutions` |
| `InspectionApiService` | `/api/inspections` |
| `MeterApiService` | `/api/apartments/{id}/meters`, `/api/meter-readings` |
| `NotificationApiService` | `/api/admin/notifications/settings` |
| `DeviceApiService` | `/api/devices` |

**JWT:** klient `"main"` — `AuthInterceptor` + `TokenAuthenticator` (`NetworkModule`). Klient `"auth"` / `"bare"` — bez JWT (logowanie, refresh).

---

### Warstwa danych — `*Service` (repozytoria aplikacyjne, 20 klas)

| Klasa | ApiService / źródło |
|-------|---------------------|
| `AuthService` | `AuthApiService`, `TokenStorage` |
| `TokenStorage` | DataStore (access, refresh, role) |
| `TicketService` | `TicketApiService` |
| `TicketCommentService` | `TicketCommentApiService` |
| `TicketImageService` | `TicketImageApiService` |
| `CategoryService` | `CategoryApiService`, `TicketApiService` (GET categories) |
| `AdminUserService` | `AdminUserApiService` |
| `PropertyService` | `PropertyApiService` |
| `FinancialLedgerService` | `FinancialApiService` |
| `ApartmentBalanceService` | `FinancialApiService` |
| `FinancesService` | **[HARDKODOWANE]** — mock, bez Retrofit |
| `UserDocumentService` | `UserDocumentApiService` |
| `AnnouncementService` | `AnnouncementApiService` |
| `ResolutionService` | `ResolutionApiService` |
| `InspectionService` | `InspectionApiService` |
| `MeterService` | `MeterApiService` |
| `NotificationService` | `NotificationApiService` |
| `DeviceService` | `DeviceApiService`, `FcmTokenProvider` |

**DI / infrastruktura:** `NetworkModule`, `FcmModule`, `AuthInterceptor`, `TokenAuthenticator`, `FcmTokenProvider`, `NoOpFcmTokenProvider`

**Bezpośrednie użycie ApiService w ViewModel:** `DocDistributionViewModel` → `DocumentApiService`; `CommunityLogoViewModel` → `DocumentApiService`

---

### Nawigacja (15 plików `*Navigation.kt` + `AppNavHost`)

| Plik | Trasy (sealed routes) |
|------|----------------------|
| `AppNavHost.kt` | root: `AuthRoutes` → `MainRoutes.Main` + nested graphs |
| `auth/AuthNavigation.kt` | Login, ForgotPassword, ResetPassword, AcceptInvitation |
| `main/MainNavigation.kt` | Main + bottom NavHost |
| `profile/ProfileNavigation.kt` | Profile Main |
| `tickets/TicketsNavigation.kt` | List, Details, Create |
| `announcements/AnnouncementsNavigation.kt` | Main, Create |
| `finances/FinancesNavigation.kt` | Main, Transactions, Documents, Ledger, Balances, CsvImport |
| `resolutions/ResolutionsNavigation.kt` | List, Detail |
| `properties/PropertyNavigation.kt` | Tree |
| `users/UsersNavigation.kt` | List, Edit |
| `categories/CategoriesNavigation.kt` | List |
| `inspections/InspectionsNavigation.kt` | List |
| `meters/MetersNavigation.kt` | List(apartmentId), Detail |
| `documents/DocumentsNavigation.kt` | Distribution |
| `settings/SettingsNavigation.kt` | Notifications, CommunityLogo |
| `notifications/NotificationsNavigation.kt` | Settings (NotificationsScreen) |

**Role a bottom navigation** (`main/utils/Data.kt`):

| Rola | Zakładki dolne |
|------|----------------|
| MIESZKANIEC | Zgłoszenia, Finanse, Uchwały, Ogłoszenia, Profil |
| ZARZĄDCA | Zgłoszenia, Lokale, Uchwały, Użytkownicy, Profil |
| KONSERWATOR | Zgłoszenia, Profil |

Dodatkowe ekrany ZARZĄDCY (spoza bottom nav): Kategorie, Przeglądy, Powiadomienia, Dystrybucja dokumentów, Logo — dostęp z **Profilu** (`ProfileScreen` → `AppNavHost` callbacks).

---

### Modele danych (`dtos/`) — 14 plików

`AdminUserDtos.kt`, `AnnouncementDtos.kt`, `AuthDtos.kt`, `CategoryDtos.kt`, `DeviceDtos.kt`, `FinancesDtos.kt`, `InspectionDtos.kt`, `MeterDtos.kt`, `NotificationDtos.kt`, `PropertyDtos.kt`, `ResolutionDtos.kt`, `TicketDtos.kt`, `TicketMediaDtos.kt`, `UserDtos.kt`

**Uwaga:** `UserProfileDto` dokumentuje `GET /api/users/me` — endpoint **nie istnieje** w backendzie; `ProfileViewModel` używa placeholderów.

---

## Plan modułów funkcjonalnych (Faza 1)

---

### Moduł 1: Infrastruktura aplikacji i sieć

**Rola:** wspólny

**Pliki:**
- `MainActivity.kt`, `AppNavHost.kt`
- `services/NetworkModule.kt`, `AuthInterceptor.kt`, `TokenAuthenticator.kt`, `TokenStorage.kt`
- `services/FcmModule.kt`, `FcmTokenProvider.kt`
- `ui/navigation/AppRoute.kt` (jeśli istnieje), `ui/theme/*`, `ui/components/*`

---

### Moduł 2: Autentykacja i onboarding

**Rola:** wspólny (przed zalogowaniem)

**Pliki:**
- Ekrany: `LoginScreen`, `ForgotPasswordScreen`, `ResetPasswordScreen`, `AcceptInvitationScreen`
- Composable: `AcceptInvitationForm`
- ViewModels: `AuthViewModel`, `ForgotPasswordViewModel`, `ResetPasswordViewModel`, `AcceptInvitationViewModel`
- `AuthNavigation.kt`, `auth/utils/AuthStates.kt`
- `AuthApiService.kt`, `AuthService.kt`
- `dtos/AuthDtos.kt`

---

### Moduł 3: Powłoka główna i nawigacja roli

**Rola:** wspólny (po zalogowaniu)

**Pliki:**
- `ResidentMainScreen.kt`, `ResidentMainViewModel.kt`, `MainNavigation.kt`
- `main/contents/BottomNavBar.kt`, `main/utils/Data.kt` (`navItemsForRole`, `NavBarOption`)
- Fragment `AppNavHost` (sekcja `mainGraph`)

---

### Moduł 4: Profil użytkownika

**Rola:** wspólny (menu różne per rola)

**Pliki:**
- `ProfileScreen.kt`, `ProfileContent.kt`, `ProfileViewModel.kt`, `ProfileNavigation.kt`
- `profile/utils/ProfileStates.kt`
- `dtos/UserDtos.kt` (kontekst)

---

### Moduł 5: Zgłoszenia serwisowe (tickets)

**Rola:** MIESZKANIEC + ZARZĄDCA + KONSERWATOR

**Pliki:**
- Ekrany: `TicketsScreen`, `TicketDetailsScreen`, `CreateTicketScreen`
- ViewModels: `TicketsViewModel`, `TicketDetailsViewModel`, `CreateTicketViewModel`
- `TicketsNavigation.kt`, `tickets/utils/*`, `tickets/contents/*`, `tickets/components/*`
- Serwisy: `TicketService.kt`, `TicketCommentService`, `TicketImageService` (`TicketMediaServices.kt`)
- API: `TicketApiService.kt`, `TicketCommentApiService.kt`, `TicketImageApiService.kt`
- `dtos/TicketDtos.kt`, `dtos/TicketMediaDtos.kt`

---

### Moduł 6: Ogłoszenia

**Rola:** MIESZKANIEC (odczyt) + ZARZĄDCA (CRUD)

**Pliki:**
- Ekrany: `AnnouncementsScreen`, `CreateAnnouncementScreen`
- Composable: `SampleAnnouncementsContent`, `CreateAnnouncementContent`
- ViewModels: `AnnouncementsViewModel`, `CreateAnnouncementViewModel`
- `AnnouncementsNavigation.kt`
- `AnnouncementService.kt`, `AnnouncementApiService.kt`, `dtos/AnnouncementDtos.kt`

---

### Moduł 7: Finanse i dokumenty mieszkańca

**Rola:** MIESZKANIEC (głównie) + ZARZĄDCA (rozszerzenia)

**Podmoduł 7a — przegląd finansów (mock + API):**
- `FinancesScreen`, `TransactionsScreen`, `DocumentsScreen`, `FinancesViewModel`
- `FinancesService.kt` **[HARDKODOWANE]**
- `FinancesNavigation.kt` (część tras)

**Podmoduł 7b — kartoteka i salda (API):**
- `FinancialLedgerScreen`, `FinancialLedgerViewModel`, `FinancialLedgerService`
- `ApartmentBalancesScreen`, `ApartmentBalancesViewModel`, `ApartmentBalanceService`
- `CsvImportScreen`, `CsvImportViewModel`
- `AddTransactionDialog`
- `FinancialApiService.kt`, `dtos/FinancesDtos.kt`

**Podmoduł 7c — dokumenty użytkownika:**
- `DocumentsScreen`, `DocumentsContent`, `DocumentItem`
- `UserDocumentService.kt`, `UserDocumentApiService.kt`

---

### Moduł 8: Uchwały i głosowania

**Rola:** MIESZKANIEC + ZARZĄDCA

**Pliki:**
- `ResolutionsListScreen`, `ResolutionDetailScreen`, `CreateResolutionDialog`
- `ResolutionsListViewModel`, `ResolutionDetailViewModel`
- `ResolutionsNavigation.kt`
- `ResolutionService.kt`, `ResolutionApiService.kt`, `dtos/ResolutionDtos.kt`

---

### Moduł 9: Nieruchomości — drzewo budynków

**Rola:** ZARZĄDCA

**Pliki:**
- `PropertyTreeScreen`, `PropertyTreeView`, `PropertyTreeViewModel`
- `PropertyNavigation.kt`, `PropertyTreeStates.kt`
- `PropertyService.kt`, `PropertyApiService.kt`, `dtos/PropertyDtos.kt`
- Nawigacja do liczników: `MetersNavigation.kt` (wejście z drzewa)

---

### Moduł 10: Liczniki i odczyty

**Rola:** ZARZĄDCA (CRUD) + KONSERWATOR/MIESZKANIEC (odczyt — wg backendu)

**Pliki:**
- `MeterListScreen`, `MeterDetailScreen`
- `CreateMeterDialog`, `CreateMeterReadingDialog`
- `MeterListViewModel`, `MeterDetailViewModel`
- `MetersNavigation.kt`
- `MeterService.kt`, `MeterApiService.kt`, `dtos/MeterDtos.kt`

---

### Moduł 11: Użytkownicy (administracja)

**Rola:** ZARZĄDCA

**Pliki:**
- `UsersScreen`, `EditUserScreen`, `CreateUserDialog`
- `UsersViewModel`, `EditUserViewModel`
- `UsersNavigation.kt`
- `AdminUserService.kt`, `AdminUserApiService.kt`, `dtos/AdminUserDtos.kt`

---

### Moduł 12: Kategorie zgłoszeń (SLA)

**Rola:** ZARZĄDCA

**Pliki:**
- `CategoriesScreen`, `CategoryFormDialog`
- `CategoriesViewModel`
- `CategoriesNavigation.kt`
- `CategoryService.kt`, `CategoryApiService.kt`, `dtos/CategoryDtos.kt`
- Odczyt aktywnych kategorii: `TicketApiService.getCategories()` (używane przy tworzeniu zgłoszenia)

---

### Moduł 13: Przeglądy techniczne

**Rola:** ZARZĄDCA (CRUD) + pozostałe role (odczyt filtrowany)

**Pliki:**
- `InspectionsListScreen`, `CreateInspectionDialog`
- `InspectionsListViewModel`
- `InspectionsNavigation.kt`
- `InspectionService.kt`, `InspectionApiService.kt`, `dtos/InspectionDtos.kt`

---

### Moduł 14: Dystrybucja dokumentów i PDF (admin)

**Rola:** ZARZĄDCA

**Pliki:**
- `DocumentDistributionScreen`, `DocDistributionViewModel`
- `DocumentsNavigation.kt`
- `DocumentApiService.kt` (rate-change, annual-settlement, pdf, logo)
- Powiązane: `CommunityLogoScreen`, `CommunityLogoViewModel` → `SettingsNavigation`

---

### Moduł 15: Ustawienia powiadomień PUSH

**Rola:** ZARZĄDCA

**Pliki:**
- `NotificationSettingsScreen`, `NotificationSettingsViewModel`
- `NotificationsScreen`, `NotificationsViewModel`
- `SettingsNavigation.kt`, `NotificationsNavigation.kt`
- `NotificationService.kt`, `NotificationApiService.kt`, `dtos/NotificationDtos.kt`

---

### Moduł 16: Urządzenia FCM (push token)

**Rola:** wspólny (po zalogowaniu)

**Pliki:**
- `DeviceService.kt`, `DeviceApiService.kt`, `dtos/DeviceDtos.kt`
- Integracja: `AuthService` / logowanie, `FcmModule`

---

## Kolejność analizy w Fazie 1

| Krok | Moduł | Rola |
|------|-------|------|
| 1 | Infrastruktura i sieć | wspólny |
| 2 | Autentykacja | wspólny |
| 3 | Powłoka główna | wspólny |
| 4 | Profil | wspólny |
| 5 | Zgłoszenia | M+Z+K |
| 6 | Ogłoszenia | M+Z |
| 7 | Finanse i dokumenty | M+Z |
| 8 | Uchwały | M+Z |
| 9 | Nieruchomości | Z |
| 10 | Liczniki | Z+K+M |
| 11 | Użytkownicy | Z |
| 12 | Kategorie | Z |
| 13 | Przeglądy | Z+wszyscy |
| 14 | Dystrybucja dokumentów / PDF | Z |
| 15 | Powiadomienia PUSH | Z |
| 16 | Urządzenia FCM | wspólny |

**Plik wynikowy Fazy 1:** `audit/02_frontend_inventory.md`

**Faza 2:** weryfikacja pokrycia ekranów + zestawienie wywołań API.

---

## Statystyki wstępne (Faza 0)

| Kategoria | Liczba |
|-----------|--------|
| Activity | 1 |
| Ekrany `*Screen.kt` | 30 |
| Dialogi / bottom sheets | 11 |
| ViewModels `@HiltViewModel` | 29 |
| Retrofit ApiService | 16 |
| Service (warstwa danych) | 18 (+ TokenStorage) |
| Pliki nawigacji | 16 (w tym AppNavHost) |
| Pliki DTO | 14 |
| Szacowane wywołania API (unikalne ścieżki w ApiService) | ~55 |

---

## Uwagi wstępne do Fazy 1 (fakty z kodu)

| Obszar | Stan |
|--------|------|
| `FinancesService` | komentarz + mock — **[HARDKODOWANE]** |
| `ProfileViewModel` | placeholder — brak API profilu |
| `NotificationSettingsViewModel` | `TODO WIP` — zapis do backendu |
| `UserProfileDto` | dokumentuje nieistniejący endpoint `/api/users/me` |
| `GET /api/categories` | w `TicketApiService` (nie `CategoryApiService`) |

---

**STATUS: Faza 0 zakończona — oczekiwanie na akceptację planu przed Fazą 1.**
