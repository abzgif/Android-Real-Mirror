package com.realmirror.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.realmirror.app.ui.theme.AccentCyan
import com.realmirror.app.ui.theme.GlassBorder
import com.realmirror.app.ui.theme.SurfaceGlass

/**
 * Modern custom slim vertical brightness slider.
 * Extremely thin visuals but wide, responsive touch-target area.
 */
@Composable
fun BrightnessSlider(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    var containerHeight by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .width(36.dp)
            .height(240.dp)
            .clip(shape)
            .background(SurfaceGlass)
            .border(1.dp, GlassBorder, shape)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top icon: full sun (high brightness)
        Icon(
            imageVector = Icons.Rounded.LightMode,
            contentDescription = "Max brightness",
            tint = AccentCyan.copy(alpha = 0.9f),
            modifier = Modifier.size(16.dp)
        )

        // Slim vertical custom slider track (occupies the vertical space between icons)
        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp) // The entire width of the slider is a touch zone
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (containerHeight > 0f) {
                            val fraction = 1f - (down.position.y / containerHeight)
                            onBrightnessChange(fraction.coerceIn(0.01f, 1.0f))
                        }
                        
                        var dragPointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val dragEvent = event.changes.firstOrNull { it.id == dragPointerId } ?: break
                            if (dragEvent.pressed) {
                                if (containerHeight > 0f) {
                                    val fraction = 1f - (dragEvent.position.y / containerHeight)
                                    onBrightnessChange(fraction.coerceIn(0.01f, 1.0f))
                                }
                                dragEvent.consume()
                            } else {
                                break
                            }
                        }
                    }
                }
                .onGloballyPositioned { coordinates ->
                    containerHeight = coordinates.size.height.toFloat()
                }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Track background (thin track)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            )

            // Active track fill (height proportional to brightness)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(brightness)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentCyan)
            )

            // Thumb box: fillMaxHeight(brightness) to place thumb at the active track boundary
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight(brightness),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, AccentCyan, CircleShape)
                )
            }
        }

        // Bottom icon: dim (low brightness)
        Icon(
            imageVector = Icons.Rounded.NightlightRound,
            contentDescription = "Min brightness",
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(14.dp)
        )
    }
}
