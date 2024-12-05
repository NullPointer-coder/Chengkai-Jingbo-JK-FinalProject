package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

object MLKitUtils {

    // 配置支持的条形码格式
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_CODE_128
        )
        .build()

    private val barcodeScanner: BarcodeScanner by lazy {
        BarcodeScanning.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val mediaImage: Image = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach { barcode ->
                    barcode.rawValue?.let { rawValue ->
                        onSuccess(rawValue) // 直接返回扫描结果
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception) // 返回错误信息
            }
            .addOnCompleteListener {
                imageProxy.close() // 确保释放资源
            }
    }
}

