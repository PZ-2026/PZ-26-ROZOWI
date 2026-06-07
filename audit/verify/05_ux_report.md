Audit UX — Batch 1 — Zgłoszenia (tickets)
Data: 2026-06-05
Zakres: weryfikacja UI/UX (bez zmian w kodzie). Ekrany: TicketsScreen, TicketDetailsScreen, CreateTicketScreen, AssignConservatorSheet, ConservatorActionSheet, ManagerRejectSheet.

[TicketsScreen] — rola: MIESZKANIEC / ZARZĄDCA / KONSERWATOR
Przyciski/akcje: ⚠️ FAB „Utwórz zgłoszenie” obecny tylko dla MIESZKANIEC (widoczność oparta na state.currentUserRole == "MIESZKANIEC"). Inne akcje (filtry, item click) są dostępne.
Stany UI: ⚠️ LoadingIndicator ok; EmptyState dla pustej listy ok; w Error używane EmptyState(s.message) bez przycisku retry — brak możliwości retry z poziomu ekranu. Brak shimmer/szkieletów.
Nawigacja: ✅ Brak TopAppBar (bottom nav) — poprawne; klik w item → event → nawigacja do szczegółów; powrót po create obsługiwany.
Teksty: ⚠️ Wszystkie etykiety po polsku, ale daty formatowane przez createdAt.take(10) (ISO substring) — niepolski format. Błędy wyświetlane bez lokalizacji/konkretnej treści (s.message może być surowy).
Spójność: ⚠️ Używane wspólne komponenty (TicketListItem, TicketFilterPanel). Niezgodność formatowania dat z innymi ekranami.
Krytyczne problemy: Brak przycisku retry w stanie błędu (blokuje odzyskanie po problemach sieciowych); daty nie sformatowane po polsku.

[TicketDetailsScreen] — rola: MIESZKANIEC / ZARZĄDCA / KONSERWATOR
Przyciski/akcje: ✅ TopAppBar z back; kontekstowe FABy dla ról/statusów (przypisz/odrzuć/wznów/pobierz PDF/rozpocznij/zakończ) — dostępne zgodnie z rolą/status. Dialogi/sheety (Assign/Reject/ConservatorAction) dostępne.
Stany UI: ⚠️ LoadingIndicator, komentarze/obrazy mają własne loadingi; upload/protokół pokazują isLoading. Jednak error state używa EmptyState bez retry; niektóre sheets nie mają widocznego stanu wysyłania (brak disabling/progress w sheet po submit).
Nawigacja: ✅ TopAppBar z przyciskiem wstecz; widoczne eventy nawigacyjne (NavigateBack).
Teksty: ⚠️ Teksty po polsku i statusy mapowane (toPresentation) — OK. Daty formatowane lokalnie w niektórych miejscach, ale formatDateTime() zwraca "yyyy-MM-dd, hh:mm" (niepolski format) — niezgodność.
Spójność: ⚠️ Komponenty spójne, ale brak jednolitego formatowania dat i różne sposoby pokazywania błędów.
Krytyczne problemy: Brak retry w przypadku błędu ładowania szczegółów (może zablokować dalsze akcje); brak kliknięcia miniatury zdjęcia (brak full-screen gallery) — utrudnia inspekcję zdjęć; niepolskie formaty dat.

[CreateTicketScreen] — rola: MIESZKANIEC
Przyciski/akcje: ✅ Formularz ma przycisk "Zgłoś usterkę"; przycisk disabled gdy wymagane pola puste; pola disabled podczas submit; success dialog z powrotem do listy.
Stany UI: ⚠️ Kategorie mają loading; submit pokazuje progress w przycisku. Jednak PhotoPlaceholderRow jest statyczny — brak mechanizmu dodania zdjęć w formularzu (brak pickera) — UX niekompletny.
Nawigacja: ✅ Po sukcesie wyświetlany dialog z opcją powrotu do listy (onNavigateBack) — zgodne z wymaganiem.
Teksty: ✅ Wszystkie stringi po polsku; snackbar błędu pokazuje "Błąd: ${state.message}" (może zawierać surową treść).
Spójność: ✅ Użycie wspólnych komponentów i stylów.
Krytyczne problemy: Brak możliwości dodania zdjęć przy tworzeniu zgłoszenia (jeśli wymóg funkcjonalny — problem). Brak(=nieblokujące) retry przy błędzie pobrania kategorii.

[AssignConservatorSheet] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Dwustopniowy sheet (data/godzina → wybór konserwatora) — walidacja czasu; przycisk zatwierdź wymaga wybranego konserwatora.
Stany UI: ⚠️ DatePicker/TimePicker dobrze z lokalizacją (SimpleDateFormat z Locale pl), ale brak widocznego stanu wysyłania po naciśnięciu "Zatwierdź" (sheet zamyka się natychmiast).
Nawigacja: ✅ ModalBottomSheet z onDismissRequest — zgodne.
Teksty: ✅ Daty lokalizowane po polsku (np. "dd MMMM yyyy").
Spójność: ✅ Komponenty zgodne z resztą aplikacji.
Krytyczne problemy: brak

[ConservatorActionSheet] — rola: KONSERWATOR
Przyciski/akcje: ✅ Typy akcji: START / FINISH / PAUSE_OR_COMMENT / CLOSE_VERIFICATION. Przyciski wymagające treści (np. zakończenie) są disabled gdy pole puste.
Stany UI: ⚠️ Brak explicit loading/disabled stanu w sheetie po submit — jednak parent ma flagi (isUploading/isSending) w innych sekcjach.
Nawigacja: ✅ Modal sheet z dismiss.
Teksty: ✅ Po polsku.
Spójność: ✅ Spójne przyciski/kolory sukcesu/błędu.
Krytyczne problemy: brak

[ManagerRejectSheet] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Formularz odrzucenia wymaga powodu; przycisk disabled dopóki pole nie jest puste.
Stany UI: ⚠️ Brak widocznego stanu wysyłania w sheet po kliknięciu — UX może mylić przy wolnej sieci.
Nawigacja: ✅ ModalBottomSheet z onDismissRequest.
Teksty: ✅ Po polsku.
Spójność: ✅ Komponenty zgodne.
Krytyczne problemy: brak

---
Następne kroki (FAZA 1):
- Proszę potwierdzić, czy kontynuować kolejną partią (następne ekrany: ResidentMainScreen, TicketsList components, Images/Comments deeper flows). Aktualnie oznaczyłem zadanie 'verify-conservator-critical' jako in_progress.
- W następnej partii sprawdzę: pełnoekranową galerię (implementacja/UX), retry/refresh dla error states, oraz spójność formatowania dat/kwot.

---
Audit UX — Batch 2 — ResidentMain & List components
Data: 2026-06-05
Zakres: ResidentMainScreen, TicketListContent, TicketListItem, TicketFilterPanel, TicketImagesSection, TicketImageThumbnail, TicketCommentsSection

