# ✅ Checklista Testów Manualnych — Aplikacja Blokur

> Wersja: 2026-05-29 | Status: `[ ]` = nie sprawdzony, `[x]` = OK, `[!]` = błąd

---

## 0. ⚙️ Przygotowanie środowiska (wykonaj PRZED pozostałymi sekcjami)

### Dane techniczne
- [ ] Backend uruchomiony (`GET /api/categories` zwraca `200`)
- [ ] PostgreSQL dostępny, baza `blokur` istnieje i ma dane z Flyway migracji
- [ ] Aplikacja mobilna zainstalowana na emulatorze lub urządzeniu (Android 8+)
- [ ] Adres `BACKEND_URL` w `BuildConfig` wskazuje na backend

### Konta testowe (utwórz przez sekcję 13, lub użyj istniejących)

| Rola | Email | Hasło | Uwagi |
|------|-------|-------|-------|
| ZARZADCA | zarzadca@test.pl | Test1234! | bez przypisanego lokalu |
| KONSERWATOR | konserwator@test.pl | Test1234! | bez przypisanego lokalu |
| MIESZKANIEC | mieszkaniec@test.pl | Test1234! | **musi** mieć przypisany lokal |

> **Kolejność tworzenia:** najpierw wykonaj sekcję **13 (Użytkownicy)**, następnie sekcje 7 (budynki/lokale) i dopiero pozostałe sekcje.

### Dane struktury (utwórz przez sekcję 7)
- [ ] Przynajmniej 1 Nieruchomość → 1 Budynek → 1 Klatka → 2 Lokale
- [ ] MIESZKANIEC przypisany do jednego z tych lokali
- [ ] Przynajmniej 1 aktywna kategoria zgłoszeń (przez sekcję 14)

### Testowanie deep linków (reset hasła, zaproszenia) na emulatorze
Zamiast klikania w link w mailu, wklej token z maila i wykonaj z powłoki:
```bash
# Reset hasła
adb shell am start -a android.intent.action.VIEW -d "blokur://reset-password?token=TOKEN_Z_MAILA"
# Akceptacja zaproszenia
adb shell am start -a android.intent.action.VIEW -d "blokur://accept-invitation?token=TOKEN_Z_MAILA"
```

---

## 1. 🔐 Uwierzytelnianie

### 1.1 Logowanie
- [ ] Poprawne dane → `200 OK`, token zapisany, redirect do głównego ekranu
- [ ] Złe hasło → komunikat błędu (nie crash)
- [ ] 3× złe hasło pod rząd → komunikat „konto zablokowane na 15 min" (HTTP `423`)
- [ ] Zablokowane konto po 15 min → logowanie znowu działa
- [ ] Puste pola → walidacja frontendowa (przycisk zablokowany lub komunikat)

### 1.2 Odświeżanie tokenu (auto)
- [ ] Skróć `jwt.expiration` do 60 sekund w `application.properties`, poczekaj minutę i wykonaj dowolne żądanie
- [ ] Token odświeżony automatycznie (użytkownik nic nie robi), żądanie zakończone sukcesem
- [ ] Wyloguj → kolejne żądanie → `401` (nie odświeżenie)

### 1.3 Reset hasła (deep link)
- [ ] Podaj istniejący email → `200 OK` + mail z linkiem resetowania
- [ ] Podaj nieistniejący email → `200 OK` (z celowo niejednoznaczną odpowiedzią — tak działa backend)
- [ ] Otwórz deep link (patrz sekcja 0) → formularz nowego hasła → zapisz → zaloguj się nowym hasłem → działa
- [ ] Użyj tego samego tokenu po raz drugi → `400 Bad Request` (token jednorazowy)
- [ ] Poczekaj >1 godzinę z tokenem → `400 Bad Request` / komunikat „token wygasł"

### 1.4 Akceptacja zaproszenia (deep link)
- [ ] ZARZADCA tworzy nowego użytkownika → system wysyła mail z zaproszeniem
- [ ] Otwórz deep link z tokenem (patrz sekcja 0) → formularz hasła → zapisz → zaloguj się → działa
- [ ] Użyj tokenu ponownie → `400` (token jednorazowy, ważny 72h)

