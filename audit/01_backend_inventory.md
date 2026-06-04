# BlokUR Backend — Inwentaryzacja API (Faza 1)

**Data rozpoczęcia:** 2026-06-05  
**Źródło:** analiza kodu źródłowego `backend/src/main/java`

---

## Moduł 1: Autentykacja, sesja JWT i reset hasła

### Endpointy

#### `AuthController` — bazowa ścieżka `/api/auth`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/auth/login` | **publiczny** (`SecurityConfig.permitAll`) | Body: `LoginRequest` — `username` (email), `password` | 200: `AuthResponse` (`token`, `refreshToken`, `role`); 401: `Map` `{message}`; 423: `Map` `{message}` (konto zablokowane); 500: `Map` `{message}` | `login` |
| POST | `/api/auth/refresh` | **publiczny** | Body: `RefreshTokenRequest` — `refreshToken` (String) | 200: `AuthResponse`; 401: `Map` `{message}` | `refresh` |
| POST | `/api/auth/forgot-password` | **publiczny** | Body: `ForgotPasswordRequest` — `email` (@Email, @NotBlank) | 200: `Map` `{message}` (zawsze ten sam komunikat) | `forgotPassword` |
| POST | `/api/auth/reset-password` | **publiczny** | Body: `ResetPasswordRequest` — `token`, `newPassword` (walidacja w DTO) | 200: `Map` `{message}`; 400: `Map` `{message}` | `resetPassword` |
| POST | `/api/auth/accept-invitation` | **publiczny** | Body: `AcceptInvitationRequest` — `token`, `newPassword` (@NotBlank) | 200: `Map` `{message}`; 400: `Map` `{message}` przy `IllegalArgumentException` | `acceptInvitation` |

**Uwagi:**
- `RateLimitFilter`: limit 60 req/min per IP dla `/api/auth/login`, `/api/auth/forgot-password`, `/api/auth/reset-password` — odpowiedź **429** z `Retry-After` i JSON body.
- `InvitationService.acceptInvitation` rzuca `TokenExpiredException` przy wygasłym tokenie (komentarz w serwisie: HTTP 410), ale `AuthController` łapie tylko `IllegalArgumentException` → **[WYMAGA WERYFIKACJI]** czy globalny handler mapuje na 410.
- Access token JWT: ważność **8 h** (`JwtService.EXPIRATION_TIME`). Refresh token: **30 dni**, rotacja przy `/refresh`.
- Blokada konta: 3 nieudane próby → 15 min (`LoginAttemptService`), `LockedException` → HTTP 423.

### SecurityConfig (brak własnych endpointów)

- `permitAll`: `/api/auth/login`, `/refresh`, `/forgot-password`, `/reset-password`, `/accept-invitation`, `/api/categories`, `/error`
- Pozostałe: `authenticated()`
- Filtry (kolejność): `RateLimitFilter` → `JwtAuthenticationFilter` → …
- `PasswordEncoder`: BCrypt koszt **12**
- `@EnableMethodSecurity` — role na poziomie metod w innych kontrolerach

### Filtry i security (bez endpointów REST)

| Klasa | Rola |
|-------|------|
| `JwtAuthenticationFilter` | Nagłówek `Authorization: Bearer <token>` → walidacja JWT → `SecurityContext` z `ROLE_<role>` |
| `RateLimitFilter` | Sliding window 60/min na wybrane ścieżki auth |
| `RequestLoggingFilter` | Logowanie żądań (MDC `userId` z JWT) |
| `CustomUserDetailsService` | `loadUserByUsername(email)` — aktywność, blokada z `LoginAttemptService` |

### Serwisy — metody publiczne

| Klasa | Metody publiczne |
|-------|------------------|
| `JwtService` | `generateToken(username, role)`, `extractUsername(token)`, `extractRole(token)`, `generateRefreshTokenValue()`, `getRefreshTokenExpiry()`, `isTokenValid(token)` |
| `RefreshTokenService` | `createRefreshToken(User)`, `exchange(tokenValue)` → `TokenPair`; record `TokenPair(accessToken, refreshToken, role)` |
| `PasswordResetService` | `requestPasswordReset(email)`, `resetPassword(token, newPassword)` |
| `InvitationService` | `inviteUser(User)`, `acceptInvitation(token, newPassword)` |
| `LoginAttemptService` | `registerFailedAttempt(email)`, `resetFailedAttempts(email)`, `isAccountLocked(email)`, `getLockedUntil(email)` |
| `CustomUserDetailsService` | `loadUserByUsername(email)` |

