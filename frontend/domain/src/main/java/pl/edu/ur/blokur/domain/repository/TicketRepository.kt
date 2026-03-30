package pl.edu.ur.blokur.domain.repository

import pl.edu.ur.blokur.domain.model.AppUser
import pl.edu.ur.blokur.domain.model.Ticket

interface TicketRepository {
    suspend fun getTickets(): List<Ticket>
    suspend fun getTicketById(id: Int): Ticket?
    suspend fun getAvailableConservators(): List<AppUser>
    suspend fun getCategories(): List<String>
    suspend fun getCurrentUserRole(): String
}
