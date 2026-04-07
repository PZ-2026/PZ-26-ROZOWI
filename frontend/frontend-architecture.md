# Frontend — Architektura

Projekt frontendowy oparty jest o architekturę **Clean Architecture** z podziałem na cztery moduły Gradle:

```
frontend/
├── domain/            ← czysta logika biznesowa (pure Kotlin, zero zależności Android)
├── infrastructure/    ← implementacje interfejsów domeny (API, bazy danych, mocki)
├── presentation/      ← UI – Jetpack Compose (ekrany, komponenty, viewmodele)
└── app/               ← Android bootstrapper (Application, Activity, nawigacja globalna)
```

Zależności modułów:

```
app  ──►  presentation  ──►  domain  ◄──  infrastructure
 │                                              │
 └──────────────►  domain  ◄────────────────────┘
 └──────────────►  infrastructure
```

> **Uwaga:** moduł `domain` jest biblioteką **pure-JVM** (`java-library`) – nie posiada żadnych zależności od Androida.  
> Jedyną zależnością jest `javax.inject:javax.inject:1`, która dostarcza anotację `@Inject` do Use Case'ów.

---

## Warstwa domeny (`domain`)

Warstwa domeny realizowana jest przez moduł **`domain`** i stanowi rdzeń aplikacji. Nie zależy od żadnego frameworka Androidowego — jest to **czysty Kotlin** (plugin `java-library`). Dzięki temu logika biznesowa może być testowana jednostkowo bez emulatora.

Pakiet bazowy: `pl.edu.ur.blokur.domain`

Struktura:

```
domain/
└── model/           ← encje domenowe (data class, enum)
└── repository/      ← interfejsy repozytoriów
└── services/        ← interfejsy usług zewnętrznych
└── usecase/         ← przypadki użycia (logika biznesowa)
└── DomainExceptions.kt  ← wyjątki domenowe
```

---

### Model

Modele to **niezmienne klasy danych** (`data class`) oraz typy wyliczeniowe (`enum class`), które opisują obiekty z dziedziny problemu. Nie zawierają żadnych adnotacji frameworkowych (np. nie `@Entity`, nie `@Serializable`).

**Przykład — `TicketEntities.kt`:**
```kotlin
enum class TicketStatus {
    NOWE, ZAPLANOWANO, W_REALIZACJI, WSTRZYMANO, ZAKONCZONE, ZAMKNIETE, ODRZUCONE
}

data class AppUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
) {
    val fullName: String get() = "$firstName $lastName"
}

data class Ticket(
    val id: Int,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val category: TicketCategory,
    val author: AppUser,
    val assignedTo: AppUser?,
    val apartment: Apartment?,
    val staircase: Staircase?,
    val building: Building?,
    val isDeleted: Boolean,
    val createdAt: String,
    val closedAt: String?,
    val images: List<String>,
    val history: List<TicketHistory>
)
```

**Przykład — `FinancesEntities.kt`:**
```kotlin
enum class TransactionType { WPLATA, NALICZENIE, KOREKTA }
enum class BalanceStatus  { NADPLATA, ZALEGLOSC, WYZEROWANY }

data class ApartmentBalance(
    val apartmentId: Int,
    val apartmentNumber: String,
    val currentBalance: Double,
    val currency: String,
    val lastTransactionDate: String?,
    val status: BalanceStatus,
    val totalPaid: Double,
    val totalCharged: Double
)
```

---

### Repository (Interface)

Interfejsy repozytoriów definiują **kontrakt dostępu do danych** bez wskazywania źródła (sieć, baza, pamięć). Wszystkie metody są `suspend`, co umożliwia asynchroniczne wywoływanie z korutyn.

**Przykład — `TicketRepository.kt`:**
```kotlin
interface TicketRepository {
    suspend fun getTickets(): List<Ticket>
    suspend fun getTicketById(id: Int): Ticket?
    suspend fun getAvailableConservators(): List<AppUser>
    suspend fun getCategories(): List<String>
    suspend fun getCurrentUserRole(): String
}
```

