# Architektura Frontendu (Android)

Aplikacja mobilna **Blokur** posiada zunifikowaną, płaską architekturę. Całość kodu znajduje się w jednym głównym module **`app`**, zorganizowanym wokół koncepcji "Feature-Driven Development".

## Struktura Katalogów

Wszystkie pakiety znajdują się wewnątrz `pl.edu.ur.blokur`.

```text
app/src/main/java/pl/edu/ur/blokur/
├── dtos/          ← Modele danych służące do komunikacji z API
├── services/      ← Serwisy sieciowe, Retrofit API oraz repozytoria danych
├── ui/            ← Interfejs użytkownika (Jetpack Compose)
│   ├── components/  ← Globalne reużywalne widżety (przyciski, karty, pola wejściowe)
│   ├── navigation/  ← Globalny system nawigacji (definicje tras)
│   ├── theme/       ← Konfiguracja motywu Material Design 3 (kolory, typografia)
│   └── views/       ← Kod ekranów pogrupowany na poszczególne funkcjonalności (Features)
```

## Warstwy aplikacji

Mimo zastosowania jednego modułu fizycznego, projekt logicznie dzieli się na trzy podstawowe obszary:

### 1. DTOs (`dtos/`)
Proste i niezmienne struktury danych (`data class` z odpowiednimi adnotacjami dla Gson/Moshi). 
Zawierają zarówno modele wysyłane (Request), jak i odbierane (Response). Pliki pogrupowane są tematycznie, np.:
- `AuthDtos.kt`
- `TicketDtos.kt`
- `PropertyDtos.kt`

### 2. Services (`services/`)
Obejmuje całą logikę dostępu do danych oraz reguły biznesowe:
- **`*ApiService.kt`** – interfejsy Retrofit definiujące endpointy HTTP.
- **`*Service.kt`** – klasy pośredniczące (często pełniące funkcję repozytoriów lub use casów). Wykonują mapowanie danych, wywołują zapytania sieciowe, obsługują rzucanie błędów (np. wyrzucanie wyjątków przy kodzie HTTP 400).
- Konfiguracja sieciowa (`NetworkModule.kt`, `TokenInterceptor.kt` itp.).

### 3. Prezentacja (`ui/views/`)
Ekrany i ich logika wizualna. Każdy folder funkcjonalności (np. `tickets/`) podzielony jest na:
- **`components/`** - proste, bezstanowe elementy charakterystyczne tylko dla tego modułu (np. element listy zgłoszeń).
- **`contents/`** - układy wizualne grupujące komponenty, np. cała lista zgłoszeń, czy fragment z detalami, pozbawiona ścisłego powiązania ze stanem.
- **`screens/`** - główne korzenie ekranów (tzw. `Scaffold`), które zajmują się pobieraniem stanu z viewmodeli i wstrzykiwaniem ich do widoków (`contents/`).
- **`utils/`** - klasy pomocnicze, definicje zdarzeń (Events) i stanów UI (States).
- **`viewmodels/`** - klasy rozszerzające `ViewModel`, zarządzające danymi specyficznymi dla interfejsu (poprzez `StateFlow`), operujące na `Services`.

Każdy moduł (funkcjonalność) definiuje własny schemat nawigacji (np. `TicketNavigation.kt`), z którego globalny host (`AppNavHost.kt`) korzysta by zbudować zagnieżdżoną hierarchię stron.

---

## Moduły funkcjonalne (Kategorie funkcjonalności)

Poniżej przedstawiono zbiór wszystkich aktualnych funkcjonalności w systemie, podzielonych na katalogi, w których są obsługiwane.

### Uwierzytelnianie i autoryzacja (`auth/`)
- Logowanie do systemu wraz ze sprawdzaniem roli konta (Mieszkaniec, Zarządca, Konserwator).
- **Zapomniane hasło i resetowanie** - mechanizm odzyskiwania dostępu na podstawie tokenu resetującego wysyłanego poprzez email (wykorzystuje `AuthService.kt`).

### Główne menu - Pasek Mieszkańca/Zarządcy (`main/`)
- Dolny pasek nawigacyjny (`BottomNavBar`) pozwalający na łatwe przełączanie pomiędzy najważniejszymi widokami.
- Warunkowe wyświetlanie zakładek na podstawie roli zalogowanego użytkownika (np. "Nieruchomości" dostępne wyłącznie dla Zarządcy).
- Automatyczne sterowanie ramką i zawartością poprzez `ResidentMainScreen`.

### Ogłoszenia (`announcements/`)
- Lista ogłoszeń dla mieszkańców z poziomu Zarządcy/Wspólnoty.

### Profil Użytkownika (`profile/`)
- Wyświetlanie danych aktualnego użytkownika.
- Wylogowanie z systemu (kasowanie danych sesyjnych za pomocą `TokenStorage`).

### Finanse (`finances/`)
- Wyświetlanie aktualnego salda użytkownika.
- Wykaz opłat oraz historii wpłat na koncie mieszkańca.

### Zgłoszenia Serwisowe (`tickets/`)
- Lista wszystkich zgłoszeń z podziałem na statusy i kategorie.
- Widok szczegółowy pojedynczego zgłoszenia z historią zdarzeń i możliwością weryfikacji progresu.
- Formularz dodawania nowego zgłoszenia (Tytuł, kategoria, opis awarii).

### Nieruchomości / Zarządzanie (`properties/`)
- **Dostępność**: Tylko Zarządca
- Wyświetlanie w widoku hierarchicznego **Drzewa Nieruchomości** (`PropertyTreeScreen`).
- Struktura wizualna typu akordeon odzwierciedla relację: Wspólnota → Budynek → Klatka → Lokal.
- **Dodawanie, edytowanie formularzy**: Wysuwany z dołu ekranu inteligentny panel (`ModalBottomSheet`), w którym na podstawie wybranego węzła dynamicznie generowany jest odpowiedni formularz (`PropertyDetailPanel`).
- Obsługa błędów, walidacja zajętości numeru NIP (obsługa `422 Unprocessable Content` połączona z wyjątkami domenowymi) wraz z obsługą po stronie UI poprzez powiadomienia kontekstowe.

# Użytkownicy (backend seed)
`
-- ==========================================
-- UŻYTKOWNICY (Hasło dla wszystkich: haslo123)
-- ==========================================
INSERT INTO users (email, password_hash, first_name, last_name, role, is_active) VALUES
('admin1@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Andrzej', 'Zarządczy', 'ZARZADCA', true), -- Hasło: haslo123
('admin2@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Beata', 'Wspólnotowa', 'ZARZADCA', true), -- Hasło: haslo123
('hydraulik@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Marian', 'Rura', 'KONSERWATOR', true),
('elektryk@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Zenon', 'Kabel', 'KONSERWATOR', true),
('serwis@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Adam', 'Złota-Rączka', 'KONSERWATOR', true),
('jan.kowalski@gmail.com', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Jan', 'Kowalski', 'MIESZKANIEC', true),
('anna.nowak@poczta.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Anna', 'Nowak', 'MIESZKANIEC', true),
('piotr.wisniewski@wp.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Piotr', 'Wiśniewski', 'MIESZKANIEC', true),
('maria.dabrowska@onet.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Maria', 'Dąbrowska', 'MIESZKANIEC', true),
('krzysztof.lewandowski@interia.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Krzysztof', 'Lewandowski', 'MIESZKANIEC', true);
`