### Encje / repozytoria (kontekst modułu)

- `RefreshToken`, `PasswordResetToken`, `InvitationToken`, `User`
- `RefreshTokenRepository`, `PasswordResetTokenRepository`, `InvitationTokenRepository`, `UserRepository`

---

## Moduł 2: Użytkownicy i administracja kont

### Endpointy

#### `UserController` — `/api/users`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/users` | **authenticated** (brak `@PreAuthorize`) — **[WYMAGA WERYFIKACJI]** czy ograniczone do ZARZĄDCA w praktyce | Query: `role` (String, wymagany) | 200: `List<UserWithTicketsDto>` | `getUsersByRole` |

#### `AdminUserController` — `/api/admin/users` — klasa: `@PreAuthorize("hasRole('ZARZADCA')")`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/admin/users` | ZARZĄDCA | — | 200: `List<UserResponse>` | `getAllUsers` |
| POST | `/api/admin/users` | ZARZĄDCA | Body: `CreateUserRequest` — `firstName`, `lastName`, `email`, `role` (regex: ZARZADCA\|MIESZKANIEC\|KONSERWATOR), `apartmentId` (UUID) | 201: `UserResponse`; 409: body `null` (email zajęty); 404: body `null` (lokal) | `createUser` |
| PATCH | `/api/admin/users/{id}` | ZARZĄDCA | Path: `id` (UUID); Body: `UpdateUserRequest` — `firstName`, `lastName`, `phone`, `role`, `apartmentId` (opcjonalny) | 200: `UserResponse`; 404: `null` | `updateUser` |
| PATCH | `/api/admin/users/{id}/deactivate` | ZARZĄDCA | Path: `id` (UUID) | 204; 404: `Map` `{message}` | `deactivateUser` |

**Uwaga:** `AdminUserService.deleteUser` istnieje w serwisie, ale **brak endpointu** w kontrolerze.

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `UserService` | `getUsersWithActiveTicketCountByRole(role)` — zapytanie repo, liczba aktywnych ticketów per użytkownik |
| `AdminUserService` | `getAllUsers()`, `createUser(request)` (+ zaproszenie email), `updateUser(id, request)`, `deactivateUser(id)`, `deleteUser(id)` (bez API) |

### Encje / repozytoria

- `User`, `UserApartment` — `UserRepository`, `ApartmentRepository` (przy tworzeniu/edycji)

---

## Moduł 3: Nieruchomości — wspólnoty, budynki, klatki, lokale

### Endpointy

#### `PropertyController` — `/api/properties` — `@PreAuthorize("hasRole('ZARZADCA')")` na klasie

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/properties` | ZARZĄDCA | Body: `PropertyRequest` — `name`, `address`, `nip` (10 cyfr), `managerPhone`, `managerEmail` | 201: `PropertyResponse` | `create` |
| PUT | `/api/properties/{id}` | ZARZĄDCA | Path: `id`; Body: `PropertyRequest` | 200: `PropertyResponse` | `update` |
| GET | `/api/properties` | ZARZĄDCA | — | 200: `List<PropertyResponse>` | `getAll` |
| GET | `/api/properties/{id}` | ZARZĄDCA | Path: `id` | 200: `PropertyResponse` | `getById` |
| PATCH | `/api/properties/{id}/logo` | ZARZĄDCA | Path: `id`; multipart: `file` (PNG/JPEG, walidacja `FileTypeValidator`) | 200: `PropertyResponse` | `uploadLogo` |

#### `BuildingController` — `/api/buildings`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/buildings/tree` | **authenticated** (brak `@PreAuthorize`) | — | 200: `List<BuildingTreeDto>` | `getBuildingTree` |
| POST | `/api/buildings` | ZARZĄDCA | Body: `BuildingRequest` — `estateName`, `name`, `address`, `latitude`, `longitude`, `propertyId` | 201: `BuildingResponse` | `createBuilding` |
| PUT | `/api/buildings/{id}` | ZARZĄDCA | Path: `id`; Body: `BuildingRequest` | 200: `BuildingResponse` | `updateBuilding` |
| DELETE | `/api/buildings/{id}` | ZARZĄDCA | Path: `id` | 204 | `deleteBuilding` |
| POST | `/api/buildings/{id}/staircases` | ZARZĄDCA | Path: `id`; Body: `StaircaseRequest` | 201: `StaircaseResponse` | `createStaircase` |
| PUT | `/api/buildings/{id}/staircases/{stId}` | ZARZĄDCA | Path: `id`, `stId`; Body: `StaircaseRequest` | 200: `StaircaseResponse` | `updateStaircase` |
| DELETE | `/api/buildings/{id}/staircases/{stId}` | ZARZĄDCA | Path: `id`, `stId` | 204 | `deleteStaircase` |