**Przykład — `FinancesRepository.kt`:**
```kotlin
interface FinancesRepository {
    suspend fun getBalance(): ApartmentBalance
    suspend fun getTransactions(): List<FinancialTransaction>
    suspend fun getDocuments(): List<FinancialDocument>
}
```

> **Zasada:** Warstwa domenowa deklaruje interfejs, a warstwa infrastruktury go implementuje. Dzięki temu domena nie wie nic o szczegółach implementacji (REST API, SQLite, mock itd.).

---

### Services (Interface)

Interfejsy serwisów opisują **zależności na usługi systemowe** (logowanie, komunikacja sieciowa, preferencje itp.), których implementacja leży poza domeną.

**Przykład — `LoggingService.kt`:**
```kotlin
interface LoggingService {
    suspend fun LogMessage(message: String)
}
```

> **Różnica Services vs Repository:**  
> - **Repository** = dostęp do danych domenowych (CRUD encji).  
> - **Service** = usługi infrastrukturalne, które nie dotyczą bezpośrednio encji domenowych (logowanie, powiadomienia, konfiguracja itd.).

---

### UseCase

Przypadki użycia (Use Cases) stanowią **implicite warstwę aplikacji** — każdy UseCase enkapsuluje **jeden scenariusz biznesowy**. Są wstrzykiwane przez Hilt za pomocą `@Inject constructor`.

Konwencje:
- Każdy UseCase posiada metodę `suspend operator fun invoke(...)`, co pozwala wywoływać go jak funkcję: `loginUseCase()`.
- Use Case'y nie implementane są opatrzone wyjątkiem `UseCaseNotImplementedException` (wzorzec stub/placeholder).
- Nazewnictwo: `<Czasownik><Rzeczownik>UseCase`, np. `LoginUseCase`, `CreateServiceTicketUseCase`.
- Pliki grupowane wg roli użytkownika: `GuestUseCases.kt`, `ResidentUseCases.kt`, `ManagerUseCases.kt`, `ConservatorUseCases.kt`, `CommonUseCases.kt`.

**Przykład — UseCase z implementacją logiki (`CommonUseCases.kt`):**
```kotlin
class TestAUseCase @Inject constructor(
    private val loggingService: LoggingService
) {
    suspend operator fun invoke(msg: String) {
        loggingService.LogMessage(msg)
    }
}
```

**Przykład — UseCase jako stub, który jeszcze nie jest zaimplementowany (`GuestUseCases.kt`):**
```kotlin
class LoginUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}
```

**Przykład — Use Casy grupowane wg roli (`ResidentUseCases.kt`):**
```kotlin
class BrowseTicketsUseCase @Inject constructor() {
    suspend operator fun invoke() { ... }
}

class CreateServiceTicketUseCase @Inject constructor() {
    suspend operator fun invoke() { ... }
}

class BrowseFinancialRecordUseCase @Inject constructor() {
    suspend operator fun invoke() { ... }
}
```

---

### Exceptions

Wyjątki domenowe sygnalizują naruszenie reguł biznesowych lub brak implementacji.

**Przykład — `DomainExceptions.kt`:**
```kotlin
class UseCaseNotImplementedException(className: String?) : 
    Exception(className + " is not implemented")
```

---

---

## Warstwa infrastruktury (`infrastructure`)

Moduł **`infrastructure`** dostarcza **konkretne implementacje** interfejsów zdefiniowanych w warstwie `domain`. Jest to moduł Android Library (`android.library`), ponieważ może korzystać z Android SDK (sieć, baza danych, preferencje).

Pakiet bazowy: `pl.edu.ur.blokur.infrastructure`

Zależność: `implementation(project(":domain"))`

Struktura:

```
infrastructure/
├── logging/
│   ├── LoggingServiceImpl.kt    ← impl. LoggingService
│   └── DI.kt                   ← moduł Hilt wiążący interfejs z implementacją
└── mock/
    ├── MockTicketRepository.kt       ← impl. TicketRepository (dane testowe)
    ├── MockFinancesRepository.kt     ← impl. FinancesRepository (dane testowe)
    └── MockRepositoriesModule.kt     ← moduł Hilt wiążący repozytoria
```

