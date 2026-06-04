# Plan analizy frontendu BlokUR — FAZA 0

> Wygenerowano: 2026-06-04  
> Łączna liczba plików .kt: **175**  
> Technologia: Jetpack Compose + Hilt DI + Retrofit2 + Navigation Compose  
> Nawigacja: Type-safe Navigation Compose (sealed interface + @Serializable routes)

---

## Globalna architektura

```
pl.edu.ur.blokur/
├── dtos/               ← modele danych (request/response DTOs) — 13 plików
├── services/           ← Retrofit API interfaces + "Repository-like" Services — 26 plików
├── ui/
│   ├── components/     ← współdzielone komponenty UI (TopBar, Buttons, etc.) — 9 plików
│   ├── navigation/     ← AppRoute (marker interface)
│   ├── theme/          ← Color, Type, Shape, Theme — 5 plików
│   └── views/          ← moduły funkcjonalne pogrupowane tematycznie
│       ├── auth/
│       ├── main/
│       ├── announcements/
│       ├── categories/
│       ├── documents/
│       ├── finances/
│       ├── inspections/
│       ├── meters/
│       ├── notifications/
│       ├── profile/
│       ├── properties/
│       ├── resolutions/
│       ├── settings/
│       ├── tickets/
│       └── users/
```

### Role użytkowników:
- **MIESZKANIEC** — dostęp do: Zgłoszeń, Finansów, Uchwał, Ogłoszeń, Profilu
- **ZARZADCA** — dostęp do: Zgłoszeń, Lokali (+ Liczniki), Uchwał, Użytkowników, Profilu  
  (+ przez Profil: Kategorie, Przeglądy, Ustawienia powiadomień, Logo wspólnoty, Dystrybucja dokumentów)
- **KONSERWATOR** — dostęp do: Zgłoszeń, Profilu

---

## Plan modułów do analizy

---

### Moduł 1: Uwierzytelnienie (Auth)
**Rola:** wspólny (wszyscy użytkownicy)  
**Pliki:**
- `ui/views/auth/AuthNavigation.kt` — routes: Login, ForgotPassword, ResetPassword(token)
- `ui/views/auth/screens/LoginScreen.kt`
- `ui/views/auth/screens/ForgotPasswordScreen.kt`
- `ui/views/auth/screens/ResetPasswordScreen.kt`
- `ui/views/auth/contents/LoginForm.kt`
- `ui/views/auth/contents/ForgotPasswordForm.kt`
- `ui/views/auth/contents/ResetPasswordForm.kt`
- `ui/views/auth/utils/AuthStates.kt`
- `ui/views/auth/viewmodels/AuthViewModel.kt`
- `ui/views/auth/viewmodels/ForgotPasswordViewModel.kt`
- `ui/views/auth/viewmodels/ResetPasswordViewModel.kt`
- `services/AuthApiService.kt`
- `services/AuthService.kt`
- `services/AuthInterceptor.kt`
- `services/TokenAuthenticator.kt`
- `services/TokenStorage.kt`
- `dtos/AuthDtos.kt` (LoginRequestDto, AuthResponseDto, RefreshTokenRequestDto, TokenPairResponseDto, ForgotPasswordRequestDto, ResetPasswordRequestDto, MessageResponseDto, AcceptInvitationRequestDto, AcceptInvitationResponseDto)

---

### Moduł 2: Nawigacja główna (Main Shell)
**Rola:** wspólny (shell renderuje per rola)  
**Pliki:**
- `ui/views/main/screens/ResidentMainScreen.kt`
- `ui/views/main/contents/BottomNavBar.kt`
- `ui/views/main/utils/Data.kt` (NavBarOption, bottomNavItems, zarzadcaNavItems, konserwatorNavItems)
- `ui/views/main/utils/ResidentMainStates.kt` (ResidentMainState, ResidentMainEvent)
- `ui/views/main/viewmodels/ResidentMainViewModel.kt`
- `services/DeviceApiService.kt`
- `services/DeviceService.kt`
- `services/FcmModule.kt`
- `services/FcmTokenProvider.kt`
- `services/NetworkModule.kt`
- `dtos/DeviceDtos.kt` (DeviceRegistrationRequestDto)

---

