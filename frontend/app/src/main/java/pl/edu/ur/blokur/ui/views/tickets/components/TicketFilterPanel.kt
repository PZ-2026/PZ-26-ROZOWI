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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import pl.edu.ur.blokur.dtos.StaircaseNodeDto
import pl.edu.ur.blokur.dtos.TicketStatus
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketFilterOptions
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketFilterState
import pl.edu.ur.blokur.ui.views.tickets.utils.toPresentation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketFilterPanel(
    filterState: TicketFilterState,
    filterOptions: TicketFilterOptions,
    currentUserRole: String,
    totalCount: Int,
    filteredCount: Int,
    onFilterChanged: (TicketFilterState) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var filtersExpanded by remember { mutableStateOf(false) }
    val isManager = currentUserRole == "ZARZADCA"
    val hasActiveFilter = filterState.hasActiveFilters()

    val selectedBuilding = filterOptions.buildings.find { it.id == filterState.buildingId }
    val staircases = selectedBuilding?.staircases.orEmpty()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {

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
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (filterState.searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            onFilterChanged(filterState.copy(searchQuery = ""))
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Wyczyść",
                                modifier = Modifier.size(18.dp)
                            )
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

                if (isManager) {
                    ManagerFilterSection(
                        filterState = filterState,
                        filterOptions = filterOptions,
                        staircases = staircases,
                        onFilterChanged = onFilterChanged
                    )
                }
            }
        }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManagerFilterSection(
    filterState: TicketFilterState,
    filterOptions: TicketFilterOptions,
    staircases: List<StaircaseNodeDto>,
    onFilterChanged: (TicketFilterState) -> Unit
) {
    Spacer(Modifier.height(4.dp))
    Text(
        "Filtry zarządcy",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (filterOptions.isLoading) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
        return
    }

    FilterDropdown(
        label = "Kategoria",
        selectedLabel = filterOptions.categories.find { it.id == filterState.categoryId }?.name
            ?: "Wszystkie kategorie",
        enabled = filterOptions.categories.isNotEmpty() || filterState.categoryId.isBlank()
    ) { onDismiss ->
        DropdownMenuItem(
            text = { Text("Wszystkie kategorie") },
            onClick = {
                onFilterChanged(filterState.copy(categoryId = ""))
                onDismiss()
            }
        )
        filterOptions.categories.forEach { category ->
            DropdownMenuItem(
                text = { Text(category.name) },
                onClick = {
                    onFilterChanged(filterState.copy(categoryId = category.id))
                    onDismiss()
                }
            )
        }
    }

    FilterDropdown(
        label = "Budynek",
        selectedLabel = filterOptions.buildings.find { it.id == filterState.buildingId }?.name
            ?: "Wszystkie budynki",
        enabled = filterOptions.buildings.isNotEmpty() || filterState.buildingId.isBlank()
    ) { onDismiss ->
        DropdownMenuItem(
            text = { Text("Wszystkie budynki") },
            onClick = {
                onFilterChanged(filterState.copy(buildingId = "", staircaseId = ""))
                onDismiss()
            }
        )
        filterOptions.buildings.forEach { building ->
            DropdownMenuItem(
                text = { Text(building.name) },
                onClick = {
                    onFilterChanged(
                        filterState.copy(buildingId = building.id, staircaseId = "")
                    )
                    onDismiss()
                }
            )
        }
    }

    FilterDropdown(
        label = "Klatka",
        selectedLabel = staircases.find { it.id == filterState.staircaseId }?.label
            ?: "Wszystkie klatki",
        enabled = filterState.buildingId.isNotBlank() &&
            (staircases.isNotEmpty() || filterState.staircaseId.isBlank())
    ) { onDismiss ->
        DropdownMenuItem(
            text = { Text("Wszystkie klatki") },
            onClick = {
                onFilterChanged(filterState.copy(staircaseId = ""))
                onDismiss()
            }
        )
        staircases.forEach { staircase ->
            DropdownMenuItem(
                text = { Text(staircase.label) },
                onClick = {
                    onFilterChanged(filterState.copy(staircaseId = staircase.id))
                    onDismiss()
                }
            )
        }
    }

    FilterDropdown(
        label = "Konserwator",
        selectedLabel = filterOptions.conservators.find { it.id == filterState.assignedTo }?.fullName
            ?: "Wszyscy konserwatorzy",
        enabled = filterOptions.conservators.isNotEmpty() || filterState.assignedTo.isBlank()
    ) { onDismiss ->
        DropdownMenuItem(
            text = { Text("Wszyscy konserwatorzy") },
            onClick = {
                onFilterChanged(filterState.copy(assignedTo = ""))
                onDismiss()
            }
        )
        filterOptions.conservators.forEach { conservator ->
            DropdownMenuItem(
                text = { Text(conservator.fullName) },
                onClick = {
                    onFilterChanged(filterState.copy(assignedTo = conservator.id))
                    onDismiss()
                }
            )
        }
    }

    DateFilterField(
        label = "Data od",
        isoValue = filterState.dateFrom,
        endOfDay = false,
        onDateSelected = { iso ->
            onFilterChanged(filterState.copy(dateFrom = iso ?: ""))
        }
    )

    DateFilterField(
        label = "Data do",
        isoValue = filterState.dateTo,
        endOfDay = true,
        onDateSelected = { iso ->
            onFilterChanged(filterState.copy(dateTo = iso ?: ""))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    selectedLabel: String,
    enabled: Boolean,
    menuContent: @Composable (onDismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = it }
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                menuContent { expanded = false }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilterField(
    label: String,
    isoValue: String,
    endOfDay: Boolean,
    onDateSelected: (String?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val displayDate = remember(isoValue) { formatIsoForDisplay(isoValue) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = displayDate.ifBlank { "Nie wybrano" },
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPicker = true },
            trailingIcon = {
                if (isoValue.isNotBlank()) {
                    IconButton(onClick = { onDateSelected(null) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Wyczyść datę", modifier = Modifier.size(18.dp))
                    }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    onDateSelected(millis?.let { millisToIso(it, endOfDay) })
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
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

private fun millisToIso(millis: Long, endOfDay: Boolean): String {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        if (endOfDay) {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        } else {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
    }
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(cal.time)
}

private fun formatIsoForDisplay(iso: String): String {
    if (iso.isBlank()) return ""
    return runCatching {
        val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(iso)
        parsed?.let {
            SimpleDateFormat("dd.MM.yyyy", Locale("pl", "PL")).format(it)
        } ?: iso.take(10)
    }.getOrDefault(iso.take(10))
}
