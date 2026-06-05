package com.realmirror.app.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraFront
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realmirror.app.ui.theme.AccentCyan
import com.realmirror.app.ui.theme.AccentPurple
import com.realmirror.app.ui.theme.DeepBlack
import com.realmirror.app.update.UpdateChecker
import com.realmirror.app.update.UpdateResult
import kotlinx.coroutines.launch

/**
 * About screen showing app information, version code, and update checks.
 * Uses GitHub Releases API to check for new versions.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    // Dynamically retrieve version name and version code
    val packageInfo = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0"
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo?.longVersionCode ?: 1L
    } else {
        @Suppress("DEPRECATION")
        packageInfo?.versionCode?.toLong() ?: 1L
    }

    // Update check state
    var isChecking by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepBlack, Color(0xFF0D0D1A))
                )
            )
    ) {
        // Main Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {
            // App icon (matches PermissionScreen styling)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentCyan.copy(alpha = 0.3f),
                                AccentPurple.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraFront,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Real Mirror",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "See yourself as others do",
                fontSize = 16.sp,
                color = AccentCyan.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "A simple, privacy-focused open-source mirror app designed to show your true, non-reversed reflection as others see you. Capture high-quality photos and record videos in real time.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Version $versionName (Build $versionCode)",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Update check result display ─────────────────────────────
            AnimatedVisibility(
                visible = updateResult != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (val result = updateResult) {
                    is UpdateResult.UpToDate -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You're up to date!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                    is UpdateResult.UpdateAvailable -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDownload,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Update available: ${result.latestVersion}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { uriHandler.openUri(result.releaseUrl) }
                            ) {
                                Text(
                                    text = "Download from GitHub →",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccentCyan
                                )
                            }
                        }
                    }
                    is UpdateResult.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Could not check for updates",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                text = result.message,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    null -> {} // Not shown
                }
            }

            // ── Check for Update button ─────────────────────────────────
            Button(
                onClick = {
                    if (!isChecking) {
                        isChecking = true
                        updateResult = null
                        scope.launch {
                            updateResult = UpdateChecker.checkForUpdate(versionName)
                            isChecking = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp),
                enabled = !isChecking
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DeepBlack,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Checking…",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepBlack,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                } else {
                    Text(
                        text = "Check for Update",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepBlack,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── View on GitHub button ───────────────────────────────────
            TextButton(
                onClick = {
                    uriHandler.openUri("https://github.com/abzgif/Android-Real-Mirror")
                }
            ) {
                Text(
                    text = "View on GitHub",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AccentCyan
                ),
                border = BorderStroke(1.dp, AccentCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text(
                    text = "Back",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}
