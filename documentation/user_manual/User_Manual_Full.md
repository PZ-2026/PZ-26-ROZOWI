# BLOKUR - Kompletna Instrukcja Użytkownika

Witamy w instrukcji użytkownika aplikacji **BLOKUR**! Ten dokument ma na celu przeprowadzenie Cię przez podstawowe kroki związane z uzyskaniem dostępu do platformy oraz korzystaniem ze wszystkich jej funkcjonalności, zależnie od przypisanej Ci roli.

Aplikacja **BLOKUR** to kompleksowy system do zarządzania wspólnotami mieszkaniowymi. Dostępne w niej funkcje różnią się w zależności od przypisanej Ci roli:
- **Mieszkaniec** – zgłaszanie usterek, podgląd finansów i głosowanie nad uchwałami.
- **Konserwator** – odbiór i realizacja przypisanych zgłoszeń technicznych.
- **Zarządca** – pełny moduł administracyjny (zarządzanie lokalami, zgłoszeniami, użytkownikami i powiadomieniami).

> [!NOTE]
> **Dane Testowe (Środowisko Mockowe)**
> Ponieważ instrukcja bazuje na środowisku testowym (Mock z backendu), możesz logować się przy użyciu poniższych danych (hasło dla wszystkich to: **haslo123**):
> - **Zarządca:** `admin1@blokur.pl`, `admin2@blokur.pl`
> - **Konserwator:** `hydraulik@blokur.pl`, `elektryk@blokur.pl`, `serwis@blokur.pl`
> - **Mieszkaniec:** `jan.kowalski@gmail.com`, `anna.nowak@poczta.pl`, `piotr.wisniewski@wp.pl`

---

## Rozdział 1: Logowanie i Pierwsze Kroki

### 1.1. Ekran Logowania

Aby rozpocząć korzystanie z aplikacji, uruchom ją na swoim urządzeniu mobilnym. Jako pierwszy zobaczysz ekran logowania oznaczony logo **BlokUR**.

1. W polu **Adres e-mail** wpisz swój adres podany administratorowi wspólnoty (zarządcy).
2. W polu **Hasło** wpisz swoje aktualne hasło.
3. Kliknij przycisk **Zaloguj się**.

Jeżeli poświadczenia są prawidłowe, aplikacja automatycznie przekieruje Cię do odpowiedniego panelu głównego, właściwego dla Twojej roli (Mieszkaniec, Konserwator lub Zarządca).

> [!TIP]
> Jeśli masz problem z logowaniem (np. wpiszesz złe hasło), na dole ekranu pojawi się odpowiedni komunikat z informacją o błędzie. Po kilku nieudanych próbach logowania konto może zostać tymczasowo zablokowane ze względów bezpieczeństwa.

### 1.2. Odzyskiwanie hasła

Jeżeli zapomniałeś swojego hasła, nie musisz kontaktować się z zarządcą – możesz je zresetować samodzielnie.

1. Na ekranie logowania, tuż pod formularzem, kliknij w przycisk **Zapomniałeś hasła?**.
2. Zostaniesz przekierowany do ekranu odzyskiwania.
3. Podaj adres e-mail przypisany do Twojego konta i zatwierdź przyciskiem **Zresetuj hasło**.
4. Otwórz swoją skrzynkę pocztową. Jeżeli podany e-mail istnieje w bazie, otrzymasz wiadomość z unikalnym linkiem.
5. Po kliknięciu w link z wiadomości e-mail będziesz mógł nadać nowe hasło (wymagane jest minimum 8 znaków).
6. Po zapisaniu nowego hasła możesz powrócić do aplikacji i zalogować się ponownie.

### 1.3. Pierwsze logowanie (Akceptacja Zaproszenia)

Zarządca może założyć dla Ciebie konto i wysłać Ci zaproszenie drogą mailową.
Proces pierwszego logowania wygląda następująco:
1. Otrzymujesz wiadomość e-mail z linkiem aktywacyjnym.
2. Klikasz w link, który przenosi Cię do formularza aktywacji konta.
3. Definiujesz swoje własne, unikalne hasło (minimum 8 znaków).
4. Po zatwierdzeniu hasła, Twoje konto zostaje aktywowane i możesz się zalogować.