#### `StaircaseController` — `/api/staircases` — `@PreAuthorize("hasRole('ZARZADCA')")` na klasie

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/staircases/{id}/apartments` | ZARZĄDCA | Path: `id` (klatka); Body: `ApartmentRequest` — `number`, `floor`, `areaM2`, `ownershipType` (WLASNOSCIOWY\|NAJEM) | 201: `ApartmentResponse` | `createApartment` |
| PUT | `/api/staircases/{id}/apartments/{aptId}` | ZARZĄDCA | Path: `id`, `aptId`; Body: `ApartmentRequest` | 200: `ApartmentResponse` | `updateApartment` |
| DELETE | `/api/staircases/{id}/apartments/{aptId}` | ZARZĄDCA | Path: `id`, `aptId` | 204 | `deleteApartment` |

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `PropertyService` | `create(request)`, `update(id, request)`, `getAll()`, `getById(id)`, `uploadLogo(id, file)` |
| `BuildingService` | `getBuildingTree()`, `createBuilding(request)`, `updateBuilding(id, request)`, `deleteBuilding(id)`, `createStaircase(buildingId, request)`, `updateStaircase(buildingId, staircaseId, request)`, `deleteStaircase(...)`, `createApartment(staircaseId, request)`, `updateApartment(...)`, `deleteApartment(...)` |
| `FileTypeValidator` | `validateImage(file)` (używany przy logo) |

### Encje / repozytoria

- `Property`, `Building`, `Staircase`, `Apartment`
- `PropertyRepository`, `BuildingRepository`, `StaircaseRepository`, `ApartmentRepository`

---

## Moduł 4: Zgłoszenia serwisowe (tickets)

### 4a — Rdzeń zgłoszeń (`TicketController`)

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/tickets` | MIESZKANIEC | Body: `TicketRequest`; `Principal` | 201: `TicketDetailDto` | `create` |
| GET | `/api/tickets` | **authenticated** — filtrowanie wg roli w `TicketService` | Query: `status`, `categoryId`, `buildingId`, `staircaseId`, `assignedTo`, `dateFrom`, `dateTo`, `search`; `Principal` | 200: `List<TicketSummaryDto>`; 403 jeśli `principal == null` | `getAll` |
| GET | `/api/tickets/{id}` | **authenticated** — dostęp wg roli w serwisie | Path: `id`; `Principal` | 200: `TicketDetailDto`; 403 | `getById` |
| PATCH | `/api/tickets/{id}/assign` | ZARZĄDCA | Path: `id`; Body: `TicketAssignRequest`; `Principal` | 200: `TicketDetailDto` | `assignTicket` |
| PATCH | `/api/tickets/{id}/close` | ZARZĄDCA | Path: `id`; `Principal` | 200: `TicketDetailDto` | `closeTicket` |
| PATCH | `/api/tickets/{id}/reject` | ZARZĄDCA | Path: `id`; Body: `TicketRejectRequest`; `Principal` | 200: `TicketDetailDto` | `rejectTicket` |
| PATCH | `/api/tickets/{id}/start` | KONSERWATOR | Path: `id`; `Principal` | 200: `TicketDetailDto` | `startWork` |
| PATCH | `/api/tickets/{id}/suspend` | KONSERWATOR | Path: `id`; Body: `TicketSuspendRequest`; `Principal` | 200: `TicketDetailDto` | `suspendWork` |
| POST | `/api/tickets/{id}/completion` | KONSERWATOR | Path: `id`; Body: `TicketCompletionRequest`; `Principal` | 200: `TicketDetailDto` | `completeWork` |
| PATCH | `/api/tickets/{id}/status` | ZARZĄDCA lub KONSERWATOR | Path: `id`; Body: `TicketStatusChangeRequest`; `Principal` | 200: `TicketDetailDto` | `changeStatus` |

