# FAZA 0 — Plan analizy backendu BlokUR

> Wygenerowano: 2026-06-04  
> Źródło: rekurencyjny przegląd `backend/src/main/java/pl/edu/ur/blokur/`

---

## Statystyki struktury

| Typ klasy | Liczba |
|---|---|
| Kontrolery (`@RestController`) | 23 |
| Serwisy (`@Service`) | 29 (w tym 5 klas infrastruktury storage) |
| Encje JPA | 32 |
| Repozytoria JPA | 25 |
| Klasy security | 5 |
| Klasy konfiguracyjne | 5 |
| Schedulery | 1 |

---

## Plan podziału na moduły funkcjonalne

### Moduł 1: Uwierzytelnianie i Sesja
**Pliki:**
- Controller: `AuthController.java` (`/api/auth/**`)
- Service: `InvitationService.java`, `LoginAttemptService.java`, `PasswordResetService.java`, `RefreshTokenService.java`
- Security: `JwtAuthenticationFilter.java`, `JwtService.java`, `CustomUserDetailsService.java`, `RateLimitFilter.java`, `RequestLoggingFilter.java`
- Config: `SecurityConfig.java`
- Model: `RefreshToken.java`, `InvitationToken.java`, `PasswordResetToken.java`
- Repo: `RefreshTokenRepository.java`, `InvitationTokenRepository.java`, `PasswordResetTokenRepository.java`

---

### Moduł 2: Zarządzanie Użytkownikami (Admin)
**Pliki:**
- Controller: `AdminUserController.java` (`/api/admin/users/**`), `UserController.java` (`/api/users`)
- Service: `AdminUserService.java`, `UserService.java`
- Model: `User.java`, `UserApartment.java`
- Repo: `UserRepository.java`

---

### Moduł 3: Nieruchomości i Struktura Budynku
**Pliki:**
- Controller: `PropertyController.java` (`/api/properties/**`), `BuildingController.java` (`/api/buildings/**`), `StaircaseController.java` (`/api/staircases/**`)
- Service: `PropertyService.java`, `BuildingService.java`
- Model: `Property.java`, `Building.java`, `Staircase.java`, `Apartment.java`
- Repo: `PropertyRepository.java`, `BuildingRepository.java`, `StaircaseRepository.java`, `ApartmentRepository.java`

---

### Moduł 4: Zgłoszenia Serwisowe (Tickets)
**Pliki:**
- Controller: `TicketController.java` (`/api/tickets/**`), `TicketCommentController.java` (`/api/tickets/{id}/comments`), `TicketImageController.java` (`/api/tickets/{id}/images`, `/api/images/{id}`)
- Controller pomocniczy: `CategoryController.java` (`/api/categories`), `AdminCategoryController.java` (`/api/admin/categories/**`)
- Service: `TicketService.java`, `TicketCommentService.java`, `TicketImageService.java`, `TicketCategoryService.java`, `TicketStateMachine.java`, `TicketNumberGenerator.java`
- Model: `Ticket.java`, `TicketCategory.java`, `TicketComment.java`, `TicketCommentType.java`, `TicketHistory.java`, `TicketImage.java`, `TicketImageType.java`, `TicketStatus.java`
- Repo: `TicketRepository.java`, `TicketCategoryRepository.java`, `TicketCommentRepository.java`, `TicketHistoryRepository.java`, `TicketImageRepository.java`

---

### Moduł 5: Ogłoszenia
**Pliki:**
- Controller: `AnnouncementController.java` (`/api/announcements/**`)
- Service: `AnnouncementService.java`
- Model: `Announcement.java`, `AnnouncementTargetType.java`
- Repo: `AnnouncementRepository.java`

---

### Moduł 6: Uchwały i Głosowania
**Pliki:**
- Controller: `ResolutionController.java` (`/api/resolutions/**`)
- Service: `ResolutionService.java`
- Model: `Resolution.java`, `ResolutionOption.java`, `ResolutionVote.java`, `ScopeType.java`
- Repo: `ResolutionRepository.java`, `ResolutionOptionRepository.java`, `ResolutionVoteRepository.java`

