package pl.edu.ur.blokur.ui.views.profile.contents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.components.NormalCard
import pl.edu.ur.blokur.ui.theme.PreviewTheme
import pl.edu.ur.blokur.ui.views.profile.utils.ProfileState

@Composable
fun ProfileContent(
    state: ProfileState,
    isManager: Boolean = false,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToCommunityLogo: () -> Unit = {},
    onNavigateToDocumentDistribution: () -> Unit = {},
    onNavigateToInspections: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (state) {
        is ProfileState.Loading -> LoadingIndicator()
        is ProfileState.Data -> ProfileDataContent(
            data = state,
            isManager = isManager,
            onNavigateToNotificationSettings = onNavigateToNotificationSettings,
            onNavigateToCommunityLogo = onNavigateToCommunityLogo,
            onNavigateToDocumentDistribution = onNavigateToDocumentDistribution,
            onNavigateToInspections = onNavigateToInspections,
            onNavigateToCategories = onNavigateToCategories,
            modifier = modifier
        )
    }
}

@Composable
private fun ProfileDataContent(
    data: ProfileState.Data,
    isManager: Boolean = false,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToCommunityLogo: () -> Unit = {},
    onNavigateToDocumentDistribution: () -> Unit = {},
    onNavigateToInspections: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        NormalCard {
            Text(
                "Dane użytkownika",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Edycja profilu będzie dostępna po rozszerzeniu systemu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = data.role,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rola") },
                singleLine = true,
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = data.email,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Adres e-mail") },
                singleLine = true,
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = data.name.ifBlank { "—" },
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Imię i nazwisko") },
                singleLine = true,
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = data.phone.ifBlank { "—" },
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Numer telefonu") },
                singleLine = true,
                readOnly = true
            )

        }

        if (!isManager) {
            AdminNavRow(
                icon = Icons.Rounded.DateRange,
                title = "Przeglądy w budynku",
                subtitle = "Harmonogram przeglądów technicznych (podgląd)",
                isFirst = true,
                isLast = true,
                onClick = onNavigateToInspections
            )
        }

        if (isManager) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Rounded.AdminPanelSettings,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Ustawienia zarządcy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                AdminNavRow(
                    icon = Icons.Rounded.Notifications,
                    title = "Ustawienia powiadomień",
                    subtitle = "Konfiguruj alerty per typ zdarzenia",
                    isFirst = true,
                    isLast = false,
                    onClick = onNavigateToNotificationSettings
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AdminNavRow(
                    icon = Icons.Rounded.Description,
                    title = "Dystrybucja dokumentów",
                    subtitle = "Zawiadomienia o stawkach, rozliczenia roczne",
                    isFirst = false,
                    isLast = false,
                    onClick = onNavigateToDocumentDistribution
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AdminNavRow(
                    icon = Icons.Rounded.DateRange,
                    title = "Harmonogram przeglądów",
                    subtitle = "Planowanie i historia przeglądów",
                    isFirst = false,
                    isLast = false,
                    onClick = onNavigateToInspections
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AdminNavRow(
                    icon = Icons.Rounded.Settings,
                    title = "Kategorie zgłoszeń",
                    subtitle = "Zarządzaj typami usterek i zgłoszeń",
                    isFirst = false,
                    isLast = false,
                    onClick = onNavigateToCategories
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AdminNavRow(
                    icon = Icons.Rounded.Image,
                    title = "Logo wspólnoty",
                    subtitle = "Zarządzaj identyfikacją wizualną",
                    isFirst = false,
                    isLast = true,
                    onClick = onNavigateToCommunityLogo
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AdminNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val shape = when {
        isFirst && isLast -> RoundedCornerShape(16.dp)
        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentLoadingPreview() {
    PreviewTheme { ProfileContent(ProfileState.Loading) }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentDataPreview() {
    PreviewTheme {
        ProfileContent(
            state = ProfileState.Data(
                role = "Mieszkaniec",
                email = "jan@example.com",
                name = "",
                phone = ""
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentManagerPreview() {
    PreviewTheme {
        ProfileContent(
            state = ProfileState.Data(
                role = "Zarządca",
                email = "admin@example.com",
                phone = ""
            ),
            isManager = true
        )
    }
}