[ResidentMainScreen] — rola: wspólne
Przyciski/akcje: ✅ TopBar z przyciskiem wyloguj; brak przycisku wstecz (poprawne dla bottom nav). Brak przycisku "logout" confirmation — nie wymagany, ale rozważyć.
Stany UI: ⚠️ Brak globalnego loading overlay lub error placeholder — tytuł zmienia się na "Błąd", ale nie ma retry/CTA; innerContent renderuje nawet w error/loading, co może prowadzić do pustego ekranu.
Nawigacja: ✅ BottomNavBar obecna, state-driven navigation; onOptionClicked obsługuje save/restore states.
Teksty: ✅ Wszystkie stringi po polsku.
Spójność: ✅ Użyto TopBar i BottomNavBar wspólnych komponentów.
Krytyczne problemy: Brak retry/global error state (może zablokować użytkownika) — rekomendacja: dodać LoadingIndicator/EmptyState z retry overlay dla globalnych błędów inicjalizacji.

[TicketListContent] — rola: M/Z/K
Przyciski/akcje: ✅ Elementy listy klikane; brak dodatkowych akcji inline (OK).
Stany UI: ⚠️ Data displayed via createdAt.take(10) — niepolski format (np. 2026-03-10). Brak shimmer per-item.
Nawigacja: ✅ onTicketClicked wywołuje detale.
Teksty: ✅ Po polsku.
Spójność: ⚠️ Konsystencja statusów OK; data format mismatch.
Krytyczne problemy: Daty nie sformatowane — powoduje niespójność z resztą aplikacji.

[TicketListItem] — rola: M/Z/K
Przyciski/akcje: ✅ Pełny obszar klikany; chevron sugeruje nawigację.
Stany UI: ✅ Brak loading per item (zarządzane globalnie).
Nawigacja: ✅ Poprawne.
Teksty: ✅ Po polsku.
Spójność: ✅ Używa wspólnych TagBadge/StatusBadge i kolorów.
Krytyczne problemy: brak

[TicketFilterPanel] — rola: M/Z/K (manager ma dodatkowe filtry)
Przyciski/akcje: ✅ Szukaj, Filtry, Odśwież (onRefresh) — Odśwież dostępny bezpośrednio; dobry dla retry manualnego.
Stany UI: ✅ Obsługa loadingu dla opcji filtra (filterOptions.isLoading) z CircularProgressIndicator. Daty filtrowania prezentowane lokalnie (formatIsoForDisplay -> dd.MM.yyyy) — poprawne.
Nawigacja: ✅ AnimatedVisibility dla rozbudowanych filtrów; dropdowny działają.
Teksty: ✅ Po polsku.
Spójność: ✅ Komponent dopracowany i spójny z resztą.
Krytyczne problemy: brak

[TicketImagesSection & TicketImageThumbnail] — rola: WSPÓLNE
Przyciski/akcje: ⚠️ Miniatury nie są interaktywne (brak onClick otwierającego pełny podgląd) — utrudnia inspekcję zdjęć. Przycisk "Dodaj zdjęcie po pracach" dostępny dla konserwatora i poprawnie wskazuje loading podczas upload.
Stany UI: ✅ Loading dla obrazów i podczas uploadu; error placeholder dla miniatury istnieje.
Nawigacja: ⚠️ Brak pełnoekranowej galerii/nawigacji między zdjęciami.
Teksty: ✅ Po polsku; daty obrazów formatowane niekonsekwentnie (formatDateTime stosuje ISO split).
Spójność: ⚠️ Kolory tagów OK; brak spójnego formatu dat.
Krytyczne problemy: Brak full-screen gallery — traktować jako poważny UX blocker dla konserwatora (nie można dokładnie obejrzeć zdjęć).

[TicketCommentsSection] — rola: WSPÓLNE
Przyciski/akcje: ✅ Formularz dodawania komentarza z przyciskiem wysyłki; internal toggle widoczny tylko dla ZARZADCA/KONSERWATOR.
Stany UI: ✅ Pokazywanie loadingu przy wysyłaniu (trailering progress) i reset pola po sukcesie (commentResetKey). Brak globalnego disabling formularza podczas wysyłania (przycisk disabled ok).
Nawigacja: ✅ Brak.
Teksty: ✅ Po polsku.
Spójność: ✅ Komponent zgodny z resztą.
Krytyczne problemy: brak

---
Krytyczne problemy (cały batch):
1) Brak retry w TicketsScreen Error state — dodaj CTA "Spróbuj ponownie" wywołujące viewModel.loadTickets().
2) Brak full-screen gallery dla zdjęć — konserwator nie może szczegółowo przejrzeć dokumentacji zdjęciowej.
3) Niejednolity format dat (createdAt.take(10) vs formatIsoForDisplay) — ujednolicić do dd.MM.yyyy w całej aplikacji.

Poważne problemy:
- Brak globalnego loading/error overlay w ResidentMainScreen — utrudnia diagnostykę przy błędach inicjalizacji.
- Brak widocznego stanu wysyłania w niektórych sheets (AssignConservatorSheet/ManagerRejectSheet) — dodać disabling i progress.

Drobne:
- PhotoPlaceholderRow w CreateTicketForm wygląda na statyczny — dodać picker.
- Teksty pomocnicze mogą zawierać surowe błędy z backendu (s.message) — mapować błędy do użytkownika po polsku.

Zapiszę ten batch do pliku i kontynuuję kolejną partię (Finanse: FinancesScreen, TransactionsScreen, DocumentsScreen).

---
Audit UX — Batch 3 — Finanse
Data: 2026-06-05
Zakres: FinancesScreen, TransactionsScreen, DocumentsScreen, FinancialLedgerScreen, CsvImportScreen

[FinancesScreen] — rola: MIESZKANIEC / ZARZĄDCA
Przyciski/akcje: ⚠️ isManager obliczane przez viewModel.isManager() i zapisywane lokalnie (LaunchedEffect) — dobre źródło (nie hardkodowane). Nawigacja do transakcji/dokumentów/kartoteki/sald/import CSV dostępna.
Stany UI: ⚠️ Brak globalnego loadingu przy dłuższej inicjalizacji; FinancesOverviewContent renderuje zawartość zależnie od state.
Nawigacja: ✅ Zdarzenia z ViewModelu (FinancesEvent) poprawnie przekazywane do nav.
Teksty: ✅ Po polsku.
Spójność: ⚠️ Ogólna spójność komponentów OK.
Krytyczne problemy: brak

[TransactionsScreen] — rola: MIESZKANIEC / ZARZĄDCA
Przyciski/akcje: ✅ TopAppBar z Back; lista transakcji renderowana w TransactionsContent.
Stany UI: ⚠️ Data/kwoty wyświetlane surowo (tx.transactionDate, tx.amount.setScale(2) + " PLN") — kwoty używają kropki i skrótu PLN zamiast polskiego formatu (650,00 zł). To pogarsza czytelność.
Nawigacja: ✅ Back na poprzedni ekran.
Teksty: ⚠️ Kwoty — wymagają formatowania lokalnego.
Spójność: ⚠️ Niejednolity format kwot/datar.
Krytyczne problemy: Kwoty nie są sformatowane po polsku — Poważny problem UX.

