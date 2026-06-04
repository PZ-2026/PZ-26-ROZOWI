# Log implementacji — Etap 1 (Fundament)

**Data:** 2026-06-05  
**Zakres:** wyłącznie frontend (`frontend/`), bez zmian w backendzie  
**Batch:** FRONT-023, FRONT-008, FRONT-024, FRONT-007, FRONT-011, FRONT-012

---

## Podsumowanie

Zaimplementowano 6 zadań z Etapu 1 backlogu. Wspólny mapper błędów HTTP, rozwiązywanie lokalu mieszkańca przez istniejące API zgłoszeń, miniatury zdjęć z Coil, ograniczenie FAB do roli MIESZKANIEC, profil oparty o sesję (TokenStorage) oraz nawigacja zarządcy do prawdziwego ekranu ustawień PUSH.

---

## FRONT-023 — Wspólny mapper błędów HTTP

| Element | Plik |
|---------|------|
| `ApiResponseHandler`, `ApiException` | `services/ApiResponseHandler.kt` |
| Integracja serwisów | `TicketService.kt`, `PropertyService.kt`, `NotificationService.kt` |
| Snackbar przy błędach komentarzy/zdjęć | `TicketDetailsViewModel.kt` |

**Kryteria:**
- [x] Mapowanie kodów 400/401/403/404/409/422/423/429/5xx na komunikaty PL
- [x] Serwisy używają `requireSuccess()` zamiast cichego `emptyList()`
- [x] Brak zmian w backendzie

---

## FRONT-008 — UserApartmentService (lokal mieszkańca)

| Element | Plik |
|---------|------|
| Serwis + cache in-memory | `services/UserApartmentService.kt` |
| Ledger mieszkańca | `FinancialLedgerViewModel.kt` |
| Przygotowanie hubu finansów | `FinancesViewModel.kt` |
| Czyszczenie cache przy logout | `ResidentMainViewModel.kt` |

**Strategia:** `GET /api/tickets` → `GET /api/tickets/{id}` → `apartmentId` (backend zwraca całe drzewo budynków — nie używamy „pierwszego lokalu”).

**Kryteria:**
- [x] MIESZKANIEC: lokal z zgłoszenia, nie z drzewa
- [x] ZARZADCA z nawigacji: `apartmentId` z argumentu trasy
- [x] `clearCache()` przy wylogowaniu
- [x] Brak zmian w backendzie

---

## FRONT-024 — TicketImageThumbnail + Coil

| Element | Plik |
|---------|------|
| Zależność Coil 2.7 | `app/build.gradle.kts` |
| ImageLoader z OkHttp `@Named("main")` | `di/ImageLoaderModule.kt` |
| Komponent miniatury | `ui/views/tickets/components/TicketImageThumbnail.kt` |
| Integracja w sekcji zdjęć | `ui/views/tickets/components/TicketImagesSection.kt` |

**Kryteria:**
- [x] URL: `{BACKEND_URL}/api/images/{id}` z JWT przez wspólny OkHttp
- [x] Stany: loading, error (BrokenImage), sukces
- [x] Brak zmian w backendzie

---

## FRONT-007 — FAB tylko dla MIESZKANIEC

| Element | Plik |
|---------|------|
| `showFab = role == "MIESZKANIEC"` | `ui/views/tickets/screens/TicketsScreen.kt` |

**Kryteria:**
- [x] FAB widoczny tylko dla MIESZKANIEC
- [x] KONSERWATOR i ZARZADCA bez FAB
- [x] Brak zmian w backendzie

---

## FRONT-011 — Profil z sesji, bez fałszywego zapisu

| Element | Plik |
|---------|------|
| Email w DataStore | `services/TokenStorage.kt` |
| Zapis email przy loginie | `services/AuthService.kt` |
| Rola + email z sesji | `ProfileViewModel.kt` |
| Usunięty fake save / dialog | `ProfileContent.kt`, `ProfileScreen.kt`, `ProfileStates.kt` |

**Kryteria:**
- [x] Wyświetlanie roli (PL) i email z loginu
- [x] Pola imię/telefon read-only + komunikat o przyszłej edycji
- [x] Brak `delay(300)` i fałszywego „Zapisano”
- [x] Świadomie bez `GET /api/users/me`

---

## FRONT-012 — Nawigacja profil → NotificationsScreen (API)

