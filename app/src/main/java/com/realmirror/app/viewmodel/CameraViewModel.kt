package com.realmirror.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.realmirror.app.camera.CameraManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CameraViewModel"

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    val cameraManager = CameraManager(application)

    // Camera state
    var isMirrorMode by mutableStateOf(false)
        private set

    // Recording state
    var isRecording by mutableStateOf(false)
        private set
    var recordingSeconds by mutableLongStateOf(0L)
        private set

    // Brightness
    var brightness by mutableFloatStateOf(0.8f)
        private set

    // UI messages
    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    // Capture animation trigger
    var captureFlash by mutableStateOf(false)
        private set

    private var timerJob: Job? = null
    private var previewViewRef: PreviewView? = null
    private var lifecycleOwnerRef: LifecycleOwner? = null

    fun initCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        previewViewRef = previewView
        lifecycleOwnerRef = lifecycleOwner
        cameraManager.startCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            isMirrorMode = isMirrorMode
        )
    }

    fun stopCamera() {
        cameraManager.stopCamera()
        previewViewRef = null
        lifecycleOwnerRef = null
    }

    fun toggleMirrorMode() {
        isMirrorMode = !isMirrorMode
        val pv = previewViewRef ?: return
        val lo = lifecycleOwnerRef ?: return
        cameraManager.rebindCamera(
            lifecycleOwner = lo,
            previewView = pv,
            isMirrorMode = isMirrorMode
        )
    }

    fun updateBrightness(value: Float) {
        brightness = value
    }

    fun capturePhoto() {
        triggerFlash()
        cameraManager.capturePhoto(
            onSuccess = { name ->
                snackbarMessage = "📸 Photo saved!"
                Log.d(TAG, "Photo captured: $name")
            },
            onError = { error ->
                snackbarMessage = "❌ Photo failed: $error"
            }
        )
    }

    fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        cameraManager.startVideoRecording(
            onStart = {
                isRecording = true
                recordingSeconds = 0L
                startTimer()
            },
            onStop = { success ->
                isRecording = false
                stopTimer()
                snackbarMessage = if (success) "🎬 Video saved!" else "❌ Video save failed"
            },
            onError = { error ->
                isRecording = false
                stopTimer()
                snackbarMessage = "❌ Recording error: $error"
            }
        )
    }

    private fun stopRecording() {
        cameraManager.stopVideoRecording()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                recordingSeconds++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        recordingSeconds = 0L
    }

    private fun triggerFlash() {
        viewModelScope.launch {
            captureFlash = true
            delay(120)
            captureFlash = false
        }
    }

    fun clearSnackbar() {
        snackbarMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.shutdown()
    }
}
