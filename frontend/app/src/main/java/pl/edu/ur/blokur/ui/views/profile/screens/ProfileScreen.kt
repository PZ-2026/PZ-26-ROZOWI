package pl.edu.ur.blokur.ui.views.profile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.profile.contents.ProfileContent
import pl.edu.ur.blokur.ui.views.profile.utils.ProfileEvent
import pl.edu.ur.blokur.ui.views.profile.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToCommunityLogo: () -> Unit = {},
    onNavigateToDocumentDistribution: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    var isManager by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isManager = viewModel.isManager()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ProfileEvent.ShowSaveDialog -> showDialog = true
                is ProfileEvent.SaveSuccess -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        ProfileContent(
            state = state,
            showSaveDialog = showDialog,
            onNameChanged = viewModel::onNameChanged,
            onRequestSave = viewModel::requestSave,
            onConfirmSave = {
                showDialog = false
                viewModel.confirmSave()
            },
            onDismissDialog = { showDialog = false },
            onSendNotification = viewModel::sendTestNotification,
            isManager = isManager,
            onNavigateToNotificationSettings = onNavigateToNotificationSettings,
            onNavigateToCommunityLogo = onNavigateToCommunityLogo,
            onNavigateToDocumentDistribution = onNavigateToDocumentDistribution,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        )
    }
}