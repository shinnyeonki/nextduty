package com.shinnk.nextduty

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorage {
    fun saveUriToInternal(context: Context, uri: Uri): String? {
        return try {
            val fileName = "duty_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val relativePath = "work_schedules/$fileName"
            val destFile = File(context.filesDir, relativePath)
            destFile.parentFile?.mkdirs()
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            relativePath
        } catch (_: Exception) {
            null
        }
    }

    fun deleteFile(context: Context, path: String) {
        try {
            val file = if (path.startsWith("/")) File(path) else File(context.filesDir, path)
            if (file.exists()) file.delete()
        } catch (_: Exception) { /* ignore */ }
    }
}
