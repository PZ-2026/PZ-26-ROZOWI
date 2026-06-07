FRONT-023 — Wspólny mapper błędów HTTP
Status: ⚠️ CZĘŚCIOWO
Pliki zweryfikowane: 
- frontend/app/src/main/java/pl/edu/ur/blokur/services/ApiResponseHandler.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/PropertyService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/NotificationService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketDetailsViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketMediaServices.kt

Wywołania API:
GET /api/tickets → TicketApiService.getTickets() → OK
POST /api/tickets → TicketApiService.createTicket() → OK
PATCH /api/tickets/{id}/status → TicketApiService.changeStatus() → OK
GET /api/categories → TicketApiService.getCategories() → OK

Problem: 
- ApiResponseHandler istnieje i mapuje 400/401/403/404/409/422/423/429/5xx na komunikaty PL (ok).
- Wiele serwisów używa ApiResponseHandler.requireSuccess() (TicketService, PropertyService, NotificationService), a ViewModel używa ApiResponseHandler.mapHttpError() — integracja widoczna.
- Kilka serwisów (TicketCommentService, TicketImageService) stosuje manualne isSuccessful + własne handleError zamiast bezpośredniego requireSuccess(), ale nadal nie ignorują błędów (rzucają wyjątki z komunikatem). Rekomendacja: ujednolicić użycie requireSuccess/mapHttpError dla spójności.

---

FRONT-008 — UserApartmentService (lokal mieszkańca)
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/services/UserApartmentService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/finances/viewmodels/FinancesViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/finances/viewmodels/FinancialLedgerViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/main/viewmodels/ResidentMainViewModel.kt

Wywołania API:
GET /api/tickets → TicketApiService.getTickets() → OK
GET /api/tickets/{id} → TicketApiService.getTicketById() → OK

Problem: brak. Uwaga: FinancialLedgerViewModel zachowuje się zgodnie z logiem — ZARZADCA wymaga navApartmentId (brak wyboru z drzewa powoduje Error), MIESZKANIEC rozwiązywany z ticketów. clearCache() istnieje i jest wywoływane przy logout.

---

FRONT-024 — TicketImageThumbnail + Coil
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/build.gradle.kts
- frontend/app/src/main/java/pl/edu/ur/blokur/di/ImageLoaderModule.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/components/TicketImageThumbnail.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/components/TicketImagesSection.kt

Wywołania API:
GET /api/images/{id} → TicketImageApiService.serveImage() / bezpośrednie URL ${BACKEND_URL}/api/images/{id} użyte w TicketImageThumbnail → OK

Problem: brak. Coil w zależności (io.coil-kt:coil-compose:2.7.0) jest dodany, ImageLoader korzysta z @Named("main") OkHttpClient — JWT będzie dołączany.

---

FRONT-001 — Galeria zdjęć (Ticket images thumbnails)
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/utils/TicketsStates.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketDetailsViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/components/TicketImagesSection.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/contents/TicketDetailsContent.kt

Wywołania API:
GET /api/tickets/{id}/images → TicketImageApiService.getImagesForTicket() — wywoływane z TicketDetailsViewModel (imageApi.getImagesForTicket) → OK
GET /api/images/{id} → używane przez miniatury (TicketImageThumbnail) → OK

Problem: brak.

---

FRONT-002 — Upload zdjęć AFTER (KONSERWATOR)
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketDetailsViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/components/TicketImagesSection.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/screens/TicketDetailsScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/components/ConservatorActionSheet.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketMediaServices.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketImageApiService.kt

Wywołania API:
POST /api/tickets/{id}/images (multipart: file, image_type) → TicketImageApiService.uploadImage() → OK
GET  /api/tickets/{id}/images → reload listy po uploadzie → OK

Problem: brak.

---

FRONT-009 — Hub finansów mieszkańca
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/services/FinancialLedgerService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/FinancialApiService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/finances/viewmodels/FinancesViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/finances/contents/FinancesOverviewContent.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/finances/viewmodels/FinancialLedgerViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/UserApartmentService.kt

Wywołania API:
GET /api/apartments/{apartmentId}/transactions → FinancialApiService.getTransactions() → OK
POST /api/apartments/{apartmentId}/transactions → FinancialApiService.createTransaction() → OK

