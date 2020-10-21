package com.like.update.downloader

import com.like.retrofit.download.model.DownloadInfo
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IDownloader {
    suspend fun download(
        url: String,
        downloadFile: File,
        threadCount: Int = 1,
        deleteCache: Boolean = false,
        callbackInterval: Long = 200L
    ): Flow<DownloadInfo>
}