package com.dev3mk.awraqi.util

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess


object SecurityManager {

    fun preventScreenMonitoring(context: Context) {
        checkAndExitIfADBEnabled(context)
    }

    private fun checkAndExitIfADBEnabled(context: Context) {
        if (isADBEnabled(context)) {
            Log.e("Security", "ADB Debugging detected! Exiting app.")
            runBlocking (Dispatchers.Main){
                Toast.makeText(context, "ADB Debugging detected! Closing app.", Toast.LENGTH_LONG)
                    .show()
                exitProcess(0)
            }
        }
    }

    private fun isADBEnabled(context: Context): Boolean {
        return Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) != 0
    }


}