### 4b — Komentarze (`TicketCommentController`)

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/tickets/{id}/comments` | ZARZĄDCA, KONSERWATOR lub MIESZKANIEC | Path: `id`; Body: `TicketCommentRequest`; `Authentication` | 201: `TicketCommentDto` | `addComment` |
| GET | `/api/tickets/{id}/comments` | ZARZĄDCA, KONSERWATOR lub MIESZKANIEC | Path: `id`; `Authentication` | 200: `List<TicketCommentDto>` | `getComments` |

### 4c — Zdjęcia (`TicketImageController`)

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/tickets/{id}/images` | MIESZKANIEC, KONSERWATOR lub ZARZĄDCA | Path: `id`; multipart: `file`, `image_type` (`TicketImageType` enum) | 200: `TicketImageDto` | `uploadImage` |
| GET | `/api/tickets/{id}/images` | j.w. | Path: `id` | 200: `List<TicketImageDto>` | `getImagesForTicket` |
| GET | `/api/images/{id}` | j.w. | Path: `id` | 200: `Resource` (inline image) | `serveImage` |

### 4d — Kategorie (`CategoryController`, `AdminCategoryController`)

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/categories` | **publiczny** (`SecurityConfig.permitAll`) | — | 200: `List<CategoryResponse>` | `getActiveCategories` |
| POST | `/api/admin/categories` | ZARZĄDCA (klasa) | Body: `CategoryRequest` | 201: `CategoryResponse` | `createCategory` |
| PUT | `/api/admin/categories/{id}` | ZARZĄDCA | Path: `id`; Body: `CategoryRequest` | 200: `CategoryResponse` | `updateCategory` |
| PATCH | `/api/admin/categories/{id}/sla` | ZARZĄDCA | Path: `id`; Body: `SlaRequest` (`slaHours`) | 204 | `setSla` |
| PATCH | `/api/admin/categories/{id}/deactivate` | ZARZĄDCA | Path: `id` | 204 | `deactivateCategory` |

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `TicketService` | `initTicketNumberGenerator()`, `create(request, username)`, `getAll(username, filters)`, `getById(ticketId, username)`, `assignTicket`, `closeTicket`, `rejectTicket`, `startWork`, `suspendWork`, `completeWork`, `changeStatus` |
| `TicketCommentService` | `addComment(ticketId, request, email)`, `getComments(ticketId, email)` |
| `TicketImageService` | `uploadImage(ticketId, file, imageType, username)`, `getImagesForTicket(ticketId, username)`, `serveImage(imageId, username)` |
| `TicketCategoryService` | `getActiveCategories()`, `createCategory`, `updateCategory`, `setSlaHours`, `deactivateCategory` |
| `TicketStateMachine` | `validateTransition(from, to)`, `getAllowedNextStatuses(current)` |
| `TicketNumberGenerator` | `generate()`, `initYear(year, lastValue)` |
| `BusinessHoursCalculator` | `calculate(from, to)` — godziny robocze SLA |
| `FileTypeValidator` | `validateImage`, `validatePdf`, `validateCsv` |

### Encje / repozytoria

- `Ticket`, `TicketHistory`, `TicketComment`, `TicketImage`, `TicketCategory`
- `TicketRepository`, `TicketHistoryRepository`, `TicketCommentRepository`, `TicketImageRepository`, `TicketCategoryRepository`
- Enumy: `TicketStatus`, `TicketCommentType`, `TicketImageType`

---

## Moduł 5: Liczniki i odczyty mediów

### Endpointy

#### `MeterController`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/apartments/{apartmentId}/meters` | ZARZĄDCA | Path: `apartmentId`; Body: `MeterRequest` — `serialNumber`, `mediumType` (enum), `installationDate` | 201: `MeterResponse` | `create` |
| GET | `/api/apartments/{apartmentId}/meters` | ZARZĄDCA, KONSERWATOR lub MIESZKANIEC | Path: `apartmentId` | 200: `List<MeterResponse>` | `getAllByApartment` |
| PATCH | `/api/meters/{id}/deactivate` | ZARZĄDCA | Path: `id` | 200: `MeterResponse` | `deactivate` |

