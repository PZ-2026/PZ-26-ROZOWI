# Raport techniczny — Weryfikacja frontendu BlokUR

Data: 2026-06-05
Zakres: frontend (frontend/), weryfikacja po wdrożeniu 27 zadań backlogu

---

## FAZA 0 — Kompilacja
- Użytkownik polecił aby NIE uruchamiać kompilacji w tej sesji. Niemniej w trakcie wcześniejszej analizy sesji zapisano próbę kompilacji lokalnej: wynik znajduje się w `audit/verify/00_compilation.txt`.
- Krótkie podsumowanie zapisu: system JDK = OpenJDK 25.0.3; Gradle/Kotlin w projekcie wymagają JDK 17/21 — próba kompilacji lokalnej zakończyła się niepowodzeniem (BUILD FAILED; JDK mismatch).

Rekomendacja: dla pełnej walidacji wykonać kompilację po ustawieniu JAVA_HOME na JDK17/21 (CI/local), ale dalsza weryfikacja logiczna przeprowadzona bez kompilacji.

---

## Pokrycie zadań backlogu (FAZA 1)
- Liczba zadań zweryfikowanych: 27
- Statusy:
  - ✅ ZWERYFIKOWANE: 25
  - ⚠️ CZĘŚCIOWO: 2
  - ❌ BRAK LUB BŁĘDNY: 0

Szczegóły i per-task zapisy znajdują się w `audit/verify/01_task_verification.md` (wszystkie 27 zadań opisane; wpisy zapisywane po każdym batchu).

---

## Błędy API (krytyczne — mogą powodować runtime)
Po przeglądzie zmienionych plików i wywołań API — nie znaleziono jawnych niezgodności ścieżek/metod/pól względem `audit/01_backend_inventory.md` które prowadziłyby do natychmiastowego błędu runtime (np. złe pathy, błędne metody HTTP według inwentarza). Jednak wykryto następujące ryzyka operacyjne i integracyjne:

1) Token refresh failure handling — brak wymuszonego logoutu
- `TokenAuthenticator` przy niepowodzeniu odświeżenia tokena zwraca `null` i nie czyści tokenów ani nie wymusza nawigacji do logowania.
- Ryzyko: użytkownik może napotkać powtarzające się 401 bez jasnego przepływu recovery — Poważne (może utrudnić dostęp do aplikacji).

2) getBalancesPdf (DocumentApiService.getBalancesPdf) — zdefiniowany, ale nieużywany
- Endpoint istnieje w backendzie (`GET /api/pdf/balances`), metoda jest zdefiniowana w `PdfApiService` lecz nie wywoływana z ViewModel (`ApartmentBalancesViewModel` nadal buduje URL ręcznie).
- Ryzyko: brak integracji — utrata korzyści z interceptorów (autojoin JWT) oraz spójnej obsługi błędów; nie wpływa bezpośrednio na runtime, ale jest bugiem do naprawy.

3) Mieszanki obsługi błędów HTTP
- Część serwisów używa `ApiResponseHandler.requireSuccess()`/`ApiException`, inne ręcznie sprawdzają `response.isSuccessful()` i rzucają wyjątki. Skutkuje to rozproszeniem logiki obsługi błędów i utrudnia centralne mapowanie (mniejsze ryzyko runtime, większe ryzyko błędów UX).

4) Inne zagrożenia (związane z konfiguracją/testami): brak prawdziwego `google-services.json` uniemożliwia pełne testy FCM; MeterService wykonuje client-side pagination/filtering (ryzyko dla dużych zestawów danych).

---

## Martwy kod API (FAZA 3)
Zgodnie z `audit/verify/02_dead_api_calls.md` (skan + ręczna weryfikacja):

- DocumentApiService.getBalancesPdf — endpoint: `GET /api/pdf/balances` — OCENA: BUG (metoda jest zdefiniowana, backend wspiera endpoint, ale frontend nie wywołuje tej metody; zamiast tego buduje URL ręcznie w `ApartmentBalancesViewModel.buildPdfUrl`).

(więcej metod: automatyczny skan wykrył tylko powyższą metodę jako bez użycia). Plik: `audit/verify/02_dead_api_calls.md`.

---

## Problemy z rolami w UI (FAZA 3)
Zestawienie z `audit/verify/03_role_verification.md` — główne wnioski:
- Kontrola ról: większość ekranów pobiera rolę z `TokenStorage` poprzez `AuthService`/`TicketService` i stosuje gating w ViewModel/Composable — ogólnie poprawne.
- Mieszanka porównań string vs enum (rekomendacja: ujednolicić na enum `UserRole`).
- Główny problem: `NotificationsViewModel` inicjuje wywołania admin API bez dodatkowego guardu po stronie klienta — ocena ⚠️ (zalecane dodać lokalne sprawdzenie roli przy inicjalizacji lub uniemożliwić tworzenie ViewModel dla nie-uprawnionych).

Plik z pełną analizą: `audit/verify/03_role_verification.md`.

---

## Top 10 problemów do naprawy (priorytetowo)
1. Wprowadzić wymuszony logout/clean-up tokenów przy nieudanym refresh (TokenAuthenticator/flow auth) — CRITICAL (auth availability).
2. Zintegrować `PdfApiService.getBalancesPdf` z widokiem generującym PDF (usuń ręczne budowanie URL) — BUG (spójność, bezpieczeństwo tokenów).
3. Dodać guardę roli w `NotificationsViewModel` lub zabezpieczyć tworzenie ViewModela — zapobiega nieautoryzowanym wywołaniom.
4. Ujednolicić obsługę błędów HTTP (użycie `ApiResponseHandler.requireSuccess()` / `ApiException`) — poprawa spójności i lepsza diagnostyka.
5. Uzupełnić pełnoekranową galerię zdjęć (brak kliknięć w miniaturach) — UX.
6. Dodać nawigację z drzewa budynków do `FinancialLedgerScreen` z parametrem `apartmentId` dla ZARZĄDCA — UX/flow.
7. Zastanowić się nad paginacją po stronie serwera (MeterService/Tickets) zamiast client-side for large datasets.
8. Uzyskać prawdziwe `google-services.json` dla E2E FCM (testy, rejestracja urządzeń).
9. Naprawić drobne niezgodności w logu implementacji (np. odniesienie do `TicketImageService.kt` zamiast `TicketMediaServices.kt`) — by uniknąć dezinformacji.
10. Zastanowić się nad centralnym testem integracyjnym (smoke tests) po poprawkach auth/FCM.

---

## Dalsze kroki (zalecane)
1. Wdrożyć krytyczne poprawki auth (punkt 1) i powtórzyć testy manualne (login / refresh / logout flow).
2. Wdrożyć drobne poprawki UX (full-screen gallery, navigation for manager ledger).
3. Po poprawkach uruchomić kompilację lokalną lub w CI z JDK 17/21 i wykonać sanity build.
4. Przygotować PRy zmieniające: (a) TokenAuthenticator -> logout on fail, (b) ApartmentBalancesViewModel -> użycie PdfApiService.getBalancesPdf, (c) NotificationsViewModel -> add role guard.

---

Pliki referencyjne:
- `audit/verify/01_task_verification.md` — szczegółowe wyniki per task
- `audit/verify/02_dead_api_calls.md` — martwy kod API
- `audit/verify/03_role_verification.md` — role gating
- `audit/verify/02_omissions.md` — wnioski nt. pominięć

Koniec raportu.