> [!IMPORTANT]
> Aplikacja BLOKUR dba o bezpieczeństwo Twojej sesji. Jeśli nie będziesz używać aplikacji przez bardzo długi czas lub Twój token dostępu straci ważność, aplikacja automatycznie Cię wyloguje ze względów bezpieczeństwa i poprosi o ponowne zalogowanie.

---

## Rozdział 2: Panel Mieszkańca

Panel Mieszkańca to serce aplikacji dla właścicieli oraz najemców lokali. Udostępnia on wszystkie narzędzia potrzebne do wygodnego funkcjonowania we wspólnocie mieszkaniowej, bezpośrednio z poziomu telefonu.

### 2.1. Zgłoszenia (Usterki)
Zakładka pozwala na zgłaszanie usterek do administracji. Po wejściu w zakładkę zobaczysz listę swoich zgłoszeń posortowaną od najnowszych. 

> [!NOTE]
> Logując się jako `jan.kowalski@gmail.com` (hasło: `haslo123`), zobaczysz na liście zgłoszenie **ZGL/2026/001** ("Wyciek pod zlewem"). Status tego zgłoszenia to `W realizacji`, a przypisany pracownik to `Marian Rura` (konserwator).

**Dodawanie nowego zgłoszenia:**
1. Kliknij niebieski przycisk z ikoną **"+"** (plus) w prawym dolnym rogu ekranu.
2. Wybierz odpowiednią **kategorię** usterki z listy rozwijanej.
3. Wpisz **tytuł** oraz **opis** problemu. 
4. Zrób zdjęcie usterki lub wybierz je z galerii.
5. Wybierz miejsce występowania (Twój lokal lub klatka schodowa).
6. Kliknij **Wyślij**.

**Komunikacja:** Wchodząc w szczegóły zgłoszenia, zobaczysz pełną historię naprawy. Możesz wymieniać komentarze z administratorem i konserwatorem, a po naprawie zobaczysz zdjęcia "Po" wykonane przez konserwatora.

### 2.2. Finanse
Zakładka to Twoje wirtualne saldo księgowe we wspólnocie.
- **Saldo bieżące:** Kwota na zielono oznacza nadpłatę, na czerwono niedopłatę.
- **Księga finansowa:** Historia Twoich transakcji (Naliczenia czynszowe, Wpłaty za przelewy, Korekty).

> [!NOTE]
> Dla konta `jan.kowalski@gmail.com` saldo uwzględnia transakcje z kwietnia 2026:
> - Naliczenie: "Czynsz 04/2026" (-450.00 zł)
> - Wpłata: "Przelew Czynsz 04/2026" (+450.00 zł)
> - Naliczenie: "Rozliczenie wody 03/2026" (-12.50 zł)

### 2.3. Uchwały (Głosowania)
Jako mieszkaniec możesz decydować o sprawach wspólnoty głosując nad uchwałami (np. "Fundusz remontowy 2026").
Z listy aktywnych uchwał wybierz jedną, zapoznaj się z opisem i kliknij **ZA**, **PRZECIW** lub **WSTRZYMUJĘ SIĘ**. 

> [!WARNING]
> Zgodnie z zasadami działania aplikacji (i logiką blockchain), raz oddanego głosu **nie można cofnąć ani zmienić**.

### 2.4. Ogłoszenia
Moduł zastępuje tradycyjną tablicę korkową. Znajdziesz tu komunikaty od Zarządcy, przerwy w dostawie wody czy zaproszenia na zebrania. Otrzymujesz powiadomienia Push o nowych wpisach.

### 2.5. Profil i Ustawienia
W zakładce Profil możesz sprawdzić swoje dane, zmienić hasło, skonfigurować powiadomienia, a także przejrzeć podpięte do mieszkania liczniki (np. `WOD-ZIM-001` - Zimna woda).

---

## Rozdział 3: Panel Zarządcy (Administracja)

Panel Zarządcy to najpotężniejsza część aplikacji BLOKUR. Ma on pełną kontrolę nad nieruchomościami, użytkownikami i codziennymi procesami.

> [!NOTE]
> Jako zarządca zaloguj się: `admin1@blokur.pl`. Baza posiada skonfigurowane budynki m.in.: **Budynek A (Solaris)**, **Rezydencja Parkowa**.