---

## 2. 📋 Zgłoszenia — MIESZKANIEC

### Lista zgłoszeń
- [ ] Widoczne tylko zgłoszenia własnego lokalu/klatki/budynku (nie cudze)
- [ ] Filtrowanie po statusie (chip) działa
- [ ] Wyszukiwanie po tytule/numerze działa
- [ ] `slaBreached = true` → zgłoszenie wyróżnione wizualnie
  > Aby wywołać: przez sekcję 14 ustaw SLA kategorii na 1 godzinę, utwórz zgłoszenie i poczekaj 1h+
- [ ] Mieszkaniec bez przypisanego lokalu → pusta lista (nie crash, nie `500`)

### Tworzenie zgłoszenia
- [ ] Kategorie ładują się z backendu (`GET /api/categories`)
- [ ] Utwórz zgłoszenie z tytułem, opisem i kategorią → `201 Created` → pojawia się na liście
- [ ] Puste wymagane pole (tytuł lub opis) → walidacja, przycisk zablokowany lub komunikat
- [ ] Mieszkaniec bez przypisanego lokalu → komunikat „brak przypisanego lokalu" (nie crash)

### Szczegóły zgłoszenia
- [ ] Otwórz zgłoszenie → wszystkie pola poprawnie wyświetlone
- [ ] Pole `internalNote` **NIE** jest widoczne dla MIESZKAŃCA (backend je zeruje)
- [ ] Komentarze typu `WEWNETRZNY` **NIE** są widoczne dla MIESZKAŃCA

### Testy uprawnień (MIESZKANIEC nie może)
- [ ] Próba przypisania konserwatora → `403`
- [ ] Próba zamknięcia/odrzucenia zgłoszenia → `403`
- [ ] Próba dostępu do cudzego zgłoszenia → `403` (nie crash)

---

## 3. 🔨 Zgłoszenia — KONSERWATOR

### Lista zgłoszeń
- [ ] Widoczne tylko zgłoszenia **przypisane** do tego konserwatora
- [ ] GET nieprzypisanego zgłoszenia → `403`

### Cykl życia zgłoszenia
- [ ] Status `ZAPLANOWANO` → przycisk „Rozpocznij prace" → `PATCH /api/tickets/{id}/start` → status zmienia się na `W_REALIZACJI`
- [ ] Status `W_REALIZACJI` → „Wstrzymaj" + podaj powód → `PATCH /api/tickets/{id}/suspend` → status `WSTRZYMANO`
- [ ] Status `WSTRZYMANO` → powrót do pracy → `PATCH /api/tickets/{id}/status` z ciałem `{"status": "W_REALIZACJI", "comment": null}` → `200 OK`, status `W_REALIZACJI`
- [ ] Status `W_REALIZACJI` → „Zakończ prace" + opis → `POST /api/tickets/{id}/completion` → status `ZAKONCZONE_DO_WERYFIKACJI`
- [ ] Próba wykonania `PATCH /api/tickets/{id}/close` (zamknięcie) → `403`
- [ ] Próba wykonania `PATCH /api/tickets/{id}/reject` (odrzucenie) → `403`

### Media i komentarze
- [ ] Dodaj komentarz `PUBLICZNY` → widoczny dla wszystkich ról
- [ ] Dodaj komentarz `WEWNETRZNY` → widoczny tylko dla ZARZADCY i KONSERWATORA, niewidoczny dla MIESZKAŃCA
- [ ] Upload zdjęcia BEFORE (JPEG lub PNG) → `200 OK`, zdjęcie widoczne w zgłoszeniu
- [ ] Upload zdjęcia AFTER (JPEG lub PNG) → `200 OK`
- [ ] Upload pliku zbyt dużego → `413` + czytelny komunikat
- [ ] Upload pliku złego formatu (np. PDF, WEBP) → `415` + komunikat

---

## 4. 👔 Zgłoszenia — ZARZADCA

### Lista zgłoszeń
- [ ] Widoczne **WSZYSTKIE** zgłoszenia z systemu
- [ ] Filtry działają: status, kategoria, budynek, klatka, konserwator, zakres dat, wyszukiwanie

