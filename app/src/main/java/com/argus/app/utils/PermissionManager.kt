package com.argus.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {
    
    companion object {
        const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
        const val AUDIO_PERMISSION_REQUEST_CODE = 1002
        
        /**
         * Check if overlay permission is granted
         */
        fun hasOverlayPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
        
        /**
         * Request overlay permission
         */
        fun requestOverlayPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(activity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${activity.packageName}")
                    )
                    activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
                }
            }
        }
        
        /**
         * Check if audio recording permission is granted
         */
        fun hasAudioPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }
        
        /**
         * Request audio recording permission
         */
        fun requestAudioPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                AUDIO_PERMISSION_REQUEST_CODE
            )
        }
        
        /**
         * Check if all required permissions are granted
         */
        fun hasAllRequiredPermissions(context: Context): Boolean {
            return hasOverlayPermission(context) && hasAudioPermission(context)
        }
        
        /**
         * Get list of missing permissions
         */
        fun getMissingPermissions(context: Context): List<String> {
            val missing = mutableListOf<String>()
            
            if (!hasOverlayPermission(context)) {
                missing.add("System Overlay")
            }
            
            if (!hasAudioPermission(context)) {
                missing.add("Microphone")
            }
            
            return missing
        }
        
        /**
         * Handle permission result
         */
        fun handlePermissionResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
            onPermissionGranted: (String) -> Unit,
            onPermissionDenied: (String) -> Unit
        ) {
            when (requestCode) {
                AUDIO_PERMISSION_REQUEST_CODE -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        onPermissionGranted("Audio recording")
                    } else {
                        onPermissionDenied("Audio recording")
                    }
                }
            }
        }
        
        /**
         * Open app settings for manual permission management
         */
        fun openAppSettings(context: Context) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }
}