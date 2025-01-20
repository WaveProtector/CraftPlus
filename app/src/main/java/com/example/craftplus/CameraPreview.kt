package com.example.craftplus

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner) // Vincular o controller ao ciclo de vida
        onDispose { controller.unbind() }
    }
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
            }
        },
        modifier = modifier
    )
}