### Przypisanie konserwatora
- [ ] Otwórz zgłoszenie w statusie `NOWE` → opcja „Przypisz konserwatora"
- [ ] Lista konserwatorów zawiera liczbę aktywnych zgłoszeń (pomaga wybrać mniej zajętego)
- [ ] Wybierz konserwatora + data planowanej wizyty + **notatka wewnętrzna** (opcjonalna) → `PATCH /api/tickets/{id}/assign` → status `ZAPLANOWANO`
- [ ] Weryfikacja: `internalNote` widoczna w szczegółach dla ZARZADCY i KONSERWATORA, **nie** dla MIESZKAŃCA

### Zamknięcie i odrzucenie
- [ ] Zgłoszenie w `ZAKONCZONE_DO_WERYFIKACJI` → „Zamknij" → `PATCH /api/tickets/{id}/close` → status `ZAMKNIETE`
  - PDF protokołu odbioru prac wygenerowany automatycznie
  - Protokół pojawia się w sekcji Dokumenty mieszkańca
- [ ] Zgłoszenie w `NOWE` → „Odrzuć" + powód → `PATCH /api/tickets/{id}/reject` → status `ODRZUCONE`
- [ ] Próba zamknięcia zgłoszenia w złym statusie (np. `NOWE`) → `409 Conflict`

### Testy uprawnień (ZARZADCA może wszystko)
- [ ] Każda akcja powyżej kończy się sukcesem bez `403`

---

## 5. 💰 Finanse

### MIESZKANIEC
- [ ] Historia transakcji + bieżące saldo → `GET /api/apartments/{id}/transactions`
- [ ] Wpłata (WPLATA) wyróżniona kolorem zielonym, naliczenie (NALICZENIE) czerwonym
- [ ] Pusta historia (nowy lokal) → czytelna informacja, nie błąd

### ZARZADCA
- [ ] Zestawienie zaległości → `GET /api/admin/apartments/balances`
- [ ] Filtry (propertyId, minDebt, minDaysOverdue) działają
- [ ] Pobierz PDF zestawienia → `GET /api/pdf/balances` → PDF pobrany (wymaga tokenu JWT)
- [ ] PDF z `?save=true` → dokument pojawia się w `/api/documents`
- [ ] Dodaj transakcję ręcznie → `POST /api/apartments/{id}/transactions` → pojawia się w historii mieszkańca
- [ ] Import CSV z poprawnymi danymi → wszystkie wiersze zaimportowane
- [ ] Import CSV z błędnymi wierszami → poprawne zaimportowane, błędne opisane w odpowiedzi
- [ ] Import pliku nie-CSV (np. XLSX, TXT) → `400` + komunikat błędu walidacji

### Testy uprawnień
- [ ] MIESZKANIEC → `POST /api/finance/import` → `403`
- [ ] KONSERWATOR → `POST /api/finance/import` → `403`
- [ ] MIESZKANIEC → transakcje **cudzego** lokalu → `403`

---

## 6. 🔢 Liczniki i Odczyty

### Liczniki (ZARZADCA)
- [ ] Lista liczników przypisanych do lokalu
- [ ] Dodaj licznik (numer seryjny, typ medium, data instalacji) → `201 Created`
- [ ] Dezaktywuj licznik → staje się nieaktywny, znika z listy aktywnych

### Odczyty (ZARZADCA i MIESZKANIEC)
- [ ] Lista odczytów z paginacją
- [ ] Dodaj odczyt z wartością dziesiętną (np. 123.456) → `201 Created` (BigDecimal, nie Double)
  - Ujemna wartość → `400 Bad Request`
- [ ] Pobierz konkretny odczyt → `GET /api/meter-readings/{id}` → `200 OK`
- [ ] Edytuj odczyt → `PUT /api/meter-readings/{id}` → zaktualizowana wartość
- [ ] Usuń odczyt → `DELETE /api/meter-readings/{id}` → `204 No Content`

