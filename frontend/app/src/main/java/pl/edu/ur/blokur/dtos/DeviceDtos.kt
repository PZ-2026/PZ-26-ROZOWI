package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

/** GET /api/documents — pojedynczy dokument. */
data class UserDocumentDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("downloadUrl") val downloadUrl: String?
)

/** DTO rejestracji tokenu FCM urządzenia (POST /api/devices/register). */
data class DeviceRegistrationRequestDto(
    @SerializedName("fcmToken") val fcmToken: String,
    @SerializedName("platform") val platform: String = "ANDROID"
)