#### `MeterReadingController`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/apartments/{apartmentId}/meter-readings` | ZARZĄDCA lub KONSERWATOR | Path: `apartmentId`; Body: `MeterReadingRequest`; `Principal` | 201: `MeterReadingResponse` | `create` |
| GET | `/api/apartments/{apartmentId}/meter-readings` | ZARZĄDCA, KONSERWATOR lub MIESZKANIEC | Path: `apartmentId`; Query: `page` (def. 0), `size` (def. 20); `Principal` | 200: `Page<MeterReadingResponse>` | `getAllByApartment` |
| GET | `/api/meter-readings/{id}` | j.w. | Path: `id`; `Principal` | 200: `MeterReadingResponse` | `getById` |
| PUT | `/api/meter-readings/{id}` | ZARZĄDCA | Path: `id`; Body: `MeterReadingRequest` | 200: `MeterReadingResponse` | `update` |
| DELETE | `/api/meter-readings/{id}` | ZARZĄDCA | Path: `id` | 204 | `delete` |

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `MeterService` | `create(apartmentId, request)`, `getAllByApartment(apartmentId)`, `deactivate(meterId)` |
| `MeterReadingService` | `create(apartmentId, request, username)`, `getAllByApartment(..., page, size, username)`, `getById(id, username)`, `update(id, request)`, `delete(id)` |

### Encje / repozytoria

- `Meter`, `MeterReading` — `MeterRepository`, `MeterReadingRepository`; enum `MediumType`

---

## Moduł 6: Finanse — transakcje, salda, import CSV

### Endpointy

#### `FinancialTransactionController`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/apartments/{apartmentId}/transactions` | ZARZĄDCA lub MIESZKANIEC | Path: `apartmentId` | 200: `ApartmentTransactionsResponse` | `getTransactions` |
| POST | `/api/apartments/{apartmentId}/transactions` | ZARZĄDCA | Path: `apartmentId`; Body: `FinancialTransactionRequest`; `Principal` | 201: `FinancialTransactionResponse` | `createTransaction` |
| POST | `/api/finance/import` | ZARZĄDCA | multipart: `file` (CSV); `Principal` | 200: `CsvImportResultDto` | `importTransactions` |

#### `AdminFinanceController` — `/api/admin/apartments` — klasa: ZARZĄDCA

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/admin/apartments/balances` | ZARZĄDCA | Query: `propertyId`, `minDebt`, `minDaysOverdue`, `sort` (def. `debt_desc`) | 200: `List<ApartmentBalanceResponse>` | `getApartmentBalances` |

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `FinancialTransactionService` | `getTransactionsForApartment(apartmentId)`, `createTransaction(apartmentId, request, userEmail)`, `importTransactionsFromCsv(file, userEmail)` |
| `ApartmentBalanceService` | `getBalances(propertyId, minDebt, minDaysOverdue, sortDesc)` |

### Encje / repozytoria

- `FinancialTransaction` — `FinancialTransactionRepository`, `ApartmentRepository`

---

## Moduł 7: Dokumenty — dystrybucja, pobieranie, storage

### Endpointy

#### `DocumentController` — `/api/documents` — brak `@PreAuthorize`; role w `DocumentService`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/documents` | **authenticated** — ZARZĄDCA / MIESZKANIEC (logika serwisu) | Query: `apartmentId`, `startDate`, `endDate`, `type`; `Principal` | 200: `List<DocumentDto>`; 403 | `getDocuments` |
| GET | `/api/documents/{id}/download` | j.w. | Path: `id`; `Principal` | 200: `Resource` (PDF); 403 | `downloadDocument` |

