package com.realmirror.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.realmirror.app.ui.theme.AccentCyan

/**
 * White shutter button with subtle press animation.
 */
@Composable
fun ShutterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "shutter_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) AccentCyan else Color.White,
        label = "shutter_border"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(3.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size - 10.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Red record/stop button with pulsing animation when recording.
 */
@Composable
fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "record_scale"
    )

    val innerColor by animateColorAsState(
        targetValue = if (isRecording) Color(0xFFFF1744) else Color(0xFFFF1744).copy(alpha = 0.85f),
        label = "record_color"
    )

    val innerSize = if (isRecording) size - 22.dp else size - 10.dp
    val innerSizeAnim by animateFloatAsState(
        targetValue = if (isRecording) (size - 22.dp).value else (size - 10.dp).value,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "inner_size"
    )

    val innerShape = if (isRecording) androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    else CircleShape

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(3.dp, Color(0xFFFF1744), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSizeAnim.dp)
                .clip(if (isRecording) androidx.compose.foundation.shape.RoundedCornerShape(6.dp) else CircleShape)
                .background(innerColor)
        )
    }
}
