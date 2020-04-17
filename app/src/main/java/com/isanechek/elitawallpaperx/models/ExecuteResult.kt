package com.isanechek.elitawallpaperx.models

sealed class ExecuteResult<out T: Any> {
    object Loading : ExecuteResult<Nothing>()
    data class Done<out T: Any>(val data: T) : ExecuteResult<T>()
    data class Error(val errorMessage: String) : ExecuteResult<Nothing>()
}