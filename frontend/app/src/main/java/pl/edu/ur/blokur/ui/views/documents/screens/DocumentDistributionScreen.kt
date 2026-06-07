package pl.edu.ur.blokur.ui.views.documents.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Announcement
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.documents.viewmodels.DocDistributionEvent
import pl.edu.ur.blokur.ui.views.documents.viewmodels.DocDistributionTab
import pl.edu.ur.blokur.ui.views.documents.viewmodels.DocDistributionViewModel
import pl.edu.ur.blokur.ui.views.documents.viewmodels.RecipientScope
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext

/**
 * Ekran dystrybucji dokumentów zarządcy:
 * - Tab 1: Zawiadomienie o zmianie stawek
 * - Tab 2: Rozliczenie roczne
 *
 * Rola: tylko ZARZADCA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDistributionScreen(
    viewModel: DocDistributionViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DocDistributionEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Dystrybucja dokumentów",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Zawiadomienia i rozliczenia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Przełącznik zakładek ────────────────────────────────────────────
            TabRow(selectedTabIndex = state.activeTab.ordinal) {
                Tab(
                    selected = state.activeTab == DocDistributionTab.RATE_CHANGE,
                    onClick = { viewModel.selectTab(DocDistributionTab.RATE_CHANGE) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Announcement,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Zmiana stawek")
                        }
                    }
                )
                Tab(
                    selected = state.activeTab == DocDistributionTab.ANNUAL_SETTLEMENT,
                    onClick = { viewModel.selectTab(DocDistributionTab.ANNUAL_SETTLEMENT) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AccountBalance,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Rozliczenie")
                        }
                    }
                )
            }

            // ── Zawartość aktywnej zakładki ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (state.activeTab) {
                    DocDistributionTab.RATE_CHANGE -> {
                        RateChangeFormCard(
                            subject = state.rateChangeSubject,
                            body = state.rateChangeBody,
                            effectiveDate = state.rateChangeEffectiveDate,
                            recipientScope = state.recipientScope,
                            targetId = state.targetId,
                            isSubmitting = state.isSubmitting,
                            isSent = state.lastSentTab == DocDistributionTab.RATE_CHANGE,
                            onSubjectChanged = viewModel::onRateChangeSubjectChanged,
                            onBodyChanged = viewModel::onRateChangeBodyChanged,
                            onEffectiveDateChanged = viewModel::onRateChangeEffectiveDateChanged,
                            onScopeChanged = viewModel::onRecipientScopeChanged,
                            onTargetIdChanged = viewModel::onTargetIdChanged,
                            onBuildingSelected = viewModel::onBuildingSelected,
                            onApartmentSelected = viewModel::onApartmentSelected,
                            selectedBuildingId = state.selectedBuildingId,
                            selectedApartmentId = state.selectedApartmentId,
                            buildingTree = state.buildingTree,
                            onSend = viewModel::sendRateChange
                        )
                    }
                    DocDistributionTab.ANNUAL_SETTLEMENT -> {
                        AnnualSettlementFormCard(
                            year = state.settlementYear,
                            note = state.settlementNote,
                            recipientScope = state.recipientScope,
                            targetId = state.targetId,
                            isSubmitting = state.isSubmitting,
                            isSent = state.lastSentTab == DocDistributionTab.ANNUAL_SETTLEMENT,
                            onYearChanged = viewModel::onSettlementYearChanged,
                            onNoteChanged = viewModel::onSettlementNoteChanged,
                            onScopeChanged = viewModel::onRecipientScopeChanged,
                            onTargetIdChanged = viewModel::onTargetIdChanged,
                            onBuildingSelected = viewModel::onBuildingSelected,
                            onApartmentSelected = viewModel::onApartmentSelected,
                            selectedBuildingId = state.selectedBuildingId,
                            selectedApartmentId = state.selectedApartmentId,
                            buildingTree = state.buildingTree,
                            onSend = viewModel::sendAnnualSettlement
                        )
                    }
                }
            }
        }
    }
}



// ── Formularz: Zawiadomienie o zmianie stawek ─────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RateChangeFormCard(
    subject: String,
    body: String,
    effectiveDate: String,
    recipientScope: RecipientScope,
    targetId: String,
    isSubmitting: Boolean,
    isSent: Boolean,
    onSubjectChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onEffectiveDateChanged: (String) -> Unit,
    onScopeChanged: (RecipientScope) -> Unit,
    onTargetIdChanged: (String) -> Unit,
    onBuildingSelected: (String) -> Unit,
    onApartmentSelected: (String) -> Unit,
    selectedBuildingId: String?,
    selectedApartmentId: String?,
    buildingTree: List<pl.edu.ur.blokur.dtos.BuildingTreeNodeDto>,
    onSend: () -> Unit
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        val formatted = String.format(
                            java.util.Locale.US,
                            "%04d-%02d-%02d",
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                        onEffectiveDateChanged(formatted)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Announcement,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Zawiadomienie o zmianie stawek",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider()

            OutlinedTextField(
                value = subject,
                onValueChange = onSubjectChanged,
                label = { Text("Temat zawiadomienia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = effectiveDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data wejścia w życie (YYYY-MM-DD)") },
                    singleLine = true,
                    placeholder = { Text("np. 2025-02-01") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Rounded.DateRange, "Wybierz datę")
                        }
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            OutlinedTextField(
                value = body,
                onValueChange = onBodyChanged,
                label = { Text("Treść zawiadomienia") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            RecipientScopeSelector(
                scope = recipientScope,
                targetId = targetId,
                selectedBuildingId = selectedBuildingId,
                selectedApartmentId = selectedApartmentId,
                buildingTree = buildingTree,
                onScopeChanged = onScopeChanged,
                onTargetIdChanged = onTargetIdChanged,
                onBuildingSelected = onBuildingSelected,
                onApartmentSelected = onApartmentSelected
            )

            SendButton(
                isSubmitting = isSubmitting,
                isSent = isSent,
                isEnabled = subject.isNotBlank() && body.isNotBlank() && effectiveDate.matches(
                    Regex("^\\d{4}-\\d{2}-\\d{2}$")
                ) && !isSubmitting && (recipientScope == RecipientScope.ALL || targetId.isNotBlank()),
                onClick = onSend
            )
        }
    }
}

// ── Formularz: Rozliczenie roczne ─────────────────────────────────────────────

@Composable
private fun AnnualSettlementFormCard(
    year: String,
    note: String,
    recipientScope: RecipientScope,
    targetId: String,
    isSubmitting: Boolean,
    isSent: Boolean,
    onYearChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onScopeChanged: (RecipientScope) -> Unit,
    onTargetIdChanged: (String) -> Unit,
    onBuildingSelected: (String) -> Unit,
    onApartmentSelected: (String) -> Unit,
    selectedBuildingId: String?,
    selectedApartmentId: String?,
    buildingTree: List<pl.edu.ur.blokur.dtos.BuildingTreeNodeDto>,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.AccountBalance,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Rozliczenie roczne",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider()

            OutlinedTextField(
                value = year,
                onValueChange = { if (it.length <= 4) onYearChanged(it.filter { c -> c.isDigit() }) },
                label = { Text("Rok rozliczeniowy") },
                singleLine = true,
                placeholder = { Text("np. 2024") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                label = { Text("Uwagi / dodatkowe informacje (opcjonalnie)") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            RecipientScopeSelector(
                scope = recipientScope,
                targetId = targetId,
                selectedBuildingId = selectedBuildingId,
                selectedApartmentId = selectedApartmentId,
                buildingTree = buildingTree,
                onScopeChanged = onScopeChanged,
                onTargetIdChanged = onTargetIdChanged,
                onBuildingSelected = onBuildingSelected,
                onApartmentSelected = onApartmentSelected
            )

            SendButton(
                isSubmitting = isSubmitting,
                isSent = isSent,
                isEnabled = year.length == 4 && year.toIntOrNull() in 1990..2100 && !isSubmitting && (recipientScope == RecipientScope.ALL || targetId.isNotBlank()),
                onClick = onSend
            )
        }
    }
}

// ── Selektor zakresu odbiorców ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipientScopeSelector(
    scope: RecipientScope,
    targetId: String,
    selectedBuildingId: String?,
    selectedApartmentId: String?,
    buildingTree: List<pl.edu.ur.blokur.dtos.BuildingTreeNodeDto>,
    onScopeChanged: (RecipientScope) -> Unit,
    onTargetIdChanged: (String) -> Unit,
    onBuildingSelected: (String) -> Unit,
    onApartmentSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Adresaci",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Column(modifier = Modifier.selectableGroup()) {
            RecipientScope.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (scope == option)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .selectable(
                            selected = scope == option,
                            onClick = { onScopeChanged(option) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = scope == option,
                        onClick = null
                    )
                    Icon(
                        when (option) {
                            RecipientScope.ALL -> Icons.Rounded.Group
                            RecipientScope.BUILDING -> Icons.Rounded.Home
                            RecipientScope.APARTMENT -> Icons.Rounded.Home
                        },
                        null,
                        tint = if (scope == option) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = when (option) {
                                RecipientScope.ALL -> "Wszyscy mieszkańcy"
                                RecipientScope.BUILDING -> "Budynek"
                                RecipientScope.APARTMENT -> "Lokal (pojedynczy)"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (scope == option) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (scope == option && option != RecipientScope.ALL) {
                            Text(
                                text = when (option) {
                                    RecipientScope.BUILDING -> "Wpisz ID budynku"
                                    else -> "Wpisz ID lokalu"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (scope != RecipientScope.ALL) {
            var expandedBuilding by remember { mutableStateOf(false) }
            val selectedBuildingName = buildingTree.find { it.id == selectedBuildingId }?.name ?: ""

            ExposedDropdownMenuBox(
                expanded = expandedBuilding,
                onExpandedChange = { expandedBuilding = !expandedBuilding }
            ) {
                OutlinedTextField(
                    value = selectedBuildingName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Wybierz budynek") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBuilding) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedBuilding,
                    onDismissRequest = { expandedBuilding = false }
                ) {
                    buildingTree.forEach { building ->
                        DropdownMenuItem(
                            text = { Text(building.name) },
                            onClick = {
                                onBuildingSelected(building.id)
                                expandedBuilding = false
                            }
                        )
                    }
                }
            }

            if (scope == RecipientScope.APARTMENT && selectedBuildingId != null) {
                var expandedApartment by remember { mutableStateOf(false) }
                val selectedBuilding = buildingTree.find { it.id == selectedBuildingId }
                val apartments = selectedBuilding?.staircases?.flatMap { it.apartments } ?: emptyList()
                val selectedApartmentNum = apartments.find { it.id == selectedApartmentId }?.number ?: ""

                ExposedDropdownMenuBox(
                    expanded = expandedApartment,
                    onExpandedChange = { expandedApartment = !expandedApartment }
                ) {
                    OutlinedTextField(
                        value = selectedApartmentNum,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wybierz lokal") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedApartment) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedApartment,
                        onDismissRequest = { expandedApartment = false }
                    ) {
                        apartments.forEach { apt ->
                            DropdownMenuItem(
                                text = { Text("Lokal nr ${apt.number}") },
                                onClick = {
                                    onApartmentSelected(apt.id)
                                    expandedApartment = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Przycisk wysyłki ───────────────────────────────────────────────────────────

@Composable
private fun SendButton(
    isSubmitting: Boolean,
    isSent: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        AnimatedContent(
            targetState = when {
                isSent -> "sent"
                isSubmitting -> "loading"
                else -> "idle"
            },
            label = "send_btn"
        ) { s ->
            when (s) {
                "sent" -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Text("Wysłano!", fontWeight = FontWeight.Bold)
                }
                "loading" -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("Wysyłam...")
                }
                else -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Wyślij dokument", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
