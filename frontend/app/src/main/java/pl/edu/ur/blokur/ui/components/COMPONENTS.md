# Komponenty UI – Blokur

Krótki opis każdego komponentu w folderze `components/`.

---

## BlokurPrimaryButton

Główny przycisk akcji z gradientem indigo→violet.

| Parametr   | Typ          | Opis                                         |
|------------|--------------|----------------------------------------------|
| `text`     | `String`     | Tekst wyświetlany na przycisku               |
| `onClick`  | `() -> Unit` | Akcja po kliknięciu                          |
| `modifier` | `Modifier`   | Modyfikator layoutu                          |
| `enabled`  | `Boolean`    | Czy przycisk jest aktywny (domyślnie `true`) |

---

## BlokurSecondaryButton

Drugorzędny przycisk z jasnym indigo fill — do anulowania lub akcji pobocznych.

| Parametr   | Typ          | Opis                                         |
|------------|--------------|----------------------------------------------|
| `text`     | `String`     | Tekst wyświetlany na przycisku               |
| `onClick`  | `() -> Unit` | Akcja po kliknięciu                          |
| `modifier` | `Modifier`   | Modyfikator layoutu                          |
| `enabled`  | `Boolean`    | Czy przycisk jest aktywny (domyślnie `true`) |

---

## BlokurFab

Pływający przycisk akcji (FAB) z gradientem i ikoną `+`.

| Parametr   | Typ          | Opis                                                      |
|------------|--------------|-----------------------------------------------------------|
| `onClick`  | `() -> Unit` | Akcja po kliknięciu                                       |
| `modifier` | `Modifier`   | Modyfikator layoutu                                       |
| `text`     | `String`     | Opis dostępności / `contentDescription` (domyślnie `"+"`) |

---

## BlokurCard

Standardowa biała karta z subtelnym obramowaniem — kontener do ogólnych treści.

| Parametr   | Typ                      | Opis                |
|------------|--------------------------|---------------------|
| `modifier` | `Modifier`               | Modyfikator layoutu |
| `content`  | `@Composable () -> Unit` | Zawartość karty     |

---

## BlokurHighlightCard

Wyróżniona karta z delikatnym gradientowym tłem indigo — do ważnych sekcji i podsumowań.

| Parametr   | Typ                      | Opis                |
|------------|--------------------------|---------------------|
| `modifier` | `Modifier`               | Modyfikator layoutu |
| `content`  | `@Composable () -> Unit` | Zawartość karty     |

---

## BlokurTextField

Pole tekstowe w stylu filled z animowanym dolnym indicatorem — do formularzy.

| Parametr        | Typ                         | Opis                                            |
|-----------------|-----------------------------|-------------------------------------------------|
| `value`         | `String`                    | Aktualna wartość pola                           |
| `onValueChange` | `(String) -> Unit`          | Callback zmiany wartości                        |
| `label`         | `String`                    | Etykieta pola                                   |
| `modifier`      | `Modifier`                  | Modyfikator layoutu                             |
| `singleLine`    | `Boolean`                   | Czy pole jest jednolinijkowe (domyślnie `true`) |
| `enabled`       | `Boolean`                   | Czy pole jest aktywne (domyślnie `true`)        |
| `leadingIcon`   | `@Composable (() -> Unit)?` | Ikona po lewej stronie (domyślnie `null`)       |
| `trailingIcon`  | `@Composable (() -> Unit)?` | Ikona po prawej stronie (domyślnie `null`)      |

---

## BlokurTagBadge

Mały pill-badge do kategorii i etykiet pomocniczych w kolorze primary.

| Parametr   | Typ        | Opis                |
|------------|------------|---------------------|
| `text`     | `String`   | Tekst badge'a       |
| `modifier` | `Modifier` | Modyfikator layoutu |

---

## BlokurStatusBadge

Pill-badge do oznaczania statusów — tło w kolorze statusu z kolorową kropką i tekstem.

| Parametr   | Typ        | Opis                  |
|------------|------------|-----------------------|
| `text`     | `String`   | Tekst statusu         |
| `dotColor` | `Color`    | Kolor kropki i tekstu |
| `modifier` | `Modifier` | Modyfikator layoutu   |

---

## BlokurTopBar

Górny pasek nawigacyjny z dużym tytułem i subtelnym separatorem.

