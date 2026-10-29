package com.munireport.app.core.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun Context.displayName(uri: Uri): String {
    if (uri.scheme == "content") {
        runCatching {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) {
                            val name = cursor.getString(index)
                            if (!name.isNullOrBlank()) return name
                        }
                    }
                }
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/') ?: "residency-id.jpg"
}
