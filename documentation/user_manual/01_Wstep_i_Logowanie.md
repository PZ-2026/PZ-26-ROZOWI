# Rozdział 1: Wstęp i Logowanie

Witamy w instrukcji użytkownika aplikacji **BLOKUR**! Ten dokument ma na celu przeprowadzenie Cię przez podstawowe kroki związane z uzyskaniem dostępu do platformy oraz rozwiązaniem problemów z logowaniem.

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

## 1.1. Ekran Logowania

Aby rozpocząć korzystanie z aplikacji, uruchom ją na swoim urządzeniu mobilnym. Jako pierwszy zobaczysz ekran logowania oznaczony logo **BlokUR**.

### Logowanie krok po kroku:
1. W polu **Adres e-mail** wpisz swój adres podany administratorowi wspólnoty (zarządcy).
2. W polu **Hasło** wpisz swoje aktualne hasło.
3. Kliknij przycisk **Zaloguj się**.

Jeżeli poświadczenia są prawidłowe, aplikacja automatycznie przekieruje Cię do odpowiedniego panelu głównego, właściwego dla Twojej roli (Mieszkaniec, Konserwator lub Zarządca).

> [!TIP]
> Jeśli masz problem z logowaniem (np. wpiszesz złe hasło), na dole ekranu pojawi się odpowiedni komunikat z informacją o błędzie. Po kilku nieudanych próbach logowania konto może zostać tymczasowo zablokowane ze względów bezpieczeństwa.

---

## 1.2. Odzyskiwanie hasła

Jeżeli zapomniałeś swojego hasła, nie musisz kontaktować się z zarządcą – możesz je zresetować samodzielnie.

1. Na ekranie logowania, tuż pod formularzem, kliknij w przycisk **Zapomniałeś hasła?**.
2. Zostaniesz przekierowany do ekranu odzyskiwania.
3. Podaj adres e-mail przypisany do Twojego konta i zatwierdź przyciskiem **Zresetuj hasło** (lub **Wyślij link**).
4. Otwórz swoją skrzynkę pocztową. Jeżeli podany e-mail istnieje w bazie, otrzymasz wiadomość z unikalnym linkiem.
5. Po kliknięciu w link z wiadomości e-mail będziesz mógł nadać nowe hasło (wymagane jest minimum 8 znaków).
6. Po zapisaniu nowego hasła możesz powrócić do aplikacji i zalogować się ponownie.

---

## 1.3. Pierwsze logowanie (Akceptacja Zaproszenia)

Zarządca może założyć dla Ciebie konto i wysłać Ci zaproszenie drogą mailową.
Proces pierwszego logowania wygląda następująco:
1. Otrzymujesz wiadomość e-mail z linkiem aktywacyjnym.
2. Klikasz w link, który przenosi Cię do formularza aktywacji konta.
3. Definiujesz swoje własne, unikalne hasło (minimum 8 znaków).
4. Po zatwierdzeniu hasła, Twoje konto zostaje aktywowane.
5. Możesz teraz otworzyć aplikację BLOKUR i zalogować się używając swojego e-maila oraz nowo utworzonego hasła.

---

> [!IMPORTANT]
> Aplikacja BLOKUR dba o bezpieczeństwo Twojej sesji. Jeśli nie będziesz używać aplikacji przez bardzo długi czas lub Twój token dostępu straci ważność, aplikacja automatycznie Cię wyloguje ze względów bezpieczeństwa i poprosi o ponowne zalogowanie.
