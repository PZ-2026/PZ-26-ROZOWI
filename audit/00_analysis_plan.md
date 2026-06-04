# BlokUR Backend — Plan analizy (Faza 0)

**Data:** 2026-06-05  
**Źródło:** rekurencyjny przegląd `backend/` (Java 21, Spring Boot 4.0.3)  
**Pakiet główny:** `pl.edu.ur.blokur`  
**Subprojekt:** `backend/pdf-lib/` (biblioteka generowania PDF, osobny moduł Gradle)

---

## Struktura katalogów `backend/`

```
backend/
├── build.gradle
├── pdf-lib/                    # subprojekt: szablony PDF (OpenPDF)
└── src/main/java/pl/edu/ur/blokur/
    ├── BlokurApplication.java
    ├── config/                 # konfiguracja Spring
    ├── controller/             # 23 kontrolery REST
    ├── dto/                    # ~50 klas DTO (pominięte w inwentaryzacji klas — tylko kontekst endpointów)
    ├── exception/              # wyjątki domenowe
    ├── models/                 # encje JPA + enumy
    ├── repository/             # 25 repozytoriów JPA
    ├── scheduler/              # zadania @Scheduled
    ├── security/               # JWT, filtry, UserDetails
    └── service/                  # logika biznesowa + storage dokumentów
```

**Zasoby:** `src/main/resources/` — `application*.properties`, `logback-spring.xml`, migracje Flyway (`db/migration/` — 28 plików SQL).

---

## Inwentaryzacja klas (Faza 0)

### Kontrolery REST (`@RestController`) — 23 klasy

| Klasa | Bazowa ścieżka |
|-------|----------------|
| `AuthController` | `/api/auth` |
| `UserController` | `/api/users` |
| `AdminUserController` | `/api/admin/users` |
| `PropertyController` | `/api/properties` |
| `BuildingController` | `/api/buildings` |
| `StaircaseController` | `/api/staircases` |
| `TicketController` | `/api/tickets` |
| `TicketCommentController` | `/api/tickets` |
| `TicketImageController` | `/api` |
| `CategoryController` | `/api/categories` |
| `AdminCategoryController` | `/api/admin/categories` |
| `MeterController` | `/api` |
| `MeterReadingController` | `/api` |
| `FinancialTransactionController` | `/api` |
| `AdminFinanceController` | `/api/admin/apartments` |
| `DocumentController` | `/api/documents` |
| `AdminDocumentController` | `/api/admin/documents` |
| `AnnouncementController` | `/api/announcements` |
| `ResolutionController` | `/api/resolutions` |
| `InspectionController` | `/api/inspections` |
| `DeviceController` | `/api/devices` |
| `AdminNotificationController` | `/api/admin/notifications/settings` |
| `PdfController` | `/api/pdf` |

Brak klas `@Controller` (tylko widoki) — wyłącznie API REST.

---

### Serwisy i komponenty logiki biznesowej

**`@Service` (27):**

`AdminUserService`, `AnnouncementService`, `ApartmentBalanceService`, `BuildingService`, `DocumentDistributionService`, `DocumentService`, `FinancialTransactionService`, `InspectionService`, `InvitationService`, `LoginAttemptService`, `MeterReadingService`, `MeterService`, `NotificationConfigService`, `PasswordResetService`, `PdfGeneratorService`, `PropertyService`, `PushNotificationService`, `RefreshTokenService`, `ResolutionService`, `TicketCategoryService`, `TicketCommentService`, `TicketImageService`, `TicketNumberGenerator`, `TicketService`, `UserDeviceService`, `UserService`

**`@Component` (pomocnicze, bez `@Service`):**

`BusinessHoursCalculator`, `FileTypeValidator`, `TicketStateMachine`

**Security (`@Service` / `@Component`):**

`JwtService` (@Service), `CustomUserDetailsService` (@Service), `JwtAuthenticationFilter`, `RateLimitFilter`, `RequestLoggingFilter` (@Component)

**Scheduler:**

`InspectionReminderJob` (@Component, `@Scheduled`)

**Storage dokumentów:**

`DocumentStorageConfig` (@Configuration + @Bean), `LocalDiskDocumentStorage`, `S3DocumentStorage`, interfejs `DocumentStorage`, `DocumentStorageException`

---

### Encje JPA (`@Entity`) — 22 klasy