### 3.1. Zgłoszenia (Obsługa Usterek)
1. **Nowe zgłoszenia:** Weryfikuj nowe zgłoszenia od mieszkańców.
2. **Akcje:** Przypisz konserwatora do usterki lub odrzuć zgłoszenie z obowiązkiem podania przyczyny.
3. **Zamykanie zgłoszenia:** Gdy pracownik skończy pracę (status `Zakończone - do weryfikacji`), sprawdź zgłoszenie i zamknij je. Następnie wygeneruj Protokół PDF.

### 3.2. Lokale (Drzewo Nieruchomości)
Widok strukturalny całej Twojej wspólnoty (Budynek -> Klatki -> Mieszkania). 
Po wejściu w szczegóły mieszkania możesz sprawdzić detale techniczne (np. 45.50 m2, typ własności) oraz kontrolować odczyty liczników (moduł raportowania zużycia mediów).

### 3.3. Uchwały (Zarządzanie głosowaniami)
Twórz nowe uchwały z określonym terminem końcowym i opcjami do wyboru. Obserwuj na bieżąco procentowy rozkład oddanych przez mieszkańców głosów.

### 3.4. Użytkownicy
Wyszukuj mieszkańców, edytuj ich dane kontaktowe, przydzielaj role oraz dostęp do odpowiednich lokali w drzewie nieruchomości.

### 3.5. Moduły zaawansowane (z poziomu Profilu)
- **Dystrybucja Dokumentów (Rachunki):** Wgraj plik `.zip` zawierający rachunki w PDF dla wszystkich mieszkańców i roześlij je do konkretnych lokali jednym kliknięciem.
- **Przeglądy (Inspekcje):** Zaplanuj i śledź audyty budowlane, kominiarskie i gaśnic.
- **Zarządzanie Finansami:** Dodawaj masowo lub ręcznie wpłaty i naliczenia dla mieszkańców.
- **Kategorie i Powiadomienia (SLA):** Zaawansowany konfigurator pozwalający na wymuszenie szybkiej obsługi awarii (np. auto-powiadomienie przy braku reakcji konserwatora przez 24h na kategorię 'Elektryka').
- **Logo wspólnoty:** Zmień branding aplikacji dla całej wspólnoty.

---

## Rozdział 4: Panel Konserwatora

Konto Konserwatora zostało zaprojektowane z myślą o prostocie w terenie. Konserwator ma dostęp tylko do zakładek **Zgłoszenia** oraz **Profil**.

### 4.1. Widok Listy Zgłoszeń
Konserwator widzi tylko przypisane do niego zgłoszenia, wyświetlające status usterki, jej kategorię i przypisany termin.

> [!NOTE]
> Logując się jako `hydraulik@blokur.pl`, na liście znajdziesz zgłoszenie **ZGL/2026/001** ("Wyciek pod zlewem", w realizacji) wraz z notatką zarządcy: *"Hydraulik Marian ma wziąć zapasowy syfon marki X."*
> Logując się jako `elektryk@blokur.pl`, zobaczysz zaplanowane zgłoszenie **ZGL/2026/002** ("Przepalona żarówka").

### 4.2. Obsługa Zgłoszenia (Szczegóły)
Po wejściu w szczegóły zgłoszenia można podejrzeć jego pełen opis, lokalizację i zdjęcia "Przed" dodane przez lokatora.

**Zmiany Statusów (Floating Action Buttons):**
- Status `Zaplanowano`: kliknięcie **"Play"** zmienia status na `W realizacji`.
- Status `W realizacji`: 
  - Przycisk **"Pauza"**: Zatrzymuje czasowo prace (wymaga komentarza z powodem).
  - Przycisk **"Zakończ" (zielony check)**: Kończy pracę i zmienia status na `Zakończone - Do weryfikacji`.
- Status `Zamknięte`: Umożliwia pobranie wygenerowanego protokołu PDF z wykonanych prac.

**Dowody Wykonania Pracy:**
Konserwator ma możliwość wymieniania publicznych wiadomości z lokatorem (np. by upewnić się, kiedy właściciel jest w domu) oraz dodania własnych zdjęć w zakładce Zdjęcia. Opcja *"Zrób zdjęcie po"* służy jako dowód naprawy ułatwiający zarządcy weryfikację.
