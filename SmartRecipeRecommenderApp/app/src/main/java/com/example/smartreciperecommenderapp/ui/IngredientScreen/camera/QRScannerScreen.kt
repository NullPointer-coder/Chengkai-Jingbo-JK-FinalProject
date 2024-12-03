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
    onQRCodeScanned: (String) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(
                    lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
                    onQRCodeScanned = { result ->
                        viewModel.onScanSuccess(result)
                        onQRCodeScanned(result)
                    },
                    onError = { exception ->
                        viewModel.onScanError(exception)
                    }
                )
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
