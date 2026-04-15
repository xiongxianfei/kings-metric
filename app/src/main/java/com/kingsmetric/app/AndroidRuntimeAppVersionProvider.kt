package com.kingsmetric.app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class AndroidRuntimeAppVersionProvider(
    private val context: Context
) : AppVersionProvider {
    override fun currentVersion(): String {
        return packageInfoVersionName()
            ?.takeIf { it.isNotBlank() }
            ?: "Unknown"
    }

    private fun packageInfoVersionName(): String? {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        return packageInfo.versionName
    }
}