`Announcement`, `Apartment`, `Building`, `Document`, `FinancialTransaction`, `Inspection`, `InvitationToken`, `Meter`, `MeterReading`, `NotificationConfig`, `NotificationSetting`, `PasswordResetToken`, `Property`, `RefreshToken`, `Resolution`, `ResolutionOption`, `ResolutionVote`, `Staircase`, `Ticket`, `TicketCategory`, `TicketComment`, `TicketHistory`, `TicketImage`, `User`, `UserApartment`, `UserDevice`

**Enumy / typy pomocnicze w `models/` (bez `@Entity`):**

`AnnouncementTargetType`, `MediumType`, `ScopeType`, `TicketCommentType`, `TicketImageType`, `TicketStatus`

**Uwaga:** `UserApartment` — encja powiązania użytkownik–lokal, brak dedykowanego repozytorium (dostęp przez `User.userApartments`).

---

### Repozytoria JPA — 25 interfejsów

`AnnouncementRepository`, `ApartmentRepository`, `BuildingRepository`, `DocumentRepository`, `FinancialTransactionRepository`, `InspectionRepository`, `InvitationTokenRepository`, `MeterReadingRepository`, `MeterRepository`, `NotificationConfigRepository`, `NotificationSettingRepository`, `PasswordResetTokenRepository`, `PropertyRepository`, `RefreshTokenRepository`, `ResolutionOptionRepository`, `ResolutionRepository`, `ResolutionVoteRepository`, `StaircaseRepository`, `TicketCategoryRepository`, `TicketCommentRepository`, `TicketHistoryRepository`, `TicketImageRepository`, `TicketRepository`, `UserDeviceRepository`, `UserRepository`

(Wszystkie rozszerzają `JpaRepository`; część ma `@Repository`, część nie — Spring i tak je rejestruje.)

---

### Konfiguracja security i infrastruktury

**Security (rdzeń autoryzacji):**

- `config/SecurityConfig.java` — `@EnableWebSecurity`, `@EnableMethodSecurity`, JWT stateless, `permitAll` dla wybranych ścieżek auth/kategorii
- `security/JwtAuthenticationFilter.java`
- `security/RateLimitFilter.java`
- `security/RequestLoggingFilter.java`
- `security/JwtService.java`
- `security/CustomUserDetailsService.java`

**Pozostała konfiguracja (`config/`):**

- `FirebaseConfig.java` — push (FCM)
- `MailConfig.java` — wysyłka e-mail
- `JpaAuditingConfig.java` — audyt JPA (`MeterReading`)
- `FlywayConfig.java` — migracje DB

**Wyjątki (`exception/`):**

`BusinessValidationException`, `NotFoundException`, `TokenExpiredException`

**Role w systemie (string w encji `User.role`):** `ZARZADCA`, `MIESZKANIEC`, `KONSERWATOR`  
(Spring Security używa prefiksu `ROLE_` w `@PreAuthorize` — np. `hasRole('ZARZADCA')`.)

---

## Plan modułów funkcjonalnych (do analizy w Fazie 1)

Moduły wywnioskowane z klastrów kontrolerów, serwisów i encji — nie z założeń dokumentacji.

---

### Moduł 1: Autentykacja, sesja JWT i reset hasła

**Zakres:** logowanie, odświeżanie tokenów, zaproszenia, reset hasła, filtry bezpieczeństwa.

**Kontrolery:**
- `AuthController`

**Serwisy / security:**
- `RefreshTokenService`, `PasswordResetService`, `InvitationService`, `LoginAttemptService`
- `JwtService`, `CustomUserDetailsService`
- `JwtAuthenticationFilter`, `RateLimitFilter`, `RequestLoggingFilter`
- `SecurityConfig`

**Encje / repozytoria:**
- `User`, `RefreshToken`, `PasswordResetToken`, `InvitationToken`
- `UserRepository`, `RefreshTokenRepository`, `PasswordResetTokenRepository`, `InvitationTokenRepository`

**DTO (kontekst endpointów):** `LoginRequest`, `AuthResponse`, `RefreshTokenRequest`, `ForgotPasswordRequest`, `ResetPasswordRequest`, `AcceptInvitationRequest`

---

### Moduł 2: Użytkownicy i administracja kont

**Kontrolery:**
- `UserController`
- `AdminUserController`

**Serwisy:**
- `UserService`, `AdminUserService`

