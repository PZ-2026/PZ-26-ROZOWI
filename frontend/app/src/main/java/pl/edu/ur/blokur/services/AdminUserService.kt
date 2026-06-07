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
            var allUsers = ApiResponseHandler.requireSuccess(api.getAllUsers(), "Błąd pobierania użytkowników")

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
            ApiResponseHandler.requireSuccess(api.createUser(request), "Błąd tworzenia użytkownika")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun deactivateUser(id: String) {
        runCatching {
            ApiResponseHandler.requireSuccessNoBody(api.deactivateUser(id), "Błąd deaktywacji konta")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun getUserById(id: String): AdminUserDto {
        return runCatching {
            val users = ApiResponseHandler.requireSuccess(api.getAllUsers(), "Błąd pobierania użytkowników")
            users.find { it.id == id } ?: throw Exception("Nie znaleziono użytkownika")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun updateUser(id: String, request: UpdateAdminUserRequest): AdminUserDto {
        return runCatching {
            ApiResponseHandler.requireSuccess(api.updateUser(id, request), "Błąd edycji profilu")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }
}