---

### Moduł 7: Liczniki i Odczyty
**Pliki:**
- Controller: `MeterController.java` (`/api/apartments/{id}/meters/**`), `MeterReadingController.java` (`/api/apartments/{id}/meter-readings/**`, `/api/meter-readings/**`)
- Service: `MeterService.java`, `MeterReadingService.java`
- Model: `Meter.java`, `MeterReading.java`, `MediumType.java`
- Repo: `MeterRepository.java`, `MeterReadingRepository.java`

---

### Moduł 8: Finanse i Transakcje
**Pliki:**
- Controller: `FinancialTransactionController.java` (`/api/apartments/{id}/transactions`, `/api/finance/import`), `AdminFinanceController.java` (`/api/admin/apartments/balances`)
- Service: `FinancialTransactionService.java`, `ApartmentBalanceService.java`
- Model: `FinancialTransaction.java`
- Repo: `FinancialTransactionRepository.java`

---

### Moduł 9: Dokumenty
**Pliki:**
- Controller: `DocumentController.java` (`/api/documents/**`), `AdminDocumentController.java` (`/api/admin/documents/**`)
- Service: `DocumentService.java`, `DocumentDistributionService.java`
- Service infrastruktura: `storage/DocumentStorage.java`, `storage/LocalDiskDocumentStorage.java`, `storage/S3DocumentStorage.java`, `storage/DocumentStorageConfig.java`, `storage/DocumentStorageException.java`
- Model: `Document.java`
- Repo: `DocumentRepository.java`

---

### Moduł 10: PDF
**Pliki:**
- Controller: `PdfController.java` (`/api/pdf/**`)
- Service: `PdfGeneratorService.java`

---

### Moduł 11: Przeglądy Techniczne
**Pliki:**
- Controller: `InspectionController.java` (`/api/inspections/**`)
- Service: `InspectionService.java`
- Scheduler: `scheduler/InspectionReminderJob.java`
- Model: `Inspection.java`
- Repo: `InspectionRepository.java`

---

### Moduł 12: Powiadomienia Push i Urządzenia
**Pliki:**
- Controller: `DeviceController.java` (`/api/devices/**`), `AdminNotificationController.java` (`/api/admin/notifications/settings/**`)
- Service: `PushNotificationService.java`, `UserDeviceService.java`, `NotificationConfigService.java`
- Config: `FirebaseConfig.java`
- Model: `UserDevice.java`, `NotificationConfig.java`, `NotificationSetting.java`
- Repo: `UserDeviceRepository.java`, `NotificationConfigRepository.java`, `NotificationSettingRepository.java`

---

### Pomocnicze / Infrastruktura (nie analizowane osobno jako moduły)
- `FileTypeValidator.java` — walidacja typów plików (MIME), używana w wielu modułach
- `BusinessHoursCalculator.java` — kalkulator godzin roboczych dla SLA
- `config/FirebaseConfig.java`, `config/FlywayConfig.java`, `config/JpaAuditingConfig.java`, `config/MailConfig.java`
- `exception/GlobalExceptionHandler.java`

---

## Kolejność analizy (Faza 1)

1. Moduł 1 — Uwierzytelnianie i Sesja
2. Moduł 2 — Zarządzanie Użytkownikami
3. Moduł 3 — Nieruchomości i Struktura Budynku
4. Moduł 4 — Zgłoszenia Serwisowe *(największy — podzielę na submóduly)*
5. Moduł 5 — Ogłoszenia
6. Moduł 6 — Uchwały i Głosowania
7. Moduł 7 — Liczniki i Odczyty
8. Moduł 8 — Finanse i Transakcje
9. Moduł 9 — Dokumenty
10. Moduł 10 — PDF
11. Moduł 11 — Przeglądy Techniczne
12. Moduł 12 — Powiadomienia Push i Urządzenia
