package com.isanechek.elitawallpaperx.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.isanechek.elitawallpaperx.BuildConfig
import com.isanechek.elitawallpaperx.BuildConfig.DEBUG

object FileUtils {

    // Еще хз какой будет минимум сдк
    @SuppressLint("ObsoleteSdkInt")
    fun getPath(context: Context, uri: Uri): String {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            when {
                isKitKat && DocumentsContract.isDocumentUri(context, uri) -> when {
                    isLocalStorageDocument(uri) -> return DocumentsContract.getDocumentId(uri)
                    isExternalStorageDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split =
                            docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]
                        if ("primary".equals(type, ignoreCase = true))
                            return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                    isDownloadsDocument(uri) -> {
                        val id = DocumentsContract.getDocumentId(uri)
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                        )
                        return getDataColumn(context, contentUri, null, null)
                    }
                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split =
                            docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]

                        var contentUri: Uri? = null
                        when (type) {
                            "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }

                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])

                        return getDataColumn(context, contentUri, selection, selectionArgs)
                    }
                }
                "content".equals(
                    uri.scheme,
                    ignoreCase = true
                ) -> return when {
                    isGooglePhotosUri(uri) -> uri.lastPathSegment ?: ""
                    else -> getDataColumn(
                        context,
                        uri,
                        null,
                        null
                    )
                }
                "file".equals(uri.scheme, ignoreCase = true) -> return uri.path ?: ""
            }
        }
        return ""
    }


    private fun isDownloadsDocument(uri: Uri): Boolean =
        "com.android.providers.downloads.documents" == uri.authority

    private fun isMediaDocument(uri: Uri): Boolean =
        "com.android.providers.media.documents" == uri.authority

    private fun isGooglePhotosUri(uri: Uri): Boolean =
        "com.google.android.apps.photos.content" == uri.authority

    private fun isExternalStorageDocument(uri: Uri): Boolean =
        "com.android.externalstorage.documents" == uri.authority

    private fun isLocalStorageDocument(uri: Uri): Boolean =
        LocalStorageProvider.AUTHORITY == uri.authority

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG) {
                    DatabaseUtils.dumpCursor(cursor)
                }

                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return ""
    }

    object LocalStorageProvider {
        const val AUTHORITY = BuildConfig.APPLICATION_ID
    }
}