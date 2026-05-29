package com.realmirror.app.camera

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.MirrorMode
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import android.util.Size
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraManager"
private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

class CameraManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var isMirrorMode: Boolean = false

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isMirrorMode: Boolean,
        onCameraReady: () -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(lifecycleOwner, previewView, isMirrorMode)
            onCameraReady()
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            Log.d(TAG, "Camera unbound and stopped.")
        } catch (exc: Exception) {
            Log.e(TAG, "Failed to unbind camera", exc)
        }
    }

    fun rebindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isMirrorMode: Boolean
    ) {
        bindCameraUseCases(lifecycleOwner, previewView, isMirrorMode)
    }

    private fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isMirrorMode: Boolean
    ) {
        val provider = cameraProvider ?: return
        this.isMirrorMode = isMirrorMode

        // Create a ResolutionSelector to enforce high-resolution 16:9 aspect ratio stream selection
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(1080, 1920),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()

        // Preview use case with explicit high-resolution request
        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        // Apply mirror/real transformation on the PreviewView
        previewView.scaleX = if (isMirrorMode) 1f else -1f

        // Image capture use case with high-resolution matching selector
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .build()

        // Video capture use case preferring Full HD (1080p) to align with Preview aspect ratio
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.FHD,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.HD)
                )
            )
            .build()
        val selectedMirrorMode = if (isMirrorMode) {
            MirrorMode.MIRROR_MODE_ON
        } else {
            MirrorMode.MIRROR_MODE_OFF
        }
        videoCapture = VideoCapture.Builder(recorder)
            .setMirrorMode(selectedMirrorMode)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
            Log.d(TAG, "Camera bound successfully. MirrorMode=$isMirrorMode")
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    fun capturePhoto(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError("Camera not ready")
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "RealMirror_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RealMirror")
            }
        }

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = isMirrorMode
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .setMetadata(metadata)
        .build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onSuccess("RealMirror_$name.jpg")
                    Log.d(TAG, "Photo saved: ${output.savedUri}")
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    onError(exc.message ?: "Unknown error")
                }
            }
        )
    }

    fun startVideoRecording(
        onStart: () -> Unit,
        onStop: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = videoCapture ?: run {
            onError("Video capture not ready")
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "RealMirror_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/RealMirror")
            }
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        activeRecording = capture.output
            .prepareRecording(context, mediaStoreOutput)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.d(TAG, "Video recording started")
                        onStart()
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            Log.d(TAG, "Video saved: ${recordEvent.outputResults.outputUri}")
                            onStop(true)
                        } else {
                            Log.e(TAG, "Video finalize error: ${recordEvent.error}")
                            onStop(false)
                            onError("Recording error: ${recordEvent.error}")
                        }
                    }
                    else -> {}
                }
            }
    }

    fun stopVideoRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun isRecording(): Boolean = activeRecording != null

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