Kluczowe zasady:
- Implementacje są oznaczone jako `internal` — nie wyciekają poza moduł. Jedynym eksportowanym API jest binding Hilt.
- Każda grupa implementacji posiada własny **moduł Hilt** (`@Module @InstallIn(SingletonComponent::class)`).

### Implementacja Service

**Przykład — `LoggingServiceImpl.kt`:**
```kotlin
internal class LoggingServiceImpl @Inject constructor() : LoggingService {
    override suspend fun LogMessage(message: String) {
        println(message)
    }
}
```

**Binding Hilt — `DI.kt`:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DependencyInjector {

    @Binds
    abstract fun bindLoggingService(
        impl: LoggingServiceImpl
    ): LoggingService
}
```

### Implementacja Repository (Mock)

Obecnie repozytoria używają danych mockowych, co umożliwia rozwój UI bez działającego backendu. W przyszłości zostaną zastąpione implementacjami Retrofit + Room.

**Przykład — `MockTicketRepository.kt` (fragment):**
```kotlin
internal class MockTicketRepository @Inject constructor() : TicketRepository {

    private val resident1 = AppUser(101, "Janusz", "Kowalski", "janusz@example.com", "MIESZKANIEC")
    private val conservator1 = AppUser(201, "Ryszard", "Klucz", "ryszard.k@blokur.pl", "KONSERWATOR")
    private val admin = AppUser(301, "Anna", "Zarządca", "anna.z@blokur.pl", "ADMINISTRATOR")

    private val tickets = listOf(
        Ticket(
            id = 1, ticketNumber = "ZGL-2026-0001",
            title = "Brak ciepłej wody",
            description = "Od wczorajszego popołudnia w pionie nie ma ciepłej wody.",
            status = TicketStatus.NOWE,
            category = catHydraulika, author = resident1, assignedTo = null,
            // ...
        ),
        // ... więcej danych testowych
    )

    override suspend fun getTickets(): List<Ticket> = tickets
    override suspend fun getTicketById(id: Int): Ticket? = tickets.find { it.id == id }
    override suspend fun getAvailableConservators(): List<AppUser> = listOf(conservator1, conservator2)
    override suspend fun getCategories(): List<String> = listOf("Hydraulika", "Elektryka", "Domofony", ...)
    override suspend fun getCurrentUserRole(): String = admin.role
}
```

**Binding Hilt — `MockRepositoriesModule.kt`:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MockRepositoriesModule {

    @Binds @Singleton
    abstract fun bindTicketRepository(impl: MockTicketRepository): TicketRepository

    @Binds @Singleton
    abstract fun bindFinancesRepository(impl: MockFinancesRepository): FinancesRepository
}
```

> **Jak podmienić mock na prawdziwą implementację?** Wystarczy utworzyć nowy moduł Hilt (np. w pakiecie `infrastructure/api/`) z klasą `RetrofitTicketRepository` implementującą `TicketRepository` i podmienić binding. Żaden inny moduł nie wymaga zmian.

---

---

## Warstwa Prezentacji (`presentation`)

Moduł **`presentation`** odpowiada za cały interfejs użytkownika w **Jetpack Compose + Material 3**.  
Jest modułem Android Library z włączonym Compose.

Pakiet bazowy: `pl.edu.ur.blokur.presentation`

Zależność: `implementation(project(":domain"))`

Struktura jest zorganizowana **per-feature** (funkcjonalność). Każda funkcjonalność zawiera powtarzalny zestaw podpakietów:

```
presentation/
├── common/                  ← współdzielone elementy (theme, komponenty bazowe)
│   ├── AppRoute.kt          ← marker-interface dla nawigacji
│   ├── theme/               ← kolory, typografia, kształty, motyw
│   └── component/           ← przyciski, karty, badge'y, dialogi, ...
│
├── auth/                    ← funkcjonalność logowania
│   ├── Auth.kt              ← deklaracja tras + NavGraphBuilder extension
│   ├── screen/              ← ekrany (Screen)
│   ├── content/             ← bezstanowe kompozycje treści (Content)
│   ├── viewmodel/           ← ViewModele (stan + logika)
│   └── util/                ← stany UI, eventy, mappery
│
├── tickets/                 ← funkcjonalność zgłoszeń serwisowych
│   ├── Tickets.kt
│   ├── screen/
│   ├── content/
│   ├── component/           ← komponenty specyficzne dla tickets
│   ├── viewmodel/
│   └── util/
│
├── finances/                ← funkcjonalność finansów
├── announcements/           ← ogłoszenia
├── profile/                 ← profil użytkownika
└── resident/                ← główny ekran mieszkańca + bottom navigation
```

---

### Theme (`common/theme/`)

Moduł motywu definiuje design system aplikacji:

| Plik | Odpowiedzialność |
|------|-------------------|
| `Color.kt` | Paleta kolorów (primary, semantic, gradient, tło) |
| `Type.kt` | Typografia (Material 3 `Typography`) |
| `Shape.kt` | Kształty komponentów (`Shapes`) |
| `Theme.kt` | `PresentationTheme` — composable opakowujący `MaterialTheme` |
| `Preview.kt` | Helper `PreviewTheme` do preview w Android Studio |

**Przykład — `Color.kt` (fragment):**
```kotlin
val PrimaryBlue      = Color(0xFF1D4ED8)
val PrimaryContainer = Color(0xFFDCEAFE)
val SecondaryPurple  = Color(0xFF7C3AED)
val SuccessGreen     = Color(0xFF059669)
val WarningOrange    = Color(0xFFD97706)
val ErrorRed         = Color(0xFFDC2626)
val InfoBlue         = Color(0xFF2563EB)
```