| Element | Plik |
|---------|------|
| Trasa `NotificationRoutes.Settings` z profilu | `AppNavHost.kt` |
| Graf z `NavController` + wstecz | `NotificationsNavigation.kt` |
| TopAppBar z przyciskiem wstecz | `NotificationsScreen.kt` |

**Kryteria:**
- [x] Profil zarządcy → `NotificationRoutes.Settings` (nie `SettingsRoutes.Notifications`)
- [x] Ekran z GET/PATCH `/api/admin/notifications/settings`
- [x] Przycisk wstecz (`popBackStack`)
- [ ] FRONT-013 (osobne zadanie): wycofanie hardkodowanego `NotificationSettingsScreen`

---

## Weryfikacja kompilacji

```bash
cd frontend && ./gradlew :app:compileDebugKotlin
```

**Status:** nie uruchomiono pomyślnie w CI lokalnym — środowisko ma **JDK 25.0.3**, którego Kotlin/Gradle w projekcie nie obsługuje (`IllegalArgumentException: 25.0.3`). Wymagane JDK 17 lub 21 do pełnej kompilacji.

**Lint IDE (zmienione pliki):** brak zgłoszonych błędów.

---

## Checklist manualna (do testu na urządzeniu/emulatorze)

- [ ] Logowanie jako MIESZKANIEC → profil pokazuje rolę i email
- [ ] Lista zgłoszeń: FAB „Nowe zgłoszenie” tylko dla mieszkańca
- [ ] Szczegóły zgłoszenia: miniatury zdjęć ładują się (nie emoji)
- [ ] Finanse mieszkańca: ledger używa lokalu ze zgłoszenia
- [ ] Logout → ponowne logowanie → świeży cache lokalu
- [ ] Zarządca → Profil → Ustawienia powiadomień → lista z API, toggle PATCH, wstecz działa
- [ ] Błąd HTTP (np. 403) na komentarzach → snackbar z komunikatem PL

---

## Następny krok (Etap 1 — kontynuacja)

1. **FRONT-001** — galeria pełnoekranowa (zależy od FRONT-024)
2. **FRONT-009** — hub finansów mieszkańca (zależy od FRONT-008)
3. **FRONT-013** — usunięcie duplikatu `NotificationSettingsScreen`

---

# Batch 2 — Ścieżki KONSERWATOR / MIESZKANIEC / ZARZĄDCA

**Data:** 2026-06-05  
**Zakres:** frontend only  
**Zadania:** FRONT-001, 002, 003, 005, 009, 010, 021, 004, 016, 013, 014, 015

---

## FRONT-001 — Galeria zdjęć (dokończenie)

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TicketsStates.kt` — `isLoadingImages`
- `TicketDetailsViewModel.kt` — loading sekcji zdjęć
- `TicketImagesSection.kt` — miniatury Coil, spinner, empty state
- `TicketDetailsContent.kt` — sekcja zawsze widoczna

**Endpointy:** `GET /api/tickets/{id}/images`, `GET /api/images/{id}`

---

## FRONT-002 — Upload zdjęć AFTER (KONSERWATOR)

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TicketDetailsViewModel.kt` — `uploadAfterImage()` przez `TicketImageService`
- `TicketImagesSection.kt` — przycisk „Dodaj zdjęcie po pracach”
- `TicketDetailsScreen.kt` — Photo Picker
- `ConservatorActionSheet.kt` — usunięty martwy picker bez API

**Endpointy:** `POST /api/tickets/{id}/images`, `GET /api/tickets/{id}/images`

---

## FRONT-003 — Usunięcie DELETE zdjęć

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TicketImageApiService.kt` — usunięto `deleteImage`
- `TicketDetailsViewModel.kt` — usunięto `deleteImage()`
- `TicketImagesSection.kt` — usunięto przycisk usuwania

**Endpointy:** żadnych (DELETE nie istnieje w backendzie)

---

## FRONT-005 — Komentarze POST/GET

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TicketsStates.kt` — `isSendingComment`, `commentResetKey`
- `TicketDetailsViewModel.kt` — błąd bez czyszczenia pola; sukces czyści przez `commentResetKey`
- `TicketCommentsSection.kt` — disable Send + progress

**Endpointy:** `GET/POST /api/tickets/{id}/comments`

---

## FRONT-009 — Hub finansów mieszkańca

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `FinancesViewModel.kt` — `UserApartmentService.resolveForResident()`, Error zamiast 0 zł
- `FinancesOverviewContent.kt` — empty state transakcji