### Moduł 3: Zgłoszenia (Tickets)
**Rola:** MIESZKANIEC (tworzenie) / ZARZADCA (zarządzanie) / KONSERWATOR (realizacja)  
**Pliki:**
- `ui/views/tickets/TicketsNavigation.kt` — routes: List, Details(ticketId), Create
- `ui/views/tickets/screens/TicketsScreen.kt`
- `ui/views/tickets/screens/TicketDetailsScreen.kt`
- `ui/views/tickets/screens/CreateTicketScreen.kt`
- `ui/views/tickets/contents/TicketListContent.kt`
- `ui/views/tickets/contents/TicketDetailsContent.kt`
- `ui/views/tickets/contents/CreateTicketFormContent.kt`
- `ui/views/tickets/components/AssignConservatorSheet.kt`
- `ui/views/tickets/components/ConservatorActionSheet.kt`
- `ui/views/tickets/components/ManagerRejectSheet.kt`
- `ui/views/tickets/components/TicketCommentsSection.kt`
- `ui/views/tickets/components/TicketFilterPanel.kt`
- `ui/views/tickets/components/TicketImagesSection.kt`
- `ui/views/tickets/components/TicketListItem.kt`
- `ui/views/tickets/utils/TicketsStates.kt`
- `ui/views/tickets/utils/TicketUiMappers.kt`
- `ui/views/tickets/viewmodels/TicketsViewModel.kt`
- `ui/views/tickets/viewmodels/TicketDetailsViewModel.kt`
- `ui/views/tickets/viewmodels/CreateTicketViewModel.kt`
- `services/TicketApiService.kt`
- `services/TicketService.kt`
- `services/TicketCommentApiService.kt`
- `services/TicketImageApiService.kt`
- `services/TicketMediaServices.kt`
- `dtos/TicketDtos.kt` (TicketStatus, CategoryDto, CreateTicketRequest, ConservatorDto, TicketAssignRequest, TicketSummaryDto, TicketDetailDto, AppUserDto, TicketRejectRequest, TicketSuspendRequest, TicketCompletionRequest, TicketStatusChangeRequest)
- `dtos/TicketMediaDtos.kt` (TicketCommentDto, TicketCommentRequestDto, TicketImageDto)

---

### Moduł 4: Finanse (Finances)
**Rola:** MIESZKANIEC (podgląd własnych) / ZARZADCA (pełny dostęp + import CSV + salda)  
**Pliki:**
- `ui/views/finances/FinancesNavigation.kt` — routes: Main, Transactions, Documents, Ledger(apartmentId?), Balances, CsvImport
- `ui/views/finances/screens/FinancesScreen.kt`
- `ui/views/finances/screens/TransactionsScreen.kt`
- `ui/views/finances/screens/DocumentsScreen.kt`
- `ui/views/finances/screens/FinancialLedgerScreen.kt`
- `ui/views/finances/screens/ApartmentBalancesScreen.kt`
- `ui/views/finances/screens/CsvImportScreen.kt`
- `ui/views/finances/screens/AddTransactionDialog.kt`
- `ui/views/finances/contents/FinancesOverviewContent.kt`
- `ui/views/finances/contents/TransactionsContent.kt`
- `ui/views/finances/contents/DocumentsContent.kt`
- `ui/views/finances/components/BalanceCard.kt`
- `ui/views/finances/components/DocumentItem.kt`
- `ui/views/finances/components/TransactionItem.kt`
- `ui/views/finances/utils/FinancesStates.kt`
- `ui/views/finances/viewmodels/FinancesViewModel.kt`
- `ui/views/finances/viewmodels/FinancialLedgerViewModel.kt`
- `ui/views/finances/viewmodels/ApartmentBalancesViewModel.kt`
- `ui/views/finances/viewmodels/CsvImportViewModel.kt`
- `services/FinancialApiService.kt`
- `services/FinancesService.kt`
- `services/FinancialLedgerService.kt`
- `services/ApartmentBalanceService.kt`
- `dtos/FinancesDtos.kt`

---

### Moduł 5: Ogłoszenia (Announcements)
**Rola:** MIESZKANIEC (odczyt) / ZARZADCA (tworzenie)  
**Pliki:**
- `ui/views/announcements/AnnouncementsNavigation.kt`
- `ui/views/announcements/screens/AnnouncementsScreen.kt`
- `ui/views/announcements/contents/SampleAnnoucementsContent.kt`
- `ui/views/announcements/utils/AnnouncementsStates.kt`
- `ui/views/announcements/viewmodels/AnnouncementsViewModel.kt`
- `services/AnnouncementApiService.kt`
- `services/AnnouncementService.kt`
- `dtos/AnnouncementDtos.kt` (AnnouncementTargetType, AnnouncementDto, AnnouncementRequestDto)

