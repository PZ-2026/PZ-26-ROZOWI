package pl.edu.ur.blokur.ui.views.tickets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dagger.hilt.android.EntryPointAccessors
import pl.edu.ur.blokur.BuildConfig
import pl.edu.ur.blokur.di.CoilEntryPoint
import pl.edu.ur.blokur.dtos.TicketImageDto

@Composable
fun TicketImageGalleryDialog(
    images: List<TicketImageDto>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    if (images.isEmpty()) return

    var currentIndex by remember(initialIndex) {
        mutableIntStateOf(initialIndex.coerceIn(0, images.lastIndex))
    }
    val currentImage = images[currentIndex]
    val context = LocalContext.current
    val imageLoader = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, CoilEntryPoint::class.java)
            .imageLoader()
    }
    val imageUrl = remember(currentImage.id) {
        "${BuildConfig.BACKEND_URL.removeSuffix("/")}/api/images/${currentImage.id}"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = currentImage.originalFilename ?: "Zdjęcie zgłoszenia",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                },
                error = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.BrokenImage,
                            contentDescription = "Błąd ładowania zdjęcia",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Zamknij", tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    currentImage.originalFilename ?: "Zdjęcie",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${currentIndex + 1} / ${images.size}",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentIndex = if (currentIndex == 0) images.lastIndex else currentIndex - 1
                        },
                        enabled = images.size > 1
                    ) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Poprzednie", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            currentIndex = if (currentIndex == images.lastIndex) 0 else currentIndex + 1
                        },
                        enabled = images.size > 1
                    ) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Następne", tint = Color.White)
                    }
                }
            }
        }
    }
}
