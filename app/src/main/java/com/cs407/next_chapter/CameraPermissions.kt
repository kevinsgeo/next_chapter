package com.cs407.next_chapter

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissions(
    content: @Composable () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current

    when {
        cameraPermissionState.status.isGranted -> {
            // Permission is granted, show the content
            content()
        }
        cameraPermissionState.status.shouldShowRationale -> {
            // Show rationale and request permission
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Camera Permission Needed") },
                text = { Text(text = "This app requires camera access to scan barcodes and upload photos.") },
                confirmButton = {
                    TextButton(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { /* Dismiss the dialog or take other actions */ }) {
                        Text("Deny")
                    }
                }
            )
        }
        !cameraPermissionState.status.isGranted -> {
            // Permission denied permanently, guide the user to app settings
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Camera Permission Denied") },
                text = { Text(text = "Please enable camera access in settings to use this feature.") },
                confirmButton = {
                    TextButton(onClick = {
                        // Open app settings
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { /* Dismiss the dialog or take other actions */ }) {
                        Text("Cancel")
                    }
                }
            )
        }
        else -> {
            // Request permission
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }
}