### Testy uprawnień
- [ ] MIESZKANIEC → `POST /api/apartments/{id}/meters` (dodanie licznika) → `403`
- [ ] MIESZKANIEC → `PATCH /api/meters/{id}/deactivate` (dezaktywacja) → `403`
- [ ] MIESZKANIEC → `PUT /api/meter-readings/{id}` (edycja odczytu) → `403`
- [ ] MIESZKANIEC → `DELETE /api/meter-readings/{id}` (usunięcie odczytu) → `403`

---

## 7. 🏢 Nieruchomości, Budynki, Lokale (ZARZADCA)

### Nieruchomości
- [ ] Lista nieruchomości → `GET /api/properties`
- [ ] Szczegóły → `GET /api/properties/{id}`
- [ ] Utwórz nieruchomość (nazwa, adres, NIP) → `201 Created`
- [ ] Edytuj → `200 OK`
- [ ] Upload logo PNG lub JPEG → `PATCH /api/properties/{id}/logo`
  - Odpowiedź zwraca pełne dane nieruchomości z nową ścieżką logo (`PropertyResponseDto`)
  - Plik > 2 MB → błąd (kod 413 lub komunikat walidacji)
  - Plik PDF → `415` (tylko PNG/JPEG dozwolone)
  - Plik WEBP lub GIF → `415`

### Budynki
- [ ] Drzewo budynków → `GET /api/buildings/tree`
- [ ] Utwórz budynek (nazwa, adres, ewentualnie nieruchomość) → `201 Created`
- [ ] Edytuj → `200 OK`
- [ ] Usuń pusty budynek (bez klatek/lokali) → `204 No Content`
- [ ] Usuń budynek z powiązanymi lokalami → `409 Conflict`

### Klatki schodowe
- [ ] Utwórz klatkę w budynku → `201 Created`
- [ ] Edytuj etykietę → `200 OK`
- [ ] Usuń pustą → `204 No Content`
- [ ] Usuń z lokalami → `409 Conflict`

### Lokale
- [ ] Utwórz lokal w klatce (numer, piętro, m²) → `201 Created`
- [ ] Edytuj → `200 OK`
- [ ] Usuń → `204 No Content` (historyczne zgłoszenia pozostają)

---

## 8. 📢 Ogłoszenia

### Odczyt (każda rola)
- [ ] Lista ogłoszeń → `GET /api/announcements`
- [ ] Ogłoszenie z PDF → „Pobierz" → `GET /api/announcements/{id}/attachment` → PDF otwarty lub pobrany

### ZARZADCA — zarządzanie
- [ ] Utwórz ogłoszenie bez załącznika → `201 Created`
- [ ] Utwórz ogłoszenie z PDF < 10 MB → `201 Created`
- [ ] Upload PDF > 10 MB → błąd (komunikat o limicie rozmiaru)
- [ ] Upload pliku nie-PDF (np. JPG, DOCX) → `415` + komunikat
- [ ] Utwórz ogłoszenie z pustą treścią → `400 Bad Request` (walidacja)
- [ ] Edytuj ogłoszenie → `200 OK`
- [ ] Usuń ogłoszenie → `204 No Content`

### Testy uprawnień
- [ ] MIESZKANIEC → `POST /api/announcements` → `403`
- [ ] KONSERWATOR → `DELETE /api/announcements/{id}` → `403`

---

## 9. 🗳️ Uchwały

- [ ] Lista uchwał → `GET /api/resolutions` (filtrowana wg roli)
- [ ] Szczegóły uchwały + opcje głosowania + aktualne wyniki
- [ ] MIESZKANIEC oddaje głos → `POST /api/resolutions/{id}/vote` → `204 No Content`
- [ ] Ten sam MIESZKANIEC głosuje drugi raz → `409 Conflict`
- [ ] ZARZADCA tworzy uchwałę (tytuł, treść, opcje) → `201 Created`
- [ ] ZARZADCA pobiera raport PDF wyników → PDF z tabelą głosów
- [ ] MIESZKANIEC → `POST /api/resolutions` → `403`
- [ ] KONSERWATOR → `POST /api/resolutions` → `403`

---

## 10. 🔍 Przeglądy Techniczne