[DocumentsScreen] — rola: MIESZKANIEC / ZARZĄDCA
Przyciski/akcje: ✅ Lista dokumentów, przycisk pobierz/otwórz; FinancesEvent.OpenPdf obsługiwany (Intent ACTION_VIEW).
Stany UI: ✅ Snackbar dla błędów; brak dedykowanego retry przy nieudanym pobraniu (można ponowić z UI listy).
Nawigacja: ✅ Back obecny.
Teksty: ✅ Po polsku.
Spójność: ✅ OK.
Krytyczne problemy: brak

[FinancialLedgerScreen] — rola: ZARZĄDCA / MIESZKANIEC (kartoteka lokalu)
Przyciski/akcje: ✅ FAB "Dodaj operację" widoczny tylko dla menedżera; AddTransactionDialog uruchamiany.
Stany UI: ✅ Loading/Error/Empty/Success obsłużone; EmptyState zawiera instrukcję dla menedżera.
Teksty: ⚠️ Salda i kwoty (balance.setScale(2) + " PLN" / tx.amount.setScale(2) + " PLN") — niepolskie formatowanie walutowe.
Nawigacja: ✅ Back obecny.
Spójność: ⚠️ Format kwot niespójny z wymogami (powinno być 650,00 zł).
Krytyczne problemy: Kwoty — Poważny problem UX.

[CsvImportScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Picker pliku, przycisk Importuj (disabled gdy brak pliku / podczas uploadu); przycisk "Importuj kolejny plik" po wyniku.
Stany UI: ✅ Wyraźny stan Uploadingu (AnimatedContent + progress); rezultaty i błędy pokazane w czytelny sposób z numerem wiersza.
Nawigacja: ✅ Back obecny.
Teksty: ✅ Instrukcje po polsku; format daty podany jako YYYY-MM-DD — warto dodać przykład i lokalne formaty.
Spójność: ✅ UX importu spójny i czytelny.
Krytyczne problemy: brak

---
Krytyczne problemy (cały batch):
1) Kwoty w Finances/Transactions/Ledger używają notacji z kropką i "PLN" — wymagane jest sformatowanie do polskiej konwencji walutowej (np. 650,00 zł). To wpływa na czytelność i zaufanie użytkowników.

Poważne problemy:
- Niejednolity format dat transakcji (może być ISO vs dd.MM.yyyy) — ujednolicić.

Drobne:
- CsvImport: dodać przykład formatu daty i komunikat przy błędach CSV (lokalizacja) oraz ewentualny retry dla pobierania dokumentów.

Kontynuuję — następna partia: Uchwały (ResolutionsListScreen, ResolutionDetailScreen) i Ogłoszenia.

---
Audit UX — Batch 4 — Uchwały i Ogłoszenia
Data: 2026-06-05
Zakres: ResolutionsListScreen, ResolutionDetailScreen, CreateResolutionDialog, AnnouncementsScreen, CreateAnnouncementScreen, EditAnnouncementScreen

[ResolutionsListScreen] — rola: MIESZKANIEC / ZARZĄDCA
Przyciski/akcje: ✅ FAB "Nowe głosowanie" widoczny dla menedżera; elementy listy klikane (ResolutionCard).
Stany UI: ⚠️ LoadingIndicator obecny; Error → EmptyState bez przycisku retry (brak szybkiego ponawiania ładowania). Pusta lista ma jasny komunikat i CTA dla menedżera.
Nawigacja: ✅ onNavigateToDetail poprawnie wywoływane.
Teksty: ⚠️ Data zakończenia wyświetlana przez ldt.toLocalDate() — format ISO (YYYY-MM-DD). Zalecane formatowanie dd.MM.yyyy.
Spójność: ✅ Komponenty wizualne spójne.
Krytyczne problemy: Brak retry w error state; format daty niepożądany.

[ResolutionDetailScreen] — rola: MIESZKANIEC / ZARZĄDCA
Przyciski/akcje: ✅ Głosowanie: wybór opcji + przycisk "Oddaj głos" z disabled podczas wysyłania; dla menedżera przycisk pobierz raport (z progress).
Stany UI: ✅ Loading/Error/Success obsłużone; widoczne stany wysyłania przy głosowaniu i pobieraniu raportu.
Nawigacja: ✅ Back dostępny.
Teksty: ⚠️ Daty (terminy) prezentowane jako ISO plus czas — zarekomendować lokalne formaty (dd.MM.yyyy HH:mm).
Spójność: ✅ UX głosowania czytelny i zabezpieczony (blokowanie przy wysyłaniu).
Krytyczne problemy: brak

[CreateResolutionDialog] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Formularz dialogowy z walidacją (min/max opcji), przycisk "Utwórz głosowanie" disabled gdy niepoprawny.
Stany UI: ⚠️ Pole daty wymaga wpisu w formacie ISO (placeholder i supportingText). Brak date picker — ryzyko błędnej walidacji daty przez użytkownika.
Nawigacja: ✅ Dismiss obsługiwany, dialog zamykany po submit.
Teksty: ✅ Po polsku.
Spójność: ⚠️ Brak date picker wyróżnia się od innych ekranów (gdzie użyto DatePicker) — proponować ujednolicenie.
Krytyczne problemy: brak (ale UX: rozważyć dodanie DatePicker)

[AnnouncementsScreen & SampleAnnoucementsContent] — rola: MIESZKANIEC (odczyt) / ZARZĄDCA (CRUD)
Przyciski/akcje: ✅ FAB create (menedżer); przyciski edycji i usuwania widoczne dla menedżera.
Stany UI: ✅ Loading/Empty/Error obsłużone (Error → snackbar / EmptyState). Lista zawiera CTA do pobrania załącznika.
Nawigacja: ✅ Edytuj → Edit screen; Create → Create screen.
Teksty: ✅ Po polsku; przycisk pobrania ma emoji i klarowną etykietę.
Spójność: ⚠️ Brak confirmation dialog przy usuwaniu ogłoszenia — bezpośrednie usuwanie po kliknięciu (ikonka Delete) może prowadzić do przypadkowych skasowań.
Krytyczne problemy: Brak potwierdzenia przy usuwaniu ogłoszeń — rekomendacja: dodać dialog potwierdzenia (destructive action).

[CreateAnnouncementScreen / EditAnnouncementScreen]
Przyciski/akcje: ✅ Obsługa załącznika (picker), submit z disabled podczas wysyłania, snackbar na błędy, sukces → navigate back.
Stany UI: ✅ Wyraźne stany isSubmitting + loader w przycisku; attachment name prezentowany.
Nawigacja: ✅ Back button obecny.
Teksty: ✅ Po polsku.
Spójność: ✅ Wykorzystuje wspólny CreateAnnouncementContent.
Krytyczne problemy: brak

