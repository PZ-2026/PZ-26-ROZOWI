package pl.edu.ur.blokur.ui.views.announcements.screens

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import pl.edu.ur.blokur.ui.views.announcements.contents.CreateAnnouncementContent
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.CreateAnnouncementState
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.EditAnnouncementEvent
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.EditAnnouncementViewModel

@Composable
fun EditAnnouncementScreen(
    viewModel: EditAnnouncementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val uiState = CreateAnnouncementState(
        title = editState.title,
        content = editState.content,
        attachmentUri = editState.attachmentUri,
        attachmentName = editState.attachmentName ?: if (editState.hasExistingAttachment) "Istniejący załącznik PDF" else null,
        isSubmitting = editState.isSubmitting
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditAnnouncementEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is EditAnnouncementEvent.Success -> onNavigateBack()
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val name = cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) it.getString(index) else null
                } else null
            } ?: "attachment.pdf"
            viewModel.onAttachmentSelected(uri, name)
        }
    }

    CreateAnnouncementContent(
        state = uiState,
        snackbarHostState = snackbarHostState,
        screenTitle = "Edytuj ogłoszenie",
        submitLabel = "Zapisz zmiany",
        onTitleChange = viewModel::onTitleChange,
        onContentChange = viewModel::onContentChange,
        onPickAttachment = { filePickerLauncher.launch("application/pdf") },
        onRemoveAttachment = viewModel::removeAttachment,
        onSubmit = viewModel::submit,
        onNavigateBack = onNavigateBack,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
    )
}
