# Rozdział 4: Panel Konserwatora

Konto Konserwatora zostało zaprojektowane z myślą o maksymalnej prostocie. Konserwator po zalogowaniu ma dostęp tylko do dwóch zakładek w dolnym menu:
1. **Zgłoszenia** – tu widzi listę przypisanych mu zadań oraz ma możliwość ich realizacji.
2. **Profil** – proste ustawienia konta, w tym możliwość wylogowania się.

---

## 4.1. Widok Listy Zgłoszeń

Na ekranie głównym konserwatora znajduje się lista zgłoszeń (usterek). Konserwator widzi tylko te zgłoszenia, które zostały do niego przypisane przez Zarządcę.

Każdy element na liście zawiera podstawowe informacje:
- Nazwa/Tytuł zgłoszenia.
- Kategoria usterki.
- Aktualny status (np. `Zaplanowano`, `W realizacji`, `Zamknięte`).
- Data utworzenia oraz informacja o tym, komu zgłoszenie zostało przypisane.

> [!NOTE]
> **Dane Testowe (Środowisko Mockowe)**
> Logując się jako konserwator (np. `hydraulik@blokur.pl`), na Twojej liście znajdziesz przykładowe zgłoszenia:
> - **ZGL/2026/001** ("Wyciek pod zlewem") – kategoria: Hydraulika, status: `W_REALIZACJI`. Zgłoszenie posiada notatkę od zarządcy: *"Hydraulik Marian ma wziąć zapasowy syfon marki X."* oraz komentarze ostrzegające przed agresywnym psem.
> 
> Logując się jako elektryk (`elektryk@blokur.pl`), zobaczysz:
> - **ZGL/2026/002** ("Przepalona żarówka") – kategoria: Elektryka, status: `ZAPLANOWANO`.
> 
> Możesz testować zmianę tych statusów i dodawanie zdjęć/komentarzy na w.w. mockach.

> [!TIP]
> Jeśli na liście jest dużo pozycji, możesz użyć ikony filtra u góry ekranu, aby filtrować zgłoszenia po ich statusie.

---

## 4.2. Obsługa Zgłoszenia (Szczegóły usterki)

Po kliknięciu w konkretne zgłoszenie na liście, przejdziesz do widoku **Szczegółów zgłoszenia**. 
Tutaj znajdziesz wszystkie potrzebne informacje:
- Tytuł i pełen opis problemu zgłoszony przez mieszkańca.
- Dokładną lokalizację usterki (o ile została podana).
- Notatkę wewnętrzną od zarządcy (np. kod do domofonu).
- Zdjęcia dodane przez mieszkańca podczas zgłaszania problemu.

### 4.2.1. Zmiany Statusów i Interakcje
W prawym dolnym rogu ekranu szczegółów znajdują się przyciski akcji (tzw. Floating Action Buttons). Dostępne przyciski zmieniają się w zależności od tego, w jakim statusie znajduje się zgłoszenie:

- **Zgłoszenie w statusie `Zaplanowano` lub `Wstrzymano`**:
  Widoczny jest przycisk z ikoną "Play". Kliknięcie go pozwala na **Rozpoczęcie realizacji**. Status zgłoszenia zmienia się na `W realizacji`.

- **Zgłoszenie w statusie `W realizacji`**:
  Widoczne są dwa przyciski:
  - **Przycisk "Pauza" (Wstrzymaj/Komentarz):** Pozwala na czasowe wstrzymanie prac (np. jeśli potrzebujesz zamówić części). Wymaga wpisania komentarza z uzasadnieniem. Status zmieni się na `Wstrzymano`.
  - **Przycisk "Zakończ" (Zielony "Check"):** Służy do zgłoszenia zakończenia prac. Opcjonalnie możesz dodać komentarz. Po zatwierdzeniu status zmieni się na `Zakończone - Do weryfikacji`. Teraz pałeczkę przejmuje Zarządca, który weryfikuje pracę.

- **Zgłoszenie w statusie `Zamknięte`**:
  Widoczny jest przycisk pobierania protokołu. Po kliknięciu wygenerowany zostanie plik PDF z protokołem podsumowującym obsługę zgłoszenia.

### 4.2.2. Komunikacja i Dowody Wykonania
- **Komentarze:** Na dole ekranu szczegółów znajduje się sekcja komentarzy. Możesz wymieniać wiadomości z mieszkańcem oraz zarządcą, np. prosząc o doprecyzowanie lokalizacji.
- **Zdjęcia "Po":** Gdy zgłoszenie jest w trakcie realizacji (lub wstrzymane / do weryfikacji), masz możliwość dodania własnych zdjęć w sekcji zdjęć. Kliknij przycisk z aparatem, aby zrobić zdjęcie lub dodać z galerii. Zdjęcia "Po" są świetnym dowodem na prawidłowe naprawienie usterki, który ułatwia zarządcy zamknięcie zgłoszenia.
