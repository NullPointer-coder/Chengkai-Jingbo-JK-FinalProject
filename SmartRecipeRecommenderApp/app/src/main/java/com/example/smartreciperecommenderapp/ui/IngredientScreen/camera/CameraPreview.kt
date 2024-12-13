package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun CameraPreview(
    navController: NavController,
    lifecycleOwner: LifecycleOwner,
    onQRCodeScanned: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Camera instance and preview reference
    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // State variables for scanning logic
    var lastScannedResult by remember { mutableStateOf<String?>(null) }
    var lastScannedTimestamp by remember { mutableLongStateOf(0L) }
    var isProcessing by remember { mutableStateOf(false) }

    // Timing constraints
    val stableTimeMillis = 1000L
    val analysisInterval = 200L

    // Flash and light-sensor logic
    // If user never touches the flash button, we use automatic flash based on ambient light.
    // Once user toggles flash manually, we stop automatic control.
    var isFlashManuallyControlled by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }

    // Ensure camera is unbound when composable is disposed
    DisposableEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()
        onDispose {
            cameraProvider.unbindAll()
        }
    }

    // Register gyroscope sensor to trigger autofocus after device becomes stable
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val sensorEventListener = object : SensorEventListener {
            var lastFocusTime = System.currentTimeMillis()

            override fun onSensorChanged(event: SensorEvent) {
                // event.values: [xRotation, yRotation, zRotation] in rad/s
                val xRotation = event.values[0]
                val yRotation = event.values[1]
                val zRotation = event.values[2]

                // Calculate rotation magnitude to determine if the device is moving
                val rotationMagnitude = sqrt(
                    (xRotation * xRotation + yRotation * yRotation + zRotation * zRotation).toDouble()
                ).toFloat()

                // If motion is above threshold, consider the device is moving
                // Once stable for 2 seconds, attempt to autofocus
                val motionThreshold = 0.5f
                val currentTime = System.currentTimeMillis()
                if (rotationMagnitude > motionThreshold) {
                    // Device is moving: update last motion timestamp
                    lastFocusTime = currentTime
                } else {
                    // Device is stable: if stable for more than 2 seconds, refocus
                    if (currentTime - lastFocusTime > 2000) {
                        lastFocusTime = currentTime
                        camera?.let { cam ->
                            previewViewRef?.let { pv ->
                                autoFocusCenter(cam, pv)
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed
            }
        }

        gyroSensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // Register light sensor to enable/disable flashlight automatically if not manually controlled
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val lightEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!isFlashManuallyControlled && camera != null && camera?.cameraInfo?.hasFlashUnit() == true) {
                    val lux = event.values[0]
                    // If it's too dark (e.g. lux < 10), turn on flash automatically
                    // Otherwise, turn it off
                    if (lux < 10) {
                        camera?.cameraControl?.enableTorch(true)
                    } else {
                        camera?.cameraControl?.enableTorch(false)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed
            }
        }

        lightSensor?.let {
            sensorManager.registerListener(lightEventListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        onDispose {
            sensorManager.unregisterListener(lightEventListener)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    previewViewRef = previewView
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build()
                    preview.surfaceProvider = previewView.surfaceProvider

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            var lastAnalysisTimestamp = 0L

                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                val currentTimestamp = System.currentTimeMillis()
                                if (currentTimestamp - lastAnalysisTimestamp >= analysisInterval) {
                                    lastAnalysisTimestamp = currentTimestamp
                                    MLKitUtils.processImageProxy(
                                        imageProxy,
                                        onSuccess = { result ->
                                            val now = System.currentTimeMillis()
                                            // Ensure stable result before invoking callback
                                            if (!isProcessing && result == lastScannedResult &&
                                                now - lastScannedTimestamp >= stableTimeMillis
                                            ) {
                                                isProcessing = true
                                                onQRCodeScanned(result)

                                                // Play a beep sound upon a successful stable scan
                                                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                                                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)

                                                lastScannedTimestamp = now

                                                CoroutineScope(Dispatchers.Main).launch {
                                                    delay(1000)
                                                    isProcessing = false
                                                }
                                            } else if (result != lastScannedResult) {
                                                lastScannedResult = result
                                                lastScannedTimestamp = now
                                            }
                                            imageProxy.close()
                                        },
                                        onError = { exception ->
                                            onError(exception)
                                            imageProxy.close()
                                        }
                                    )
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        onError(exc)
                    }

                    previewView
                }
            )

            // Custom barcode alignment frame with corner lines
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.Center)
            ) {
                Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                    val strokeWidthVal = 6.dp.toPx()
                    val cornerLength = size.minDimension * 0.2f

                    // Draw corner lines
                    // Top-left corner
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, strokeWidthVal / 2),
                        end = Offset(cornerLength, strokeWidthVal / 2),
                        strokeWidth = strokeWidthVal
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(strokeWidthVal / 2, 0f),
                        end = Offset(strokeWidthVal / 2, cornerLength),
                        strokeWidth = strokeWidthVal
                    )

                    // Top-right corner
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width - cornerLength, strokeWidthVal / 2),
                        end = Offset(size.width, strokeWidthVal / 2),
                        strokeWidth = strokeWidthVal
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width - strokeWidthVal / 2, 0f),
                        end = Offset(size.width - strokeWidthVal / 2, cornerLength),
                        strokeWidth = strokeWidthVal
                    )

                    // Bottom-left corner
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, size.height - strokeWidthVal / 2),
                        end = Offset(cornerLength, size.height - strokeWidthVal / 2),
                        strokeWidth = strokeWidthVal
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(strokeWidthVal / 2, size.height - cornerLength),
                        end = Offset(strokeWidthVal / 2, size.height),
                        strokeWidth = strokeWidthVal
                    )

                    // Bottom-right corner
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width - cornerLength, size.height - strokeWidthVal / 2),
                        end = Offset(size.width, size.height - strokeWidthVal / 2),
                        strokeWidth = strokeWidthVal
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width - strokeWidthVal / 2, size.height - cornerLength),
                        end = Offset(size.width - strokeWidthVal / 2, size.height),
                        strokeWidth = strokeWidthVal
                    )
                }

                Text(
                    text = "Align the Barcode here",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Top bar with back button and flash toggle button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Flash toggle button
                // If user taps this, we manually control flash and stop auto mode
                camera?.let { cam ->
                    if (cam.cameraInfo.hasFlashUnit()) {
                        IconButton(
                            onClick = {
                                isFlashManuallyControlled = true
                                isFlashOn = !isFlashOn
                                cam.cameraControl.enableTorch(isFlashOn)
                            }
                        ) {
                            val iconRes = if (isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = if (isFlashOn) "Flash On" else "Flash Off",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Initiates an autofocus action at the center of the camera preview.
 * Called when the device has stabilized to improve scan accuracy.
 */
private fun autoFocusCenter(camera: Camera, previewView: PreviewView) {
    val factory = previewView.meteringPointFactory
    val centerPoint = factory.createPoint(previewView.width / 2f, previewView.height / 2f)
    val action = FocusMeteringAction.Builder(centerPoint)
        .disableAutoCancel()
        .build()
    camera.cameraControl.startFocusAndMetering(action)
}
