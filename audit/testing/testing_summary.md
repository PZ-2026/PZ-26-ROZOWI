# Podsumowanie audytu i priorytetyzacja napraw aplikacji BlokUR

Niniejszy raport podsumowuje analizę błędów manualnych (TEST-011, TEST-012, TEST-020), systematyzuje priorytety napraw oraz przedstawia rekomendowaną chronologię działań wdrożeniowych, uwzględniając najnowszy stan repozytorium (w tym poprawki opisane w `06_final_report.md` oraz historię commitów git).

---

## 1. Tabela statusów i klasyfikacja błędów

| ID błędu | Nazwa usterki | Warstwa | Priorytet | Wpływ na użytkownika / Uzasadnienie |
| :--- | :--- | :--- | :--- | :--- |
| **Bug 20 (TEST-020)** | Brak przypisanego lokalu w profilu mieszkańca oraz błędne saldo / brak transakcji | Integracja (kontrakt API) + Baza danych + Frontend | **KRYTYCZNY** | Blokuje dostęp do całego modułu Finansów dla mieszkańców, którzy nie posiadają zgłoszeń lub posiadają zgłoszenia wspólne. Dodatkowo niespójny słownik transakcji (`NALEZNOSC` vs `NALICZENIE`) powoduje przekłamania sald. |
| **Bug 11 (TEST-011)** | Wyjątek bezpieczeństwa przy pobieraniu zdjęć zgłoszeń wspólnych | Backend (Autoryzacja) | **WYSOKI** | Blokuje wyświetlenie całego ekranu szczegółów zgłoszenia wspólnego u mieszkańca, który nie jest autorem (powoduje błędy HTTP 500/403 w logach backendu). |
| **Bug 12 (TEST-012)** | Brak reaktywności listy po dodaniu zgłoszenia oraz niespójny format numeru | Frontend (UI) + Backend (Generator) | **ŚREDNI** | Pogarsza wrażenia użytkownika (wymaga ręcznego odświeżania listy). Różnice w formacie numerów (`ZGL-RRRR-NNNN` vs `ZGL/RRRR/NNN`) grożą błędami dopasowania danych. |

---

## 2. Ocena i wybór optymalnych wariantów napraw (Analiza Bezpieczeństwa)

Po przeanalizowaniu historii zmian git (ostatnich commitów stabilizujących frontend) oraz wytycznych z raportu `06_final_report.md`, podjęto decyzję o wyborze następujących wariantów naprawy, które zapewniają maksymalne bezpieczeństwo i brak skutków ubocznych:

### A. Rozwiązanie Bugu 20 (Profil i Finanse) — Dedykowany endpoint `/api/users/me` [REKOMENDOWANE]
*   **Analiza alternatyw:** Rozważano pozostawienie heurystyki w `UserApartmentService` z dopasowywaniem pierwszego lokalu ze zgłoszeń. Zostało to odrzucone, ponieważ dla nowego użytkownika (który nie ma żadnego zgłoszenia w bazie) moduł finansów byłby całkowicie zablokowany. Rozważano też pobieranie lokalu z drzewa budynków, jednak drzewo nie zawiera informacji o powiązaniach użytkowników z lokalami.
*   **Decyzja:** Dodanie endpointu `/api/users/me` na backendzie jest najprostszym i najbezpieczniejszym architektonicznie rozwiązaniem. Nie modyfikuje ono istniejących mechanizmów autoryzacji (korzysta z gotowych filtrów JWT). Dodatkowo, rozwiązuje to problem mockowania profilu użytkownika na ekranie `ProfileScreen` (który dotychczas w `06_final_report.md` był oznaczony jako "świadome ograniczenie poza scope").
*   **Typ transakcji:** Zastosowanie migracji Flyway SQL podmieniającej `'NALEZNOSC'` na `'NALICZENIE'` w seedzie `V111` jest bezpieczniejsze niż dodawanie obsługi nowego typu transakcji w kodzie (co powiększałoby dług techniczny i komplikowało kod mapowania).

### B. Rozwiązanie Bugu 11 (Autoryzacja zdjęć) — Wspólna klasa pomocnicza autoryzacji [REKOMENDOWANE]
*   **Decyzja:** Wydzielenie logiki autoryzacji widoczności zgłoszeń dla mieszkańca do wspólnego komponentu pomocniczego na backendzie (np. `TicketSecurityHelper` lub metody statycznej) i reużycie jej w `TicketService` oraz `TicketImageService`. Kopiowanie logiki sprawdzania budynku/klatki/lokalu bezpośrednio do serwisu zdjęć groziłoby rozjazdem uprawnień (regresją) przy przyszłych modyfikacjach reguł widoczności.

### C. Rozwiązanie Bugu 12 (Reaktywność listy i format) — Mieszane podejście [REKOMENDOWANE]
*   **Odświeżanie:** Wybrano wariant przeładowania listy w cyklu życia widoku (np. wywołanie `loadTickets()` w LaunchedEffect / onResume we frontendowym `TicketsScreen.kt`). Wprowadzanie rozproszonego reaktywnego cache'owania (inwalidacji) w ViewModelu bez centralnego repozytorium danych mogłoby prowadzić do wycieków pamięci i race-conditions przy nałożonych filtrach wyszukiwania.
*   **Format numeru:** Wybrano pełne ujednolicenie generatora na backendzie (`ZGL/RRRR/NNN`), dostosowanie powiązanych testów jednostkowych oraz ujednolicenie formatów w seedach bazy danych Flyway.

---

## 3. Rekomendowana kolejność działań naprawczych (Chronologia)

1.  **Krok 1 (Baza i Backend - Finanse):** Dodanie endpointu `/api/users/me` w `UserController.java` i `UserService.java`. Utworzenie migracji Flyway SQL ujednolicającej typy transakcji z `'NALEZNOSC'` na `'NALICZENIE'`.
2.  **Krok 2 (Frontend - Finanse & Profil):** Usunięcie heurystyki z `UserApartmentService.kt`. Pobieranie danych profilu mieszkańca z `/api/users/me` przy logowaniu/uruchomieniu i odczytywanie stamtąd `apartmentId` dla Finansów, Odczytów i Dokumentów. Integracja danych z profilu na ekranie `ProfileScreen` (usunięcie hardkodowanych danych).
3.  **Krok 3 (Backend - Zdjęcia):** Refaktoryzacja autoryzacji widoczności zgłoszenia i wdrożenie poprawki w `TicketImageService.java`.
4.  **Krok 4 (Backend + Frontend - Numeracja i Reaktywność):** Aktualizacja formatu w `TicketNumberGenerator.java` oraz dodanie przeładowania listy zgłoszeń przy powrocie z formularza zapisu.

---

## 4. Status planów testowych

*   **Scenariusze błędów manualnych:** Szczegółowe przepływy testów manualnych wariantów poprawek znajdują się w pliku [flow_test_plan.md](file:///home/pprezydent/Desktop/studia/semestr-6/programowanie-zespolowe/PZ-26-ROZOWI/audit/testing/flow_test_plan.md).
*   **Kompleksowy plan testów całej aplikacji:** Został opracowany i zapisany w pliku [comprehensive_test_plan.md](file:///home/pprezydent/Desktop/studia/semestr-6/programowanie-zespolowe/PZ-26-ROZOWI/audit/testing/comprehensive_test_plan.md) (11 modułów, 81 endpointów, 30 ekranów - 100% pokrycia funkcjonalnego).