**Przykład — `Theme.kt`:**
```kotlin
@Composable
fun PresentationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

---

### Component (`common/component/` oraz `<feature>/component/`)

Komponent to **pojedynczy, atomowy element UI** (przycisk, karta, badge, pole tekstowe itp.).  
Komponenty **nie posiadają własnego stanu** — wszelkie dane przyjmują przez parametry. Dzięki temu wyglądają identycznie w każdym miejscu aplikacji.

Komponenty globalne znajdują się w `common/component/`, a specyficzne dla danej funkcjonalności — w `<feature>/component/`.

**Wspólne komponenty (`common/component/`):**

| Plik | Komponenty |
|------|-----------|
| `Buttons.kt` | `PrimaryButton`, `SecondaryButton`, `FloatingActionButton` |
| `Cards.kt` | `NormalCard`, `HighlightCard` |
| `Badges.kt` | `TagBadge`, `StatusBadge` |
| `TextField.kt` | Pola tekstowe |
| `TopBar.kt` | Górny pasek nawigacyjny |
| `LoadingIndicator.kt` | Wskaźnik ładowania |
| `EmptyState.kt` | Widok pustego stanu |
| `AlertDialog.kt` | Dialog potwierdzenia |
| `Snackbar.kt` | Powiadomienie Snackbar |

**Przykład — `Badges.kt`:**
```kotlin
@Composable
fun TagBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun StatusBadge(text: String, dotColor: Color) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), ...) {
            Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
            Text(text = text, ...)
        }
    }
}
```

**Przykład — komponent specyficzny (`tickets/component/TicketListItem.kt`):**
```kotlin
@Composable
fun TicketListItem(
    title: String,
    date: String,
    categoryName: String,
    statusText: String,
    statusColorHex: Long,
    onClick: () -> Unit
) {
    // Komponent przyjmuje wyłącznie proste typy (String, Long, lambda)
    // Nie ma dostępu do ViewModel ani modelu domenowego
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        // ... ikona, tytuł, data, badge'y statusu i kategorii
    }
}
```

> **Zasada:** Komponenty nie przyjmują modeli domenowych — jedynie prymitywy (`String`, `Int`, `Color`, lambdy). Konwersja z domeny na dane prezentacyjne odbywa się w `Content` lub `util/`.

---

### Content (`<feature>/content/`)

Content to **bezstanowa kompozycja** komponentów, która tworzy spójny fragment UI.  
Content **przyjmuje stan jako argument** (zwykle listę danych + lambdy obsługi zdarzeń), ale sam go nie zarządza.

Różnica między Content a Screen:
- **Content** — „co wyświetlić?" — czysta prezentacja danych, brak Scaffold, brak ViewModel.
- **Screen** — „jak wyświetlić?" — Scaffold, obsługa stanów Loading/Error/Success, podpięcie do ViewModel.

**Przykład — `TicketListContent.kt`:**
```kotlin
@Composable
fun TicketListContent(
    tickets: List<Ticket>,            // ← stan przekazany z Screen
    onTicketClicked: (Int) -> Unit     // ← callback do ViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tickets.forEach { ticket ->
            val presentation = ticket.status.toPresentation()  // mapper z util/
            TicketListItem(
                title = ticket.title,
                date = "${ticket.createdAt.take(10)} • ...",
                categoryName = ticket.category.name,
                statusText = presentation.label,
                statusColorHex = presentation.color.value.toLong(),
                onClick = { onTicketClicked(ticket.id) }
            )
        }
    }
}
```

---

### Screen (`<feature>/screen/`)

Screen to **composable najwyższego poziomu** dla danej trasy nawigacji.  
Odpowiada za:
1. Podpięcie do `ViewModel` (odbieranie `state`, nasłuchiwanie `events`).
2. Wyświetlenie struktury ekranu (`Scaffold`, `TopBar`, FAB).
3. Obsługę stanów: Loading → Error → Success.
4. Delegowanie rysowania zawartości do `Content`.

**Przykład — `TicketsScreen.kt`:**
```kotlin
@Composable
fun TicketsScreen(
    viewModel: TicketsViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Obsługa jednorazowych eventów nawigacyjnych
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TicketsScreenEvent.NavigateToDetails -> onNavigateToDetails(event.ticketId)
                is TicketsScreenEvent.NavigateToCreate  -> onNavigateToCreate()
            }
        }
    }

    Scaffold(
        topBar = { TopBar(title = "Zgłoszenia") },
        floatingActionButton = {
            if (showFab) FloatingActionButton(text = "+", onClick = viewModel::onCreateTicketClicked)
        }
    ) { innerPadding ->
        when (val s = state) {
            is TicketsListState.Loading -> LoadingIndicator()
            is TicketsListState.Error   -> EmptyState(title = "Błąd", description = s.message)
            is TicketsListState.Success -> {
                // Deleguje wyświetlanie listy do Content
                TicketListContent(
                    tickets = s.tickets,
                    onTicketClicked = viewModel::onTicketClicked
                )
            }
        }
    }
}
```

---

### Util (`<feature>/util/`)

Pakiet `util/` zawiera dwa rodzaje plików:

1. **States & Events** — `sealed interface` definiujące stany UI ekranu i jednorazowe zdarzenia (nawigacja, snackbar).
2. **Mappery** — funkcje konwertujące modele domenowe na dane prezentacyjne.

**Przykład — `TicketsStates.kt`:**
```kotlin
// Stany ekranu listy zgłoszeń
sealed interface TicketsListState {
    data object Loading : TicketsListState
    data class Error(val message: String) : TicketsListState
    data class Success(val tickets: List<Ticket>, val currentUserRole: String) : TicketsListState
}

// Jednorazowe zdarzenia
sealed interface TicketsScreenEvent {
    data class NavigateToDetails(val ticketId: Int) : TicketsScreenEvent
    data object NavigateToCreate : TicketsScreenEvent
}

// Stan formularza tworzenia zgłoszenia
data class CreateTicketFormState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: String = "",
    val isCategoryExpanded: Boolean = false
)
```

**Przykład — `TicketUiMappers.kt`:**
```kotlin
data class StatusPresentation(val label: String, val color: Color)

