package com.munireport.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munireport.app.core.designsystem.colors.MuniBlue
import com.munireport.app.core.designsystem.colors.MuniBlue100
import com.munireport.app.core.designsystem.colors.MuniRed
import com.munireport.app.core.utils.MuntinlupaGeo
import kotlin.math.roundToInt

/**
 * An offline-friendly illustrated map for selecting a report location.
 *
 * Google Maps is intentionally not required: this keeps the demo and offline
 * flows usable without an API key while still providing a draggable map/pin
 * interaction and a municipal geofence check.
 */
@Composable
fun MuniMapPicker(
    latitude: Double?,
    longitude: Double?,
    onCoordinateChange: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    height: Dp = 188.dp,
    instruction: String? = null,
) {
    val hasPin = latitude != null && longitude != null
    var pin by remember {
        mutableStateOf(Offset(0.56f, 0.50f))
    }
    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            val x = ((longitude - MuntinlupaGeo.MIN_LONGITUDE) /
                (MuntinlupaGeo.MAX_LONGITUDE - MuntinlupaGeo.MIN_LONGITUDE))
                .toFloat().coerceIn(0.05f, 0.95f)
            val y = (1f - ((latitude - MuntinlupaGeo.MIN_LATITUDE) /
                (MuntinlupaGeo.MAX_LATITUDE - MuntinlupaGeo.MIN_LATITUDE))
                .toFloat()).coerceIn(0.07f, 0.93f)
            pin = Offset(x, y)
        }
    }

    val mapBackground = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF17243C)
    } else {
        Color(0xFFEAF0F8)
    }
    val water = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF163C55)
    } else {
        Color(0xFFC9E4F2)
    }
    val park = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF1D3B31)
    } else {
        Color(0xFFD8EADB)
    }
    val block = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF22304A)
    } else {
        Color(0xFFF8FAFD)
    }
    val road = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF34435E)
    } else {
        Color.White
    }
    val roadEdge = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF2A3A55)
    } else {
        Color(0xFFD8E0EC)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .background(mapBackground)
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            val heightPx = size.height.toFloat().coerceAtLeast(1f)
                            fun place(position: Offset) {
                                val x = (position.x / width).coerceIn(0.04f, 0.96f)
                                val y = (position.y / heightPx).coerceIn(0.08f, 0.92f)
                                pin = Offset(x, y)
                                val longitude = MuntinlupaGeo.MIN_LONGITUDE +
                                    x * (MuntinlupaGeo.MAX_LONGITUDE - MuntinlupaGeo.MIN_LONGITUDE)
                                val latitude = MuntinlupaGeo.MAX_LATITUDE -
                                    y * (MuntinlupaGeo.MAX_LATITUDE - MuntinlupaGeo.MIN_LATITUDE)
                                onCoordinateChange(latitude, longitude)
                            }
                            place(down.position)
                            down.consume()
                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change == null || !change.pressed) {
                                    pressed = false
                                } else {
                                    place(change.position)
                                    change.consume()
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawMapBase(mapBackground, water, park, block, road, roadEdge)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Muntinlupa City",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(
                    x = maxWidth * pin.x - 20.dp,
                    y = maxHeight * pin.y - 40.dp,
                )
                .size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = if (hasPin) {
                    "Pinned report location"
                } else {
                    "Unpinned location"
                },
                tint = if (hasPin) MuniRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        }

        if (interactive) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 1.dp,
            ) {
                Text(
                    text = instruction ?: if (hasPin) {
                        "Tap or drag the map to adjust the pin"
                    } else {
                        "Tap the map to pin the exact location"
                    },
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun MuniMapPreview(
    latitude: Double?,
    longitude: Double?,
    modifier: Modifier = Modifier,
    label: String = "Report location",
) {
    MuniMapPicker(
        latitude = latitude,
        longitude = longitude,
        onCoordinateChange = { _, _ -> },
        modifier = modifier,
        interactive = false,
        height = 156.dp,
        instruction = label,
    )
}

private fun DrawScope.drawMapBase(
    background: Color,
    water: Color,
    park: Color,
    block: Color,
    road: Color,
    roadEdge: Color,
) {
    drawRect(background)

    // Laguna de Bay on the eastern edge.
    val bay = Path().apply {
        moveTo(size.width * 0.86f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(size.width * 0.90f, size.height)
        cubicTo(
            size.width * 0.82f,
            size.height * 0.66f,
            size.width * 0.95f,
            size.height * 0.34f,
            size.width * 0.86f,
            0f,
        )
        close()
    }
    drawPath(bay, water)

    // Parks and neighbourhood blocks.
    drawRoundRect(
        color = park,
        topLeft = Offset(size.width * 0.08f, size.height * 0.59f),
        size = Size(size.width * 0.18f, size.height * 0.20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
    )
    drawRoundRect(
        color = park,
        topLeft = Offset(size.width * 0.63f, size.height * 0.10f),
        size = Size(size.width * 0.13f, size.height * 0.18f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
    )

    val buildings = listOf(
        Offset(0.05f, 0.10f) to Size(0.14f, 0.13f),
        Offset(0.24f, 0.06f) to Size(0.18f, 0.15f),
        Offset(0.47f, 0.08f) to Size(0.12f, 0.11f),
        Offset(0.67f, 0.34f) to Size(0.15f, 0.14f),
        Offset(0.05f, 0.31f) to Size(0.15f, 0.12f),
        Offset(0.28f, 0.37f) to Size(0.14f, 0.10f),
        Offset(0.46f, 0.31f) to Size(0.14f, 0.16f),
        Offset(0.26f, 0.66f) to Size(0.13f, 0.12f),
        Offset(0.44f, 0.63f) to Size(0.16f, 0.16f),
        Offset(0.66f, 0.56f) to Size(0.12f, 0.12f),
        Offset(0.34f, 0.84f) to Size(0.17f, 0.09f),
        Offset(0.56f, 0.84f) to Size(0.16f, 0.09f),
        Offset(0.05f, 0.85f) to Size(0.14f, 0.08f),
    )
    buildings.forEach { (fraction, sizeFraction) ->
        drawRoundRect(
            color = block,
            topLeft = Offset(size.width * fraction.x, size.height * fraction.y),
            size = Size(size.width * sizeFraction.width, size.height * sizeFraction.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        )
    }

    // Main roads with a soft edge underneath.
    fun roadLine(points: List<Offset>, width: Float) {
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, roadEdge, style = Stroke(width = width + 5f))
        drawPath(path, road, style = Stroke(width = width))
    }

    val mainWidth = size.minDimension * 0.055f
    roadLine(
        listOf(
            Offset(0f, size.height * 0.50f),
            Offset(size.width * 0.58f, size.height * 0.47f),
            Offset(size.width * 0.87f, size.height * 0.54f),
        ),
        mainWidth,
    )
    roadLine(
        listOf(
            Offset(size.width * 0.44f, 0f),
            Offset(size.width * 0.42f, size.height * 0.40f),
            Offset(size.width * 0.51f, size.height),
        ),
        mainWidth * 0.9f,
    )
    roadLine(
        listOf(
            Offset(0f, size.height * 0.78f),
            Offset(size.width * 0.52f, size.height * 0.74f),
            Offset(size.width * 0.86f, size.height * 0.82f),
        ),
        mainWidth * 0.68f,
    )
    roadLine(
        listOf(
            Offset(size.width * 0.18f, 0f),
            Offset(size.width * 0.20f, size.height * 0.44f),
            Offset(size.width * 0.10f, size.height),
        ),
        mainWidth * 0.55f,
    )

    // A small civic plaza marker.
    drawCircle(
        color = MuniBlue.copy(alpha = 0.22f),
        radius = size.minDimension * 0.045f,
        center = Offset(size.width * 0.57f, size.height * 0.49f),
    )
    drawCircle(
        color = MuniBlue,
        radius = size.minDimension * 0.015f,
        center = Offset(size.width * 0.57f, size.height * 0.49f),
    )
}
