package pl.edu.ur.blokur.data.mock

import pl.edu.ur.blokur.data.model.ApartmentDto
import pl.edu.ur.blokur.data.model.AppUserDto
import pl.edu.ur.blokur.data.model.BuildingDto
import pl.edu.ur.blokur.data.model.StaircaseDto
import pl.edu.ur.blokur.data.model.TicketCategoryDto
import pl.edu.ur.blokur.data.model.TicketDto
import pl.edu.ur.blokur.data.model.TicketHistoryDto
import pl.edu.ur.blokur.data.model.TicketStatus

object MockTickets {
    private val categoryHydraulika = TicketCategoryDto(1, "Hydraulika")
    private val categoryElektryka = TicketCategoryDto(2, "Elektryka")
    private val categoryDomofony = TicketCategoryDto(3, "Domofony")
    private val categoryCzesciWspolne = TicketCategoryDto(4, "Części wspólne")
    
    private val mockUser1 = AppUserDto(101, "Janusz", "Kowalski", "janusz@example.com", "MIESZKANIEC")
    private val mockUser2 = AppUserDto(102, "Grażyna", "Nowak", "grazyna@example.com", "MIESZKANIEC")
    private val mockUser3 = AppUserDto(105, "Mieczysław", "Wąs", "mietek@example.com", "MIESZKANIEC")
    
    private val mockConservator1 = AppUserDto(201, "Ryszard", "Klucz", "ryszard.k@blokur.pl", "KONSERWATOR")
    private val mockConservator2 = AppUserDto(202, "Zdzisław", "Kabel", "zdzislaw.k@blokur.pl", "KONSERWATOR")
    
    private val mockAdmin1 = AppUserDto(301, "Anna", "Zarządca", "anna.z@blokur.pl", "ADMINISTRATOR")
    
    private val buildingKwiatowa = BuildingDto(12, "Osiedle Róż", "Blok 12", "ul. Kwiatowa 12")
    private val buildingMickiewicza = BuildingDto(15, "Osiedle Centrum", "Więżowiec 15", "al. Mickiewicza 15")
    
    private val staircaseA = StaircaseDto(1, "A")
    private val staircaseB = StaircaseDto(3, "B")
    
    private val apt45 = ApartmentDto(45, "45")