fun TicketStatus.toPresentation(): StatusPresentation = when (this) {
    TicketStatus.NOWE         -> StatusPresentation("Nowe", InfoBlue)
    TicketStatus.ZAPLANOWANO  -> StatusPresentation("Zaplanowano", InfoBlue)
    TicketStatus.W_REALIZACJI -> StatusPresentation("W realizacji", WarningOrange)
    TicketStatus.WSTRZYMANO   -> StatusPresentation("Wstrzymano", WarningOrange)
    TicketStatus.ZAKONCZONE   -> StatusPresentation("Zakończone", SuccessGreen)
    TicketStatus.ZAMKNIETE    -> StatusPresentation("Zamknięte", SuccessGreen)
    TicketStatus.ODRZUCONE    -> StatusPresentation("Odrzucone", ErrorRed)
}
```

---

### ViewModel (`<feature>/viewmodel/`)

ViewModel zarządza stanem UI ekranu. Używa wzorca:
- `StateFlow<State>` — reaktywny stan do obserwacji przez Screen.
- `Channel<Event>` / `Flow<Event>` — jednorazowe zdarzenia (nawigacja, snackbar).
- Wstrzykiwanie zależności (`@HiltViewModel`, `@Inject constructor`).

ViewModel **nie wie nic o Compose** — operuje na interfejsach repozytoriów z domeny.

**Przykład — `TicketsViewModel.kt`:**
```kotlin
@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val ticketRepository: TicketRepository   // ← interfejs domenowy
) : ViewModel() {

    private val _state = MutableStateFlow<TicketsListState>(TicketsListState.Loading)
    val state: StateFlow<TicketsListState> = _state.asStateFlow()

    private val _events = Channel<TicketsScreenEvent>()
    val events: Flow<TicketsScreenEvent> = _events.receiveAsFlow()

    init { loadTickets() }

    private fun loadTickets() {
        viewModelScope.launch {
            runCatching {
                val tickets = ticketRepository.getTickets()
                val role = ticketRepository.getCurrentUserRole()
                tickets to role
            }.onSuccess { (tickets, role) ->
                _state.value = TicketsListState.Success(tickets, currentUserRole = role)
            }.onFailure { e ->
                _state.value = TicketsListState.Error(e.message ?: "Błąd ładowania zgłoszeń")
            }
        }
    }

    fun onTicketClicked(ticketId: Int) {
        viewModelScope.launch { _events.send(TicketsScreenEvent.NavigateToDetails(ticketId)) }
    }

    fun onCreateTicketClicked() {
        viewModelScope.launch { _events.send(TicketsScreenEvent.NavigateToCreate) }
    }
}
```

---

### Nawigacja per-feature (`<feature>/<Feature>.kt`)

Każda funkcjonalność posiada plik główny (np. `Tickets.kt`, `Auth.kt`, `Finances.kt`) zawierający:
1. **`sealed interface <Feature>Routes : AppRoute`** — definicje tras nawigacyjnych serializowalnych przez `kotlinx.serialization`.
2. **`fun NavGraphBuilder.<feature>Graph(navController)`** — extension function rejestrującą composable'e w grafie nawigacji.

**Przykład — `Tickets.kt`:**
```kotlin
sealed interface TicketRoutes : AppRoute {
    @Serializable data object List : TicketRoutes
    @Serializable data class Details(val ticketId: Int) : TicketRoutes
    @Serializable data object Create : TicketRoutes
}

fun NavGraphBuilder.ticketsGraph(navController: NavController) {
    composable<TicketRoutes.List> {
        val viewModel: TicketsViewModel = hiltViewModel()
        TicketsScreen(
            viewModel = viewModel,
            onNavigateToDetails = { ticketId -> navController.navigate(TicketRoutes.Details(ticketId)) },
            onNavigateToCreate = { navController.navigate(TicketRoutes.Create) }
        )
    }

    composable<TicketRoutes.Details> {
        val viewModel: TicketDetailsViewModel = hiltViewModel()
        TicketDetailsScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
    }

    composable<TicketRoutes.Create> {
        val viewModel: CreateTicketViewModel = hiltViewModel()
        CreateTicketScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
    }
}
```

**Przykład — `Auth.kt`:**
```kotlin
sealed interface AuthRoutes : AppRoute {
    @Serializable data object Login : AuthRoutes
}

