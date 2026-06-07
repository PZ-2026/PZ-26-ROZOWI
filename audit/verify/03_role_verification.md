# Role verification — UI controls

Cel: sprawdzić, czy akcje w UI są pokazywane/ukrywane na podstawie roli użytkownika oraz skąd pochodzi informacja o roli.

Metodologia: przeszukano źródła UI (frontend/app/src/main/java) pod kątem porównań ról i miejsc, w których role są pobierane.

Ogólne wnioski
- Źródło roli: głównie TokenStorage -> AuthService.getCurrentUserRole() lub TicketService.getCurrentUserRole() (która zwraca tokenStorage.getUserRole()). Czyli rola pochodzi z tokenu przechowywanego w TokenStorage. (pliki: services/TokenStorage.kt, services/AuthService.kt, services/TicketService.kt)
- Mechanizm kontroli: UI używa głównie dwóch podejść:
  * przekazywanie currentUserRole/isManager z ViewModel do Composable (np. TicketsViewModel -> TicketsScreen; ProfileViewModel -> ProfileScreen)
  * bezpośredne porównania (string lub enum) w komponencie (np. currentUserRole == "ZARZADCA", lub role == UserRole.ZARZADCA).
- Konkluzja: kontrola widoczności jest wdrożona konsekwentnie w większości ekranów; nie znaleziono oczywistych miejsc, gdzie elementy admin-only są renderowane bez żadnej kontroli roli.

Szczegóły per ekran / komponent

- TicketsScreen / TicketsViewModel / TicketFilterPanel
  - Pliki: ui/views/tickets/screens/TicketsScreen.kt, ui/views/tickets/viewmodels/TicketsViewModel.kt, ui/views/tickets/components/TicketFilterPanel.kt
  - Kontrola: FAB widoczny tylko gdy currentUserRole == "MIESZKANIEC"; panel filtrów pokazuje sekcje zarządcy gdy currentUserRole == "ZARZADCA" (TicketsViewModel pobiera rolę przez TicketService.getCurrentUserRole()).
  - Ocena: ✅ poprawne. Rola pochodzi z TokenStorage przez TicketService/AuthService.

- TicketDetailsContent / TicketCommentsSection
  - Pliki: ui/views/tickets/contents/TicketDetailsContent.kt, ui/views/tickets/components/TicketCommentsSection.kt
  - Kontrola: akcje (upload, internal comment toggle, przyciski statusów) warunkowane są przez currentUserRole (string) lub porównanie z rolami. currentRole przekazywane z ViewModel.
  - Ocena: ✅ poprawne.

- ProfileContent / ProfileScreen / ProfileViewModel
  - Pliki: ui/views/profile/contents/ProfileContent.kt, ui/views/profile/screens/ProfileScreen.kt, ui/views/profile/viewmodels/ProfileViewModel.kt
  - Kontrola: sekcja "Ustawienia zarządcy" (AdminNavRow) renderowana tylko gdy isManager == true. isManager ustalane w ProfileScreen przez viewModel.isManager() -> AuthService.getCurrentUserRole().
  - Ocena: ✅ poprawne.

- NotificationsScreen / NotificationsViewModel
  - Pliki: ui/views/notifications/screens/NotificationsScreen.kt, ui/views/notifications/viewmodels/NotificationsViewModel.kt
  - Kontrola: dostęp do ekranu realizowany przez link w ProfileContent, który jest widoczny tylko dla isManager. ViewModel zainicjuje wywołania admin API (notificationService.getSettings()) bez dodatkowego sprawdzenia roli.
  - Ocena: ⚠️ akceptowalne przy założeniu, że nawigacja do ekranu jest zabezpieczona UI (link widoczny tylko dla zarządcy). Zalecenie: dodać dodatkową ochronę po stronie klienta (np. w inicjalizacji ViewModel sprawdź isManager) lub polegać na 403 z backendu.

- Inspections / Announcements / Resolutions / Finances
  - Pliki: odpowiednie ViewModel/Screen pliki (inspections, announcements, resolutions, finances)
  - Kontrola: wszystkie używają isManager lub porównania z UserRole.ZARZADCA; ViewModel pobiera rolę przez AuthService.getCurrentUserRole().
  - Ocena: ✅ poprawne.

Uwagi i rekomendacje
- Konsystencja: w paru miejscach używane są porównania stringowe ("ZARZADCA"/"MIESZKANIEC"), a w innych enum UserRole. Rekomendacja: ujednolicić (preferować enum UserRole) by uniknąć literówek.
- UI-level gating nie zastępuje walidacji po stronie serwera. Nawet jeśli przyciski są ukryte, backend musi egzekwować uprawnienia (403). Zaznaczyć to w raporcie technicznym.
- Miejsce do poprawy: NotificationsViewModel inicjuje admin API bez lokalnego sprawdzenia roli — dodać guardę lub zabezpieczyć tworzenie ViewModel tylko dla adminów.

Pliki odnalezione (dowód wyszukiwania):
- services/TokenStorage.kt, services/AuthService.kt, services/TicketService.kt
- ui/views/tickets/screens/TicketsScreen.kt
- ui/views/tickets/viewmodels/TicketsViewModel.kt
- ui/views/tickets/components/TicketFilterPanel.kt
- ui/views/tickets/contents/TicketDetailsContent.kt
- ui/views/tickets/components/TicketCommentsSection.kt
- ui/views/profile/contents/ProfileContent.kt
- ui/views/profile/screens/ProfileScreen.kt
- ui/views/profile/viewmodels/ProfileViewModel.kt
- ui/views/notifications/viewmodels/NotificationsViewModel.kt
- ui/views/inspections/viewmodels/InspectionsViewModels.kt
- ui/views/announcements/viewmodels/AnnouncementsViewModel.kt
- ui/views/resolutions/viewmodels/ResolutionViewModels.kt
- ui/views/finances/viewmodels/FinancesViewModel.kt

W razie akceptacji: przejść do FAZA 4 (podsumowanie techniczne) i wygenerować audit/verify/04_technical_report.md zawierający liczbę zadań ✅/⚠️/❌, listę krytycznych niezgodności API, martwy kod API (plik 02), problemy z rolami (ten plik), oraz Top10 problemów do naprawy.
