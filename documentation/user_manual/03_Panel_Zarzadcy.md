# Rozdział 3: Panel Zarządcy (Moduł Administracyjny)

Panel Zarządcy to najpotężniejsza część aplikacji BLOKUR. Zarządca wspólnoty (lub pracownik administracji) ma tu pełną kontrolę nad nieruchomościami, użytkownikami, a także zarządza codziennymi procesami (usterki, uchwały, finanse, komunikacja).

Aby ułatwić zarządzanie, interfejs został podzielony na **zakładki w dolnym menu** (najczęściej używane) oraz **rozszerzone funkcje w Profilu**.

> [!NOTE]
> **Dane Testowe (Środowisko Mockowe)**
> Jako zarządca możesz się zalogować używając konta: `admin1@blokur.pl` lub `admin2@blokur.pl` (hasło: `haslo123`).
> 
> Baza danych posiada wstępnie skonfigurowaną nieruchomość:
> - Nieruchomość: **Wspólnota Mieszkaniowa "Blokur"**
> - Budynki: **Budynek A (Solaris)**, **Budynek B (Luna)**, **Rezydencja Parkowa**.

---

## 3.1. Zgłoszenia (Obsługa Usterek)

To główny moduł, na którym pracuje zarządca przy obsłudze technicznej budynku. Zamiast realizować zgłoszenia samemu, zarządca weryfikuje je, deleguje i zamyka.

1. **Nowe zgłoszenia:** Gdy Mieszkaniec wyśle nowe zgłoszenie, pojawia się ono na liście ze statusem `Nowe`.
2. **Akcje na nowym zgłoszeniu (Wewnątrz szczegółów zgłoszenia):**
   - Możesz kliknąć przycisk **"Przypisz konserwatora"** (ikona osoby). Otworzy się lista dostępnych pracowników technicznych z możliwością określenia planowanej daty naprawy. Po przypisaniu, status zmienia się na `Zaplanowano`.
   - Możesz też kliknąć czerwony przycisk **"Odrzuć"** (krzyżyk), co zablokuje zgłoszenie z obowiązkiem podania przyczyny (np. "To usterka wewnątrz lokalu własnościowego, wspólnota tego nie pokrywa").
3. **Bieżący kontakt:** Jeśli konserwator zatrzyma pracę i doda status `Wstrzymano`, to Zarządca może wymusić jej powrót do realizacji klikając przycisk "Wznów zgłoszenie" (ikona Play).
4. **Zamykanie zgłoszenia:** Kiedy pracownik skończy pracę (status `Zakończone - do weryfikacji`), zarządca sprawdza czy wszystko się zgadza i wewnątrz zgłoszenia klika zielony przycisk "Zatwierdź i zamknij" (status zmienia się na `Zamknięte`). Po tym kroku zarządca oraz mieszkaniec mogą wygenerować z systemu **Protokół PDF** podsumowujący interwencję.

---

## 3.2. Lokale (Drzewo Nieruchomości)

Zakładka **Lokale** to widok strukturalny (drzewiasty) całej Twojej wspólnoty. Możesz tu przeglądać budynki, klatki schodowe oraz poszczególne lokale.

- Posiada opcję zwijania i rozwijania gałęzi (Budynek -> Klatki -> Mieszkania).
- Każde mieszkanie ma podany numer, saldo bieżące oraz informacje o tym, do kogo należy.
- Po wejściu w szczegóły mieszkania (Kliknięcie na lokal) Zarządca może:
  - Sprawdzić szczegóły techniczne (metraż: np. 45.50 m2, piętro, typ własności).
  - Podglądać i przypisywać liczniki (np. `WOD-ZIM-001`).
  - Sprawdzać odczyty dla wybranego licznika (moduł raportowania zużycia mediów).

---

## 3.3. Uchwały (Zarządzanie głosowaniami)

W odróżnieniu od Mieszkańca, który tylko głosuje, Zarządca ma możliwość tworzenia nowych uchwał.

1. W zakładce **Uchwały** użyj przycisku "+" (plus), aby przejść do kreatora.
2. Wybierz Budynek, którego dotyczy głosowanie.
3. Wpisz Tytuł, Opis (np. "Monitoring w windzie").
4. Ustaw datę i godzinę zakończenia głosowania (np. 30 dni od teraz).
5. Zdefiniuj opcje odpowiedzi (domyślnie sugerowane to: TAK, NIE, WSTRZYMUJĘ SIĘ).
6. Opublikuj uchwałę.

System będzie zliczał głosy automatycznie. Zarządca widzi na bieżąco procentowy rozkład oddanych głosów.

---

## 3.4. Użytkownicy

Moduł zarządzania kontami w systemie.

Zarządca może:
- Wyszukiwać mieszkańców oraz innych zarządców i konserwatorów.
- Aktualizować podstawowe dane oraz przydzielać nowym użytkownikom role.
- Dodawać i odbierać użytkownikom dostęp do poszczególnych lokali mieszkalnych (jeśli np. ktoś wynajął mieszkanie lub sprzedał nieruchomość).

---

## 3.5. Moduły zaawansowane (z poziomu Profilu)

Aby nie zaśmiecać głównego interfejsu, wiele opcji administracyjnych znajduje się w zakładce **Profil**:

### Dystrybucja Dokumentów (Rachunki)
Funkcja ta pozwala na szybką wysyłkę faktur, rachunków lub pasków czynszowych do wszystkich mieszkańców.
1. Administrator generuje zewnętrznie spakowany plik `.zip` zawierający dokumenty w formacie PDF (gdzie nazwa PDF to np. numer lokalu lub ID mieszkańca) oraz plik z mapowaniem (CSV).
2. Moduł dystrybucji parsuje paczkę ZIP i automatycznie przypina poszczególne dokumenty PDF do kont właściwych mieszkańców.
3. System natychmiastowo wysyła mieszkańcom powiadomienia na e-mail / push, że na koncie pojawił się nowy dokument księgowy.

### Przeglądy (Inspekcje)
Zarządzanie zaplanowanymi audytami (np. przegląd gaśnic, kominiarz, audyt energetyczny). System pozwala powiązać przegląd z konkretnym budynkiem lub klatką i informować lokatorów z wyprzedzeniem.

### Zarządzanie Finansami
Z poziomu Profilu zarządca może otworzyć **Finanse**, gdzie może masowo lub ręcznie dodawać transakcje finansowe ("Naliczenie", "Wpłata") wybranym mieszkańcom, regulując ich saldo. 

### Kategorie i Powiadomienia (SLA)
- **Kategorie:** Dodawanie i ukrywanie kategorii zgłoszeń usterkowych (np. "Domofony", "Hydraulika").
- **Powiadomienia (Ustawienia konfiguracji):** Zaawansowany konfigurator pozwalający sterować logiką biznesową. Zarządca może zdefiniować zasady SLA (Service Level Agreement). Przykładowo, jeśli kategoria "Elektryka" to pilna awaria, system automatycznie nada przypomnienie, jeżeli zgłoszenie nie zostanie podjęte do realizacji w czasie 24h.
- **Logo wspólnoty:** Możliwość wgrania logotypu wspólnoty z dysku telefonu.
