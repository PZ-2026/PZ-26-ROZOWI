# Dokumentacja Frontendu Blokur — v2

> Dokument opisuje aktualny stan kodu po refaktoryzacji UI/UX (czerwiec 2026).  
> Poprzednia wersja: `frontend.md`

---

## Technologie i zależności

| Warstwa | Technologia |
|---------|-------------|
| Język | **Kotlin** |
| UI Framework | **Jetpack Compose** (Material Design 3) |
| Nawigacja | **Navigation Compose** + typesafe routes (`@Serializable`) |
| Dependency Injection | **Hilt** (`@HiltViewModel`, `hiltViewModel()`) |
| Sieć | **Retrofit 2** + **OkHttp 3** (z interceptorami) |
| Autoryzacja | JWT (access token + refresh token) przechowywany w `DataStore` |
| Async | **Kotlin Coroutines** + **StateFlow** / **Channel** |
| Minimalna wersja Android | SDK 26 (Android 8.0) |

---

## Struktura katalogów

Cały kod aplikacji mieści się w jednym module `app`, w pakiecie głównym `pl.edu.ur.blokur`.

```text
app/src/main/java/pl/edu/ur/blokur/
│
├── AppNavHost.kt                  ← Globalny NavHost łączący wszystkie grafy nawigacji
├── BlokurApp.kt                   ← Klasa Application (inicjalizacja Hilt)
├── MainActivity.kt                ← Punkt wejścia aplikacji
│
├── dtos/                          ← Modele danych (Request / Response)
│   ├── AuthDtos.kt
│   ├── TicketDtos.kt
│   ├── TicketMediaDtos.kt
│   ├── PropertyDtos.kt
│   ├── FinancialDtos.kt
│   ├── InspectionDtos.kt
│   ├── ResolutionDtos.kt
│   ├── UserDtos.kt
│   └── ...
│
├── services/                      ← Warstwa dostępu do danych i logiki sieciowej
│   ├── *ApiService.kt             ← Interfejsy Retrofit (definicje endpointów HTTP)
│   ├── *Service.kt                ← Klasy pośredniczące / repozytoria
│   ├── NetworkModule.kt           ← Konfiguracja Retrofit + OkHttp (Hilt module)
│   ├── TokenStorage.kt            ← Przechowywanie JWT w DataStore
│   ├── TokenInterceptor.kt        ← Dołączanie Bearer token do każdego żądania
│   └── TokenAuthenticator.kt      ← Automatyczne odświeżanie access tokena (401 handler)
│
└── ui/
    ├── components/                ← Globalne, reużywalne komponenty UI
    │   ├── Buttons.kt             ← PrimaryButton, SecondaryButton, BlokurFab
    │   ├── Cards.kt               ← NormalCard
    │   ├── EmptyState.kt          ← Widok pustego stanu (ikona + tekst)
    │   ├── LoadingIndicator.kt    ← Kółko ładowania
    │   ├── TextField.kt           ← Pola tekstowe
    │   └── TopBar.kt              ← Górny pasek nawigacji (TopAppBar)
    │
    ├── navigation/
    │   └── AppRoute.kt            ← Bazowy sealed interface tras nawigacji
    │
    ├── theme/
    │   ├── Color.kt               ← Paleta kolorów (stałe + kolory semantyczne)
    │   ├── Theme.kt               ← MaterialTheme (Light + Dark mode)
    │   └── Type.kt                ← Typografia (skala tekstów Material 3)
    │
    └── views/                     ← Ekrany pogrupowane per funkcjonalność
        ├── auth/
        ├── announcements/
        ├── categories/
        ├── documents/
        ├── finances/
        ├── inspections/
        ├── main/
        ├── meters/
        ├── notifications/
        ├── profile/
        ├── properties/
        ├── resolutions/
        ├── settings/
        ├── tickets/
        └── users/
```

---

## Architektura — wzorzec MVVM

Każdy moduł funkcjonalny stosuje ten sam wzorzec warstwowy:

