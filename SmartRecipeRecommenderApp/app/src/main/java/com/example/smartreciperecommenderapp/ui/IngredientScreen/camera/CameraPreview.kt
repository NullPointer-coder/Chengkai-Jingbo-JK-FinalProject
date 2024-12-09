package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraPreview(
    navController: NavController,
    lifecycleOwner: LifecycleOwner,
    onQRCodeScanned: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var lastScannedResult by remember { mutableStateOf<String?>(null) }
    var lastScannedTimestamp by remember { mutableLongStateOf(0L) }
    var isProcessing by remember { mutableStateOf(false) }

    val stableTimeMillis = 1000L // 设置稳定时间为 1000ms
    val analysisInterval = 200L // 限制图像分析频率为每200毫秒

    DisposableEffect(Unit) {
        Log.d("CameraPreview", "DisposableEffect triggered")
        val cameraProvider = cameraProviderFuture.get()
        Log.d("CameraPreview", "Got cameraProvider: $cameraProvider")
        onDispose {
            Log.d("CameraPreview", "onDispose called, unbinding camera")
            cameraProvider.unbindAll() // 确保释放摄像头
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 相机预览部分
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    Log.d("CameraPreview", "AndroidView factory invoked")
                    val previewView = PreviewView(ctx)
                    val cameraProvider = cameraProviderFuture.get()
                    Log.d("CameraPreview", "cameraProvider from factory: $cameraProvider")

                    // 配置相机预览
                    val preview = androidx.camera.core.Preview.Builder().build().also {
                        Log.d("CameraPreview", "Preview instance created")
                    }
                    preview.surfaceProvider = previewView.surfaceProvider
                    Log.d("CameraPreview", "SurfaceProvider set to preview")

                    // 配置扫描逻辑
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            Log.d("CameraPreview", "ImageAnalysis instance created")
                            var lastAnalysisTimestamp = 0L

                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                // 每次拿到图像数据都会触发这里
                                Log.d("CameraPreview", "Analyzer invoked with imageProxy: ${imageProxy.imageInfo}")
                                val currentTimestamp = System.currentTimeMillis()
                                if (currentTimestamp - lastAnalysisTimestamp >= analysisInterval) {
                                    lastAnalysisTimestamp = currentTimestamp
                                    MLKitUtils.processImageProxy(
                                        imageProxy,
                                        onSuccess = { result ->
                                            val now = System.currentTimeMillis()
                                            Log.d(
                                                "CameraPreview",
                                                "Scanned Result: $result, Last Result: $lastScannedResult, Time Diff: ${now - lastScannedTimestamp}"
                                            )
                                            if (!isProcessing && result == lastScannedResult &&
                                                now - lastScannedTimestamp >= stableTimeMillis
                                            ) {
                                                isProcessing = true
                                                Log.d("CameraPreview", "Stable Result Triggered: $result")
                                                onQRCodeScanned(result)
                                                lastScannedTimestamp = now

                                                // 重置防抖标志位
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
                                            Log.e("CameraPreview", "Error in MLKitUtils.processImageProxy", exception)
                                            onError(exception)
                                            imageProxy.close()
                                        }
                                    )
                                } else {
                                    Log.d("CameraPreview", "Skipping this frame due to analysisInterval limit")
                                    imageProxy.close()
                                }
                            }
                        }

                    // 配置相机选择器（后置摄像头）
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        Log.d("CameraPreview", "Attempting to bind camera use cases to lifecycle")
                        if (!cameraProvider.isBound(preview) && !cameraProvider.isBound(imageAnalyzer)) {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalyzer
                            )
                            Log.d("CameraPreview", "Camera use cases successfully bound")
                        } else {
                            Log.d("CameraPreview", "Camera use cases already bound")
                        }
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Exception while binding camera use cases", exc)
                        onError(exc)
                    }
                    previewView
                }
            )

            // 扫描框
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.Center)
                    .border(2.dp, Color.White)
            ) {
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

        // 顶部返回按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    }
}