---

### Moduł 6: Uchwały (Resolutions)
**Rola:** MIESZKANIEC (głosowanie) / ZARZADCA (tworzenie + wyniki)  
**Pliki:**
- `ui/views/resolutions/ResolutionsNavigation.kt` — routes: List, Detail(resolutionId)
- `ui/views/resolutions/screens/ResolutionsListScreen.kt`
- `ui/views/resolutions/screens/ResolutionDetailScreen.kt`
- `ui/views/resolutions/screens/CreateResolutionDialog.kt`
- `ui/views/resolutions/viewmodels/ResolutionViewModels.kt` (ResolutionsListViewModel, ResolutionDetailViewModel)
- `services/ResolutionApiService.kt`
- `services/ResolutionService.kt`
- `dtos/ResolutionDtos.kt` (ResolutionDto, ResolutionDetailDto, ResolutionOptionDto, ResolutionOptionResultDto, CreateResolutionRequest, CastVoteRequest)

---

### Moduł 7: Lokale i Liczniki (Properties + Meters)
**Rola:** ZARZADCA (lokale — drzewo nieruchomości) / MIESZKANIEC (liczniki własnego lokalu)  
**Pliki:**
- `ui/views/properties/PropertyNavigation.kt` — routes: Tree
- `ui/views/properties/screens/PropertyTreeScreen.kt`
- `ui/views/properties/contents/PropertyTreeView.kt`
- `ui/views/properties/contents/PropertyDetailPanel.kt`
- `ui/views/properties/utils/PropertyTreeStates.kt`
- `ui/views/properties/viewmodels/PropertyTreeViewModel.kt`
- `ui/views/meters/MetersNavigation.kt` — routes: List(apartmentId), Detail(apartmentId, meterId, serialNumber, mediumType)
- `ui/views/meters/screens/MeterListScreen.kt`
- `ui/views/meters/screens/MeterDetailScreen.kt`
- `ui/views/meters/screens/CreateMeterDialog.kt`
- `ui/views/meters/screens/CreateMeterReadingDialog.kt`
- `ui/views/meters/viewmodels/MeterViewModels.kt` (MeterListViewModel, MeterDetailViewModel)
- `services/PropertyApiService.kt`
- `services/PropertyService.kt`
- `services/MeterApiService.kt`
- `services/MeterService.kt`
- `dtos/PropertyDtos.kt`
- `dtos/MeterDtos.kt`

---

### Moduł 8: Użytkownicy (Users)
**Rola:** ZARZADCA  
**Pliki:**
- `ui/views/users/UsersNavigation.kt` — routes: List
- `ui/views/users/screens/UsersScreen.kt`
- `ui/views/users/screens/CreateUserDialog.kt`
- `ui/views/users/viewmodels/UsersViewModel.kt`
- `services/AdminUserService.kt`
- `dtos/AdminUserDtos.kt` (AdminUserDto, CreateAdminUserRequest, UpdateAdminUserRequest)

---

### Moduł 9: Kategorie (Categories)
**Rola:** ZARZADCA  
**Pliki:**
- `ui/views/categories/CategoriesNavigation.kt` — routes: List
- `ui/views/categories/screens/CategoriesScreen.kt`
- `ui/views/categories/screens/CategoryFormDialog.kt`
- `ui/views/categories/viewmodels/CategoriesViewModel.kt`
- `services/CategoryApiService.kt`
- `services/CategoryService.kt`
- `dtos/CategoryDtos.kt` (AdminCategoryDto, CategoryCreateRequest, SlaRequest)

---

### Moduł 10: Przeglądy Techniczne (Inspections)
**Rola:** ZARZADCA (tworzenie/zarządzanie) / KONSERWATOR (realizacja)  
**Pliki:**
- `ui/views/inspections/screens/InspectionsListScreen.kt`
- `ui/views/inspections/screens/CreateInspectionDialog.kt`
- `ui/views/inspections/viewmodels/InspectionsViewModels.kt`
- `services/InspectionApiService.kt`
- `services/InspectionService.kt`
- `dtos/InspectionDtos.kt`

---