| Parametr         | Typ                               | Opis                                                  |
|------------------|-----------------------------------|-------------------------------------------------------|
| `title`          | `String`                          | Tytuł ekranu                                          |
| `navigationIcon` | `@Composable () -> Unit`          | Ikona nawigacji (np. strzałka wstecz, domyślnie brak) |
| `actions`        | `@Composable RowScope.() -> Unit` | Akcje po prawej stronie (domyślnie brak)              |

---

## BlokurTicketItem

Wiersz listy zgłoszeń z lewą kolorową belką statusu, ikoną kategorii, tytułem i badge'ami.

| Parametr       | Typ           | Opis                                              |
|----------------|---------------|---------------------------------------------------|
| `title`        | `String`      | Tytuł zgłoszenia                                  |
| `date`         | `String`      | Data i godzina                                    |
| `categoryText` | `String`      | Kategoria (wyświetlana jako `BlokurTagBadge`)     |
| `statusText`   | `String`      | Status (wyświetlany jako `BlokurStatusBadge`)     |
| `statusColor`  | `Color`       | Kolor statusu (belka + badge)                     |
| `onClick`      | `() -> Unit`  | Akcja po kliknięciu wiersza                       |
| `modifier`     | `Modifier`    | Modyfikator layoutu                               |
| `icon`         | `ImageVector` | Ikona kategorii (domyślnie `Icons.Rounded.Build`) |

---

## BlokurFinanceCard

Karta salda w stylu bankowej karty — pełne gradient tło, duża kwota i ikona portfela.

| Parametr   | Typ        | Opis                                                              |
|------------|------------|-------------------------------------------------------------------|
| `balance`  | `String`   | Kwota do wyświetlenia                                             |
| `dateText` | `String`   | Data stanu konta                                                  |
| `modifier` | `Modifier` | Modyfikator layoutu                                               |
| `isDebt`   | `Boolean`  | Jeśli `true`, karta jest czerwona (zadłużenie), domyślnie `false` |

---

## BlokurEmptyState

Widok pustego stanu z emoji w kółku i opisem — wyświetlany gdy lista jest pusta.

| Parametr      | Typ        | Opis                                         |
|---------------|------------|----------------------------------------------|
| `title`       | `String`   | Nagłówek                                     |
| `description` | `String`   | Opis pomocniczy                              |
| `modifier`    | `Modifier` | Modyfikator layoutu                          |
| `emoji`       | `String`   | Emoji wyświetlane w kółku (domyślnie `"📭"`) |

---

## BlokurLoader

Wskaźnik ładowania — spinner z okrągłym tłem w kolorze primary.

| Parametr   | Typ        | Opis                |
|------------|------------|---------------------|
| `modifier` | `Modifier` | Modyfikator layoutu |

---

## BlokurSnackbarHost

Host do wyświetlania snackbarów wewnątrz `Scaffold`.

| Parametr    | Typ                 | Opis                                        |
|-------------|---------------------|---------------------------------------------|
| `hostState` | `SnackbarHostState` | Stan hosta zarządzający kolejką komunikatów |

---

## BlokurAlertDialog

Dialog potwierdzający z tytułem, treścią i dwoma przyciskami akcji.

| Parametr      | Typ          | Opis                                              |
|---------------|--------------|---------------------------------------------------|
| `title`       | `String`     | Tytuł dialogu                                     |
| `message`     | `String`     | Treść komunikatu                                  |
| `onConfirm`   | `() -> Unit` | Akcja potwierdzenia                               |
| `onDismiss`   | `() -> Unit` | Akcja odrzucenia / zamknięcia                     |
| `confirmText` | `String`     | Tekst przycisku potwierdzenia (domyślnie `"OK"`)  |
| `dismissText` | `String`     | Tekst przycisku anulowania (domyślnie `"Anuluj"`) |

---

## BlokurImageAttachment

Kafelek załącznika zdjęcia — tryb dodawania lub podglądu z opcją usunięcia.

| Parametr      | Typ             | Opis                                                                      |
|---------------|-----------------|---------------------------------------------------------------------------|
| `onClick`     | `() -> Unit`    | Akcja po kliknięciu kafelka                                               |
| `modifier`    | `Modifier`      | Modyfikator layoutu                                                       |
| `isAddButton` | `Boolean`       | Jeśli `true`, wyświetla przycisk `+` zamiast podglądu (domyślnie `false`) |
| `onRemove`    | `(() -> Unit)?` | Akcja usunięcia — jeśli podana, pokazuje przycisk `×` (domyślnie `null`)  |
