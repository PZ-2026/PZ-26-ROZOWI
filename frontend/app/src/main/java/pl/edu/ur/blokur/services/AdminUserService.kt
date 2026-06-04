package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.AdminUserDto
import pl.edu.ur.blokur.dtos.CreateAdminUserRequest
import pl.edu.ur.blokur.dtos.PageDto
import pl.edu.ur.blokur.dtos.UpdateAdminUserRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminUserService @Inject constructor(
    private val api: AdminUserApiService
) {
    suspend fun getAllUsers(page: Int, size: Int, search: String? = null): PageDto<AdminUserDto> {
        return runCatching {
            val resp = api.getAllUsers()
            if (!resp.isSuccessful) throw Exception("Błąd pobierania użytkowników (${resp.code()})")
            var allUsers = resp.body() ?: throw Exception("Pusta odpowiedź z serwera")

            if (!search.isNullOrBlank()) {
                val s = search.lowercase()
                allUsers = allUsers.filter {
                    it.firstName.lowercase().contains(s) ||
                    it.lastName.lowercase().contains(s) ||
                    it.email.lowercase().contains(s)
                }
            }

            val totalElements = allUsers.size
            val totalPages = if (totalElements == 0) 1 else (totalElements + size - 1) / size
            val start = page * size
            val content = if (start < totalElements) {
                allUsers.subList(start, minOf(start + size, totalElements))
            } else {
                emptyList()
            }

            PageDto(
                content = content,
                last = page >= totalPages - 1,
                totalPages = totalPages,
                totalElements = totalElements,
                size = size,
                number = page,
                first = page == 0,
                numberOfElements = content.size,
                empty = content.isEmpty()
            )
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

    suspend fun getUserById(id: String): AdminUserDto {
        return runCatching {
            val resp = api.getAllUsers()
            if (!resp.isSuccessful) throw Exception("Błąd pobierania użytkowników (${resp.code()})")
            val users = resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
            users.find { it.id == id } ?: throw Exception("Nie znaleziono użytkownika")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun updateUser(id: String, request: UpdateAdminUserRequest): AdminUserDto {
        return runCatching {
            val resp = api.updateUser(id, request)
            if (!resp.isSuccessful) throw Exception("Błąd edycji profilu (${resp.code()})")
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }
}
