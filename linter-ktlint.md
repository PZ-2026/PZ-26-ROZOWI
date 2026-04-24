# Linter i formatowanie kodu

## 1. Kontekst

W aktualnej wersji projektu została przygotowana konfiguracja **Ktlint** dla frontendu (aplikacji mobilnej w Kotlinie).

Linter odpowiada za sprawdzanie zgodności kodu Kotlin z ustalonym stylem formatowania.
W przypadku wykrycia naruszeń proces sprawdzania kończy się błędem, co pozwala szybciej wychwycić problemy związane ze stylem kodu.

Dodatkowo istniejący kod został sformatowany, a podstawowe reguły zostały zapisane w pliku `.editorconfig`.

> **Uwaga:** backend został zmigrowany z Kotlina na Javę, dlatego Ktlint nie ma tam zastosowania.
> Dla backendu używamy **Spotless** z formatterem **Google Java Format** — szczegóły w sekcji 5.

---

## 2. Założenia techniczne

Konfiguracja została przygotowana w oparciu o:

- **Kotlin**
- **Gradle**
- **Ktlint**
- plik konfiguracyjny **`.editorconfig`**

Zakres działania konfiguracji obejmuje:

- sprawdzanie stylu kodu,
- automatyczne formatowanie kodu,
- zatrzymywanie procesu sprawdzania przy wykryciu naruszeń,
- utrzymanie spójnego stylu kodowania we frontendzie.

---

## 3. Uruchamianie lintera dla frontendu

Aby sprawdzić styl kodu we frontendzie, należy przejść do folderu `frontend` i uruchomić polecenie:

```powershell
.\gradlew ktlintCheck
```

Polecenie sprawdza pliki Kotlin i zgłasza ewentualne błędy stylu.

Jeżeli wszystko jest poprawne, w terminalu powinien pojawić się komunikat:

```text
BUILD SUCCESSFUL
```

Aby automatycznie poprawić formatowanie kodu, należy uruchomić:

```powershell
.\gradlew ktlintFormat
```

Po zakończeniu formatowania również powinien pojawić się komunikat:

```text
BUILD SUCCESSFUL
```

---

## 4. Co zrobić, jeśli linter zgłosi błąd

Jeżeli `ktlintCheck` zakończy się błędem, należy najpierw uruchomić:

```powershell
.\gradlew ktlintFormat
```

Polecenie to automatycznie poprawia większość problemów związanych z formatowaniem.

Jeżeli po automatycznym formatowaniu nadal pojawiają się błędy, należy poprawić wskazane miejsca ręcznie zgodnie z komunikatem w terminalu, a następnie ponownie uruchomić:

```powershell
.\gradlew ktlintCheck
```

Dzięki temu można upewnić się, że kod jest już zgodny z ustalonym stylem.

---

## 5. Linter dla backendu (Spotless + Google Java Format)

Backend (Java) korzysta z pluginu **Spotless** z formatterem **Google Java Format**.

Aby sprawdzić styl kodu w backendzie, z folderu `backend`:

```powershell
.\gradlew spotlessCheck
```

Aby automatycznie sformatować kod:

```powershell
.\gradlew spotlessApply
```

`spotlessCheck` jest dopięty do standardowego `check`, więc uruchamia się razem z testami (`./gradlew check` / `./gradlew build`).

---

## 6. Podsumowanie

- **frontend** (Kotlin) — Ktlint (`ktlintCheck`, `ktlintFormat`),
- **backend** (Java) — Spotless + Google Java Format (`spotlessCheck`, `spotlessApply`).
