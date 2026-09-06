package com.example.util

import android.content.Context

data class SecuritySettings(
    val isLockEnabled: Boolean = false,
    val pinCode: String = "",
    val unlockTimeoutSeconds: Int = 300
)

object SecurityLockManager {
    private const val PREFS_NAME = "storage_security_prefs"

    fun loadSettings(context: Context): SecuritySettings {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return SecuritySettings(
            isLockEnabled = sp.getBoolean("is_lock_enabled", false),
            pinCode = sp.getString("pin_code", "") ?: "",
            unlockTimeoutSeconds = sp.getInt("unlock_timeout", 300)
        )
    }

    fun saveSettings(context: Context, settings: SecuritySettings) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean("is_lock_enabled", settings.isLockEnabled)
            .putString("pin_code", settings.pinCode)
            .putInt("unlock_timeout", settings.unlockTimeoutSeconds)
            .apply()
    }
}