fun NavGraphBuilder.authGraph(navController: NavController, onLoginSuccess: () -> Unit) {
    composable<AuthRoutes.Login> {
        val viewModel: AuthViewModel = hiltViewModel()
        LoginScreen(viewModel = viewModel, onLoginSuccess = onLoginSuccess)
    }
}
```

> **`AppRoute`** to pusty interfejs-marker definiowany w `common/`:
> ```kotlin
> interface AppRoute
> ```
> Używany jako typ bazowy tras nawigacyjnych, co pozwala typowanemu API nawigacji (`startDestination: AppRoute`) zaakceptować dowolną trasę modułu.

---

---

## Android Bootstrapper (`app`)

Moduł **`app`** to punkt wejścia aplikacji Android. Nie zawiera logiki biznesowej ani UI — jedynie **konfigurację startową**.

Zależności: `implementation(project(":presentation"))`, `implementation(project(":domain"))`, `implementation(project(":infrastructure"))`

Zawiera trzy pliki:

### `AndroidApplication.kt`

Klasa `Application` z adnotacją `@HiltAndroidApp`, która uruchamia generowanie kodu Hilt (Dependency Injection).

```kotlin
@HiltAndroidApp
class AndroidApplication : Application()
```

### `MainActivity.kt`

Jedyne `Activity` w aplikacji (single-activity architecture). Inicjuje Compose i ustawia motyw.

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PresentationTheme {
                AppNavHost()
            }
        }
    }
}
```

### `AppNavHost.kt`

Globalny graf nawigacji, który **łączy wszystkie feature-grafy** w jedną hierarchię. Określa, która trasa jest startowa (`AuthRoutes.Login`) i definiuje powiązania między modułami.

```kotlin
@Composable
fun AppNavHost(
    appNavController: NavHostController = rememberNavController(),
    startDestination: AppRoute = AuthRoutes.Login
) {
    NavHost(navController = appNavController, startDestination = startDestination) {
        authGraph(
            navController = appNavController,
            onLoginSuccess = {
                appNavController.navigate(ResidentRoutes.Main) {
                    popUpTo(AuthRoutes.Login) { inclusive = true }
                }
            }
        )

        residentGraph(
            navController = appNavController,
            announcementsRoute = AnnouncementsRoutes.Main,
            financesRoute = FinancesRoutes.Main,
            profileRoute = ProfileRoutes.Main,
            ticketsRoute = TicketRoutes.List,
            nestedGraphs = { bottomNavController ->
                announcementsGraph(bottomNavController)
                financesGraph(bottomNavController)
                profileGraph(bottomNavController)
                ticketsGraph(bottomNavController)
            }
        )
    }
}
```

> Moduł `ResidentRoutes.Main` implementuje nawigację z **bottom navigation bar** — wewnątrz tego ekranu osadzony jest zagnieżdżony `NavHost` z osobnym `bottomNavController`, który przełącza między zakładkami (ogłoszenia, finanse, profil, zgłoszenia).

---

## Podsumowanie — przepływ danych

```
Użytkownik klika → Screen → ViewModel → Repository (interfejs) → Infrastructure (impl)
                                              ↓
                                         Domain Model
                                              ↓
ViewModel aktualizuje StateFlow → Screen reaguje → Content renderuje → Component rysuje UI
```

| Warstwa | Moduł | Zna | Nie zna |
|---------|-------|-----|---------|
| Domain | `domain` | Kotlin stdlib, javax.inject | Android, Compose, Hilt, Retrofit |
| Infrastructure | `infrastructure` | Domain, Android SDK, Hilt | Presentation, App |
| Presentation | `presentation` | Domain (modele, interfejsy), Compose, Hilt | Infrastructure, App |
| App | `app` | Presentation, Domain, Infrastructure | — (zna wszystko) |