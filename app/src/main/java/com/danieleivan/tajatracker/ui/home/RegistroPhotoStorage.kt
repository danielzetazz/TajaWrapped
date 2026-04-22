package com.danieleivan.tajatracker.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal object RegistroPhotoStorage {
    fun copyFromUri(context: Context, sourceUri: Uri): String? {
        val targetFile = createTargetFile(context) ?: return null
        return runCatching {
            context.contentResolver.openInputStream(sourceUri).use { input ->
                if (input == null) {
                    return@runCatching null
                }
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        }.getOrNull()
    }

    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        val targetFile = createTargetFile(context) ?: return null
        return runCatching {
            FileOutputStream(targetFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
            }
            targetFile.absolutePath
        }.getOrNull()
    }

    private fun createTargetFile(context: Context): File? {
        return runCatching {
            val dir = File(context.filesDir, "registro_photos")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            File(dir, "registro_${UUID.randomUUID()}.jpg")
        }.getOrNull()
    }
}

