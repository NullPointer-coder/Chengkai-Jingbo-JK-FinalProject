package com.example.smartreciperecommenderapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A helper class for monitoring network connectivity changes.
 *
 * It uses the system's ConnectivityManager to listen for changes
 * in network availability and provides a StateFlow that can be
 * collected to react to connectivity changes in real-time.
 */
class NetworkMonitor(context: Context) {
    // Retrieve the ConnectivityManager system service
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Holds the current connectivity state; true if connected, false if disconnected
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected

    init {
        // Define a callback to handle network changes
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // Called when the device is connected to a network
            override fun onAvailable(network: Network) {
                _isConnected.value = true
            }

            // Called when the device loses network connectivity
            override fun onLost(network: Network) {
                _isConnected.value = false
            }
        }

        // Register the network callback with a basic network request to listen for changes
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }
}