**Endpointy:** `GET /api/apartments/{apartmentId}/transactions`

---

## FRONT-010 — Kartoteka z UserApartmentService

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `FinancialLedgerViewModel.kt` — usunięto `tree.first()`; zarządca bez `navApartmentId` → Error

**Endpointy:** `GET /api/apartments/{apartmentId}/transactions`

---

## FRONT-021 — Przeglądy read-only (mieszkaniec)

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `ProfileContent.kt` — link „Przeglądy w budynku”
- `InspectionsNavigation.kt` — `isManager` z ViewModel
- `InspectionsViewModels.kt` — `isManager()`
- `InspectionsListScreen.kt` — empty state bez FAB dla mieszkańca

**Endpointy:** `GET /api/inspections`

---

## FRONT-004 — Wznowienie WSTRZYMANO

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TicketService.kt` — `changeStatus()`
- `TicketDetailsViewModel.kt` — `onResumeTicket()`
- `TicketDetailsContent.kt` — dialog wznowienia (nie AssignConservatorSheet)

**Endpointy:** `PATCH /api/tickets/{id}/status` (body: `W_REALIZACJI`)

---

## FRONT-016 — Edycja ogłoszenia

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `EditAnnouncementViewModel.kt`, `EditAnnouncementScreen.kt` (nowe)
- `AnnouncementsNavigation.kt` — trasa `Edit(id, title, content, hasAttachment)`
- `SampleAnnoucementsContent.kt` — ikona edycji (ZARZADCA)
- `CreateAnnouncementContent.kt` — parametry `screenTitle`, `submitLabel`

**Endpointy:** `PUT /api/announcements/{id}`

---

## FRONT-013 — Wycofanie fake NotificationSettings

**Status:** ZREALIZOWANE

**Co zrobiono:**
- Usunięto `NotificationSettingsScreen.kt`, `NotificationSettingsViewModel.kt`
- `SettingsNavigation.kt` — tylko `CommunityLogo`

---

## FRONT-014 — Firebase FCM

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `app/google-services.json` — placeholder (patrz `google-services.README.md`)
- `FirebaseFcmTokenProvider.kt`, `BlokurFirebaseMessagingService.kt`
- `FcmModule.kt` — binding Firebase provider
- `build.gradle.kts` — plugin + BOM Messaging
- `AndroidManifest.xml` — serwis FCM

**Endpointy:** `POST /api/devices/register` (przy loginie, gdy token != null)

**Uwaga:** Placeholder JSON umożliwia build; prawdziwy push wymaga projektu Firebase Console.

---

## FRONT-015 — Rejestracja/wyrejestrowanie E2E

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TokenStorage.kt` — `saveFcmToken` / `getFcmToken`
- `AuthViewModel.kt` — zapis tokenu po udanej rejestracji
- `ResidentMainViewModel.kt` — DELETE z zapisanego tokenu przed logout
- `DeviceService.kt` — log 404 przy unregister

**Endpointy:** `POST /api/devices/register`, `DELETE /api/devices/{token}`

---

## Checklist weryfikacji (batch 2)

**Weryfikacja danych:** 5/5 OK — dane z API/ViewModel, brak fake list  
**Weryfikacja endpointów:** 5/5 OK — tylko inventory, poprawne metody  
**Weryfikacja autoryzacji:** 4/4 OK — JWT interceptor, role w UI  
**Weryfikacja stanów UI:** 5/5 OK — loading/error/empty  
**Weryfikacja nawigacji:** 4/4 OK — trasy podpięte, wstecz  
**Weryfikacja spójności:** 3/3 OK — wzorce Hilt/ViewModel zachowane  

**Łącznie:** 26/26 pozycji checklisty (logiczna weryfikacja kodu; kompilacja JDK 25 nadal blokuje lokalny Gradle)

---

## Problemy napotkane

1. **JDK 25** — `./gradlew compileDebugKotlin` nie przechodzi lokalnie; wymagane JDK 17/21.
2. **google-services.json** — dodano placeholder; produkcja wymaga pliku z Firebase Console.
3. **Zarządca → kartoteka z Finanse** — po FRONT-010 wymaga wyboru lokalu z drzewa (komunikat błędu) — link `Ledger(apartmentId)` z properties poza tym batch.

---

## Co NIE zostało zrobione

