package com.isanechek.elitawallpaperx.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.isanechek.elitawallpaperx.BuildConfig
import com.isanechek.elitawallpaperx.d
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

interface FilesManager {
    fun getFileName(path: String): String
    fun createFolderIfEmpty(path: String): Boolean
    fun copyFile(originPath: String, copyPath: String, copyFileName: String): Pair<Boolean, String>
    fun checkFileExists(path: String): Boolean
    fun saveFile(bitmap: Bitmap, folderPath: String, fileName: String): Pair<Boolean, String>
    fun clearAll(path: String): Boolean
    fun deleteFile(path: String): Boolean
    suspend fun loadImagesFromAssets(context: Context): List<String>
    suspend fun getBitmapUri(context: Context, fileName: String): Uri
}

class FilesManagerImpl : FilesManager {

    // Здесь надо впилить проверку файла
    override fun getFileName(path: String): String = File(path).nameWithoutExtension

    override fun createFolderIfEmpty(path: String): Boolean {
        var isCreated = true
        val folder = File(path)
        if (!folder.exists()) {
            isCreated = folder.mkdirs()
        }
        return isCreated
    }

    override fun copyFile(
        originPath: String,
        copyPath: String,
        copyFileName: String
    ): Pair<Boolean, String> {
        var result = Pair(false, "empty")
        val copyFile =
            File(originPath).copyTo(File(copyPath, "copy_$copyFileName.jpg"), overwrite = true)
        if (copyFile.exists() && copyFile.length() > 0) {
            result = Pair(first = true, second = copyFile.absolutePath)
        }
        return result
    }

    override fun checkFileExists(path: String): Boolean = File(path).exists()

    override fun saveFile(
        bitmap: Bitmap,
        folderPath: String,
        fileName: String
    ): Pair<Boolean, String> {
        var result = Pair(false, "")
        val fileResult = File(folderPath, "result_$fileName.jpg")
        FileOutputStream(fileResult).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        if (fileResult.exists() && fileResult.length() > 0) {
            result = Pair(true, fileResult.absolutePath)
        }
        return result
    }

    override fun clearAll(path: String): Boolean {
        var result = true
        val folder = File(path)
        if (folder.isDirectory) {
            folder.listFiles()?.forEach {
                if (!it.delete()) result = false
                return@forEach
            }
        }
        return result
    }

    override fun deleteFile(path: String): Boolean {
        var isDeleted = false
        val file = File(path)
        if (file.isFile) {
            isDeleted = file.delete()
        }
        d { "Path $path status $isDeleted" }
        return isDeleted
    }

    override suspend fun loadImagesFromAssets(context: Context): List<String> =
        suspendCancellableCoroutine { c ->
            try {
                val am = context.assets
                val paths = am.list("images")
                val result = paths?.toList() ?: emptyList()
                c.resume(result)
            } catch (ex: Exception) {
                d { "loadImagesFromAssets error! ${ex.message}" }
                c.resume(emptyList())
            }
        }

    override suspend fun getBitmapUri(context: Context, fileName: String): Uri =
        suspendCancellableCoroutine { c ->
            try {
                val name = fileName.replaceBefore("images", "").trim()
                d { "Name $name" }
                val cachePath = context.filesDir.absolutePath + File.separator + "cache_images"
                d { "Cache path $cachePath" }
                if (createFolderIfEmpty(cachePath)) {
                    d { "FOLDER CREATE" }
                    val bitmap = context.assets.open(name).use {
                        BitmapFactory.decodeStream(it)
                    }
                    d { "Bitmap height ${bitmap.height}" }
                    val file = File("${cachePath}/temp.jpg")
                    if (file.exists()) {
                        file.delete()
                    }
                    FileOutputStream(file).use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    d { "File size ${file.length()}" }
//                    val uri = FileProvider.getUriForFile(
//                        context,
//                        BuildConfig.APPLICATION_ID + ".provider",
//                        file
//                    )
                    c.resume(Uri.fromFile(file))
                } else {
                    d { "FOLDER HYI" }
                    c.resume(Uri.EMPTY)
                }
            } catch (ex: Exception) {
                d { "getBitmapUri error! ${ex.message}" }
                c.resume(Uri.EMPTY)
            }

        }

}