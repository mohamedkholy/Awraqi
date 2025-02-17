package com.dev3mk.awraqi.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ConnectivityObserver(val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

      val connectionObserver = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback(){
            init {
                launch { send(ConnectionStatus.Unavailable) }
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                 launch { send(ConnectionStatus.Available) }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                launch { send(ConnectionStatus.Unavailable) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                launch { send(ConnectionStatus.Unavailable) }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                launch { send(ConnectionStatus.Unavailable) }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }

    }.distinctUntilChanged()


    enum class ConnectionStatus {
        Available,Unavailable
    }

}