#### `AdminDocumentController` — `/api/admin/documents` — ZARZĄDCA

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/admin/documents/rate-change` | ZARZĄDCA | Body: `RateChangeDistributionRequest`; `Principal` | 200: `DocumentDistributionResult`; 403 | `distributeRateChange` |
| POST | `/api/admin/documents/annual-settlement` | ZARZĄDCA | Body: `AnnualSettlementDistributionRequest`; `Principal` | 200: `DocumentDistributionResult`; 403 | `distributeAnnualSettlement` |

### Serwisy / storage

| Klasa | Metody publiczne |
|-------|------------------|
| `DocumentService` | `storeGeneratedDocument(...)`, `getDocuments(apartmentId, startDate, endDate, type, username)`, `downloadDocument(documentId, username)` |
| `DocumentDistributionService` | `distributeRateChange(request, username)`, `distributeAnnualSettlement(request, username)` |
| `DocumentStorage` (interfejs) | implementacje: `LocalDiskDocumentStorage`, `S3DocumentStorage` via `DocumentStorageConfig` |

### Encje / repozytoria

- `Document` — `DocumentRepository`

---

## Moduł 8: Ogłoszenia

### Endpointy — `AnnouncementController`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/announcements` | **authenticated** (`isAuthenticated()`) | `Principal` | 200: `List<AnnouncementDto>` | `getAnnouncements` |
| POST | `/api/announcements` | ZARZĄDCA | multipart: `data` (`AnnouncementRequest`), opcjonalnie `attachment` (PDF) | 201: `AnnouncementDto` | `createAnnouncement` |
| PUT | `/api/announcements/{id}` | ZARZĄDCA | Path: `id`; multipart jak POST | 200: `AnnouncementDto` | `updateAnnouncement` |
| DELETE | `/api/announcements/{id}` | ZARZĄDCA | Path: `id` | 204 | `deleteAnnouncement` |
| GET | `/api/announcements/{id}/attachment` | **authenticated** (brak `@PreAuthorize`) — **[WYMAGA WERYFIKACJI]** dostęp wg widoczności ogłoszenia | Path: `id` | 200: `byte[]` PDF; 403/404 | `getAttachment` |

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `AnnouncementService` | `getAnnouncementsForUser(username)`, `createAnnouncement`, `updateAnnouncement`, `deleteAnnouncement`, `sendPushNotificationsAsync` |

### Encje / repozytoria

- `Announcement` — `AnnouncementRepository`; enum `AnnouncementTargetType`

---

## Moduł 9: Uchwały i głosowania (resolutions)

### Endpointy — `ResolutionController` — brak `@PreAuthorize`; role w `ResolutionService`

| Metoda | Ścieżka | Rola (serwis) | Wejście | Wyjście | Metoda |
|--------|---------|---------------|---------|---------|--------|
| POST | `/api/resolutions` | ZARZĄDCA (403 w serwisie dla innych) | Body: `CreateResolutionRequest`; `Principal` | 201; 403 | `createResolution` |
| GET | `/api/resolutions` | ZARZĄDCA (wszystkie) / MIESZKANIEC (budynek) | `Principal` | 200: `List<ResolutionDto>`; 403 | `getResolutions` |
| GET | `/api/resolutions/{id}` | wg zasięgu użytkownika | Path: `id`; `Principal` | 200: `ResolutionDetailDto`; 403 | `getResolutionDetails` |
| GET | `/api/resolutions/{id}/report` | ZARZĄDCA (`generateResolutionReport`) | Path: `id`; `Principal` | 200: `byte[]` PDF | `getResolutionReport` |
| POST | `/api/resolutions/{id}/vote` | MIESZKANIEC (głosowanie) | Path: `id`; Body: `CastVoteRequest` (`optionId`); `Principal` | 204; 403; 404/409/400 via `ResponseStatusException` | `castVote` |

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `ResolutionService` | `castVote`, `createResolution`, `getResolutionsForUser`, `getResolutionDetails`, `generateResolutionReport` |

