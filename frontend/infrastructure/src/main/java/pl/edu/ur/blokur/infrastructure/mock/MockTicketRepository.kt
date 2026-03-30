package pl.edu.ur.blokur.infrastructure.mock

import pl.edu.ur.blokur.domain.model.Apartment
import pl.edu.ur.blokur.domain.model.AppUser
import pl.edu.ur.blokur.domain.model.Building
import pl.edu.ur.blokur.domain.model.Staircase
import pl.edu.ur.blokur.domain.model.Ticket
import pl.edu.ur.blokur.domain.model.TicketCategory
import pl.edu.ur.blokur.domain.model.TicketHistory
import pl.edu.ur.blokur.domain.model.TicketStatus
import pl.edu.ur.blokur.domain.repository.TicketRepository
import javax.inject.Inject

internal class MockTicketRepository @Inject constructor() : TicketRepository {

    private val resident1 = AppUser(101, "Janusz", "Kowalski", "janusz@example.com", "MIESZKANIEC")
    private val resident2 = AppUser(102, "Grażyna", "Nowak", "grazyna@example.com", "MIESZKANIEC")
    private val resident3 = AppUser(105, "Mieczysław", "Wąs", "mietek@example.com", "MIESZKANIEC")
    private val conservator1 = AppUser(201, "Ryszard", "Klucz", "ryszard.k@blokur.pl", "KONSERWATOR")
    private val conservator2 = AppUser(202, "Zdzisław", "Kabel", "zdzislaw.k@blokur.pl", "KONSERWATOR")
    private val admin = AppUser(301, "Anna", "Zarządca", "anna.z@blokur.pl", "ADMINISTRATOR")

    private val buildingKwiatowa = Building(12, "Osiedle Róż", "Blok 12", "ul. Kwiatowa 12")
    private val buildingMickiewicza = Building(15, "Osiedle Centrum", "Wieżowiec 15", "al. Mickiewicza 15")
    private val staircaseA = Staircase(1, "A")
    private val staircaseB = Staircase(3, "B")
    private val apt45 = Apartment(45, "45")

    private val catHydraulika = TicketCategory(1, "Hydraulika")
    private val catElektryka = TicketCategory(2, "Elektryka")
    private val catDomofony = TicketCategory(3, "Domofony")
    private val catCzesciWspolne = TicketCategory(4, "Części wspólne")

