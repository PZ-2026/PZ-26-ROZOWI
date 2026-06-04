package pl.edu.ur.blokur.ui.views.tickets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dagger.hilt.android.EntryPointAccessors
import pl.edu.ur.blokur.BuildConfig
import pl.edu.ur.blokur.di.CoilEntryPoint

/**
 * Miniatura zdjęcia zgłoszenia — GET /api/images/{id} z nagłówkiem JWT (OkHttp main).
 */
@Composable
fun TicketImageThumbnail(
    imageId: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Zdjęcie zgłoszenia"
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, CoilEntryPoint::class.java)
            .imageLoader()
    }
    val imageUrl = remember(imageId) {
        "${BuildConfig.BACKEND_URL.removeSuffix("/")}/api/images/$imageId"
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.BrokenImage,
                    contentDescription = "Błąd ładowania zdjęcia",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    )
}