### Encje / repozytoria

- `Resolution`, `ResolutionOption`, `ResolutionVote` — odpowiednie repozytoria; enum `ScopeType`

---

## Moduł 10: Przeglądy techniczne (inspections)

### Endpointy — `InspectionController`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/inspections` | ZARZĄDCA | Body: `InspectionRequest`; `Principal` | 201: `InspectionResponse` | `create` |
| GET | `/api/inspections` | **authenticated** — filtrowanie w serwisie | `Principal` | 200: `List<InspectionResponse>`; 403 | `getAll` |
| PUT | `/api/inspections/{id}` | ZARZĄDCA | Path: `id`; Body: `InspectionRequest` | 200: `InspectionResponse` | `update` |
| DELETE | `/api/inspections/{id}` | ZARZĄDCA | Path: `id` | 204 | `delete` |

### Scheduler

| Klasa | Opis |
|-------|------|
| `InspectionReminderJob` | `@Scheduled(cron = "0 0 8 * * *")` — `sendReminders()`; powiadomienia PUSH 7 dni i 24 h przed przeglądem |

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `InspectionService` | `create(request, username)`, `getAll(username)`, `update(id, request)`, `delete(id)` |

### Encje / repozytoria

- `Inspection` — `InspectionRepository`

---

## Moduł 11: Powiadomienia push, urządzenia i konfiguracja zdarzeń

### Endpointy

#### `DeviceController` — brak `@PreAuthorize`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/devices/register` | **authenticated** | Body: `DeviceRegistrationRequest` — `fcmToken`, `platform`; `Principal` (email) | 204; 404 | `registerDevice` |
| DELETE | `/api/devices/{token}` | **authenticated** | Path: `token`; `Principal` | 204; 404 | `unregisterDevice` |

#### `AdminNotificationController` — `/api/admin/notifications/settings` — ZARZĄDCA

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| GET | `/api/admin/notifications/settings` | ZARZĄDCA | — | 200: `List<NotificationConfigResponse>` | `getAll` |
| PATCH | `/api/admin/notifications/settings/{eventType}` | ZARZĄDCA | Path: `eventType`; Body: `UpdateNotificationConfigRequest` (`enabled`) | 200: `NotificationConfigResponse` | `update` |

### Serwisy / config

| Klasa | Metody publiczne |
|-------|------------------|
| `UserDeviceService` | `registerDevice(userId, fcmToken, platform)`, `unregisterDevice(userId, fcmToken)` |
| `PushNotificationService` | `sendToUsers(...)`, `send(...)`; stałe `EVENT_*` |
| `NotificationConfigService` | `getAll()`, `update(eventType, enabled)` |
| `FirebaseConfig` | konfiguracja FCM (bean) |

### Encje / repozytoria

- `UserDevice`, `NotificationSetting`, `NotificationConfig` — odpowiednie repozytoria

---

## Moduł 12: Generowanie PDF (API + pdf-lib)

### Endpointy

#### `PdfController` — `/api/pdf`

| Metoda | Ścieżka | Rola | Wejście | Wyjście | Metoda |
|--------|---------|------|---------|---------|--------|
| POST | `/api/pdf/work-acceptance-protocol` | ZARZĄDCA lub KONSERWATOR | Body: `WorkAcceptanceProtocolRequest` | 200: `byte[]` PDF | `generateWorkAcceptanceProtocol` |
| GET | `/api/pdf/balances` | ZARZĄDCA | Query: `propertyId`, `minDebt`, `minDaysOverdue`, `sort`, `save` (boolean) | 200: `byte[]` PDF; opcjonalna archiwizacja dokumentu | `generateBalancesReport` |

**Powiązany endpoint w module 9:** `GET /api/resolutions/{id}/report` → `ResolutionService.generateResolutionReport`

### Serwisy

| Klasa | Metody publiczne |
|-------|------------------|
| `PdfGeneratorService` | `generateWorkAcceptanceProtocol`, `generateRateChangeNotification`, `generateAnnualSettlement`, `generateBalancesReport` |