```
Screen.kt  ←  (collectAsState)  ←  ViewModel.kt
   │                                     │
   │  wywołuje callback                  │  wywołuje
   ▼                                     ▼
Content.kt / Component.kt          Service.kt / ApiService.kt
                                         │
                                         ▼
                                    Backend REST API
```

### Konwencja plików w każdym module `views/<feature>/`:

| Folder | Zawartość |
|--------|-----------|
| `screens/` | Scaffold + logika zbierania stanu z VM; punkt wejścia nawigacji |
| `contents/` | Bezstanowe układy wizualne (przyjmują dane jako parametry) |
| `components/` | Drobne, lokalne elementy specyficzne dla danego modułu |
| `viewmodels/` | ViewModele (`StateFlow`, `Channel` na eventy) |
| `utils/` | Sealed class States, Events, helpery mapowania |
| `*Navigation.kt` | Definicja tras i composable wpisów do NavHost |

---

## System motywu (Material Design 3)

### Kolory (`Color.kt`)
- Zdefiniowane jako stałe Kotlin (`val InfoBlue = Color(0xFF2563EB)`)
- Używane przez tokeny motywu, **nie hardcodowane** bezpośrednio w komponentach

### Motyw (`Theme.kt`)
- `LightColorScheme` i `DarkColorScheme` – pełne zestawy tokenów M3
- Automatyczne przełączanie w oparciu o `isSystemInDarkTheme()`
- Używany przez każdy Screen poprzez `MaterialTheme.colorScheme.*`

### Typografia (`Type.kt`)
- Skala Material 3: `titleLarge`, `titleMedium`, `bodyLarge`, `bodyMedium`, `labelSmall` itp.
- Używana spójnie w całym projekcie

---

## Nawigacja

### `AppNavHost.kt`
Globalny korzeń nawigacji. Montuje:
1. `authGraph` – ekrany przed zalogowaniem
2. `mainGraph` – główna powłoka po zalogowaniu (z zagnieżdżonym NavHostem per rola)

### Zagnieżdżony NavHost (wewnątrz `mainGraph`)
Po zalogowaniu każda rola użytkownika widzi **te same grafy**, ale `BottomNavBar` filtruje dostępne zakładki:

| Rola | Zakładki w NavBar |
|------|------------------|
| `MIESZKANIEC` | Zgłoszenia · Finanse · Uchwały · Ogłoszenia · Profil |
| `ZARZADCA` | Zgłoszenia · Lokale · Uchwały · Użytkownicy · Profil |
| `KONSERWATOR` | Zgłoszenia · Profil |

