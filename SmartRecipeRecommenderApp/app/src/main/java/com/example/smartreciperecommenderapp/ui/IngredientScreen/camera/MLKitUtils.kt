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

/**
 * Utility object for using ML Kit to scan barcodes from camera frames.
 * It is configured to handle multiple barcode formats.
 */
object MLKitUtils {

    // Configure supported barcode formats
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_CODE_128
        )
        .build()

    // Lazy initialization of the barcode scanner
    private val barcodeScanner: BarcodeScanner by lazy {
        BarcodeScanning.getClient(options)
    }

    /**
     * Processes the given [ImageProxy] frame to detect barcodes using ML Kit.
     *
     * @param imageProxy The image frame from CameraX.
     * @param onSuccess Callback triggered when a barcode is successfully detected.
     * @param onError Callback triggered when an error occurs during processing.
     */
    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val mediaImage: Image = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Process the image using the ML Kit barcode scanner
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                // If one or more barcodes are detected, call onSuccess with the raw value
                barcodes.forEach { barcode ->
                    barcode.rawValue?.let { rawValue ->
                        onSuccess(rawValue)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // If an error occurs, call onError
                onError(exception)
            }
            .addOnCompleteListener {
                // Close the imageProxy to free resources
                imageProxy.close()
            }
    }
}
