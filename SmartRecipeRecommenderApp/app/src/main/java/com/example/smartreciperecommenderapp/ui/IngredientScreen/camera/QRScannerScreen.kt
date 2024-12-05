package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    viewModel: QRScannerViewModel = viewModel(),
) {
    val cameraPermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)
    val productDetails by viewModel.productDetails.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val productImage by viewModel.productImage.collectAsState()

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

                    // 显示产品图片
                    productImage?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
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
            PermissionDeniedMessage(cameraPermissionState)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRationale(cameraPermissionState: PermissionState) {
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
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Camera permission denied. Please grant it to scan QR codes.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (cameraPermissionState.status.shouldShowRationale) {
                    // 如果可以显示权限理由，重新请求权限
                    cameraPermissionState.launchPermissionRequest()
                } else {
                    // 如果用户选择了“不再询问”，引导到设置页面
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
