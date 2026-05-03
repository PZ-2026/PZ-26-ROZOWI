package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.AdminUserDto
import pl.edu.ur.blokur.dtos.CreateAdminUserRequest
import pl.edu.ur.blokur.dtos.UpdateAdminUserRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminUserService @Inject constructor(
    private val api: AdminUserApiService
) {
    suspend fun getAllUsers(): List<AdminUserDto> {
        return runCatching {
            val resp = api.getAllUsers()
            if (!resp.isSuccessful) throw Exception("Błąd pobierania użytkowników (${resp.code()})")
            resp.body() ?: emptyList()
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun createUser(request: CreateAdminUserRequest): AdminUserDto {
        return runCatching {
            val resp = api.createUser(request)
            if (!resp.isSuccessful) throw Exception(
                when (resp.code()) {
                    400 -> "Nieprawidłowe dane użytkownika."
                    404 -> "Nie znaleziono wybranego lokalu."
                    409 -> "Użytkownik z tym adresem e-mail już istnieje."
                    else -> "Błąd tworzenia użytkownika (${resp.code()})"
                }
            )
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun deactivateUser(id: String) {
        runCatching {
            val resp = api.deactivateUser(id)
            if (!resp.isSuccessful && resp.code() != 204) {
                throw Exception("Błąd deaktywacji konta (${resp.code()})")
            }
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }
}
