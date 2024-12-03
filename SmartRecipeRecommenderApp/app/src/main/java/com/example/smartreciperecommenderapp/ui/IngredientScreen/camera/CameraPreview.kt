package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    onQRCodeScanned: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val previewView = PreviewView(context)
            Log.d("CameraX", "PreviewView initialized.")

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    Log.d("CameraX", "CameraProvider initialized successfully.")

                    val preview = androidx.camera.core.Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 实时优化
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                MLKitUtils.processImageProxy(
                                    imageProxy,
                                    onSuccess = { result ->
                                        Log.d("MLKit", "QR Code scanned: $result")
                                        onQRCodeScanned(result)
                                    },
                                    onError = { exception ->
                                        Log.e("MLKit", "Error scanning QR Code: ${exception.message}")
                                        onError(exception)
                                    }
                                )
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalyzer
                    )
                    Log.d("CameraX", "Camera successfully bound to lifecycle.")
                } catch (exception: Exception) {
                    Log.e("CameraX", "Failed to bind CameraX: ${exception.message}")
                    onError(exception)
                }
            }, ContextCompat.getMainExecutor(context))
            previewView
        }
    )
}