Problem:
- FinancialLedgerService używa manualnych isSuccessful + Exception zamiast ApiResponseHandler.requireSuccess()/ApiException — rekomendacja: ujednolicić obsługę błędów.

---

FRONT-010 — Kartoteka z UserApartmentService
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/finances/viewmodels/FinancialLedgerViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/UserApartmentService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/FinancialLedgerService.kt

Wywołania API:
GET /api/apartments/{apartmentId}/transactions → FinancialLedgerService.getTransactions(apartmentId) → OK

Problem:
- Dla roli ZARZADCA bez navApartmentId ViewModel zgłasza UserApartmentException → UI wyświetla Error (zgodne z logiem).

---

FRONT-012 — Nawigacja profil → NotificationsScreen (API)
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/AppNavHost.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/notifications/NotificationsNavigation.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/notifications/screens/NotificationsScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/NotificationService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/NotificationApiService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/notifications/viewmodels/NotificationsViewModel.kt

Wywołania API:
GET /api/admin/notifications/settings → NotificationApiService.getSettings() → OK
PATCH /api/admin/notifications/settings/{eventType} → NotificationApiService.updateSetting() → OK

Problem:
- NetworkModule zapewnia NotificationApiService przez @Named("main") — żądania mają JWT. TopAppBar w NotificationsScreen ma przycisk wstecz i wywołuje popBackStack — OK.

---

FRONT-003 — Usunięcie DELETE zdjęć
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketImageApiService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketMediaServices.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/components/TicketImagesSection.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketDetailsViewModel.kt

Wywołania API:
DELETE /api/images/{id} → Nieobecny w kodzie źródłowym (brak wywołań deleteImage) → OK

Problem:
- brak.

---

FRONT-004 — Wznowienie WSTRZYMANO
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketApiService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketDetailsViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/contents/TicketDetailsContent.kt

Wywołania API:
PATCH /api/tickets/{id}/status → TicketApiService.changeStatus() → OK

Problem:
- UI używa dialogu potwierdzenia i TicketDetailsViewModel.onResumeTicket wywołuje changeStatus() bez otwierania AssignConservatorSheet — zgodne z logiem.


---

FRONT-007 — FAB tylko dla MIESZKANIEC
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/screens/TicketsScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketsViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketService.kt

Wywołania API:
(brak — zadanie UI)

Problem:
- Brak. showFab kontrolowane jest przez `currentUserRole` w stanie (`TicketsViewModel`) i ustawiane przez `ticketService.getCurrentUserRole()` (czyli z TokenStorage). Zachowanie zgodne z logiem.

---

FRONT-011 — Profil z sesji, bez fałszywego zapisu
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TokenStorage.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/AuthService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/profile/viewmodels/ProfileViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/profile/contents/ProfileContent.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/profile/screens/ProfileScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/profile/utils/ProfileStates.kt

Wywołania API:
(brak) — profil oparty na sesji (TokenStorage)

Problem:
- Brak. AuthService.login zapisuje email/role do TokenStorage; ProfileViewModel pobiera rolę z AuthService/TokenStorage i wyświetla pola jako read-only. Nie znaleziono `delay(300)` ani fałszywego "Zapisano".

---

FRONT-013 — Wycofanie fake NotificationSettings
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- brak plików NotificationSettingsScreen.kt / NotificationSettingsViewModel.kt (usunięte)
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/notifications/NotificationsNavigation.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/notifications/screens/NotificationsScreen.kt

Wywołania API:
GET /api/admin/notifications/settings → NotificationApiService.getSettings() → OK
PATCH /api/admin/notifications/settings/{eventType} → NotificationApiService.updateSetting() → OK

Problem:
- Brak. Stare pliki `NotificationSettings*` nie istnieją (zgodne z logiem). Rzeczywista funkcjonalność używa `NotificationsScreen` + `NotificationsViewModel` i prawdziwych endpointów admin (GET/PATCH).

---