### Moduł 11: Powiadomienia (Notifications)
**Rola:** wspólny (wszyscy po zalogowaniu)  
**Pliki:**
- `ui/views/notifications/NotificationsNavigation.kt`
- `ui/views/notifications/screens/NotificationsScreen.kt`
- `ui/views/notifications/viewmodels/NotificationsViewModel.kt`
- `services/NotificationApiService.kt`
- `services/NotificationService.kt`
- `dtos/NotificationDtos.kt`

---

### Moduł 12: Profil użytkownika (Profile)
**Rola:** wspólny (każda rola ma profil, ZARZADCA ma dodatkowe opcje)  
**Pliki:**
- `ui/views/profile/ProfileNavigation.kt` — routes: Main
- `ui/views/profile/screens/ProfileScreen.kt`
- `ui/views/profile/contents/ProfileContent.kt`
- `ui/views/profile/utils/ProfileStates.kt`
- `ui/views/profile/viewmodels/ProfileViewModel.kt`

---

### Moduł 13: Ustawienia (Settings)
**Rola:** ZARZADCA  
**Pliki:**
- `ui/views/settings/SettingsNavigation.kt` — routes: Notifications, CommunityLogo
- `ui/views/settings/screens/NotificationSettingsScreen.kt`
- `ui/views/settings/screens/CommunityLogoScreen.kt`
- `ui/views/settings/viewmodels/NotificationSettingsViewModel.kt`
- `ui/views/settings/viewmodels/CommunityLogoViewModel.kt`

---

### Moduł 14: Dokumenty / Dystrybucja (Documents)
**Rola:** ZARZADCA (dystrybucja) / MIESZKANIEC (podgląd własnych dokumentów — przez Finanse)  
**Pliki:**
- `ui/views/documents/DocumentsNavigation.kt`
- `ui/views/documents/screens/DocumentDistributionScreen.kt`
- `ui/views/documents/viewmodels/DocDistributionViewModel.kt`
- `services/DocumentApiService.kt`
- `services/UserDocumentApiService.kt`
- `services/UserDocumentService.kt`
- `dtos/DeviceDtos.kt` (UserDocumentDto — reużyty w FinancesDtos)

---

### Moduł 15: Infrastruktura sieciowa (Network Infrastructure)
**Rola:** wspólny (warstwa techniczna)  
**Pliki:**
- `services/NetworkModule.kt`
- `services/AuthInterceptor.kt`
- `services/TokenAuthenticator.kt`
- `services/TokenStorage.kt`
- `services/FcmModule.kt`
- `services/FcmTokenProvider.kt`

---

## Podsumowanie wstępne

| Moduł | Ekrany (główne) | Rola |
|-------|----------------|------|
| 1. Auth | LoginScreen, ForgotPasswordScreen, ResetPasswordScreen | wspólny |
| 2. Main Shell | ResidentMainScreen | wspólny |
| 3. Tickets | TicketsScreen, TicketDetailsScreen, CreateTicketScreen | wspólny |
| 4. Finances | FinancesScreen, TransactionsScreen, DocumentsScreen, FinancialLedgerScreen, ApartmentBalancesScreen, CsvImportScreen | MIESZKANIEC / ZARZADCA |
| 5. Announcements | AnnouncementsScreen | MIESZKANIEC / ZARZADCA |
| 6. Resolutions | ResolutionsListScreen, ResolutionDetailScreen | MIESZKANIEC / ZARZADCA |
| 7. Properties + Meters | PropertyTreeScreen, MeterListScreen, MeterDetailScreen | ZARZADCA / MIESZKANIEC |
| 8. Users | UsersScreen | ZARZADCA |
| 9. Categories | CategoriesScreen | ZARZADCA |
| 10. Inspections | InspectionsListScreen | ZARZADCA / KONSERWATOR |
| 11. Notifications | NotificationsScreen | wspólny |
| 12. Profile | ProfileScreen | wspólny |
| 13. Settings | NotificationSettingsScreen, CommunityLogoScreen | ZARZADCA |
| 14. Documents | DocumentDistributionScreen | ZARZADCA |
| 15. Infrastructure | — | techniczny |

**Łącznie ekranów (wstępnie):** ~25 ekranów głównych + dialogi  
**Łącznie API services (interfejsy Retrofit):** 15 interfejsów + 12 klas serwisowych

---

*Status: oczekuje na akceptację przed przejściem do Fazy 1*
