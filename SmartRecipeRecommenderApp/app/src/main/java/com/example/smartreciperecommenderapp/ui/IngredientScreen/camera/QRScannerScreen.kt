package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    viewModel: QRScannerViewModel = viewModel(),
) {
    val cameraPermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)
    val productDetails by viewModel.productDetails.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            if (scanResult == null) {
                // 如果尚未扫描，显示相机预览
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
                        onQRCodeScanned = { result ->
                            viewModel.onScanSuccess(result)
                        },
                        onError = { exception ->
                            viewModel.onScanError(exception)
                        }
                    )
                }
            } else {
                // 如果已扫描，显示产品详情
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Product Details:\n$productDetails",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.resetScan() }) {
                        Text("Scan Another")
                    }
                }
            }
        }
        cameraPermissionState.status.shouldShowRationale -> {
            PermissionRationale(cameraPermissionState)
        }
        else -> {
            PermissionDeniedMessage()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRationale(cameraPermissionState: PermissionState) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Camera permission is required to scan QR codes.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun PermissionDeniedMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Camera permission denied. Please enable it in settings.")
    }
}