FRONT-014 — Firebase FCM
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/google-services.json
- frontend/app/src/main/java/pl/edu/ur/blokur/services/FirebaseFcmTokenProvider.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/BlokurFirebaseMessagingService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/FcmModule.kt
- frontend/app/src/main/AndroidManifest.xml
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/viewmodels/AuthViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/DeviceService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/DeviceApiService.kt
- frontend/app/build.gradle.kts

Wywołania API:
POST /api/devices/register → DeviceApiService.registerDevice() — wywoływane z AuthViewModel.tryRegisterFcmToken() → OK
DELETE /api/devices/{token} → DeviceApiService.unregisterDevice() — wywoływane z ResidentMainViewModel.logout() → OK

Problem:
- Brak. Google-services placeholder istnieje; FCM provider i service zarejestrowane w manifeście; rejestracja tokenu wykonywana fire-and-forget po loginie, token zapisywany w TokenStorage po sukcesie.

---

FRONT-016 — Edycja ogłoszenia (PUT)
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/announcements/viewmodels/EditAnnouncementViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/announcements/screens/EditAnnouncementScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/announcements/AnnouncementsNavigation.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/AnnouncementService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/AnnouncementApiService.kt

Wywołania API:
PUT /api/announcements/{id} → AnnouncementApiService.updateAnnouncement() → OK

Problem:
- Brak.


---

FRONT-005 — Komentarze POST/GET
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketCommentApiService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketMediaServices.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketDetailsViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/components/TicketCommentsSection.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/dtos/TicketMediaDtos.kt

Wywołania API:
GET /api/tickets/{id}/comments → TicketCommentApiService.getComments(...) → OK
POST /api/tickets/{id}/comments → TicketCommentApiService.addComment(...) → OK

Problem:
- Brak. Ścieżki i metody są identyczne z audit/01_backend_inventory.md. Request body (`TicketCommentRequestDto`) zgadza się z backendem. Endpoints wywoływane są przez klienta `main` (NetworkModule dostarcza TicketCommentApiService z @Named("main")). Odpowiedzi są sprawdzane przez `response.isSuccessful`, błędy mapowane przez ApiResponseHandler, UI obsługuje loading i disable przy wysyłce.

---

FRONT-006 — Paginacja listy zgłoszeń
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketsViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/utils/TicketsStates.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/screens/TicketsScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TicketService.kt

Wywołania API:
GET /api/tickets → TicketService.getTickets(...) → OK

Problem:
- Brak. UI usunięto infinite scroll/paginację (TicketsScreen/TicketsViewModel nie wykonują kolejnych stron). TicketService nadal posiada opcjonalne parametry `page`/`size` z wartościami domyślnymi — to nie łamie zgodności, raczej upraszcza UI zgodnie z backlogiem.

---

FRONT-015 — Rejestracja/wyrejestrowanie E2E
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/services/TokenStorage.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/viewmodels/AuthViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/main/viewmodels/ResidentMainViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/DeviceService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/DeviceApiService.kt

Wywołania API:
POST /api/devices/register → DeviceApiService.registerDevice(...) — wywoływane z AuthViewModel.tryRegisterFcmToken() po zalogowaniu → OK
DELETE /api/devices/{token} → DeviceApiService.unregisterDevice(...) — wywoływane z ResidentMainViewModel.logout() przed czyszczeniem tokenów → OK

Problem:
- Brak. DeviceApiService jest dostarczany przez `main` Retrofit (JWT obecne), DeviceService sprawdza `response.isSuccessful` i obsługuje 404 łagodnie. Po udanej rejestracji token zapisywany jest w TokenStorage.

---

FRONT-017 — Obsługa HTTP 429 (rate limit) w auth
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/dtos/AuthDtos.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/AuthService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/viewmodels/AuthViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/viewmodels/ForgotPasswordViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/viewmodels/ResetPasswordViewModel.kt

Wywołania API:
POST /api/auth/login → AuthService.login(...) — mapowanie 429 -> AuthException.RateLimited(parseRetryAfter(response)) → OK
POST /api/auth/forgot-password, POST /api/auth/reset-password → analogiczna obsługa w mapAuthFailure/checkExpiredToken → OK

Problem:
- Brak. `Retry-After` przetwarzane są na komunikat PL przez formatRateLimitMessage; ViewModel wyświetla przyjazny komunikat.