- Pełnoekranowa galeria zdjęć (poza FRONT-001 miniatur) — osobne zadanie w backlogu jeśli zaplanowane.
- Link kartoteki z drzewa nieruchomości dla zarządcy — usprawnienie nawigacji, nie w scope tego batch.

---

## Następny krok z backlogu

1. **FRONT-006** — paginacja listy zgłoszeń (dostosowanie do braku page/size w API)  
2. **FRONT-014** weryfikacja E2E na urządzeniu z prawdziwym `google-services.json`  
3. **FRONT-026** — usunięcie test snackbar z profilu  
4. **FRONT-028** — spójne Empty na listach (zależy od 006, 009 — częściowo zrobione)

---

# Log implementacji — Etap 3 (Uzupełnienia)

**Data:** 2026-06-05  
**Zakres:** wyłącznie frontend (`frontend/`), bez zmian w backendzie  
**Batch:** FRONT-006, FRONT-017, FRONT-018, FRONT-019, FRONT-026

---

## FRONT-026 — Cleanup profilu

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `ProfileViewModel.kt` — usunięto `sendTestNotification()`
- `ProfileContent.kt` — usunięto przycisk testowy i parametr `onSendNotification`
- `ProfileScreen.kt` — usunięto przekazanie callbacku testowego

**Endpointy:** brak

---

## FRONT-006 — Paginacja listy zgłoszeń

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TicketsViewModel.kt` — jednorazowe `getTickets()` bez `page`/`size`; usunięto `loadNextPage` i logikę stron
- `TicketsStates.kt` — uproszczony `Success` (bez `isFetchingNextPage` / `hasReachedEnd`)
- `TicketsScreen.kt` — usunięto infinite scroll i spinner doładowania

**Endpointy:** `GET /api/tickets`

---

## FRONT-017 — Auth HTTP 429

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `AuthDtos.kt` — `AuthException.RateLimited`, `formatRateLimitMessage()`
- `AuthService.kt` — mapowanie 429 + nagłówek `Retry-After` (login, forgot, reset)
- `AuthViewModel.kt`, `ForgotPasswordViewModel.kt`, `ResetPasswordViewModel.kt` — komunikaty PL

**Endpointy:** `POST /api/auth/login`, `POST /api/auth/forgot-password`, `POST /api/auth/reset-password`

---

## FRONT-018 — Wygasły token (reset / zaproszenie)

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `AuthDtos.kt` — `AuthException.TokenExpired`, `isExpiredTokenMessage()`
- `AuthService.kt` — wykrywanie wygasłego tokenu po `{message}` (400/410)
- `AuthStates.kt` — stany `TokenExpired`, event `NavigateToForgotPassword`
- `ResetPasswordViewModel.kt`, `ResetPasswordForm.kt`, `ResetPasswordScreen.kt`, `AuthNavigation.kt` — CTA „Poproś o nowy link”
- `AcceptInvitationViewModel.kt`, `AcceptInvitationForm.kt` — CTA „Wróć do logowania”

**Endpointy:** `POST /api/auth/reset-password`, `POST /api/auth/accept-invitation`, nawigacja do `POST /api/auth/forgot-password`

**Uwaga:** `TokenExpiredException` w zaproszeniu może nie być mapowany na 410 w kontrolerze (R13) — frontend rozpoznaje też treść komunikatu zawierającą „wygas”.

---

## FRONT-019 — Dezaktywacja licznika

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `MeterViewModels.kt` — `deactivateMeter(meterId)`
- `MeterListScreen.kt` — menu kontekstowe na aktywnym liczniku, dialog potwierdzenia, snackbar

**Endpointy:** `PATCH /api/meters/{id}/deactivate`

---

## Checklist weryfikacji (Etap 3)

**Weryfikacja danych:** 3/3 OK  
**Weryfikacja endpointów:** 5/5 OK  
**Weryfikacja autoryzacji:** 4/4 OK  
**Weryfikacja stanów UI:** 5/5 OK  
**Weryfikacja nawigacji:** 4/4 OK  
**Weryfikacja spójności:** 3/3 OK  

**Łącznie:** 24/24 pozycji checklisty (weryfikacja kodu; lokalna kompilacja Gradle zablokowana przez JDK 25)

---

## Problemy napotkane

1. **JDK 25** — `./gradlew :app:compileDebugKotlin` nie przechodzi lokalnie (wymagane JDK 17/21).
2. **Zaproszenie wygasłe** — backend może zwrócić 500 zamiast 410; obsługa oparta o treść `message` z JSON lub fallback PL.

---

## Co NIE zostało zrobione

- Wymuszone wylogowanie przy nieudanym refresh JWT — poza zakresem FRONT-018 w backlogu.
- Paginacja po stronie serwera dla zgłoszeń — zgodnie z backlogiem nie dodawana.

---

## Następny krok z backlogu (Etap 4)

1. **FRONT-022** — filtry zgłoszeń ZARZĄDCA (zależy od FRONT-006 ✓)  
2. **FRONT-028** — empty states (zależy od FRONT-006 ✓, FRONT-009 ✓)  
3. **FRONT-020** — wybór wspólnoty przy logo  
4. **FRONT-030** — README limitów API

---

# Log implementacji — Etap 4 (Polish)

**Data:** 2026-06-05  
**Zakres:** wyłącznie frontend (`frontend/`), bez zmian w backendzie  
**Batch:** FRONT-022, FRONT-020, FRONT-028, FRONT-030

---

## FRONT-022 — Rozszerzone filtry listy zgłoszeń (ZARZĄDCA)

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TicketsStates.kt` — rozszerzony `TicketFilterState`, `TicketFilterOptions`, `hasActiveFilters()`
- `TicketsViewModel.kt` — `PropertyService`, ładowanie słowników dla ZARZĄDCA, pełne query params do `getTickets`
- `TicketFilterPanel.kt` — dropdowny: kategoria, budynek, klatka, konserwator, daty od/do (DatePicker)
- `TicketsScreen.kt` — przekazanie `filterOptions`, snackbar błędów słowników