    val tickets = listOf(
        TicketDto(
            id = 1,
            ticketNumber = "ZGL-2026-0001",
            title = "Brak ciepłej wody",
            description = "Od wczorajszego popołudnia w pionie nie ma ciepłej wody. Proszę o pilną interwencję.",
            status = TicketStatus.NOWE,
            category = categoryHydraulika,
            author = mockUser1,
            assignedTo = null,
            apartment = apt45,
            staircase = staircaseB,
            building = buildingKwiatowa,
            isDeleted = false,
            createdAt = "2026-03-17T08:30:00Z",
            closedAt = null,
            images = listOf(
                "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=400"
            ),
            history = listOf(
                TicketHistoryDto(
                    id = 1,
                    ticketId = 1,
                    status = TicketStatus.NOWE,
                    changedBy = mockUser1,
                    comment = "Zgłoszenie awarii - brak ciepłej wody.",
                    createdAt = "2026-03-17T08:30:00Z"
                )
            )
        ),
        TicketDto(
            id = 2,
            ticketNumber = "ZGL-2026-0002",
            title = "Przepalona żarówka na półpiętrze",
            description = "Żarówka między parterem a pierwszym piętrem jest przepalona.",
            status = TicketStatus.ZAPLANOWANO,
            category = categoryElektryka,
            author = mockUser2,
            assignedTo = mockConservator2,
            apartment = null,
            staircase = staircaseA,
            building = buildingKwiatowa,
            isDeleted = false,
            createdAt = "2026-03-16T14:15:00Z",
            closedAt = null,
            images = listOf(
                "https://images.unsplash.com/photo-1550567706-eec56c6c594c?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1542566212-32a229a4a754?auto=format&fit=crop&q=80&w=400"
            ),
            history = listOf(
                TicketHistoryDto(
                    id = 2,
                    ticketId = 2,
                    status = TicketStatus.NOWE,
                    changedBy = mockUser2,
                    comment = "Utworzono zgłoszenie usterki elektrycznej.",
                    createdAt = "2026-03-16T14:15:00Z"
                ),
                TicketHistoryDto(
                    id = 3,
                    ticketId = 2,
                    status = TicketStatus.ZAPLANOWANO,
                    changedBy = mockAdmin1,
                    comment = "Przydzielono konserwatora. Zaplanowano wizytę w dniu jutrzejszym.",
                    createdAt = "2026-03-16T15:00:00Z"
                )
            )
        ),
        TicketDto(
            id = 3,
            ticketNumber = "ZGL-2026-0003",
            title = "Uszkodzony domofon",
            description = "Zamek w domofonie nie reaguje na kody wejściowe ani na pastylkę.",
            status = TicketStatus.ZAMKNIETE,
            category = categoryDomofony,
            author = mockUser3,
            assignedTo = mockConservator2,
            apartment = null,
            staircase = staircaseB,
            building = buildingMickiewicza,
            isDeleted = false,
            createdAt = "2026-03-10T09:45:00Z",
            closedAt = "2026-03-11T11:20:00Z",
            images = listOf(),
            history = listOf(
                TicketHistoryDto(
                    id = 4,
                    ticketId = 3,
                    status = TicketStatus.NOWE,
                    changedBy = mockUser3,
                    comment = "Zgłoszenie usterki domofonu.",
                    createdAt = "2026-03-10T09:45:00Z"
                ),
                TicketHistoryDto(
                    id = 5,
                    ticketId = 3,
                    status = TicketStatus.ZAPLANOWANO,
                    changedBy = mockAdmin1,
                    comment = "Zlecono naprawę.",
                    createdAt = "2026-03-10T10:30:00Z"
                ),
                TicketHistoryDto(
                    id = 6,
                    ticketId = 3,
                    status = TicketStatus.W_REALIZACJI,
                    changedBy = mockConservator2,
                    comment = "Rozpoczęto weryfikację usterki elektroniki przy furtce.",
                    createdAt = "2026-03-11T08:00:00Z"
                ),
                TicketHistoryDto(
                    id = 7,
                    ticketId = 3,
                    status = TicketStatus.ZAKONCZONE,
                    changedBy = mockConservator2,
                    comment = "Wymieniono obwód kodowy. Do potwierdzenia przez lokatora.",
                    createdAt = "2026-03-11T10:15:00Z"
                ),
                TicketHistoryDto(
                    id = 8,
                    ticketId = 3,
                    status = TicketStatus.ZAMKNIETE,
                    changedBy = mockAdmin1,
                    comment = "Usterka zweryfikowana. Zamknięto temat.",
                    createdAt = "2026-03-11T11:20:00Z"
                )
            )
        ),
        TicketDto(
            id = 4,
            ticketNumber = "ZGL-2026-0004",
            title = "Zalany sufit w łazience",
            description = "Z sufitu kapie woda, prawdopodobnie sąsiad z góry nas zalewa. Zgłoszenie pilne!",
            status = TicketStatus.W_REALIZACJI,
            category = categoryHydraulika,
            author = mockUser1,
            assignedTo = mockConservator1,
            apartment = apt45,
            staircase = staircaseB,
            building = buildingKwiatowa,
            isDeleted = false,
            createdAt = "2026-03-17T21:05:00Z",
            closedAt = null,
            images = listOf(
                "https://images.unsplash.com/photo-1584622650111-993a426fbf0b?auto=format&fit=crop&q=80&w=400"
            ),
            history = listOf(
                TicketHistoryDto(
                    id = 9,
                    ticketId = 4,
                    status = TicketStatus.NOWE,
                    changedBy = mockUser1,
                    comment = "Pilne zgłoszenie zalania.",
                    createdAt = "2026-03-17T21:05:00Z"
                ),
                TicketHistoryDto(
                    id = 10,
                    ticketId = 4,
                    status = TicketStatus.ZAPLANOWANO,
                    changedBy = mockAdmin1,
                    comment = "Priorytet wysoki. Wysłano hydraulika dyżurnego.",
                    createdAt = "2026-03-17T21:20:00Z"
                ),
                TicketHistoryDto(
                    id = 11,
                    ticketId = 4,
                    status = TicketStatus.W_REALIZACJI,
                    changedBy = mockConservator1,
                    comment = "Rozpoczęto działania naprawcze w szachcie.",
                    createdAt = "2026-03-17T21:40:00Z"
                )
            )
        ),
        TicketDto(
            id = 5,
            ticketNumber = "ZGL-2026-0005",
            title = "Dewastacja windy",
            description = "Ktoś pomazał sprayem lustro i panel sterowania w windzie.",
            status = TicketStatus.WSTRZYMANO,
            category = categoryCzesciWspolne,
            author = mockUser3,
            assignedTo = mockConservator1,
            apartment = null,
            staircase = staircaseA,
            building = buildingKwiatowa,
            isDeleted = false,
            createdAt = "2026-03-15T18:20:00Z",
            closedAt = null,
            images = listOf(
                "https://images.unsplash.com/photo-1588196749597-9ff046f40656?auto=format&fit=crop&q=80&w=400"
            ),
            history = listOf(
                TicketHistoryDto(
                    id = 12,
                    ticketId = 5,
                    status = TicketStatus.NOWE,
                    changedBy = mockUser3,
                    comment = "Zgłoszono wandalizm.",
                    createdAt = "2026-03-15T18:20:00Z"
                ),
                TicketHistoryDto(
                    id = 13,
                    ticketId = 5,
                    status = TicketStatus.ZAPLANOWANO,
                    changedBy = mockAdmin1,
                    comment = "Skierowano do obsługi konserwatorskiej czystości.",
                    createdAt = "2026-03-16T08:00:00Z"
                ),
                TicketHistoryDto(
                    id = 14,
                    ticketId = 5,
                    status = TicketStatus.W_REALIZACJI,
                    changedBy = mockConservator1,
                    comment = "Przystąpiono do mycia kabiny.",
                    createdAt = "2026-03-16T09:00:00Z"
                ),
                TicketHistoryDto(
                    id = 15,
                    ticketId = 5,
                    status = TicketStatus.WSTRZYMANO,
                    changedBy = mockConservator1,
                    comment = "Brak specjalistycznego rozpuszczalnika do sprayu. Czekam na dostawę.",
                    createdAt = "2026-03-16T09:10:00Z"
                )
            )
        ),
        TicketDto(
            id = 6,
            ticketNumber = "ZGL-2026-0006",
            title = "Prywatna usterka kranu w kuchni",
            description = "Dzień dobry, kapie mi kran w kuchni nad zlewem.",
            status = TicketStatus.ODRZUCONE,
            category = categoryHydraulika,
            author = mockUser2,
            assignedTo = null,
            apartment = ApartmentDto(12, "12"),
            staircase = staircaseA,
            building = buildingKwiatowa,
            isDeleted = false,
            createdAt = "2026-03-14T10:00:00Z",
            closedAt = "2026-03-14T12:00:00Z",
            images = emptyList(),
            history = listOf(
                TicketHistoryDto(
                    id = 16,
                    ticketId = 6,
                    status = TicketStatus.NOWE,
                    changedBy = mockUser2,
                    comment = "Zgłoszono drobną usterkę prywatną.",
                    createdAt = "2026-03-14T10:00:00Z"
                ),
                TicketHistoryDto(
                    id = 17,
                    ticketId = 6,
                    status = TicketStatus.ODRZUCONE,
                    changedBy = mockAdmin1,
                    comment = "Odrzucono - naprawy wewnętrzne w lokalach leżą po stronie właściciela.",
                    createdAt = "2026-03-14T12:00:00Z"
                )
            )
        )
    )
}
