package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serwis zgłoszeń — aktualnie mockowe dane (do przyszłej zamiany na Retrofit).
 */
@Singleton
class TicketService @Inject constructor() {

    private val resident1 = AppUserDto("Janusz", "Kowalski", "janusz@example.com", "MIESZKANIEC")
    private val resident2 = AppUserDto("Grażyna", "Nowak", "grazyna@example.com", "MIESZKANIEC")
    private val resident3 = AppUserDto("Mieczysław", "Wąs", "mietek@example.com", "MIESZKANIEC")
    private val conservator1 = AppUserDto("Ryszard", "Klucz", "ryszard.k@blokur.pl", "KONSERWATOR")
    private val conservator2 = AppUserDto("Zdzisław", "Kabel", "zdzislaw.k@blokur.pl", "KONSERWATOR")
    private val admin = AppUserDto("Anna", "Zarządca", "anna.z@blokur.pl", "ADMINISTRATOR")

    private val buildingKwiatowa = BuildingDto("Blok 12", "ul. Kwiatowa 12")
    private val buildingMickiewicza = BuildingDto("Wieżowiec 15", "al. Mickiewicza 15")
    private val staircaseA = StaircaseDto("A")
    private val staircaseB = StaircaseDto("B")
    private val apt45 = ApartmentDto("45")

    private val catHydraulika = TicketCategoryDto("Hydraulika")
    private val catElektryka = TicketCategoryDto("Elektryka")
    private val catDomofony = TicketCategoryDto("Domofony")
    private val catCzesciWspolne = TicketCategoryDto("Części wspólne")

    private val tickets = listOf(
        TicketDto(
            id = 1, ticketNumber = "ZGL-2026-0001",
            title = "Brak ciepłej wody",
            description = "Od wczorajszego popołudnia w pionie nie ma ciepłej wody. Proszę o pilną interwencję.",
            status = TicketStatus.NOWE,
            category = catHydraulika, author = resident1, assignedTo = null,
            apartment = apt45, staircase = staircaseB, building = buildingKwiatowa,
            isDeleted = false, createdAt = "2026-03-17T08:30:00Z", closedAt = null,
            images = listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=400"),
            history = listOf(
                TicketHistoryDto(TicketStatus.NOWE, resident1, "Zgłoszenie awarii - brak ciepłej wody.", "2026-03-17T08:30:00Z")
            )
        ),
        TicketDto(
            id = 2, ticketNumber = "ZGL-2026-0002",
            title = "Przepalona żarówka na półpiętrze",
            description = "Żarówka między parterem a pierwszym piętrem jest przepalona.",
            status = TicketStatus.ZAPLANOWANO,
            category = catElektryka, author = resident2, assignedTo = conservator2,
            apartment = null, staircase = staircaseA, building = buildingKwiatowa,
            isDeleted = false, createdAt = "2026-03-16T14:15:00Z", closedAt = null,
            images = emptyList(),
            history = listOf(
                TicketHistoryDto(TicketStatus.NOWE, resident2, "Żarówka przepalona.", "2026-03-16T14:15:00Z"),
                TicketHistoryDto(TicketStatus.ZAPLANOWANO, admin, "Zaplanowano wymianę na wtorek.", "2026-03-17T09:00:00Z")
            )
        ),
        TicketDto(
            id = 3, ticketNumber = "ZGL-2026-0003",
            title = "Zepsuty domofon — klatka B",
            description = "Domofon przestał działać po burzy. Nie można otworzyć drzwi z zewnątrz.",
            status = TicketStatus.W_REALIZACJI,
            category = catDomofony, author = resident3, assignedTo = conservator1,
            apartment = null, staircase = staircaseB, building = buildingMickiewicza,
            isDeleted = false, createdAt = "2026-03-10T11:00:00Z", closedAt = null,
            images = emptyList(),
            history = listOf(
                TicketHistoryDto(TicketStatus.NOWE, resident3, null, "2026-03-10T11:00:00Z"),
                TicketHistoryDto(TicketStatus.ZAPLANOWANO, admin, "Zlecono konserwatorowi.", "2026-03-11T08:00:00Z"),
                TicketHistoryDto(TicketStatus.W_REALIZACJI, conservator1, "Trwa diagnostyka panelu zewnętrznego.", "2026-03-14T10:30:00Z")
            )
        ),
        TicketDto(
            id = 4, ticketNumber = "ZGL-2026-0004",
            title = "Uszkodzona poręcz schodów",
            description = "Poręcz na 3 piętrze klatki A jest obluzowana i grozi wypadkiem.",
            status = TicketStatus.ZAMKNIETE,
            category = catCzesciWspolne, author = resident1, assignedTo = conservator2,
            apartment = null, staircase = staircaseA, building = buildingKwiatowa,
            isDeleted = false, createdAt = "2026-02-20T09:45:00Z", closedAt = "2026-03-01T12:00:00Z",
            images = emptyList(),
            history = listOf(
                TicketHistoryDto(TicketStatus.NOWE, resident1, null, "2026-02-20T09:45:00Z"),
                TicketHistoryDto(TicketStatus.W_REALIZACJI, conservator2, "Naprawiono i wzmocniono śruby.", "2026-02-28T14:00:00Z"),
                TicketHistoryDto(TicketStatus.ZAMKNIETE, admin, "Weryfikacja OK. Zamykam zgłoszenie.", "2026-03-01T12:00:00Z")
            )
        ),
        TicketDto(
            id = 5, ticketNumber = "ZGL-2026-0005",
            title = "Wyciek wody w piwnicy",
            description = "W piwnicy bloku 12 stoi woda — prawdopodobnie pęknięta rura od instalacji.",
            status = TicketStatus.ODRZUCONE,
            category = catHydraulika, author = resident2, assignedTo = null,
            apartment = null, staircase = null, building = buildingKwiatowa,
            isDeleted = false, createdAt = "2026-02-05T17:30:00Z", closedAt = null,
            images = emptyList(),
            history = listOf(
                TicketHistoryDto(TicketStatus.NOWE, resident2, "Woda w piwnicy.", "2026-02-05T17:30:00Z"),
                TicketHistoryDto(TicketStatus.ODRZUCONE, admin,
                    "Weryfikacja na miejscu — woda pochodzi z topniejącego śniegu przez nieszczelne okienko piwniczne. Właściciel piwnicy zobowiązany do usunięcia we własnym zakresie.",
                    "2026-02-06T10:00:00Z")
            )
        )
    )

    suspend fun getTickets(): List<TicketDto> = tickets

    suspend fun getTicketById(id: Int): TicketDto? = tickets.find { it.id == id }

    suspend fun getAvailableConservators(): List<AppUserDto> =
        listOf(conservator1, conservator2)

    suspend fun getCategories(): List<String> =
        listOf("Hydraulika", "Elektryka", "Domofony", "Części wspólne", "Winda", "Inne")

    suspend fun getCurrentUserRole(): String = admin.role
}