---
Krytyczne problemy (cały batch):
1) Brak dialogu potwierdzenia przy usuwaniu ogłoszeń — użytkownik może przypadkowo usunąć informację.
2) Brak retry w niektórych error states (ResolutionsList) — dodać CTA "Spróbuj ponownie".

Poważne:
- Wprowadzanie dat w formacie ISO bez date picker w CreateResolutionDialog — ryzyko błędnych danych.

Drobne:
- Format dat w listach (ISO) — ujednolicić do dd.MM.yyyy.
- Rozważyć ujednolicenie użycia DatePicker gdzie to możliwe.

Zapisuję wyniki. Kontynuuję kolejną partię: Profil + Powiadomienia + Nieruchomości.

---
Audit UX — Batch 5 — Profil, Powiadomienia, Użytkownicy, Nieruchomości, Logo wspólnoty
Data: 2026-06-05
Zakres: ProfileScreen, ProfileContent, NotificationsScreen, UsersScreen, PropertyTreeScreen, CommunityLogoScreen

[ProfileScreen] — rola: WSPÓLNE
Przyciski/akcje: ✅ Nawigacja do ustawień (powiadomienia, logo, przeglądy) widoczna tylko gdy isManager = true (isManager pobierane z ViewModelu). Brak edycji profilu (celowe).
Stany UI: ⚠️ Loading ok. Brak explicite stanu Error — ViewModel przy błędzie ustawia ProfileState.Data(role = "Błąd") i pokazuje snackbar. Skutkuje nieczytelnym widokiem zamiast jasnego komunikatu i CTA retry.
Nawigacja: ✅ Brak TopAppBar (bottom nav), linki do podstron działają.
Teksty: ✅ Po polsku.
Spójność: ✅ Komponenty i styl zgodne z resztą aplikacji.
Krytyczne problemy: Brak explicite Error state i CTA retry — może zablokować użytkownika przy problemach z ładowaniem profilu.

[NotificationsScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Włącz/wyłącz per-typ (Switch) + inline progress (isUpdating) — dobre UX dla akcji.
Stany UI: ⚠️ Loading/Success ok. Error pokazany jako EmptyState bez przycisku "Spróbuj ponownie". EmptyState gdy brak ustawień również bez CTA. Błędy backendu mogą być wyświetlane surowo (s.message).
Nawigacja: ✅ TopAppBar z back.
Teksty: ✅ Po polsku (etykiety i statusy: "Włączone"/"Wyłączone").
Spójność: ✅ Spójne komponenty i inline progress dla toggle.
Krytyczne problemy: Brak retry w Error/Empty — dodać CTA wywołujące viewModel.loadSettings().

[CommunityLogoScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Picker pliku, przycisk Prześlij z disabling podczas uploadu; wybór wspólnoty (dropdown) jeśli więcej niż 1.
Stany UI: ✅ Wyraźny isUploading + progress, success state (ikonka) i snackbar.
Nawigacja: ✅ TopAppBar z back.
Teksty: ✅ Instrukcje po polsku.
Spójność: ✅ Ujednolicone karty i przyciski.
Krytyczne problemy: ⚠️ Brak walidacji rozmiaru/typu pliku po stronie UI — tylko informacja tekstowa; dodać walidację przed uploadem, którą pokazuje komunikat.

[UsersScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ FAB "Nowe konto" otwiera CreateUserDialog z walidacją; deaktywacja wymaga potwierdzenia (AlertDialog).
Stany UI: ⚠️ Loading ok. Error pokazany jako EmptyState bez CTA retry. CreateUserDialog: przycisk potwierdzenia disabled gdy niepoprawny, pokazuje loader podczas submit — OK. Deaktywacja: dialog zamyka się natychmiast po potwierdzeniu i nie pokazuje progress/disabling podczas wywołania API (viewModel.deactivateUser wykonywane asynchronicznie) — UX mylący przy wolnej sieci.
Nawigacja: ✅ Lista → szczegóły (onNavigateToUser). Brak TopAppBar lokalnego (sterowane przez rodzica) — akceptowalne.
Teksty: ✅ Po polsku; drobna uwaga: "Deaktywuj" (można rozważyć spójne użycie "Dezaktywuj").
Spójność: ✅ Komponenty i badge’y spójne.
Krytyczne problemy: Brak retry w Error state; brak feedbacku/progressu podczas deaktywacji (zamknięcie dialogu przed zakończeniem operacji).

[PropertyTreeScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Dodawanie/edycja przez BottomSheet (PropertyDetailPanel) z isSaving przekazywanym do panelu; delete wymaga potwierdzenia (AlertDialog).
Stany UI: ✅ Loading/Success/Error obsłużone. Error state zawiera przycisk "Spróbuj ponownie" wywołujący viewModel.loadTree() — dobra praktyka.
Nawigacja: ✅ ModalBottomSheet z dismiss; wybór elementów otwiera panel.
Teksty: ✅ Po polsku.
Spójność: ✅ Użycie wspólnych formularzy i styli.
Krytyczne problemy: ⚠️ Potwierdzenie usunięcia nie pokazuje stanu wysyłania — dialog zamyka się od razu, brak disabling/loader podczas operacji usuwania.

---
Krytyczne problemy batchu:
- Brak retry/CTA w Error stateach: ProfileScreen (brak Error), NotificationsScreen (EmptyState bez retry), UsersScreen (Error bez retry). Dodać widoczny CTA "Spróbuj ponownie" lub przycisk odświeżania.
- Deaktywacja/usuń: brak feedbacku podczas operacji (dialog zamyka się od razu). Zamiast natychmiastowego zamknięcia, pokazać progress lub zablokować opcje do momentu odpowiedzi.

Poważne problemy:
- Brak walidacji rozmiaru pliku w CommunityLogoScreen (może prowadzić do błędów uploadu).

Drobne:
- ProfileViewModel ustawia Data z role="Błąd" zamiast użyć stanu Error — zmienić dla czytelności.
- Spójność terminów: "Deaktywuj" vs "Dezaktywuj" (opcjonalne).

Zapisuję ten batch i mogę kontynuować następną partię: Liczniki (meters), Przeglądy (inspections), Dokumenty dystrybucji.

---
Audit UX — Batch 6 — Liczniki, Przeglądy, Dystrybucja dokumentów
Data: 2026-06-05
Zakres: MeterListScreen, MeterDetailScreen, CreateMeterDialog, CreateMeterReadingDialog, InspectionsListScreen, CreateInspectionDialog, DocumentDistributionScreen

[MeterListScreen] — rola: MIESZKANIEC / ZARZĄDCA
Przyciski/akcje: ⚠️ FAB "Dodaj licznik" widoczny bez warunku roli (widoczny w UI bez isManager check). Menu deaktywacji obecne; potwierdzenie usuwa dialog od razu i nie pokazuje stanu wysyłania.
Stany UI: ⚠️ Loading ok; Error używa EmptyState(title = "Błąd", description = s.message) bez CTA "Spróbuj ponownie". EmptyState dla pustej listy obecne.
Nawigacja: ✅ TopBar z powrotem (↑) obecny.
Teksty: ⚠️ Data montażu wyświetlana surowo (ISO). Placeholder/formularz CreateMeter używa "YYYY-MM-DD" (ISO) — niepolski format.
Spójność: ⚠️ Brak ujednoliconego formatowania dat z resztą aplikacji; numery/liczniki pokazane bez lokalnego formatowania.
Krytyczne problemy: FAB dostępny bez kontroli roli (może ujawnić akcję tylko dla menedżera) — zalecane: ukryć FAB gdy użytkownik nie jest zarządcą; dodać retry CTA w Error state.

[MeterDetailScreen] — rola: MIESZKANIEC / ZARZĄDCA
Przyciski/akcje: ✅ FAB "Wprowadź stan" + edycja odczytu; jednak przycisk usuń odczytu działa bez potwierdzenia (ikonka usuń wywołuje bezpośrednio viewModel.deleteReading).
Stany UI: ⚠️ Loading/empty ok; Error używa EmptyState bez retry; paginacja z loaderem obecna.
Nawigacja: ✅ TopBar z back.
Teksty: ⚠️ Daty odczytów i pola wartości wyświetlane surowo (ISO i surowe BigDecimal → string). Zalecane formatowanie dat po PL i format liczb (separator dziesiętny).
Spójność: ⚠️ Brak potwierdzenia dla destrukcyjnej akcji usuń odczyt — niezgodne z polityką przycisków destructive.
Krytyczne problemy: ❌ Brak dialogu potwierdzającego przy usuwaniu odczytu (może prowadzić do przypadkowej utraty danych).

[CreateMeterDialog] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Formularz z walidacją; przycisk confirm disabled gdy niepoprawny; loading w przycisku — OK.
Stany UI: ⚠️ Pole Data montażu jako tekst z placeholderem "YYYY-MM-DD" i supportingText "Format: RRRR-MM-DD" — brak DatePicker/TimePicker (ryzyko błędnego formatu przez użytkownika).
Nawigacja: ✅ Dismiss działa gdy nie jest isSubmitting.
Teksty: ⚠️ Użycie ISO w placeholderze zamiast formatu po polsku.
Spójność: ✅ Styl zgodny z resztą.
Krytyczne problemy: brak (ale UX: dodać DatePicker + lokalne formatowanie dat).

[CreateMeterReadingDialog] — rola: ZARZĄDCA / KONSERWATOR
Przyciski/akcje: ✅ Confirm disabled gdy niepoprawny; Animated loader podczas submit — OK.
Stany UI: ⚠️ Pole Data odczytu jako tekst ("YYYY-MM-DD"); walidacja regex na ISO. Brak date picker.
Nawigacja: ✅ Dismiss zabezpieczony przy isSubmitting.
Teksty: ⚠️ Niepolskie formaty dat.
Spójność: ✅ Przycisk submit i stany spójne z innymi dialogami.
Krytyczne problemy: brak

[InspectionsListScreen] — rola: ZARZĄDCA / MIESZKANIEC (widok zależny od isManager)
Przyciski/akcje: ⚠️ FAB "Zaplanuj przegląd" warunkowany parametrem isManager, ale domyślna wartość parametru to true (isManager: Boolean = true) — ryzyko nieintencjonalnego pokazania funkcji menedżerskich jeśli ekran zostanie użyty bez parametru.
Stany UI: ⚠️ Loading ok; Error → EmptyState bez CTA "Spróbuj ponownie". Karty przeglądów pokazują termin parsowany i formatowany jako ISO (np. YYYY-MM-DD HH:MM) — niepolski format.
Nawigacja: ✅ TopAppBar z back.
Teksty: ⚠️ Data/czas przedstawione w formacie ISO; sugestia: lokalne dd.MM.yyyy HH:mm i/lub DateTimePicker w formularzu.
Spójność: ⚠️ Brak potwierdzenia przy usuwaniu przeglądu (onDelete wywołuje viewModel.deleteInspection bez dialogu) — niespójne z zasadą destructive → confirmation.
Krytyczne problemy: ⚠️ Domyślne isManager = true (potencjalne ujawnienie akcji zarządcy); brak potwierdzenia usuwania.

[CreateInspectionDialog] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Formularz z walidacją i loaderem — OK.
Stany UI: ⚠️ Planowana data wprowadzana jako tekst ISO (placeholder: 2026-06-15T08:00:00) — brak date/time picker, łatwo o niepoprawny format.
Nawigacja: ✅ Dismiss/confirm zachowują stan isSubmitting.
Teksty: ⚠️ SupportingText używa ISO-formatu zamiast przyjaznego przykładu PL.
Spójność: ⚠️ Ujednolicić z resztą aplikacji (DatePicker).
Krytyczne problemy: brak

[DocumentDistributionScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Zakładki: Zmiana stawek / Rozliczenie; SendButton bierze pod uwagę isSubmitting/isSent — dobry feedback podczas wysyłki.
Stany UI: ⚠️ Pole "Data wejścia w życie (YYYY-MM-DD)" wymusza ISO-regex w walidacji — UX oczekuje formatu ISO zamiast lokalnego. Brak widocznego retry dla błędów wysyłki (tu: snackbar używany przez events).
Nawigacja: ✅ TopAppBar z back.
Teksty: ⚠️ Placeholdery i walidacja daty nie są po polsku — zrekomendować przykład i DatePicker.
Spójność: ✅ Ogólne komponenty spójne.
Krytyczne problemy: brak

---
Krytyczne problemy (cały batch):
- Brak kontroli widoczności akcji menedżerskich: FAB "Dodaj licznik" widoczny bez warunku roli w MeterListScreen; inspekcje mają domyślnie isManager = true — zabezpieczyć (ukryć/disable gdy nie-manager).
- Brak dialogów potwierdzających dla destrukcyjnych akcji: usuwanie odczytu (MeterDetail), usuwanie przeglądu (InspectionsList) — wprowadzić AlertDialog z potwierdzeniem i pokazywać loader podczas operacji.
- Brak CTA "Spróbuj ponownie" w Error stateach (MeterList, MeterDetail, InspectionsList) — dodać przycisk wywołujący odpowiednie load()/refresh() w ViewModel.
- Niejednolity i niepolski format dat (ISO używany w wielu polach) — ujednolicić do dd.MM.yyyy (gdzie stosowne) i wprowadzić Date/Time picker w formularzach.

Poważne problemy:
- Domyślne isManager = true w InspectionsListScreen — potencjalne ujawnienie uprawnień menedżera.
- Deaktywacja/licznik: dialog deaktywacji zamyka się natychmiast bez feedbacku — dodać disabling/progress.

Drobne:
- Placeholdery "YYYY-MM-DD" / supportingText używają anglojęzycznych skrótów (RRRR-MM-DD) — ujednolicić i lokalizować.
- Liczniki: rozważyć format liczb z polskim separatorem dziesiętnym przy wyświetlaniu wartości.

Zapisuję ten batch.

---
Audit UX — Batch 7 — Autentykacja (auth)
Data: 2026-06-05
Zakres: LoginScreen, ForgotPasswordScreen, ResetPasswordScreen, AcceptInvitationScreen (+ formularze: LoginForm, ForgotPasswordForm, ResetPasswordForm, AcceptInvitationForm)

[LoginScreen] — rola: wspólne
Przyciski/akcje: ✅ Przycisk „Zaloguj się” disabled gdy email/hasło puste; disabled podczas Loading i AccountLocked; link „Zapomniałem hasła”; toggle widoczności hasła.
Stany UI: ✅ Loading przez tekst „Logowanie…” i disabled pola; banner AccountLocked (423); błąd inline w polu hasła + snackbar (AuthEvent.ShowError).
Nawigacja: ✅ Brak TopAppBar (ekran startowy) — poprawne; sukces → NavigateToMain; forgot-password → callback.
Teksty: ✅ Po polsku; komunikat błędu logowania lokalizowany w ViewModel. ⚠️ Nazwa marki „Blokur” (bez „UR”) — niespójność z nazwą projektu.
Spójność: ✅ Gradient + karta formularza spójne z innymi ekranami auth; PrimaryButton wspólny komponent.
Krytyczne problemy: brak

[ForgotPasswordScreen] — rola: wspólne
Przyciski/akcje: ✅ Submit disabled gdy email pusty / podczas Loading; przycisk „Wróć” / „Wróć do logowania” po sukcesie.
Stany UI: ⚠️ Success banner w formularzu — OK; błędy inline w polu. ShowSnackbar w LaunchedEffect ignorowany (`Unit`) — jeśli VM wysyła snackbar zamiast inline, użytkownik go nie zobaczy.
Nawigacja: ✅ Brak TopAppBar; NavigateBack po sukcesie (auto) i manualnie przez „Wróć”.
Teksty: ✅ Po polsku.
Spójność: ⚠️ Tytuł „Blokur” używa `colorScheme.primary` na gradiencie — mniejszy kontrast niż LoginScreen (biały tekst).
Krytyczne problemy: brak

[ResetPasswordScreen] — rola: wspólne (deep link)
Przyciski/akcje: ✅ Submit disabled gdy pola puste / Loading; CTA „Poproś o nowy link” przy TokenExpired; „Przejdź do logowania” po sukcesie.
Stany UI: ✅ Loading, Success banner, TokenExpired z komunikatem; błędy inline. ShowSnackbar ignorowany jak wyżej.
Nawigacja: ✅ Po sukcesie → login; wygasły token → forgot-password.
Teksty: ✅ Po polsku; walidacja hasła opisana w UI (min. 8 znaków).
Spójność: ✅ Ten sam układ co AcceptInvitation.
Krytyczne problemy: brak

[AcceptInvitationScreen] — rola: zaproszony użytkownik
Przyciski/akcje: ✅ Analogicznie do ResetPassword; submit disabled gdy pola puste.
Stany UI: ✅ Loading/Success/TokenExpired/Error obsłużone w formularzu.
Nawigacja: ✅ Po sukcesie → login; brak TopAppBar (deep link) — poprawne.
Teksty: ✅ Po polsku.
Spójność: ✅ Spójne z ResetPasswordScreen.
Krytyczne problemy: brak

---
Audit UX — Batch 8 — Admin pozostałe + dialogi
Data: 2026-06-05
Zakres: CategoriesScreen, CategoryFormDialog, SlaEditDialog, EditUserScreen, ApartmentBalancesScreen, AddTransactionDialog (uzupełnienie)

[CategoriesScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ FAB „Nowa kategoria”; edycja nazwy, SLA, deaktywacja z AlertDialog potwierdzenia. ⚠️ Dialog zamyka się natychmiast po confirm (create/update/deactivate/Sla) — brak progress w dialogu podczas API.
Stany UI: ⚠️ Loading OK; Error → EmptyState bez CTA retry; Empty z instrukcją; snackbar przez events.
Nawigacja: ✅ TopAppBar z back.
Teksty: ⚠️ Badge SLA: „24h SLA” (skrót angielski „h” zamiast „godz.”); reszta po polsku.
Spójność: ✅ Karty i FAB spójne z resztą admin UI.
Krytyczne problemy: brak retry w Error state.

[CategoryFormDialog] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Confirm disabled gdy nazwa pusta / isSubmitting; loader w przycisku; dismiss zablokowany przy submit.
Stany UI: ✅ isSubmitting z progress; limit 100 znaków z licznikiem.
Nawigacja: ✅ AlertDialog z dismiss.
Teksty: ✅ Po polsku; placeholder „Np. Hydraulika, Elektryka...”.
Spójność: ✅ Zgodny z innymi dialogami formularzowymi.
Krytyczne problemy: brak

[SlaEditDialog] — rola: ZARZĄDCA (w CategoriesScreen.kt)
Przyciski/akcje: ✅ Confirm disabled gdy SLA < 1; walidacja numeryczna.
Stany UI: ⚠️ Brak isSubmitting/loader — dialog zamyka się od razu po confirm (parent wywołuje API asynchronicznie).
Nawigacja: ✅ AlertDialog.
Teksty: ✅ Po polsku.
Spójność: ✅ OK.
Krytyczne problemy: brak

[EditUserScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ „Zapisz” disabled gdy !formState.isValid || isSubmitting; overlay podczas zapisu; dropdowny budynek/klatka/lokal dla MIESZKANIEC. ❌ Brak przycisku dezaktywacji (dezaktywacja tylko z listy UsersScreen — flow OK, ale import DeleteForever nieużyty). Brak walidacji wymaganego lokalu przy submit poza snackbar.
Stany UI: ⚠️ Loading/Error/Success; Error bez retry CTA; isLoadingBuildings z CircularProgressIndicator.
Nawigacja: ✅ TopAppBar back; NavigateBack po sukcesie zapisu; bottom bar Anuluj/Zapisz.
Teksty: ✅ Etykiety ról po polsku (Mieszkaniec/Konserwator/Zarządca); email read-only.
Spójność: ✅ FilterChip, OutlinedTextField — spójne z CreateUserDialog.
Krytyczne problemy: brak retry w Error state.

[ApartmentBalancesScreen] — rola: ZARZĄDCA
Przyciski/akcje: ✅ Filtry + „Zastosuj filtry”; sort toggle; przycisk PDF. ⚠️ Brak pola propertyId w UI (backend wspiera filtr po nieruchomości). PDF otwierany przez Intent z hardkodowanym URL `https://blokur.pl` — token JWT nie jest dołączany → prawdopodobna awaria pobierania w produkcji/dev.
Stany UI: ⚠️ Loading OK; Error/Empty bez retry; brak stanu ładowania przy generowaniu PDF.
Nawigacja: ✅ TopAppBar z back.
Teksty: ⚠️ Kwoty: `650.00 PLN` (kropka, skrót PLN); daty wpłat surowe z API (ISO); etykieta „Min. zaległość (PLN)”.
Spójność: ⚠️ Niespójne formatowanie walut względem wymagań audytu.
Krytyczne problemy: ❌ Hardkodowany URL API w `ApartmentBalancesScreen.kt` (linia 61) — PDF i środowisko dev/staging mogą nie działać; brak JWT przy otwarciu PDF w przeglądarce.

[AddTransactionDialog] — rola: ZARZĄDCA (FinancialLedgerScreen)
Przyciski/akcje: ✅ Typ operacji (chip), kwota, opis, data; confirm disabled gdy !isValid || isSubmitting; loader w przycisku.
Stany UI: ✅ isSubmitting blokuje dismiss i pola.
Nawigacja: ✅ AlertDialog.
Teksty: ⚠️ Data w formacie ISO (RRRR-MM-DD, placeholder „2026-05-03”); brak DatePicker.
Spójność: ✅ Spójny z innymi dialogami finansowymi.
Krytyczne problemy: brak

---
## FAZA 2 — Weryfikacja flow per rola

Ocena kroków: ✅ DOSTĘPNY | ⚠️ CZĘŚCIOWO | ❌ NIEDOSTĘPNY

### MIESZKANIEC

| Krok | Status | Uwagi |
|------|--------|-------|
| Login | ✅ | LoginScreen — formularz kompletny, stany OK |
| Lista zgłoszeń (własne) | ✅ | TicketsScreen — filtrowanie po roli w backendzie |
| Nowe zgłoszenie | ⚠️ | CreateTicketScreen — formularz działa, brak pickera zdjęć (PhotoPlaceholder statyczny) |
| Szczegóły zgłoszenia | ✅ | TicketDetailsScreen — status, komentarze, zdjęcia |
| Dodaj komentarz | ✅ | TicketCommentsSection |
| Finanse (hub) | ⚠️ | FinancesScreen — brak retry w Error; kwoty nie sformatowane po PL |
| Dokumenty + pobierz PDF | ✅ | DocumentsScreen — Intent ACTION_VIEW |
| Uchwały (lista) | ⚠️ | ResolutionsListScreen — brak retry w Error; daty ISO |
| Szczegóły uchwały + głos | ✅ | ResolutionDetailScreen — disabled przy wysyłaniu |
| Ogłoszenia + załącznik PDF | ✅ | AnnouncementsScreen — download attachment |
| Profil (rola + email) | ⚠️ | ProfileScreen — email z TokenStorage (może być pusty „Brak zapisanego…”); imię zawsze „—” (brak API profilu) |

### ZARZĄDCA

| Krok | Status | Uwagi |
|------|--------|-------|
| Login | ✅ | Jak wyżej |
| Lista zgłoszeń + filtry | ✅ | TicketsScreen + TicketFilterPanel — filtry rozszerzone dla Z |
| Szczegóły → Przypisz / Odrzuć / Zamknij | ✅ | FAB + sheets; resume dialog dla WSTRZYMANO |
| WSTRZYMANO → Wznów | ✅ | AlertDialog + akcja resume |
| Protokół PDF po zamknięciu | ✅ | FAB PDF przy statusie ZAMKNIETE |
| Użytkownicy → Lista → Utwórz → Edytuj → Dezaktywuj | ⚠️ | UsersScreen + EditUserScreen; dezaktywacja z dialogiem ale bez progress; brak retry w Error listy |
| Nieruchomości → CRUD drzewa → Liczniki → Dezaktywuj licznik | ⚠️ | PropertyTreeScreen OK; MeterListScreen — FAB bez kontroli roli; deaktywacja licznika bez progress po confirm |
| Finanse → Salda → Import CSV → Kartoteka lokalu | ❌ | **Brak zakładki Finanse w bottom nav zarządcy** (`zarzadcaNavItems`); brak linku z Profilu; ekrany istnieją ale nieosiągalne z UI |
| Ogłoszenia → CRUD | ❌ | **Brak zakładki Ogłoszenia dla zarządcy**; ekrany Create/Edit istnieją, brak nawigacji z głównego UI |
| Uchwały → Utwórz → Raport PDF | ✅ | ResolutionsListScreen FAB + ResolutionDetailScreen raport |
| Kategorie SLA | ✅ | CategoriesScreen z Profilu |
| Dystrybucja dokumentów | ✅ | DocumentDistributionScreen z Profilu |
| Przeglądy → CRUD | ⚠️ | InspectionsListScreen — brak confirm przy delete; isManager z VM (OK w nav) |
| Profil → Powiadomienia PUSH | ✅ | NotificationsScreen — toggle z API |
| Logo wspólnoty | ✅ | CommunityLogoScreen |
| Kartoteka lokalu z drzewa nieruchomości | ❌ | `FinancesRoutes.Ledger(apartmentId)` zdefiniowane, ale PropertyDetailPanel linkuje tylko do liczników — brak nawigacji do kartoteki |

### KONSERWATOR

| Krok | Status | Uwagi |
|------|--------|-------|
| Login | ✅ | |
| Lista zgłoszeń (przypisane) | ✅ | TicketsScreen |
| Szczegóły → Start pracy | ✅ | ConservatorActionSheet START |
| Zdjęcie BEFORE / AFTER | ⚠️ | Tylko upload AFTER (`canUploadAfter`); brak UI upload BEFORE; brak zdjęć przy tworzeniu zgłoszenia przez mieszkańca |
| Galeria zdjęć | ⚠️ | Miniatury wyświetlane, **brak pełnoekranowego podglądu** (kliknięcie nie działa) |
| Dodaj komentarz | ✅ | TicketCommentsSection |
| Zakończ pracę | ✅ | ConservatorActionSheet FINISH |
| Profil (rola + email) | ⚠️ | Jak u MIESZKAŃCA — brak imienia z API |

---
## FAZA 3 — Podsumowanie problemów UX

### Blokery UX

| # | Ekran / flow | Plik | Opis | Propozycja naprawy |
|---|--------------|------|------|-------------------|
| 1 | ZARZĄDCA: Finanse | `main/utils/Data.kt`, `ProfileContent.kt` | Zarządca nie ma dostępu do FinancesScreen (salda, import CSV, kartoteka) — brak w bottom nav i profilu | Dodać link „Finanse” w profilu zarządcy lub 6. zakładkę / menu overflow |
| 2 | ZARZĄDCA: Ogłoszenia | `main/utils/Data.kt` | Brak nawigacji do AnnouncementsScreen dla roli ZARZĄDCA — CRUD ogłoszeń niemożliwy z UI | Dodać pozycję w profilu lub skrót z listy zgłoszeń / osobna zakładka |
| 3 | ZARZĄDCA: Kartoteka z drzewa | `properties/contents/PropertyDetailPanel.kt`, `FinancesNavigation.kt` | Brak `navigate(FinancesRoutes.Ledger(apartmentId))` przy wyborze lokalu | Dodać przycisk „Kartoteka finansowa” w panelu lokalu |
| 4 | PDF sald lokali | `finances/screens/ApartmentBalancesScreen.kt` | Hardkodowany `API_BASE_URL = "https://blokur.pl"` + otwarcie URL bez JWT | Użyć `PdfApiService.getBalancesPdf` / BuildConfig.BACKEND_URL z autoryzacją |
| 5 | Galeria zdjęć zgłoszeń | `tickets/components/TicketImageThumbnail.kt`, `TicketImagesSection.kt` | Brak pełnoekranowego podglądu — konserwator nie może dokładnie obejrzeć dokumentacji | Dodać FullScreenImageDialog / HorizontalPager po kliknięciu miniatury |
| 6 | Retry po błędzie sieci (wiele ekranów) | `TicketsScreen.kt`, `ResolutionsListScreen.kt`, `UsersScreen.kt`, itd. | Error state = EmptyState bez „Spróbuj ponownie” — użytkownik utknięty po błędzie fetch | Dodać CTA wywołujące odpowiedni `load()` / `refresh()` we wszystkich listach |

### Poważne problemy UX

| # | Ekran | Plik | Opis | Propozycja naprawy |
|---|-------|------|------|-------------------|
| 1 | Finanse / transakcje | `TransactionsScreen.kt`, `FinancialLedgerScreen.kt`, `ApartmentBalancesScreen.kt` | Kwoty jako `650.00 PLN` zamiast `650,00 zł` | Wprowadzić `NumberFormat` locale pl_PL + „zł” |
| 2 | Daty w całej aplikacji | Wiele plików tickets/finances/resolutions | ISO (`2026-03-10`, `take(10)`) zamiast `dd.MM.yyyy` | Centralny formatter dat; użyć w listach i szczegółach |
| 3 | Destructive bez confirm | `SampleAnnoucementsContent.kt`, `MeterDetailScreen.kt`, `InspectionsListScreen.kt` | Usuwanie ogłoszenia / odczytu / przeglądu bez AlertDialog | Dodać dialog potwierdzenia + loader podczas operacji |
| 4 | MeterListScreen FAB | `meters/screens/MeterListScreen.kt` | FAB „Dodaj licznik” widoczny bez sprawdzenia roli | Ukryć FAB gdy użytkownik ≠ ZARZĄDCA |
| 5 | Dialogi admin — brak feedbacku | `CategoriesScreen.kt`, `UsersScreen.kt`, sheets ticketów | Dialog zamyka się przed zakończeniem API | Trzymać dialog otwarty z progress do odpowiedzi; snackbar sukcesu/błędu |
| 6 | CreateTicket — brak zdjęć | `CreateTicketScreen.kt` / formularz | PhotoPlaceholder bez pickera | Dodać ActivityResultLauncher + upload AFTER create lub multipart w POST |
| 7 | ApartmentBalances — filtry | `ApartmentBalancesScreen.kt` | Brak UI dla `propertyId` mimo wsparcia backendu | Dropdown wspólnot z GET `/api/properties` |
| 8 | Profile Error state | `ProfileViewModel.kt`, `ProfileContent.kt` | Przy błędzie rola = „Błąd”, brak dedykowanego stanu Error/retry | Użyć `ProfileState.Error` + EmptyState z retry |
| 9 | ResidentMainScreen | `ResidentMainScreen.kt` | Globalny błąd init bez overlay/retry | LoadingIndicator / EmptyState z retry gdy `ResidentMainState.Error` |
| 10 | Formularze dat | `CreateResolutionDialog.kt`, `CreateMeterDialog.kt`, `DocumentDistributionScreen.kt`, `AddTransactionDialog.kt` | Ręczne wpisywanie ISO bez DatePicker | Ujednolicić z `TicketFilterPanel` (DatePickerDialog) |

### Drobne problemy UX

| # | Ekran | Plik | Opis | Propozycja naprawy |
|---|-------|------|------|-------------------|
| 1 | Login / auth | `LoginScreen.kt` | Marka „Blokur” vs „BlokUR” | Ujednolicić nazwę produktu |
| 2 | ForgotPassword | `ForgotPasswordScreen.kt` | `ShowSnackbar` event ignorowany | Podłączyć snackbarHostState lub usunąć dead code |
| 3 | Categories SLA badge | `CategoriesScreen.kt` | „24h SLA” — ang. skrót | „24 godz. SLA” lub „SLA: 24 h” |
| 4 | Terminologia | `UsersScreen.kt`, `CategoriesScreen.kt` | „Deaktywuj” vs „Dezaktywuj” | Ujednolicić copy |
| 5 | Profil | `ProfileContent.kt` | Imię zawsze „—”, email opcjonalny z DataStore | Docelowo GET profilu; tymczasowo wyświetlać email z loginu konsekwentnie |
| 6 | Announcements | `SampleAnnoucementsContent.kt` | Emoji w etykiecie pobierania | Opcjonalnie zamienić na ikonę Material |
| 7 | InspectionsListScreen | `InspectionsListScreen.kt` | Domyślny param `isManager = true` w sygnaturze | Zmienić default na `false` |
| 8 | PrimaryButton auth | `LoginForm.kt` | Loading tylko tekstem, bez spinnera w przycisku | Dodać CircularProgressIndicator jak w innych ekranach |
| 9 | EditUserScreen | `EditUserScreen.kt` | Nieużywany import `DeleteForever` | Usunąć martwy import (porządek kodu) |
| 10 | Surowe błędy API | Wiele ViewModeli | `s.message` / `it.message` może zawierać techniczne treści | Mapować na polskie komunikaty użytkownika |

---
## Status audytu UX

| Faza | Status |
|------|--------|
| FAZA 0 — Plan | ✅ Zakończona |
| FAZA 1 — Ekrany (30/30) + dialogi/sheets (11/11) | ✅ Zakończona (batch 1–8) |
| FAZA 2 — Flow per rola | ✅ Zakończona |
| FAZA 3 — Podsumowanie | ✅ Zakończona |

**Uwaga:** Raport dotyczy wyłącznie warstwy UI/UX na podstawie analizy kodu Composable — bez uruchamiania aplikacji. Inventory `02_frontend_inventory.md` wskazuje na hardkodowany profil; aktualny `ProfileViewModel` pobiera rolę z AuthService i email z TokenStorage (poprawione względem starszej wersji).

**Koniec raportu UX — 2026-06-05**