---

FRONT-018 — Wygasły token (reset/zaproszenie)
Status: ⚠️ CZĘŚCIOWO
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/dtos/AuthDtos.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/AuthService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/utils/AuthStates.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/viewmodels/ResetPasswordViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/ResetPasswordForm.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/ResetPasswordScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/viewmodels/AcceptInvitationViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/auth/AcceptInvitationForm.kt

Wywołania API:
POST /api/auth/reset-password → AuthService.resetPassword(...) — mapowanie błędu na AuthException.TokenExpired (gdy checkExpiredToken=true i isExpiredTokenMessage(message)==true) → częściowo OK
POST /api/auth/accept-invitation → analogiczna obsługa → częściowo OK

Problem:
- Implementacja rozpoznaje wygasły token na podstawie treści błędu (`message` zawierające "wygas"). To działa jeśli backend zwraca opis zawierający to słowo, jednak backend może stosować status 410 lub inny komunikat. W związku z tym rozpoznanie jest heurystyczne i może zawieść (zalecane: backend powinien zwracać jednoznaczny status 410 lub dedykowane pole błędu, a frontend używać kodu statusu zamiast parsowania treści).


---

FRONT-019 — Dezaktywacja licznika
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/meters/viewmodels/MeterViewModels.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/meters/screens/MeterListScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/MeterService.kt
Wywołania API:
PATCH /api/meters/{id}/deactivate → OK
Problem: brak. Note: MeterService używa ApiResponseHandler-like pattern (response.isSuccessful + handleError), MeterApiService dostarczony przez NetworkModule @Named("main") — JWT dołączany.

---

FRONT-020 — Wybór wspólnoty przy logo
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/services/PropertyService.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/settings/viewmodels/CommunityLogoViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/settings/screens/CommunityLogoScreen.kt
Wywołania API:
GET /api/properties → OK
GET /api/properties/{id} → OK
PATCH /api/properties/{id}/logo → OK
Problem: brak. Uwaga: uploadPropertyLogo sprawdzany manualnie przez response.isSuccessful (zgodne, ale różne style obsługi błędów w projekcie).

---

FRONT-021 — Przeglądy read-only (mieszkaniec)
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/inspections/viewmodels/InspectionsViewModels.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/inspections/screens/InspectionsListScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/services/InspectionService.kt
Wywołania API:
GET /api/inspections → InspectionApiService.getAll() → OK
Problem: brak. UI kontroluje widoczność FAB/actions przez parametr isManager (ViewModel pobiera rolę z AuthService/TokenStorage).

---

FRONT-022 — Rozszerzone filtry listy zgłoszeń (ZARZĄDCA)
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/utils/TicketsStates.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/viewmodels/TicketsViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/components/TicketFilterPanel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/screens/TicketsScreen.kt
Wywołania API:
GET /api/tickets → TicketService.getTickets(...) → OK
GET /api/categories → TicketService.getCategories() → OK
GET /api/buildings/tree → PropertyService.getBuildingTree() → OK
Problem: brak. FilterPanel przekazuje parametry do ViewModel, ViewModel buduje query params prawidłowo.

---

FRONT-026 — Cleanup profilu
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/profile/viewmodels/ProfileViewModel.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/profile/contents/ProfileContent.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/profile/screens/ProfileScreen.kt
Wywołania API:
(brak) — zmiana UI/usunięcie testowych callbacków
Problem: brak. Testowy przycisk/powiadomienie usunięte zgodnie z logiem.

---

FRONT-028 — Spójne empty states
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/tickets/screens/TicketsScreen.kt
- frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/finances/contents/TransactionsContent.kt
Wywołania API: brak (UI)
Problem: brak. TicketsScreen rozróżnia "Brak zgłoszeń" vs "Brak wyników" w zależności od aktywnych filtrów; TransactionsContent pokazuje EmptyState gdy lista pusta.

---

FRONT-030 — README limitów API
Status: ✅ ZWERYFIKOWANE
Pliki zweryfikowane:
- frontend/README.md
Wywołania API: brak (dokumentacja)
Problem: brak. README zawiera sekcję "Ograniczenia API" zgodnie z logiem.
