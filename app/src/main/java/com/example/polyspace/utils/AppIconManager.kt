package com.example.polyspace.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.polyspace.R

enum class AppIcon(val aliasName: String, val foregroundResId: Int, val title: String) {
    DEFAULT("MainActivityDefault", R.drawable.ic_launcher_foreground, "PolySpace"),
    UGLY("MainActivityUgly", R.drawable.ic_foreground_ugly, "Option 1"),
    UGLIER("MainActivityUglier", R.drawable.ic_foreground_uglier, "Option 2")
}

object AppIconManager {

    fun setIcon(context: Context, newIcon: AppIcon) {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val newComponentName = ComponentName(packageName, "$packageName.${newIcon.aliasName}")

        if (getCurrentIcon(context) == newIcon) return

        try {
            packageManager.setComponentEnabledSetting(
                newComponentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            AppIcon.entries.forEach { icon ->
                if (icon != newIcon) {
                    val oldComponent = ComponentName(packageName, "$packageName.${icon.aliasName}")
                    packageManager.setComponentEnabledSetting(
                        oldComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AppIconManager", "Erreur lors du changement d'icône", e)
        }
    }

    fun getCurrentIcon(context: Context): AppIcon {
        val packageManager = context.packageManager
        val packageName = context.packageName

        return AppIcon.entries.firstOrNull { icon ->
            val component = ComponentName(packageName, "$packageName.${icon.aliasName}")
            packageManager.getComponentEnabledSetting(component) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } ?: AppIcon.DEFAULT
    }
}