**Encje / repozytoria:**
- `User`, `UserApartment`
- `UserRepository`

**DTO:** `UserResponse`, `CreateUserRequest`, `UpdateUserRequest` (i powiązane)

---

### Moduł 3: Nieruchomości — wspólnoty, budynki, klatki, lokale

**Kontrolery:**
- `PropertyController`
- `BuildingController`
- `StaircaseController`

**Serwisy:**
- `PropertyService`, `BuildingService`

**Encje / repozytoria:**
- `Property`, `Building`, `Staircase`, `Apartment`
- `PropertyRepository`, `BuildingRepository`, `StaircaseRepository`, `ApartmentRepository`

**DTO:** `PropertyRequest`, `PropertyResponse`, `BuildingRequest`, `BuildingResponse`, `BuildingTreeDto`, `ApartmentRequest`, `ApartmentResponse`, itd.

---

### Moduł 4: Zgłoszenia serwisowe (tickets)

**Podmoduł 4a — rdzeń zgłoszeń:**
- `TicketController`
- `TicketService`, `TicketStateMachine`, `TicketNumberGenerator`, `BusinessHoursCalculator`

**Podmoduł 4b — komentarze:**
- `TicketCommentController`
- `TicketCommentService`

**Podmoduł 4c — zdjęcia zgłoszeń:**
- `TicketImageController`
- `TicketImageService`, `FileTypeValidator`

**Podmoduł 4d — kategorie i SLA:**
- `CategoryController`, `AdminCategoryController`
- `TicketCategoryService`

**Encje / repozytoria:**
- `Ticket`, `TicketHistory`, `TicketComment`, `TicketImage`, `TicketCategory`
- `TicketRepository`, `TicketHistoryRepository`, `TicketCommentRepository`, `TicketImageRepository`, `TicketCategoryRepository`
- Enumy: `TicketStatus`, `TicketCommentType`, `TicketImageType`

---

### Moduł 5: Liczniki i odczyty mediów

**Kontrolery:**
- `MeterController`
- `MeterReadingController`

**Serwisy:**
- `MeterService`, `MeterReadingService`

**Encje / repozytoria:**
- `Meter`, `MeterReading`
- `MeterRepository`, `MeterReadingRepository`
- Enum: `MediumType`

**Konfiguracja powiązana:** `JpaAuditingConfig` (audyt `MeterReading`)

---

### Moduł 6: Finanse — transakcje, salda, import CSV

**Kontrolery:**
- `FinancialTransactionController`
- `AdminFinanceController`

**Serwisy:**
- `FinancialTransactionService`, `ApartmentBalanceService`

**Encje / repozytoria:**
- `FinancialTransaction`
- `FinancialTransactionRepository`
- `ApartmentRepository` (używane przy saldach)

**DTO:** `FinancialTransactionRequest`, `FinancialTransactionResponse`, `ApartmentBalanceResponse`, `ApartmentTransactionsResponse`, `CsvImportResultDto`, itd.

---

### Moduł 7: Dokumenty — dystrybucja, pobieranie, storage

**Kontrolery:**
- `DocumentController`
- `AdminDocumentController`

**Serwisy:**
- `DocumentService`, `DocumentDistributionService`

**Storage:**
- `DocumentStorage`, `DocumentStorageConfig`, `LocalDiskDocumentStorage`, `S3DocumentStorage`, `DocumentStorageException`

**Encje / repozytoria:**
- `Document`
- `DocumentRepository`

**DTO:** `DocumentDto`, `RateChangeDistributionRequest`, `AnnualSettlementDistributionRequest`, `DocumentDistributionResult`, itd.

---

### Moduł 8: Ogłoszenia

**Kontrolery:**
- `AnnouncementController`

**Serwisy:**
- `AnnouncementService`

**Encje / repozytoria:**
- `Announcement`
- `AnnouncementRepository`
- Enum: `AnnouncementTargetType`

---

### Moduł 9: Uchwały i głosowania (resolutions)

**Kontrolery:**
- `ResolutionController`

**Serwisy:**
- `ResolutionService`

**Encje / repozytoria:**
- `Resolution`, `ResolutionOption`, `ResolutionVote`
- `ResolutionRepository`, `ResolutionOptionRepository`, `ResolutionVoteRepository`
- Enum: `ScopeType` (używany także w inspections)

**DTO:** `CreateResolutionRequest`, `CastVoteRequest`, `ResolutionDetailDto`, itd.

