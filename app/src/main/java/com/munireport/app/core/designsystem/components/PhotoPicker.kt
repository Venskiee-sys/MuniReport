package com.munireport.app.core.designsystem.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun PhotoPicker(
    selected: List<Uri>,
    onAdd: (List<Uri>) -> Unit,
    onRemove: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    maxPhotos: Int = 3,
) {
    val remaining = (maxPhotos - selected.size).coerceAtLeast(1)
    val multipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = remaining),
    ) { uris ->
        if (uris.isNotEmpty()) onAdd(uris.take(remaining))
    }
    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onAdd(listOf(uri))
    }

    fun launchPicker() {
        if (maxPhotos == 1) {
            singleLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        } else {
            multipleLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
    }

    if (maxPhotos == 1) {
        if (selected.isEmpty()) {
            Surface(
                onClick = ::launchPicker,
                modifier = modifier
                    .fillMaxWidth()
                    .height(134.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp),
                    ),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Add a photo of the concern",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "One clear photo helps the city team respond faster",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            val uri = selected.first()
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(196.dp),
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Attached photo, tap to replace",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = ::launchPicker),
                )
                Surface(
                    onClick = ::launchPicker,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                ) {
                    Text(
                        text = "Replace photo",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            CircleShape,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove photo",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        return
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        selected.forEach { uri ->
            Box(modifier = Modifier.size(92.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Attached photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(10.dp)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove photo",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        if (selected.size < maxPhotos) {
            Surface(
                onClick = ::launchPicker,
                modifier = Modifier
                    .size(92.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(13.dp),
                    ),
                shape = RoundedCornerShape(13.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(23.dp),
                        )
                        Spacer(Modifier.size(5.dp))
                        Text(
                            text = "Add photo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PhotoStrip(
    urls: List<String>,
    modifier: Modifier = Modifier,
    imageSize: Int = 220,
) {
    if (urls.isEmpty()) return
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(urls.size) { index ->
            AsyncImage(
                model = urls[index],
                contentDescription = "Complaint photo ${index + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = imageSize.dp, height = 150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
fun DocumentPreview(
    uri: String?,
    fileName: String?,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val isPdf = fileName?.endsWith(".pdf", ignoreCase = true) == true ||
        uri?.contains(".pdf", ignoreCase = true) == true
    Surface(
        modifier = if (isPdf && !uri.isNullOrBlank()) {
            modifier.clickable { runCatching { uriHandler.openUri(uri) } }
        } else {
            modifier
        },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (uri.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 112.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No document",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (isPdf) {
            Column(
                modifier = Modifier
                    .size(width = 180.dp, height = 120.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
                Spacer(Modifier.size(7.dp))
                Text(
                    text = "Open PDF",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (fileName != null) {
                    Spacer(Modifier.size(2.dp))
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        } else {
            AsyncImage(
                model = uri,
                contentDescription = fileName ?: "Verification document",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 180.dp, height = 120.dp),
            )
        }
    }
}
