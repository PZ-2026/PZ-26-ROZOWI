package pl.edu.ur.blokur.ui.views.resolutions.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.ResolutionDetailDto
import pl.edu.ur.blokur.dtos.ResolutionOptionDto
import pl.edu.ur.blokur.dtos.ResolutionOptionResultDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.ResolutionDetailState
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.ResolutionDetailViewModel
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.ResolutionEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolutionDetailScreen(
    viewModel: ResolutionDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ResolutionEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Uchwała", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        (state as? ResolutionDetailState.Success)?.let {
                            Text(
                                if (it.detail.isActive) "Głosowanie aktywne" else "Głosowanie zakończone",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (it.detail.isActive) Color(0xFF2E7D32)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Wróć")
                    }
                },
                actions = {
                    val s = state as? ResolutionDetailState.Success
                    if (s?.isManager == true && !s.detail.isActive) {
                        IconButton(
                            onClick = viewModel::downloadReport,
                            enabled = !s.isDownloadingReport
                        ) {
                            AnimatedContent(s.isDownloadingReport, label = "dl") { loading ->
                                if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                else Icon(Icons.Rounded.Download, "Pobierz raport PDF",
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        when (val s = state) {
            is ResolutionDetailState.Loading -> LoadingIndicator()
            is ResolutionDetailState.Error -> EmptyState(title = "Błąd", description = s.message)
            is ResolutionDetailState.Success -> ResolutionDetailContent(
                detail = s.detail,
                selectedOptionId = s.selectedOptionId,
                isVoting = s.isVoting,
                hasVoted = s.hasVoted,
                isManager = s.isManager,
                onOptionSelected = viewModel::selectOption,
                onVote = viewModel::castVote,
                onDownloadReport = viewModel::downloadReport,
                isDownloadingReport = s.isDownloadingReport,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// ── Zawartość ekranu szczegółów ───────────────────────────────────────────────

@Composable
private fun ResolutionDetailContent(
    detail: ResolutionDetailDto,
    selectedOptionId: String?,
    isVoting: Boolean,
    hasVoted: Boolean,
    isManager: Boolean,
    onOptionSelected: (String) -> Unit,
    onVote: () -> Unit,
    onDownloadReport: () -> Unit,
    isDownloadingReport: Boolean,
    modifier: Modifier = Modifier
) {
    val showResults = isManager || hasVoted || !detail.isActive
    val showVoting = !isManager && !hasVoted && detail.isActive

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // ── Nagłówek ─────────────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(detail.title, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text(detail.description, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider()
                    val endLabel = try {
                        "Głosowanie do: ${pl.edu.ur.blokur.ui.utils.PolishFormat.formatDate(detail.endDate)}"
                    } catch (_: Exception) { "Do: ${detail.endDate}" }
                    Text(endLabel, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    detail.authorName?.let {
                        Text("Autor: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // ── Sekcja głosowania (mieszkaniec, aktywne, niegłosował) ─────────────
        if (showVoting) {
            item {
                Text("Wybierz opcję", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            items(detail.options) { option ->
                VotingOptionRow(
                    option = option,
                    isSelected = selectedOptionId == option.id,
                    isEnabled = !isVoting && !hasVoted,
                    onSelect = { onOptionSelected(option.id) }
                )
            }
            item {
                if (hasVoted) {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Twój głos został zapisany", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onVote,
                        enabled = selectedOptionId != null && !isVoting,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        AnimatedContent(isVoting, label = "vote") { voting ->
                            if (voting) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary)
                                    Text("Zapisuję głos...")
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Rounded.HowToVote, null, modifier = Modifier.size(18.dp))
                                    Text("Oddaj głos", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Wyniki głosowania ─────────────────────────────────────────────────
        if (showResults && !detail.results.isNullOrEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null,
                            tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                        Text(
                            if (hasVoted) "Twój głos został zapisany — wyniki na żywo"
                            else "Wyniki głosowania",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text("Łączna liczba głosów: ${detail.totalVotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(detail.results!!) { result ->
                ResultProgressRow(result = result, totalVotes = detail.totalVotes)
            }
        }

        // ── Raport PDF (zarządca, zakończone) ─────────────────────────────────
        if (isManager && !detail.isActive) {
            item {
                OutlinedButton(
                    onClick = onDownloadReport,
                    enabled = !isDownloadingReport,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    AnimatedContent(isDownloadingReport, label = "report") { loading ->
                        if (loading) Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("Generuję raport...")
                        }
                        else Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
                            Text("Pobierz raport PDF", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ── Wiersz opcji głosowania ───────────────────────────────────────────────────

@Composable
private fun VotingOptionRow(
    option: ResolutionOptionDto,
    isSelected: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                1.5.dp,
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = isEnabled, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            option.optionText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── Pasek postępu wyników ─────────────────────────────────────────────────────

@Composable
private fun ResultProgressRow(
    result: ResolutionOptionResultDto,
    totalVotes: Long
) {
    val percent = if (totalVotes > 0) result.votesCount.toFloat() / totalVotes.toFloat() else 0f
    val animatedPercent by animateFloatAsState(targetValue = percent, label = "progress")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(result.optionText, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(
                "${result.votesCount} głosów (${(percent * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { animatedPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
