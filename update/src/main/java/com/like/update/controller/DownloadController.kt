package com.like.update.controller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import com.like.common.util.ApkUtils
import com.like.livedatabus.liveDataBusRegister
import com.like.livedatabus.liveDataBusUnRegister
import com.like.livedatabus_annotations.BusObserver
import com.like.retrofit.download.model.DownloadInfo
import com.like.update.TAG_CONTINUE
import com.like.update.TAG_PAUSE_OR_CONTINUE
import com.like.update.downloader.IDownloader
import com.like.update.shower.IShower
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import java.io.File

/**
 * 下载控制类
 *
 * Manifest.permission.INTERNET
 * Manifest.permission.WRITE_EXTERNAL_STORAGE
 */
@SuppressLint("MissingPermission")
internal class DownloadController(private val context: Context) {
    companion object {
        private const val TAG_PAUSE = "pause"
    }

    private var downloadJob: Job? = null
    internal var mShower: IShower? = null
    internal var mDownloader: IDownloader? = null
    internal var mUrl: String = ""
    internal var mDownloadFile: File? = null

    init {
        liveDataBusRegister()
    }

    internal fun cancel() {
        downloadJob?.cancel()
        downloadJob = null
        liveDataBusUnRegister(TAG_PAUSE_OR_CONTINUE)
        liveDataBusUnRegister(TAG_PAUSE)
        liveDataBusUnRegister(TAG_CONTINUE)
    }

    /**
     * 如果是暂停，就继续下载；如果是下载中，就暂停
     */
    @BusObserver([TAG_PAUSE_OR_CONTINUE])
    internal fun pauseOrContinue() {
        if (downloadJob != null) {// 正在下载
            pause()
        } else {
            cont()
        }
    }

    @BusObserver([TAG_PAUSE])
    internal fun pause() {
        downloadJob?.cancel(CancellationException(TAG_PAUSE))
        downloadJob = null
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @Throws(Exception::class)
    @BusObserver([TAG_CONTINUE])
    internal fun cont() {
        val downloadFile = mDownloadFile ?: throw IllegalArgumentException("wrong download url")
        val downloader = mDownloader ?: throw IllegalArgumentException("you must call setDownloader() to set IDownloader")
        val shower = mShower ?: throw IllegalArgumentException("you must call setShower() to set IShower")
        val url = mUrl
        if (url.isEmpty()) throw IllegalArgumentException("url can not be empty")

        if (downloadJob != null) return// 正在下载

        shower.onDownloadPending()

        // 下载
        downloadJob = GlobalScope.launch(Dispatchers.Main) {
            downloader.download(
                url,
                downloadFile,
                Runtime.getRuntime().availableProcessors()
            ).onCompletion {
                if (it is CancellationException && it.message == TAG_PAUSE) {
                    shower.onDownloadPaused()
                }
            }
                .collect {
                    when (it.status) {
                        DownloadInfo.Status.STATUS_PENDING -> {
                        }
                        DownloadInfo.Status.STATUS_RUNNING -> {
                            shower.onDownloadRunning(it.cachedSize, it.totalSize)
                        }
                        DownloadInfo.Status.STATUS_SUCCESSFUL -> {
                            ApkUtils.install(context, downloadFile)
                            downloadJob = null
                            shower.onDownloadSuccess(it.totalSize)
                        }
                        DownloadInfo.Status.STATUS_FAILED -> {
                            shower.onDownloadFailed(it.throwable)
                            downloadJob = null// 用于点击继续重试
                        }
                    }
                }
        }
    }

}