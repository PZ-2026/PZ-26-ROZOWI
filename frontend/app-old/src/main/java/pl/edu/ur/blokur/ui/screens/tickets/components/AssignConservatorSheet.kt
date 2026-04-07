package pl.edu.ur.blokur.ui.screens.tickets.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.data.model.AppUserDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignConservatorSheet(
    conservators: List<AppUserDto>,
    onDismissRequest: () -> Unit,
    onAssign: (AppUserDto, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var step by remember { mutableStateOf(1) } // 1: wybór osoby, 2: wybór daty
    var selectedConservator by remember { mutableStateOf<AppUserDto?>(null) }

    // Stany dla pickerów
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Daty z przeszłości zablokowane, dzisiaj dozwolone
    val todayMillisStart =
        remember {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

    val dateState =
        rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
            selectableDates =
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= todayMillisStart
                    }
                },
        )
    val timeState = rememberTimePickerState(is24Hour = true)

    val formattedDate =
        remember(selectedDateMillis) {
            selectedDateMillis?.let {
                SimpleDateFormat("dd MMMM yyyy", Locale("pl", "PL")).format(Date(it))
            } ?: "Wybierz datę"
        }

    val formattedTime =
        remember(selectedHour, selectedMinute) {
            if (selectedHour != null && selectedMinute != null) {
                String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            } else {
                "Wybierz godzinę"
            }
        }

    // Walidacja czasu dla "dzisiaj"
    val isTimeValid =
        remember(selectedDateMillis, selectedHour, selectedMinute) {
            if (selectedDateMillis == null || selectedHour == null || selectedMinute == null) return@remember false

            val selectedCalendar =
                Calendar.getInstance().apply {
                    timeInMillis = selectedDateMillis!!
                    set(Calendar.HOUR_OF_DAY, selectedHour!!)
                    set(Calendar.MINUTE, selectedMinute!!)
                }

            val now = Calendar.getInstance()

            // Jeśli wybrany dzień to dzisiaj to czas musi być w przyszłości
            if (selectedCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                selectedCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            ) {
                selectedCalendar.timeInMillis > now.timeInMillis
            } else {
                true // Dla dni z przyszłości każda godzina jest ok
            }
        }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (step == 1) {
                Text(
                    text = "Termin prac",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Data wizyty UI (prawdziwy picker)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Data wejścia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Zmień date",
                                modifier = Modifier.clickable { showDatePicker = true },
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                // Czas wizyty UI (prawdziwy picker)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Godzina wejścia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    OutlinedTextField(
                        value = formattedTime,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Zmień godzinę",
                                modifier = Modifier.clickable { showTimePicker = true },
                            )
                        },
                        isError = (selectedDateMillis != null && selectedHour != null && !isTimeValid),
                        shape = RoundedCornerShape(12.dp),
                    )
                    if (selectedDateMillis != null && selectedHour != null && !isTimeValid) {
                        Text(
                            text = "Godzina musi być późniejsza niż obecna",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { step = 2 },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    enabled = isTimeValid,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = "Dalej",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Text(
                    text = "Wybierz konserwatora",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Podsumowanie wybranego terminu
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Column {
                            Text(
                                text = "Wybrany termin",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "$formattedDate, $formattedTime",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    IconButton(onClick = { step = 1 }) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Zmień", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    conservators.forEach { conservator ->
                        val isSelected = selectedConservator == conservator
                        val bgColor =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f,
                                )
                            }
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .clickable { selectedConservator = conservator }
                                    .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Column {
                                    Text(
                                        text = "${conservator.firstName} ${conservator.lastName}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = contentColor,
                                    )
                                    Text(
                                        text = "Specjalista ds. usterek",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color =
                                            if (isSelected) {
                                                contentColor.copy(
                                                    alpha = 0.8f,
                                                )
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Wybrano",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        selectedConservator?.let {
                            onAssign(it, "$formattedDate o $formattedTime")
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    enabled = selectedConservator != null,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = "Zatwierdź i powiadom",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = dateState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        // W Jetpack Compose Material 3 nie ma wbudowanego TimePickerDialog, zazwyczaj to AlertDialog z TimePickerem w środku,
        // Ale tu zbudujemy prosty custom dialog oparty o androidx.compose.material3.AlertDialog
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timeState.hour
                    selectedMinute = timeState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timeState)
                }
            },
        )
    }
}
