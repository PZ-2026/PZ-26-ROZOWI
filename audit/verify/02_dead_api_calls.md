# Dead API calls — initial automated scan

Lista metod Retrofit (ApiService) bez bezpośrednich wywołań w kodzie frontendu (UI/ViewModel/Service).

Total ApiService methods scanned: 81\nMethods without usages: 1\n\n---

- Metoda: getBalancesPdf
  Interfejs: frontend/app/src/main/java/pl/edu/ur/blokur/services/DocumentApiService.kt
  Endpoint: api/pdf/balances
  Endpoint w backend inventory: TAK
  Ocena: BUG — brak integracji (pominięta funkcja). Szczegóły: metoda `getBalancesPdf` jest zdefiniowana w `PdfApiService` (@GET "api/pdf/balances`) ale NIE JEST wywoływana. Zamiast tego `ApartmentBalancesViewModel.buildPdfUrl(baseUrl)` nadal buduje URL ręcznie (frontend/app/src/main/java/pl/edu/ur/blokur/ui/views/finances/viewmodels/ApartmentBalancesViewModel.kt:85-96). Rekomendacja: wstrzyknąć `PdfApiService` do ViewModel i użyć `getBalancesPdf(...)` do pobrania `ResponseBody` i zapisania/udostępnienia PDF; alternatywnie, jeśli ręczne budowanie URL było świadome (np. bezpośrednie pobranie linku), oznaczyć metodę jako `deprecated` i dodać komentarz wyjaśniający decyzję.


---
Uwaga: to automatyczne narzędzie wykrywa tylko bezpośrednie wywołania metod (pattern methodName()). Metody używane przez refleksję, referencje ::method, lub przekazywane jako lambda mogą nie być wykryte. Wymagana ręczna weryfikacja i przypisanie: świadoma decyzja / bug / do usunięcia.
