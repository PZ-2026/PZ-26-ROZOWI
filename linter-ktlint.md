# Linter i formatowanie kodu

## 1. Kontekst

W aktualnej wersji projektu została przygotowana konfiguracja **Ktlint** dla dwóch części aplikacji:

- **frontendu**,
- **backendu**.

Linter odpowiada za sprawdzanie zgodności kodu Kotlin z ustalonym stylem formatowania.  
W przypadku wykrycia naruszeń proces sprawdzania kończy się błędem, co pozwala szybciej wychwycić problemy związane ze stylem kodu.

Dodatkowo istniejący kod został sformatowany, a podstawowe reguły zostały zapisane w plikach `.editorconfig`.

Poniżej znajduje się opis konfiguracji oraz instrukcja uruchamiania lintera i formatowania kodu dla obu części projektu.

---

## 2. Założenia techniczne

Konfiguracja została przygotowana w oparciu o:

- **Kotlin**
- **Gradle**
- **Ktlint**
- pliki konfiguracyjne **`.editorconfig`**

Zakres działania konfiguracji obejmuje:

- sprawdzanie stylu kodu,
- automatyczne formatowanie kodu,
- zatrzymywanie procesu sprawdzania przy wykryciu naruszeń,
- utrzymanie spójnego stylu kodowania w całym projekcie.

---

## 3. Zakres konfiguracji

Linter został skonfigurowany osobno dla dwóch części projektu:

- **frontend** – aplikacja mobilna,
- **backend** – część serwerowa projektu.

W obu przypadkach dostępne są podstawowe polecenia do:

- sprawdzania stylu kodu,
- automatycznego formatowania,
- dodatkowej weryfikacji poprawności działania po zmianach.

---

## 4. Uruchamianie lintera dla frontendu

Aby sprawdzić styl kodu we frontendzie, należy przejść do folderu `frontend` i uruchomić polecenie:

```powershell
.\gradlew ktlintCheck
```

Polecenie sprawdza pliki Kotlin i zgłasza ewentualne błędy stylu.

Jeżeli wszystko jest poprawne, w terminalu powinien pojawić się komunikat:

```text
BUILD SUCCESSFUL
```

Aby automatycznie poprawić formatowanie kodu frontendu, należy uruchomić:

```powershell
.\gradlew ktlintFormat
```

Po zakończeniu formatowania również powinien pojawić się komunikat:

```text
BUILD SUCCESSFUL
```

---

## 5. Uruchamianie lintera dla backendu

Aby sprawdzić styl kodu w backendzie, należy przejść do folderu `backend` i uruchomić polecenie:

```powershell
.\gradlew ktlintCheck
```

Polecenie sprawdza kod backendu zgodnie z regułami Ktlint.

Jeżeli kod jest poprawny, w terminalu powinien pojawić się komunikat:

```text
BUILD SUCCESSFUL
```

Aby automatycznie poprawić formatowanie kodu backendu, należy uruchomić:

```powershell
.\gradlew ktlintFormat
```

Po zakończeniu również powinien pojawić się komunikat:

```text
BUILD SUCCESSFUL
```

---

## 6. Co zrobić, jeśli linter zgłosi błąd

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

## 7. Podsumowanie

Wdrożenie Ktlint w projekcie pozwala utrzymać spójny styl kodowania zarówno w **frontendzie**, jak i w **backendzie**.