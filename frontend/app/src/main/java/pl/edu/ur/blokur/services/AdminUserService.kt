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
            val resp = api.getAllUsers(page, size, search)
            if (!resp.isSuccessful) throw Exception("Błąd pobierania użytkowników (${resp.code()})")
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
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
            val resp = api.getUserById(id)
            if (!resp.isSuccessful) throw Exception("Nie znaleziono użytkownika (${resp.code()})")
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun updateUser(id: String, request: UpdateAdminUserRequest): AdminUserDto {
        return runCatching {
            val resp = api.updateUser(id, request)
            if (!resp.isSuccessful) throw Exception("Błąd edycji profilu (${resp.code()})")
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun deleteUser(id: String) {
        runCatching {
            val resp = api.deleteUser(id)
            if (!resp.isSuccessful && resp.code() != 204) {
                throw Exception("Błąd stałego usuwania konta (${resp.code()})")
            }
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }
}
