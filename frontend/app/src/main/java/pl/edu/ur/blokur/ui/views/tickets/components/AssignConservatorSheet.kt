package pl.edu.ur.blokur.ui.views.tickets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.ConservatorDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignConservatorSheet(
    conservators: List<ConservatorDto>,
    isLoading: Boolean = false,
    onDismissRequest: () -> Unit,
    onAssign: (ConservatorDto, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { !isLoading }
    )
    
    var step by remember { mutableStateOf(1) }
    var selected by remember { mutableStateOf<ConservatorDto?>(null) }

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val todayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
        }
    )
    val timePickerState = rememberTimePickerState(is24Hour = true)

    val formattedDate = remember(selectedDateMillis) {
        selectedDateMillis?.let {
            SimpleDateFormat("dd MMMM yyyy", Locale("pl", "PL")).format(Date(it))
        } ?: "Wybierz datę"
    }
    val formattedTime = remember(selectedHour, selectedMinute) {
        if (selectedHour != null && selectedMinute != null)
            String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
        else "Wybierz godzinę"
    }
    // Format ISO dla backendu (LocalDateTime)
    val isoDateTime = remember(selectedDateMillis, selectedHour, selectedMinute) {
        if (selectedDateMillis != null && selectedHour != null && selectedMinute != null) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis!!
                set(Calendar.HOUR_OF_DAY, selectedHour!!)
                set(Calendar.MINUTE, selectedMinute!!)
                set(Calendar.SECOND, 0)
            }
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(cal.time)
        } else null
    }

    val isTimeValid by remember(selectedDateMillis, selectedHour, selectedMinute) {
        derivedStateOf {
            if (selectedDateMillis == null || selectedHour == null || selectedMinute == null) return@derivedStateOf false
            val selected = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis!!
                set(Calendar.HOUR_OF_DAY, selectedHour!!)
                set(Calendar.MINUTE, selectedMinute!!)
            }
            val now = Calendar.getInstance()
            if (selected.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                selected.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            ) selected.timeInMillis > now.timeInMillis else true
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (step == 1) {
                // ── Krok 1: Wybór terminu ──
                Text("Termin prac", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Data wejścia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.CalendarToday, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            Icon(Icons.Rounded.Edit, "Zmień datę",
                                modifier = Modifier.clickable { showDatePicker = true })
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Godzina wejścia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    val timeError = selectedDateMillis != null && selectedHour != null && !isTimeValid
                    OutlinedTextField(
                        value = formattedTime,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = timeError,
                        leadingIcon = { Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            Icon(Icons.Rounded.Edit, "Zmień godzinę",
                                modifier = Modifier.clickable { showTimePicker = true })
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (timeError) {
                        Text(
                            "Godzina musi być późniejsza niż obecna",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                Button(
                    onClick = { step = 2 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = isTimeValid,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Dalej", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

            } else {
                // ── Krok 2: Wybór konserwatora ──
                Text("Wybierz konserwatora", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                // Podsumowanie terminu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("Wybrany termin", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$formattedDate, $formattedTime", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(
                        onClick = { step = 1 },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Rounded.Edit, "Zmień", tint = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    conservators.forEach { conservator ->
                        val isSelected = selected == conservator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable(enabled = !isLoading) { selected = conservator }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Person, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conservator.fullName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                val ticketsLabel = when (conservator.activeTicketsCount) {
                                    0L -> "Brak aktywnych zleceń"
                                    1L -> "1 aktywne zlecenie"
                                    else -> "${conservator.activeTicketsCount} aktywne zlecenia"
                                }
                                Text(ticketsLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Button(
                    onClick = { selected?.let { onAssign(it, isoDateTime ?: "") } },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = selected != null && !isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Zatwierdź i powiadom", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") } },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}
