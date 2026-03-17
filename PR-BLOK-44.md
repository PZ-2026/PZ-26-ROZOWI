Closes BLOK-44

## Co zostało zrobione?
Zgodnie z wymaganiami taska, przeprojektowałem system designu oraz wszystkie komponenty UI aplikacji mobilnej. Porzucono domyślny wygląd Material3 na rzecz spójnego, premium motywu jasnego z własną paletą kolorów i typografią.

Wprowadzone zmiany obejmują:
- nową paletę kolorów (tło `#F8F9FF`, primary indigo `#4F46E5`, secondary amber `#D97706`),
- czcionkę **Nunito** zintegrowaną przez Google Fonts Downloadable Fonts API,
- przeprojektowanie wszystkich komponentów: przyciski z gradientem, karty z obramowaniem, pola tekstowe w stylu filled, pill-badge'y z kolorami semantycznymi,
- przeprojektowanie komponentów domenowych: `BlokurTicketItem` z lewą belką statusu, `BlokurFinanceCard` w stylu karty bankowej,
- dokumentację komponentów w pliku `COMPONENTS.md`.

## Jak przetestować?

Otworzyć projekt w Android Studio i uruchomić podglądy `@Preview` w poszczególnych plikach komponentów:

**Komponenty do sprawdzenia:**
1. `BlokurButtons.kt` → przycisk primary z gradientem, secondary z jasnym fill, FAB
2. `BlokurCards.kt` → karta z border, karta wyróżniona z gradient tłem
3. `BlokurTicketItem.kt` → wiersz zgłoszenia z lewą kolorową belką statusu
4. `BlokurFinanceCard.kt` → karta salda (wariant zielony i czerwony)
5. `BlokurBadge.kt` → badge kategorii i statusu w różnych kolorach

**Weryfikacja budowania:**
```bash
./gradlew assembleDebug
```
Spodziewany wynik: `BUILD SUCCESSFUL`, exit code `0`.