### Subprojekt `pdf-lib` (bez REST)

- `PdfGenerator`, `PdfTemplate`, szablony: `AnnualSettlementTemplate`, `BalancesReportTemplate`, `RateChangeNotificationTemplate`, `WorkAcceptanceProtocolTemplate`
- Dane: `AnnualSettlementData`, `BalancesReportData`, `RateChangeNotificationData`, `WorkAcceptanceProtocolData`, itd.
- `UnicodeFontLoader`, `PdfGenerationException`

---

## Moduł 13: Infrastruktura wspólna

### Klasy bez własnych endpointów REST

| Klasa | Opis |
|-------|------|
| `BlokurApplication` | punkt wejścia Spring Boot |
| `MailConfig` | SMTP (`JavaMailSender`) |
| `FlywayConfig` | migracje bazy |
| `JpaAuditingConfig` | audyt encji (np. `MeterReading`) |
| `BusinessValidationException`, `NotFoundException`, `TokenExpiredException` | wyjątki domenowe — **[WYMAGA WERYFIKACJI]** globalny `@ControllerAdvice` (brak w `src/main`) |

### Weryfikacja pokrycia kontrolerów (Faza 2)

Wszystkie **23** kontrolery `@RestController` z Fazy 0 przypisane do modułów 1–12. Brak modułu „Pozostałe”.

| Kontroler | Moduł |
|-----------|-------|
| AuthController | 1 |
| UserController, AdminUserController | 2 |
| PropertyController, BuildingController, StaircaseController | 3 |
| TicketController, TicketCommentController, TicketImageController, CategoryController, AdminCategoryController | 4 |
| MeterController, MeterReadingController | 5 |
| FinancialTransactionController, AdminFinanceController | 6 |
| DocumentController, AdminDocumentController | 7 |
| AnnouncementController | 8 |
| ResolutionController | 9 |
| InspectionController | 10 |
| DeviceController, AdminNotificationController | 11 |
| PdfController | 12 |

---

## Podsumowanie (Faza 2)

### Łączna liczba endpointów HTTP: **81**

| Moduł | Liczba endpointów |
|-------|-------------------|
| 1 — Autentykacja | 5 |
| 2 — Użytkownicy | 5 |
| 3 — Nieruchomości | 15 |
| 4 — Zgłoszenia | 20 |
| 5 — Liczniki | 8 |
| 6 — Finanse | 4 |
| 7 — Dokumenty | 4 |
| 8 — Ogłoszenia | 5 |
| 9 — Uchwały | 5 |
| 10 — Przeglądy | 4 |
| 11 — Powiadomienia / urządzenia | 4 |
| 12 — PDF | 2 |
| **Razem** | **81** |

### Endpointy per rola (na podstawie `@PreAuthorize` + `SecurityConfig` + jawnej logiki w serwisie)

| Rola / dostęp | Liczba endpointów* |
|---------------|-------------------|
| **Publiczny** (`permitAll`, bez JWT) | **6** |
| **ZARZĄDCA** (wyłącznie lub w `hasAnyRole`) | 38 |
| **MIESZKANIEC** | 12 |
| **KONSERWATOR** | 11 |
| **authenticated** (dowolna rola z JWT, szczegóły w serwisie) | 9 |

\*Suma kategorii > 81, bo wiele endpointów obsługuje wiele ról (`hasAnyRole`). **Publiczne (6):** 5× `/api/auth/*` + `GET /api/categories`.

**Ścieżki `permitAll` w `SecurityConfig`:**  
`/api/auth/login`, `/api/auth/refresh`, `/api/auth/forgot-password`, `/api/auth/reset-password`, `/api/auth/accept-invitation`, `/api/categories`, `/error`

**Endpointy z logiką roli wyłącznie w serwisie (bez `@PreAuthorize`):**  
`UserController`, `DocumentController`, `ResolutionController`, `DeviceController`, część `TicketController` (GET), `BuildingController` (GET tree), `InspectionController` (GET), `AnnouncementController` (GET attachment).

---

**STATUS: Faza 1 i Faza 2 zakończone.**

