package com.realmirror.app.ui.screens

import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.realmirror.app.ui.theme.SurfaceGlass
import com.realmirror.app.ui.theme.GlassBorder
import com.realmirror.app.ui.components.BrightnessSlider
import com.realmirror.app.ui.components.MirrorRealToggle
import com.realmirror.app.ui.components.RecordButton
import com.realmirror.app.ui.components.RecordingIndicator
import com.realmirror.app.ui.components.ShutterButton
import com.realmirror.app.viewmodel.CameraViewModel

/**
 * Root screen that switches between PermissionScreen and CameraScreen
 * based on permission state.
 */
@Composable
fun MainScreen(
    allPermissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onSetBrightness: (Float) -> Unit
) {
    var showAboutScreen by remember { mutableStateOf(false) }

    if (showAboutScreen) {
        AboutScreen(onBack = { showAboutScreen = false })
    } else if (allPermissionsGranted) {
        CameraScreen(
            onAboutClick = { showAboutScreen = true },
            onSetBrightness = onSetBrightness
        )
    } else {
        PermissionScreen(
            onRequestPermissions = onRequestPermissions,
            onAboutClick = { showAboutScreen = true }
        )
    }
}

/**
 * The main camera viewfinder screen with all controls overlaid.
 */
@Composable
fun CameraScreen(
    onAboutClick: () -> Unit,
    viewModel: CameraViewModel = viewModel(),
    onSetBrightness: (Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Initialize camera and cleanup on dispose
    DisposableEffect(lifecycleOwner) {
        viewModel.initCamera(lifecycleOwner, previewView)
        onDispose {
            viewModel.stopCamera()
        }
    }

    // Update brightness callback when slider changes
    LaunchedEffect(viewModel.brightness) {
        onSetBrightness(viewModel.brightness)
    }

    // Show snackbar messages as Toasts (simple & reliable on fullscreen)
    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── Camera Preview ──────────────────────────────────────────────
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // ── Capture flash overlay ───────────────────────────────────────
        AnimatedVisibility(
            visible = viewModel.captureFlash,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.65f))
            )
        }

        // ── Top bar: toggle + recording indicator / about button ────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MirrorRealToggle(
                isMirrorMode = viewModel.isMirrorMode,
                onToggle = { viewModel.toggleMirrorMode() }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (viewModel.isRecording) {
                RecordingIndicator(seconds = viewModel.recordingSeconds)
            } else {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceGlass)
                        .border(1.dp, GlassBorder, RoundedCornerShape(50))
                        .clickable(onClick = onAboutClick)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABOUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // ── Right side: brightness slider ───────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
        ) {
            BrightnessSlider(
                brightness = viewModel.brightness,
                onBrightnessChange = { viewModel.updateBrightness(it) }
            )
        }

        // ── Bottom: unified glass capture & record control capsule ─────
        val bottomCapsuleShape = RoundedCornerShape(percent = 50)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp)
                .clip(bottomCapsuleShape)
                .background(SurfaceGlass)
                .border(1.dp, GlassBorder, bottomCapsuleShape)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShutterButton(
                onClick = { viewModel.capturePhoto() },
                size = 60.dp
            )

            Spacer(modifier = Modifier.width(28.dp))

            RecordButton(
                isRecording = viewModel.isRecording,
                onClick = { viewModel.toggleRecording() },
                size = 60.dp
            )
        }
    }
}