---

### Moduł 10: Przeglądy techniczne (inspections)

**Kontrolery:**
- `InspectionController`

**Serwisy / scheduler:**
- `InspectionService`
- `InspectionReminderJob`

**Encje / repozytoria:**
- `Inspection`
- `InspectionRepository`

**Powiązania:** `PushNotificationService` (powiadomienia z joba — analiza zależności w Fazie 1)

---

### Moduł 11: Powiadomienia push, urządzenia i konfiguracja zdarzeń

**Kontrolery:**
- `DeviceController`
- `AdminNotificationController`

**Serwisy:**
- `UserDeviceService`, `PushNotificationService`, `NotificationConfigService`

**Konfiguracja:**
- `FirebaseConfig`

**Encje / repozytoria:**
- `UserDevice`, `NotificationSetting`, `NotificationConfig`
- `UserDeviceRepository`, `NotificationSettingRepository`, `NotificationConfigRepository`

---

### Moduł 12: Generowanie PDF (API + biblioteka pdf-lib)

**Kontrolery:**
- `PdfController`

**Serwisy:**
- `PdfGeneratorService`

**Subprojekt `pdf-lib/` (klasy produkcyjne):**
- `PdfGenerator`, `PdfTemplate`
- `UnicodeFontLoader`
- Szablony: `AnnualSettlementTemplate`, `BalancesReportTemplate`, `RateChangeNotificationTemplate`, `WorkAcceptanceProtocolTemplate`
- Dane szablonów: `AnnualSettlementData`, `AnnualSettlementRow`, `BalancesReportData`, `BalanceRow`, `RateChangeNotificationData`, `WorkAcceptanceProtocolData`
- `PdfGenerationException`

**Uwaga:** część PDF jest też serwowana przez `ResolutionController` (`GET /{id}/report`) — endpoint zostanie udokumentowany w module 9 z odesłaniem do `PdfGeneratorService`.

---

### Moduł 13: Infrastruktura wspólna (poza endpointami)

**Klasy bez własnych kontrolerów REST — do opisu zależności i wyjątków:**

- `BlokurApplication`
- `MailConfig`, `FlywayConfig`
- `exception/BusinessValidationException`, `NotFoundException`, `TokenExpiredException`

**Kontrolery bez `@PreAuthorize` (role w warstwie serwisu lub tylko `authenticated`) — wymagają weryfikacji w Fazie 1:**

- `ResolutionController` — brak `@PreAuthorize` na klasie/metodach
- `DocumentController` — brak `@PreAuthorize`
- `DeviceController` — brak `@PreAuthorize`
- `UserController` — brak `@PreAuthorize`
- Endpointy `TicketController` bez adnotacji roli (poza oznaczonymi `@PreAuthorize`)

---

## Kolejność analizy w Fazie 1

| Krok | Moduł |
|------|-------|
| 1 | Autentykacja, sesja JWT i reset hasła |
| 2 | Użytkownicy i administracja kont |
| 3 | Nieruchomości — wspólnoty, budynki, klatki, lokale |
| 4 | Zgłoszenia serwisowe (4a → 4d) |
| 5 | Liczniki i odczyty mediów |
| 6 | Finanse |
| 7 | Dokumenty |
| 8 | Ogłoszenia |
| 9 | Uchwały i głosowania |
| 10 | Przeglądy techniczne |
| 11 | Powiadomienia push i urządzenia |
| 12 | Generowanie PDF |
| 13 | Infrastruktura wspólna (krótki przegląd) |

**Plik wynikowy Fazy 1:** `audit/01_backend_inventory.md` (tworzony iteracyjnie po każdym module).

**Faza 2:** weryfikacja pokrycia wszystkich 23 kontrolerów + podsumowanie statystyk endpointów/rol.

---

## Statystyki wstępne (Faza 0)

| Kategoria | Liczba |
|-----------|--------|
| Kontrolery `@RestController` | 23 |
| Serwisy `@Service` | 27 (+ 2 security) |
| Komponenty `@Component` (service + security + scheduler) | 7 |
| Encje `@Entity` | 22 |
| Repozytoria | 25 |
| Klasy `config/` | 5 |
| Szacowane endpointy HTTP (z grep `@*Mapping`) | ~85 |

---

**STATUS: Faza 0 zakończona — oczekiwanie na akceptację planu przed Fazą 1.**
