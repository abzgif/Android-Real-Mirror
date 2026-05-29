package com.realmirror.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realmirror.app.ui.theme.AccentCyan
import com.realmirror.app.ui.theme.AccentPurple
import com.realmirror.app.ui.theme.GlassBorder
import com.realmirror.app.ui.theme.SurfaceGlass

/**
 * A custom pill-shaped toggle between "MIRROR" and "REAL" modes.
 * isMirror = true → left pill active (cyan)
 * isMirror = false → right pill active (purple)
 */
@Composable
fun MirrorRealToggle(
    isMirrorMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillWidth = 80.dp
    val pillHeight = 36.dp
    val toggleWidth = pillWidth * 2
    val shape = RoundedCornerShape(50)

    val activeColor by animateColorAsState(
        targetValue = if (isMirrorMode) AccentCyan else AccentPurple,
        label = "toggle_color"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (isMirrorMode) 0.dp else pillWidth,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "thumb_offset"
    )

    Box(
        modifier = modifier
            .width(toggleWidth)
            .height(pillHeight)
            .clip(shape)
            .background(SurfaceGlass)
            .border(1.dp, GlassBorder, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        // Active thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(pillWidth)
                .fillMaxHeight()
                .clip(shape)
                .background(activeColor.copy(alpha = 0.25f))
                .border(1.dp, activeColor.copy(alpha = 0.7f), shape)
        )

        // Labels row
        Row(
            modifier = Modifier
                .width(toggleWidth)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(pillWidth).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MIRROR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMirrorMode) AccentCyan else Color.White.copy(alpha = 0.5f)
                )
            }
            Box(
                modifier = Modifier.width(pillWidth).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "REAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isMirrorMode) AccentPurple else Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
