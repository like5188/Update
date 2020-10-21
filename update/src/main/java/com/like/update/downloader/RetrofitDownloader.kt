package com.like.update.downloader

import com.like.retrofit.download.DownloadRetrofit
import com.like.retrofit.download.model.DownloadInfo
import kotlinx.coroutines.flow.Flow
import java.io.File

class RetrofitDownloader(private val downloadRetrofit: DownloadRetrofit) : IDownloader {

    override suspend fun download(
        url: String,
        downloadFile: File,
        threadCount: Int,
        deleteCache: Boolean,
        callbackInterval: Long
    ): Flow<DownloadInfo> {
        return downloadRetrofit.download(
            url,
            downloadFile,
            threadCount,
            deleteCache,
            callbackInterval
        )
    }

}