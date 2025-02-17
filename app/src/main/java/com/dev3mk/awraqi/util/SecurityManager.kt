package com.dev3mk.awraqi.util

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.Toast


class SecurityManager {

    companion object {

        fun preventScreenMonitoring(context: Context) {
            checkAndExitIfADBEnabled(context)
        }


        private fun isADBEnabled(context: Context): Boolean {
            return Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            ) != 0
        }

        private fun checkAndExitIfADBEnabled(context: Context) {
            if (isADBEnabled(context)) {
                Log.e("Security", "ADB Debugging detected! Exiting app.")
                Toast.makeText(context, "ADB Debugging detected! Closing app.", Toast.LENGTH_LONG)
                    .show()
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }

}