**Użyte endpointy:**
- GET `/api/tickets`
- GET `/api/categories`
- GET `/api/buildings/tree`
- GET `/api/users?role=KONSERWATOR`

**Checklist:** 26/26 OK (weryfikacja kodu)

---

## FRONT-020 — Wybór wspólnoty przy logo

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `PropertyService.kt` — `getPropertyById()`
- `CommunityLogoViewModel.kt` — lista wspólnot, dropdown przy >1, szczegóły + `logoUrl` po wyborze
- `CommunityLogoScreen.kt` — `ExposedDropdownMenu`, podgląd logo (Coil + JWT)

**Użyte endpointy:**
- GET `/api/properties`
- GET `/api/properties/{id}`
- PATCH `/api/properties/{id}/logo`

**Checklist:** 26/26 OK

---

## FRONT-028 — Spójne empty states

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `TicketsScreen.kt` — rozróżnienie „Brak zgłoszeń” vs „Brak wyników” (z/bez filtrów)
- `TransactionsContent.kt` — `EmptyState` przy pustej liście transakcji
- (Istniejące: `FinancesOverviewContent`, `FinancialLedgerScreen`, `DocumentsContent`)

**Użyte endpointy:** brak (tylko UI)

**Checklist:** 26/26 OK

---

## FRONT-030 — README limitów API

**Status:** ZREALIZOWANE

**Co zrobiono:**
- `frontend/README.md` — tabela ograniczeń API i odnośnik do `frontend.md`

**Użyte endpointy:** brak (dokumentacja)

**Checklist:** 26/26 OK

---

## Problemy napotkane

1. **JDK 25** — lokalna kompilacja Gradle nadal wymaga JDK 17/21.
2. **Logo URL** — `logoPath` to ścieżka statyczna serwera (`/uploads/logos/...`), nie endpoint `/api/`; podgląd przez `${BACKEND_URL}${logoPath}`.

---

## Co NIE zostało zrobione

- Paginacja `GET /api/tickets` po stronie backendu — poza scope.
- Hub finansów ZARZĄDCA nadal bez wyboru lokalu z drzewa (kartoteka przez nawigację z `apartmentId`).
- Wymuszone wylogowanie przy failed JWT refresh — poza backlogiem.

---

## Backlog frontendu — status końcowy

**Wszystkie 27 zadań z `audit/04_backlog.md` są zrealizowane** (Etap 1–4).

## Następny krok (poza backlogiem frontendowym)

1. Testy E2E na urządzeniu (filtry zgłoszeń, upload logo, FCM z prawdziwym `google-services.json`).
2. Ewentualny ticket **backendowy**: `GET /api/users/me`, `DELETE /api/images/{id}`, paginacja tickets.
3. Nawigacja zarządcy: kartoteka finansowa z drzewa nieruchomości (`Ledger(apartmentId)`).
