package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.ui.navigation.Screen
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    viewModel: QRScannerViewModel,
) {
    LaunchedEffect(Unit) {
        viewModel.resetScan()
    }
    
    val cameraPermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)
    val scanResult by viewModel.scanResult.collectAsState()
    val ingredient by viewModel.ingredient.collectAsState()

    // Launch permission request if not granted
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            // If camera permission is granted
            if (scanResult == null) {
                // If no result yet, show camera preview for scanning
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        navController = navController,
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
                // Once scanning is done and result is available
                LaunchedEffect(ingredient) {
                    ingredient?.let { ing ->
                        if (ing.name.isNotBlank()) {
                            navController.navigate(Screen.ProductDetail.route)
                        }
                    }
                }
            }
        }
        cameraPermissionState.status.shouldShowRationale -> {
            // If user denies permission but we can still show rationale
            PermissionRationale(cameraPermissionState)
        }
        else -> {
            // If permission is denied and 'don't ask again' has been selected
            PermissionDeniedMessage(cameraPermissionState)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRationale(cameraPermissionState: PermissionState) {
    // This UI is shown when the user has denied the permission once but not permanently.
    // Here we can explain why the camera permission is needed.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Camera permission is required to scan Barcodes.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDeniedMessage(cameraPermissionState: PermissionState) {
    // This UI is shown if the user has permanently denied the permission (or no rationale can be shown).
    // We provide instructions to open the app settings to grant permission again.
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Camera permission denied. Please grant it to scan Barcodes.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (cameraPermissionState.status.shouldShowRationale) {
                    // If we can still show rationale, request again
                    cameraPermissionState.launchPermissionRequest()
                } else {
                    // If 'don't ask again' was selected, direct user to the app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }) {
                Text(if (cameraPermissionState.status.shouldShowRationale) "Request Permission Again" else "Open Settings")
            }
        }
    }
}
