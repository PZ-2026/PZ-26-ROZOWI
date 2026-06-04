# BlokUR — Frontend (Android)

Aplikacja mobilna BlokUR (Kotlin, Jetpack Compose, Hilt, Retrofit).

Pełna dokumentacja architektury UI i modułów: [`frontend.md`](frontend.md).

## Ograniczenia API backendu

Frontend jest dostosowany do **istniejącego** API — poniższe punkty opisują świadome decyzje i uniknięcie „martwych” wywołań.

| Obszar | Stan API | Zachowanie UI |
|--------|----------|---------------|
| **Profil użytkownika** | Brak `GET /api/users/me` | Rola i e-mail z sesji (`TokenStorage` po logowaniu). Imię/telefon tylko do odczytu — brak fałszywego zapisu. |
| **Zdjęcia zgłoszeń** | Brak `DELETE /api/images/{id}` | Brak przycisku usuwania; wyświetlanie przez `GET /api/images/{id}` (Coil + JWT). |
| **Lista zgłoszeń** | `GET /api/tickets` **bez** paginacji (`page`/`size` ignorowane) | Jednorazowe ładowanie pełnej listy; brak infinite scroll. |
| **Filtry zgłoszeń** | Query: `status`, `categoryId`, `buildingId`, `staircaseId`, `assignedTo`, `dateFrom`, `dateTo`, `search` | Panel rozszerzony dla ZARZĄDCA; pozostałe role: status + wyszukiwanie. |
| **Lokal mieszkańca** | Brak endpointu „mój lokal” | `UserApartmentService` — `apartmentId` z pierwszego zgłoszenia mieszkańca (`GET /api/tickets` → szczegóły). |
| **Push (FCM)** | `POST /api/devices/register`, `DELETE /api/devices/{token}` | Wymaga prawdziwego `google-services.json` z Firebase Console (patrz `app/google-services.README.md`). |
| **Logo wspólnoty** | `GET /api/properties`, `GET /api/properties/{id}`, `PATCH /api/properties/{id}/logo` | Wybór wspólnoty przy wielu rekordach; podgląd ze ścieżki `logoPath` na serwerze. |
| **Powiadomienia globalne** | `GET/PATCH /api/admin/notifications/settings` | Jedyny ekran: `NotificationsScreen` (nie hardkodowany ekran ustawień). |

## Konfiguracja

- **Backend URL:** `BuildConfig.BACKEND_URL` (Gradle / `local.properties`).
- **JDK do buildu:** 17 lub 21 (Kotlin/Gradle w projekcie nie wspiera JDK 25).
- **Firebase:** opcjonalny plik `app/google-services.json` dla push.

## Uruchomienie

```bash
cd frontend
./gradlew :app:assembleDebug
```