- [ ] Lista przeglądów → `GET /api/inspections`
- [ ] ZARZADCA: utwórz przegląd → `201 Created`
- [ ] ZARZADCA: edytuj → `200 OK`
- [ ] ZARZADCA: usuń → `204 No Content`
- [ ] KONSERWATOR: próba CREATE → `403`
- [ ] MIESZKANIEC: próba CREATE / UPDATE / DELETE → `403`

---

## 11. 📄 Dokumenty

- [ ] MIESZKANIEC: widzi tylko swoje dokumenty (powiązane z jego lokalem)
- [ ] ZARZADCA: widzi wszystkie dokumenty systemu
- [ ] Filtry (apartmentId, type, startDate, endDate) działają
- [ ] Pobierz PDF dokumentu → `GET /api/documents/{id}/download` → plik otwiera się lub pobiera
- [ ] ZARZADCA: dystrybucja zmiany stawek → odpowiedź zawiera `documentsGenerated` i `recipientsNotified`
- [ ] ZARZADCA: roczne rozliczenie → analogicznie do dystrybucji stawek

---

## 12. 🔔 Powiadomienia PUSH

> **Jak weryfikować PUSH:** Zaloguj się do Firebase Console → Twój projekt → Messaging → „Send test message" i wklej FCM token urządzenia. Lub użyj Postmana do Firebase HTTP v1 API.

- [ ] Zaloguj się → `POST /api/devices/register` wysłany z FCM tokenem → `204 No Content`
- [ ] Ten sam token zarejestrowany ponownie (ponowne logowanie) → `204 No Content` (idempotentność)
- [ ] Zmiana statusu zgłoszenia → PUSH dociera do autora zgłoszenia
- [ ] Wstrzymanie zgłoszenia → PUSH do ZARZADCY
- [ ] Zamknięcie + nowy dokument → PUSH do MIESZKAŃCA
- [ ] Wyloguj się → `DELETE /api/devices/{token}` → `204 No Content` → PUSH przestają docierać na to urządzenie
- [ ] Zaloguj na urządzeniu A, wyloguj na urządzeniu B → PUSH nadal docierają na A (tokeny niezależne)

---

## 13. 👥 Zarządzanie Użytkownikami (ZARZADCA)

> **Wykonaj tę sekcję jako pierwszą** jeśli konta testowe nie istnieją.

- [ ] Lista użytkowników → `GET /api/admin/users` → lista z rolami, statusami, lokalami
- [ ] Utwórz MIESZKAŃCA z przypisanym lokalem → `201 Created` → mail z zaproszeniem wysłany
- [ ] Utwórz KONSERVATORA → `201 Created` → mail z zaproszeniem
- [ ] Próba tworzenia z duplikatem email → `409 Conflict` + komunikat
- [ ] Edytuj użytkownika (imię, nazwisko, telefon, rola, lokal) → `200 OK`
- [ ] Dezaktywuj użytkownika → konto nieaktywne
- [ ] Zdezaktywowany próbuje się zalogować → `401` / komunikat o braku dostępu
- [ ] Pobierz listę konserwatorów z liczbą aktywnych zgłoszeń → `GET /api/users?role=KONSERWATOR`

---

## 14. ⚙️ Ustawienia Administracyjne

### Kategorie (ZARZADCA)
- [ ] Utwórz kategorię → `POST /api/admin/categories` → `201 Created`
- [ ] Edytuj kategorię → `PUT /api/admin/categories/{id}` → `200 OK`
- [ ] Ustaw SLA dla kategorii (np. 8 godzin) → `PATCH /api/admin/categories/{id}/sla` → `204 No Content`
- [ ] Dezaktywuj kategorię → `PATCH /api/admin/categories/{id}/deactivate` → `204 No Content` → kategoria znika z formularza tworzenia zgłoszenia

### Powiadomienia (ZARZADCA)
- [ ] Lista typów powiadomień → `GET /api/admin/notifications`
- [ ] Włącz/wyłącz typ → `PATCH /api/admin/notifications/{type}` → `200 OK`

---

## 15. 🔒 Bezpieczeństwo i Autoryzacja