    private val tickets = listOf(
        Ticket(
            id = 1, ticketNumber = "ZGL-2026-0001",
            title = "Brak ciepłej wody",
            description = "Od wczorajszego popołudnia w pionie nie ma ciepłej wody. Proszę o pilną interwencję.",
            status = TicketStatus.NOWE,
            category = catHydraulika, author = resident1, assignedTo = null,
            apartment = apt45, staircase = staircaseB, building = buildingKwiatowa,
            isDeleted = false, createdAt = "2026-03-17T08:30:00Z", closedAt = null,
            images = listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=400"),
            history = listOf(
                TicketHistory(1, 1, TicketStatus.NOWE, resident1, "Zgłoszenie awarii - brak ciepłej wody.", "2026-03-17T08:30:00Z")
            )
        ),
        Ticket(
            id = 2, ticketNumber = "ZGL-2026-0002",
            title = "Przepalona żarówka na półpiętrze",
            description = "Żarówka między parterem a pierwszym piętrem jest przepalona.",
            status = TicketStatus.ZAPLANOWANO,
            category = catElektryka, author = resident2, assignedTo = conservator2,
            apartment = null, staircase = staircaseA, building = buildingKwiatowa,
            isDeleted = false, createdAt = "2026-03-16T14:15:00Z", closedAt = null,
            images = emptyList(),
            history = listOf(
                TicketHistory(2, 2, TicketStatus.NOWE, resident2, "Żarówka przepalona.", "2026-03-16T14:15:00Z"),
                TicketHistory(3, 2, TicketStatus.ZAPLANOWANO, admin, "Zaplanowano wymianę na wtorek.", "2026-03-17T09:00:00Z")
            )
        ),
        Ticket(
            id = 3, ticketNumber = "ZGL-2026-0003",
            title = "Zepsuty domofon — klatka B",
            description = "Domofon przestał działać po burzy. Nie można otworzyć drzwi z zewnątrz.",
            status = TicketStatus.W_REALIZACJI,
            category = catDomofony, author = resident3, assignedTo = conservator1,
            apartment = null, staircase = staircaseB, building = buildingMickiewicza,
            isDeleted = false, createdAt = "2026-03-10T11:00:00Z", closedAt = null,
            images = emptyList(),
            history = listOf(
                TicketHistory(4, 3, TicketStatus.NOWE, resident3, null, "2026-03-10T11:00:00Z"),
                TicketHistory(5, 3, TicketStatus.ZAPLANOWANO, admin, "Zlecono konserwatorowi.", "2026-03-11T08:00:00Z"),
                TicketHistory(6, 3, TicketStatus.W_REALIZACJI, conservator1, "Trwa diagnostyka panelu zewnętrznego.", "2026-03-14T10:30:00Z")
            )
        ),
        Ticket(
            id = 4, ticketNumber = "ZGL-2026-0004",
            title = "Uszkodzona poręcz schodów",
            description = "Poręcz na 3 piętrze klatki A jest obluzowana i grozi wypadkiem.",
            status = TicketStatus.ZAMKNIETE,
            category = catCzesciWspolne, author = resident1, assignedTo = conservator2,
            apartment = null, staircase = staircaseA, building = buildingKwiatowa,
            isDeleted = false, createdAt = "2026-02-20T09:45:00Z", closedAt = "2026-03-01T12:00:00Z",
            images = emptyList(),
            history = listOf(
                TicketHistory(7, 4, TicketStatus.NOWE, resident1, null, "2026-02-20T09:45:00Z"),
                TicketHistory(8, 4, TicketStatus.W_REALIZACJI, conservator2, "Naprawiono i wzmocniono śruby.", "2026-02-28T14:00:00Z"),
                TicketHistory(9, 4, TicketStatus.ZAMKNIETE, admin, "Weryfikacja OK. Zamykam zgłoszenie.", "2026-03-01T12:00:00Z")
            )
        ),
        Ticket(
            id = 5, ticketNumber = "ZGL-2026-0005",
            title = "Wyciek wody w piwnicy",
            description = "W piwnicy bloku 12 stoi woda — prawdopodobnie pęknięta rura od instalacji.",
            status = TicketStatus.ODRZUCONE,
            category = catHydraulika, author = resident2, assignedTo = null,
            apartment = null, staircase = null, building = buildingKwiatowa,
            isDeleted = false, createdAt = "2026-02-05T17:30:00Z", closedAt = null,
            images = emptyList(),
            history = listOf(
                TicketHistory(10, 5, TicketStatus.NOWE, resident2, "Woda w piwnicy.", "2026-02-05T17:30:00Z"),
                TicketHistory(11, 5, TicketStatus.ODRZUCONE, admin,
                    "Weryfikacja na miejscu — woda pochodzi z topniejącego śniegu przez nieszczelne okienko piwniczne. Właściciel piwnicy zobowiązany do usunięcia we własnym zakresie.",
                    "2026-02-06T10:00:00Z")
            )
        )
    )

    override suspend fun getTickets(): List<Ticket> = tickets

    override suspend fun getTicketById(id: Int): Ticket? = tickets.find { it.id == id }

    override suspend fun getAvailableConservators(): List<AppUser> =
        listOf(conservator1, conservator2)

    override suspend fun getCategories(): List<String> =
        listOf("Hydraulika", "Elektryka", "Domofony", "Części wspólne", "Winda", "Inne")
}
