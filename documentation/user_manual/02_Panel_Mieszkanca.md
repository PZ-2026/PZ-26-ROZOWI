# Rozdział 2: Panel Mieszkańca

Panel Mieszkańca to serce aplikacji dla właścicieli oraz najemców lokali. Udostępnia on wszystkie narzędzia potrzebne do wygodnego funkcjonowania we wspólnocie mieszkaniowej, bezpośrednio z poziomu telefonu.

Dolne menu nawigacyjne dla mieszkańca składa się z 5 zakładek:
1. Zgłoszenia
2. Finanse
3. Uchwały
4. Ogłoszenia
5. Profil

---

## 2.1. Zgłoszenia (Usterki)

Zakładka **Zgłoszenia** pozwala na zgłaszanie usterek, awarii i innych problemów technicznych do administracji.

### Przegląd Zgłoszeń
Po wejściu w zakładkę zobaczysz listę swoich zgłoszeń posortowaną od najnowszych. Każdy kafelek pokazuje:
- Tytuł problemu.
- Kategorię (np. Hydraulika, Elektryka).
- Aktualny status (oznaczony kolorem, np. `Nowe`, `W realizacji`, `Zakończone`).
- Datę zgłoszenia.

> [!NOTE]
> **Dane Testowe (Środowisko Mockowe)**
> Logując się jako `jan.kowalski@gmail.com` (hasło: `haslo123`), zobaczysz na liście zgłoszenie **ZGL/2026/001** ("Wyciek pod zlewem"). Status tego zgłoszenia to `W realizacji`, a przypisany pracownik to `Marian Rura` (konserwator).

### Dodawanie nowego zgłoszenia
1. Kliknij niebieski przycisk z ikoną **"+"** (plus) w prawym dolnym rogu ekranu.
2. Wybierz odpowiednią **kategorię** usterki z listy rozwijanej.
3. Wpisz **tytuł** oraz **opis** problemu. Postaraj się być jak najbardziej precyzyjny.
4. Zrób zdjęcie usterki lub wybierz je z galerii (opcjonalnie, ale bardzo pomocne!).
5. Wybierz miejsce występowania (Twój lokal lub konkretna część wspólna - np. klatka schodowa).
6. Kliknij **Wyślij**. Zarządca zostanie natychmiast powiadomiony.

### Komunikacja w zgłoszeniu
Wchodząc w szczegóły swojego zgłoszenia, zobaczysz pełną historię naprawy. Możesz tam wymieniać komentarze z administratorem oraz konserwatorem, aby dopytać o status lub dodać nowe szczegóły. Jeżeli usterka została naprawiona, znajdziesz tam również dokumentację zdjęciową "Po" dodaną przez konserwatora.

---

## 2.2. Finanse

Zakładka **Finanse** to Twoje wirtualne saldo księgowe we wspólnocie.

### Saldo bieżące
Na samej górze ekranu znajduje się duże podsumowanie Twojego **aktualnego salda**. 
- Kwota na zielono oznacza nadpłatę.
- Kwota na czerwono (z minusem) oznacza niedopłatę (zaległość), którą należy uregulować.

### Historia transakcji (Księga finansowa)
Poniżej salda znajduje się przewijana lista historii Twoich transakcji posortowana od najnowszych. 
Transakcje dzielą się na:
- **Naliczenia:** Comiesięczne obciążenia czynszowe, rozliczenia mediów (np. wody). Zmniejszają one Twoje saldo.
- **Wpłaty:** Twoje przelewy zaksięgowane przez administrację. Zwiększają Twoje saldo.
- **Korekty:** Ręczne poprawki administracyjne.

> [!NOTE]
> **Dane Testowe (Środowisko Mockowe)**
> Dla konta `jan.kowalski@gmail.com` saldo powinno uwzględniać transakcje z kwietnia 2026:
> - Naliczenie: "Czynsz 04/2026" (-450.00 zł)
> - Wpłata: "Przelew Czynsz 04/2026" (+450.00 zł)
> - Naliczenie: "Rozliczenie wody 03/2026" (-12.50 zł)
> 
> Sumarycznie możesz sprawdzić, jak transakcje wpływają na ogólny stan konta.

---

## 2.3. Uchwały (Głosowania)

Karta **Uchwały** to miejsce, w którym jako mieszkaniec możesz brać udział w decydowaniu o sprawach wspólnoty.

Lista uchwał dzieli się na aktywne (w trakcie głosowania) i zakończone. Przy każdej uchwale widzisz pasek postępu pokazujący, ile czasu zostało do końca głosowania oraz procentowy rozkład oddanych do tej pory głosów.

### Jak oddać głos?
1. Wybierz aktywną uchwałę z listy (np. "Fundusz remontowy 2026").
2. Zapoznaj się z tytułem i pełnym opisem zaproponowanym przez Zarząd.
3. Wybierz jedną z opcji na dole ekranu: **ZA**, **PRZECIW** lub **WSTRZYMUJĘ SIĘ**.
4. Potwierdź swój wybór. Twój głos zostanie trwale zapisany w systemie blockchain.

> [!WARNING]
> Zgodnie z zasadami działania aplikacji (i logiką blockchain), raz oddanego głosu **nie można cofnąć ani zmienić**. Zastanów się dobrze, zanim klikniesz przycisk głosowania!

---

## 2.4. Ogłoszenia (Tablica)

Moduł **Ogłoszenia** zastępuje tradycyjną tablicę korkową na klatce schodowej. Znajdziesz tu wszystkie komunikaty od Zarządcy:
- Zaplanowane przerwy w dostawie wody/prądu.
- Zaproszenia na zebrania wspólnoty.
- Przypomnienia o przeglądach kominiarskich (np. "Przegląd kominiarski w dniu 15.04").

Nowe, nieprzeczytane ogłoszenia mogą wywoływać powiadomienie Push na Twoim telefonie, dzięki czemu nic Cię nie ominie. Oprócz ogłoszeń globalnych (dla całego budynku), możesz otrzymywać też komunikaty dedykowane tylko dla Twojej klatki schodowej!

---

## 2.5. Profil i Ustawienia

Zakładka **Profil** to miejsce na zarządzanie swoimi danymi.
Z poziomu profilu możesz:
- Sprawdzić swoje dane osobowe (imię, nazwisko, adres lokalu).
- Zmienić hasło do konta.
- Skonfigurować powiadomienia (np. czy chcesz dostawać e-maile o nowych uchwałach).
- Przejrzeć podpięte liczniki do Twojego lokalu (np. `WOD-ZIM-001` - Zimna woda).
- Wylogować się z aplikacji.
