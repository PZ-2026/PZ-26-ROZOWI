package pl.edu.ur.blokur.ui.screens.tickets.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import pl.edu.ur.blokur.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConservatorActionSheet(
    actionType: String, // "START", "FINISH", "PAUSE_OR_COMMENT"
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var comment by remember { mutableStateOf("") }
    var pauseTicket by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        val validUris = uris.take(3 - selectedImages.size)
        selectedImages = selectedImages + validUris
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (actionType) {
                "START" -> {
                    Text(
                        text = "Rozpoczęcie realizacji",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Czy na pewno chcesz rozpocząć pracę nad tym zgłoszeniem? Status zmieni się na 'W realizacji'.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Rozpocznij pracę", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                "FINISH" -> {
                    Text(
                        text = "Zakończenie pracy",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Opisz wykonane naprawy i opcjonalnie dodaj zdjęcia dokumentujące.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Np. Wymieniono uszkodzony zawór ciepłej wody w szachcie. Przetestowano ciśnienie - brak przecieków.") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    PhotoUploaderMock(
                        selectedImages = selectedImages,
                        onAddClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveImage = { uri -> selectedImages = selectedImages - uri }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = comment.isNotBlank()
                    ) {
                        Text("Zakończ usterkę", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                "PAUSE_OR_COMMENT" -> {
                    Text(
                        text = "Komentarz / Wstrzymanie",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wstrzymaj zgłoszenie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Zmień status na 'Wstrzymano' (np. brak części, lokatora nie ma w domu)", 
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = pauseTicket,
                            onCheckedChange = { pauseTicket = it }
                        )
                    }

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("Dodaj treść komentarza...") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    PhotoUploaderMock(
                        selectedImages = selectedImages,
                        onAddClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveImage = { uri -> selectedImages = selectedImages - uri }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = comment.isNotBlank()
                    ) {
                        Text("Dodaj wpis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PhotoUploaderMock(
    selectedImages: List<Uri>,
    onAddClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Zarłączniki (opcjonalnie)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedImages.size < 3) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Dodaj zdjęcie", tint = MaterialTheme.colorScheme.primary)
                }
            }

            selectedImages.forEach { uri ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = "Miniaturka",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                    IconButton(
                        onClick = { onRemoveImage(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .padding(2.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Usuń", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