### Dostęp bez tokenu JWT
- [ ] `GET /api/tickets` bez nagłówka `Authorization` → `401 Unauthorized`
- [ ] `GET /api/pdf/balances` → `401 Unauthorized` (PDF chronione JWT)
- [ ] `POST /api/pdf/work-acceptance-protocol` → `401 Unauthorized`
- [ ] `GET /api/admin/users` → `401 Unauthorized`

### Zła rola — `403 Forbidden`
- [ ] MIESZKANIEC → `POST /api/admin/categories` → `403`
- [ ] MIESZKANIEC → `POST /api/resolutions` → `403`
- [ ] MIESZKANIEC → `PATCH /api/tickets/{id}/assign` → `403`
- [ ] KONSERWATOR → `GET /api/admin/users` → `403`
- [ ] KONSERWATOR → `PATCH /api/tickets/{id}/close` → `403`
- [ ] KONSERWATOR → `PATCH /api/tickets/{id}/reject` → `403`
- [ ] KONSERWATOR → `POST /api/properties` → `403`
- [ ] MIESZKANIEC → `DELETE /api/announcements/{id}` → `403`

### Cudze zasoby
- [ ] MIESZKANIEC → `GET /api/apartments/{cudzeLokaleId}/transactions` → `403`
- [ ] KONSERWATOR → `GET /api/tickets/{nieprzypisaneId}` → `403`
- [ ] MIESZKANIEC → `GET /api/tickets/{cudzieZgloszenieId}` → `403`

### Path Traversal / wstrzyknięcia
- [ ] `GET /api/announcements/../../etc/passwd/attachment` → `400` lub `404` (nie crash)

---

## 16. 📊 PDF — Generowanie

- [ ] `GET /api/pdf/balances` (token ZARZADCA) → PDF sald pobrany poprawnie
- [ ] `GET /api/pdf/balances?save=true` → PDF + dokument pojawia się w `/api/documents`
- [ ] `POST /api/pdf/work-acceptance-protocol` z danymi zgłoszenia → PDF protokołu pobrany
- [ ] Zamknięcie zgłoszenia przez ZARZADCA → protokół wygenerowany automatycznie + dostępny dla MIESZKAŃCA w Dokumentach
- [ ] Raport uchwały → `GET /api/resolutions/{id}/report` → PDF z wynikami głosowania

---

## 📝 Historia zmian kodu

| # | Plik | Zmiana |
|---|---|---|
| 1 | `TicketDtos.kt` | `internalNote` w `TicketAssignRequest` + nowy `TicketStatusChangeRequest` |
| 2 | `TicketApiService.kt` | Nowy endpoint `changeStatus()` |
| 3 | `MeterApiService.kt` | `getMeterReadingById()` i `updateMeterReading()` |
| 4 | `PropertyApiService.kt` | `getPropertyById()` |
| 5 | `DocumentApiService.kt` | `uploadPropertyLogo()` → `PropertyResponseDto`, nowy `PdfApiService` |
| 6 | `MeterDtos.kt` | `value: Double` → `value: BigDecimal` w request |
| 7 | `NetworkModule.kt` | Rejestracja `PdfApiService` w Hilt |
| 8 | `SecurityConfig.java` | **🔴 KRYTYCZNE**: usunięto `/api/pdf/**` z `permitAll()` |
| 9 | `application.properties` | **🔴 KRYTYCZNE**: `spring.jackson.serialization.write-dates-as-timestamps=false` — daty jako ISO string |
| 10 | `AdminUserController.java` | **🔴 KRYTYCZNE**: `createUser`/`updateUser` zwraca `UserResponse` zamiast `Map<>` (brak pól `phone`, `apartmentId`) |
| 11 | `DocumentApiService.kt` | **🔴 KRYTYCZNE**: `WorkAcceptanceProtocolRequestDto` — poprawiono nazwy pól: `conservatorName` → `maintenanceWorkerName`, `beforeImages` → `beforeImagesPaths`, `afterImages` → `afterImagesPaths` |
| 12 | `TicketImageApiService.kt` | `@Query("image_type")` → `@Part("image_type") imageType: RequestBody` (multipart form field) |
| 13 | `TicketMediaServices.kt` | Zaktualizowano wywołanie `uploadImage()` — imageType jako `RequestBody` |
