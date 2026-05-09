package pl.edu.ur.blokur.ui.views.tickets.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.TicketStatus
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketFilterState
import pl.edu.ur.blokur.ui.views.tickets.utils.toPresentation

// Definicja statusów dostępnych jako filtry
private data class StatusChip(val status: TicketStatus?, val label: String, val color: Color)

private val ALL_STATUS_CHIPS = listOf(
    StatusChip(null, "Wszystkie", InfoBlue),
    StatusChip(TicketStatus.NOWE, "Nowe", InfoBlue),
    StatusChip(TicketStatus.ZAPLANOWANO, "Zaplanowano", InfoBlue),
    StatusChip(TicketStatus.W_REALIZACJI, "W realizacji", WarningOrange),
    StatusChip(TicketStatus.WSTRZYMANO, "Wstrzymano", WarningOrange),
    StatusChip(TicketStatus.ZAKONCZONE_DO_WERYFIKACJI, "Do weryfikacji", SuccessGreen),
    StatusChip(TicketStatus.ZAMKNIETE, "Zamknięte", SuccessGreen),
    StatusChip(TicketStatus.ODRZUCONE, "Odrzucone", ErrorRed),
)

@Composable
fun TicketFilterPanel(
    filterState: TicketFilterState,
    totalCount: Int,
    filteredCount: Int,
    onFilterChanged: (TicketFilterState) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var filtersExpanded by remember { mutableStateOf(false) }

    val hasActiveFilter = filterState.searchQuery.isNotBlank() || filterState.selectedStatus.isNotBlank()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // ── Pasek wyszukiwania ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { onFilterChanged(filterState.copy(searchQuery = it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Szukaj po tytule, numerze...") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (filterState.searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            onFilterChanged(filterState.copy(searchQuery = ""))
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Wyczyść",
                                modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Przycisk filtrów
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (hasActiveFilter) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .clickable { filtersExpanded = !filtersExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.FilterList,
                    contentDescription = "Filtry",
                    tint = if (hasActiveFilter) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Odśwież
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { onRefresh() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = "Odśwież",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Panel filtrów (animowany rozwijany) ──
        AnimatedVisibility(
            visible = filtersExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Filtruj po statusie",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasActiveFilter) {
                        Text(
                            "Wyczyść filtry",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                onFilterChanged(TicketFilterState())
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ALL_STATUS_CHIPS.forEach { chip ->
                        val isSelected = filterState.selectedStatus == (chip.status?.name ?: "")
                        StatusFilterChip(
                            label = chip.label,
                            color = chip.color,
                            isSelected = isSelected,
                            onClick = {
                                onFilterChanged(
                                    filterState.copy(selectedStatus = chip.status?.name ?: "")
                                )
                            }
                        )
                    }
                }
            }
        }

        // ── Licznik wyników ──
        if (hasActiveFilter) {
            Text(
                text = if (filteredCount == totalCount)
                    "$totalCount zgłoszeń"
                else
                    "Znaleziono $filteredCount z $totalCount zgłoszeń",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) color.copy(alpha = 0.15f)
                  else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (label != "Wszystkie") {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(color, CircleShape)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