Dodatkowe ekrany zarządcy dostępne przez **ekran Profilu** (sekcja „Ustawienia zarządcy"):
- Ustawienia powiadomień
- Dystrybucja dokumentów
- Harmonogram przeglądów
- Kategorie zgłoszeń
- Logo wspólnoty

### Typesafe routes
Trasy definiowane jako `sealed interface` z `@Serializable data object`:
```kotlin
sealed interface TicketRoutes : AppRoute {
    @Serializable data object List : TicketRoutes
    @Serializable data object Create : TicketRoutes
    @Serializable data class Detail(val id: String) : TicketRoutes
}
```

---

## Warstwa sieciowa (`services/`)

### Autoryzacja
- `TokenInterceptor` – dołącza `Authorization: Bearer <token>` do każdego żądania
- `TokenAuthenticator` – przy odpowiedzi `401` automatycznie odświeża access token używając refresh tokena i ponawia żądanie
- `TokenStorage` – DataStore Preferences (klucze: `accessToken`, `refreshToken`, `userRole`)

### Serwisy API (Retrofit interfaces)

| Plik | Endpointy |
|------|-----------|
| `AuthApiService.kt` | `POST /auth/login`, `POST /auth/refresh`, `POST /auth/forgot-password`, `POST /auth/reset-password` |
| `TicketApiService.kt` | CRUD zgłoszeń + zmiana statusów (`/api/tickets/**`) |
| `TicketCommentApiService.kt` | Komentarze do zgłoszeń |
| `TicketImageApiService.kt` | Upload i pobieranie zdjęć do zgłoszeń |
| `FinancialApiService.kt` | Transakcje, salda, import CSV (`/api/apartments/**/transactions`, `/api/finance/import`) |
| `AnnouncementApiService.kt` | Lista ogłoszeń (`/api/announcements`) |
| `ResolutionApiService.kt` | Uchwały wspólnoty i głosowanie |
| `MeterApiService.kt` | Liczniki i odczyty |
| `UserDocumentApiService.kt` | Dokumenty PDF użytkownika |
| `DocumentApiService.kt` | Dystrybucja dokumentów (zarządca) |
| `PropertyApiService.kt` | Drzewo nieruchomości |
| `AdminUserApiService.kt` | Zarządzanie użytkownikami (admin) |
| `InspectionApiService.kt` | Przeglądy techniczne |
| `NotificationApiService.kt` | Ustawienia powiadomień |
| `CommunityLogoApiService.kt` | Logo wspólnoty |
| `CategoryApiService.kt` | Kategorie zgłoszeń |

---

## Moduły funkcjonalne — opis szczegółowy

### 1. Uwierzytelnianie (`auth/`)

**Ekrany:** `LoginScreen`, `ForgotPasswordScreen`, `ResetPasswordScreen`

| Funkcja | Opis |
|---------|------|
| Logowanie | Email + hasło → JWT access + refresh token → zapis w DataStore → redirect per rola |
| Zapomniałem hasła | Wysyła email z linkiem do resetu (token jednorazowy) |
| Reset hasła | Formularz nowego hasła z tokenem z emaila |

---

### 2. Główna powłoka (`main/`)

**Pliki kluczowe:** `ResidentMainScreen.kt`, `BottomNavBar.kt`, `Data.kt`, `ResidentMainStates.kt`

- `ResidentMainScreen` – Scaffold z `TopBar` (tytuł aktywnej zakładki + przycisk wylogowania) i `BottomNavBar`
- `BottomNavBar` – renderuje wyłącznie zakładki z `availableNavItems` (filtrowane per rola przez ViewModel)
- `ResidentMainViewModel` – zarządza aktywnym stanem (`ResidentMainState`) i listą zakładek

---

### 3. Zgłoszenia serwisowe (`tickets/`)

**Ekrany:** `TicketsScreen`, `TicketDetailsScreen`, `CreateTicketScreen`

| Funkcja | Role | Opis |
|---------|------|------|
| Lista zgłoszeń | Wszyscy | Zgłoszenia filtrowane wg statusu i kategorii, z pagingiem |
| Panel filtrów | Wszyscy | `TicketFilterPanel` – dolny sheet z filtrami statusu/kategorii |
| Szczegóły zgłoszenia | Wszyscy | Historia statusów, opis, zdjęcia, komentarze |
| Tworzenie zgłoszenia | Mieszkaniec | Tytuł, kategoria (lista z API), opis + opcjonalne zdjęcia |
| Przypisanie konserwatora | Zarządca | `AssignConservatorSheet` – wybór konserwatora + data planowana |
| Odrzucenie zgłoszenia | Zarządca | `ManagerRejectSheet` – wymagana przyczyna odrzucenia |
| Akcje konserwatora | Konserwator | `ConservatorActionSheet` – Start/Wstrzymaj/Zakończ pracę |
| Upload zdjęć | Wszyscy | Galeria lub aparat → multipart upload |

**Cykl życia zgłoszenia (statusy):**
```
NOWE → ZAAKCEPTOWANE → PRZYPISANE → W_TRAKCIE → [WSTRZYMANE →] ZAKONCZONE
                  └→ ODRZUCONE
```

---

### 4. Finanse (`finances/`)

**Ekrany:** `FinancesScreen`, `TransactionsScreen`, `DocumentsScreen`, `ApartmentBalancesScreen`, `FinancialLedgerScreen`, `CsvImportScreen`

| Funkcja | Role | Opis |
|---------|------|------|
| Saldo i historia | Mieszkaniec | Aktualne saldo lokalu + lista transakcji |
| Dokumenty finansowe | Mieszkaniec | Zawiadomienia o stawkach, rozliczenia roczne (PDF) |
| Przegląd zaległości | Zarządca | `ApartmentBalancesScreen` – lista lokali z saldem i zaległościami |
| Księga finansowa | Zarządca | `FinancialLedgerScreen` – pełna historia transakcji dla lokalu |
| Ręczna transakcja | Zarządca | `AddTransactionDialog` – dodanie wpłaty/obciążenia |
| Import CSV | Zarządca | `CsvImportScreen` – masowy import wyciągu bankowego (multipart) |

---

### 5. Ogłoszenia (`announcements/`)

**Ekrany:** `AnnouncementsScreen`

- Lista ogłoszeń wspólnoty pobierana z `GET /api/announcements`
- Widoczna dla mieszkańców jako jedna z głównych zakładek

---

### 6. Uchwały wspólnoty (`resolutions/`)

**Ekrany:** `ResolutionsListScreen`, `ResolutionDetailScreen`, `CreateResolutionDialog`

| Funkcja | Role | Opis |
|---------|------|------|
| Lista uchwał | Wszyscy | Uchwały z datą, tytułem i statusem głosowania |
| Szczegóły uchwały | Wszyscy | Pełna treść, wyniki głosowania (Za/Przeciw/Wstrzymało się) |
| Głosowanie | Mieszkaniec | Przycisk za / przeciw / wstrzymaj się |
| Tworzenie uchwały | Zarządca | `CreateResolutionDialog` – tytuł, treść, termin głosowania |

---

### 7. Lokale — drzewo nieruchomości (`properties/`)

**Dostęp:** Tylko `ZARZADCA`

**Ekrany:** `PropertyTreeScreen`, `PropertyDetailPanel`, `PropertyTreeView`

| Funkcja | Opis |
|---------|------|
| Widok drzewa | Akordeony: Wspólnota → Budynek → Klatka → Lokal |
| Dodawanie węzłów | `ModalBottomSheet` z dynamicznym formularzem per poziom hierarchii |
| Edycja węzłów | Ten sam panel z wypełnionymi danymi |
| Walidacja NIP | Obsługa `422 Unprocessable Content` z komunikatem dla użytkownika |

---

### 8. Użytkownicy (`users/`)

**Dostęp:** Tylko `ZARZADCA`

**Ekrany:** `UsersScreen`, `CreateUserDialog`

| Funkcja | Opis |
|---------|------|
| Lista użytkowników | Wszyscy z rolą, statusem (aktywny/nieaktywny) i przypisanym lokalem |
| Tworzenie konta | `CreateUserDialog` – dane + rola + opcjonalny lokal |
| Edycja konta | Inline edit z walidacją |
| Deaktywacja konta | Miękkie usunięcie (`is_active = false`), historia zachowana |

---

### 9. Kategorie zgłoszeń (`categories/`)

**Dostęp:** Tylko `ZARZADCA` (przez Profil)

**Ekrany:** `CategoriesScreen`, `CategoryFormDialog`

| Funkcja | Opis |
|---------|------|
| Lista kategorii | Aktywne kategorie z wartością SLA |
| Tworzenie kategorii | `CategoryFormDialog` – nazwa kategorii |
| Edycja nazwy | Ten sam dialog z trybem edycji |
| Ustawienie SLA | `SlaEditDialog` – czas reakcji w godzinach |
| Deaktywacja | Kategoria ukryta przed mieszkańcami przy tworzeniu zgłoszeń |

---

### 10. Harmonogram przeglądów (`inspections/`)

**Dostęp:** Tylko `ZARZADCA` (przez Profil)

**Ekrany:** `InspectionsListScreen`, `CreateInspectionDialog`

| Funkcja | Opis |
|---------|------|
| Lista przeglądów | Zaplanowane i zakończone przeglądy z datą i zakresem |
| Tworzenie przeglądu | `CreateInspectionDialog` – tytuł, opis, data, zakres (lokal/budynek/nieruchomość) |

---

### 11. Liczniki (`meters/`)

**Ekrany:** `MeterListScreen`, `MeterDetailScreen`, `CreateMeterDialog`, `CreateMeterReadingDialog`

| Funkcja | Role | Opis |
|---------|------|------|
| Lista liczników | Zarządca/Mieszkaniec | Liczniki przypisane do lokalu |
| Szczegóły licznika | Wszyscy | Historia odczytów w czasie |
| Dodawanie licznika | Zarządca | Typ medium, numer seryjny |
| Dodawanie odczytu | Mieszkaniec/Zarządca | Wartość + data odczytu |

---

### 12. Profil (`profile/`)

**Ekrany:** `ProfileScreen`

| Funkcja | Role | Opis |
|---------|------|------|
| Wyświetlanie danych | Wszyscy | Imię i nazwisko, email (readonly) |
| Edycja imienia | Wszyscy | Pole tekstowe + dialog potwierdzenia |
| Test powiadomień | Wszyscy | Wysyła push notyfikację testową |
| Sekcja zarządcy | Zarządca | Kafelki nawigacyjne do: Powiadomień, Dokumentów, Przeglądów, Kategorii, Logo |
| Wylogowanie | Wszyscy | Kasuje tokeny z DataStore, redirect do LoginScreen |

---

### 13. Ustawienia (`settings/`)

**Dostęp:** Tylko `ZARZADCA` (przez Profil → Ustawienia powiadomień / Logo)

| Ekran | Opis |
|-------|------|
| `NotificationSettingsScreen` | Konfiguracja typów alertów per zdarzenie (zgłoszenie, przegląd itp.) |
| `CommunityLogoScreen` | Upload logo wspólnoty (obraz PNG/JPG, multipart) |

---

### 14. Dystrybucja dokumentów (`documents/`)

**Dostęp:** Tylko `ZARZADCA` (przez Profil → Dystrybucja dokumentów)

**Ekrany:** `DocumentDistributionScreen`

- Wysyłanie dokumentów PDF (zawiadomień o stawkach, rozliczeń) do wybranych lokatorów

---

### 15. Powiadomienia push (`notifications/`)

**Ekrany:** `NotificationsScreen`

- Wyświetlanie historii otrzymanych powiadomień push
- Dostępne po wejściu przez link z `SettingsNavigation`

---

## Globalne komponenty UI (`ui/components/`)

| Komponent | Opis |
|-----------|------|
| `PrimaryButton` | Wypełniony przycisk z kolorem `primary` |
| `SecondaryButton` | Przycisk konturowy (`OutlinedButton`) |
| `BlokurFab` | FloatingActionButton z ikoną `Icons.Rounded.Add` |
| `NormalCard` | Karta (`Card`) z paddingiem i zaokrąglonymi narożnikami |
| `EmptyState` | Ilustracyjna ikona + tytuł + opis dla pustych list |
| `LoadingIndicator` | `CircularProgressIndicator` wycentrowany na całym ekranie |
| `TopBar` | `TopAppBar` z tytułem, `navigationIcon` i `actions` |

---

## Konta testowe (seed bazy danych)

Hasło dla wszystkich kont: **`haslo123`**

| Email | Rola |
|-------|------|
| `admin1@blokur.pl` | ZARZADCA |
| `admin2@blokur.pl` | ZARZADCA |
| `hydraulik@blokur.pl` | KONSERWATOR |
| `elektryk@blokur.pl` | KONSERWATOR |
| `serwis@blokur.pl` | KONSERWATOR |
| `jan.kowalski@gmail.com` | MIESZKANIEC |
| `anna.nowak@poczta.pl` | MIESZKANIEC |
| `piotr.wisniewski@wp.pl` | MIESZKANIEC |
| `maria.dabrowska@onet.pl` | MIESZKANIEC |
| `krzysztof.lewandowski@interia.pl` | MIESZKANIEC |
