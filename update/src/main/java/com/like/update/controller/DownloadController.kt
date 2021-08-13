package com.like.update.controller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import com.like.common.util.ApkUtils
import com.like.floweventbus.FlowEventBus
import com.like.floweventbus_annotations.BusObserver
import com.like.retrofit.download.model.DownloadInfo
import com.like.update.downloader.IDownloader
import com.like.update.shower.IShower
import com.like.update.util.TAG_CANCEL
import com.like.update.util.TAG_CONTINUE
import com.like.update.util.TAG_PAUSE
import com.like.update.util.TAG_PAUSE_OR_CONTINUE
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
internal class DownloadController {
    companion object {
        private const val PAUSE = "pause"
    }

    private var downloadJob: Job? = null
    var mShower: IShower? = null
    var mDownloader: IDownloader? = null
    var context: Context? = null
    var mUrl: String = ""
    var mDownloadFile: File? = null

    init {
        FlowEventBus.register(this)
    }

    @BusObserver([TAG_CANCEL])
    fun cancel() {
        downloadJob?.cancel()
        downloadJob = null
    }

    /**
     * 如果是暂停，就继续下载；如果是下载中，就暂停
     */
    @BusObserver([TAG_PAUSE_OR_CONTINUE])
    fun pauseOrContinue() {
        if (downloadJob != null) {// 正在下载
            pause()
        } else {
            cont()
        }
    }

    @BusObserver([TAG_PAUSE])
    fun pause() {
        downloadJob?.cancel(CancellationException(PAUSE))
        downloadJob = null
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @BusObserver([TAG_CONTINUE])
    fun cont() {
        val downloadFile = mDownloadFile ?: throw IllegalArgumentException("wrong download url")
        val downloader = mDownloader
            ?: throw IllegalArgumentException("you must call setDownloader() to set IDownloader")
        val shower =
            mShower ?: throw IllegalArgumentException("you must call setShower() to set IShower")
        val url = mUrl
        if (url.isEmpty()) throw IllegalArgumentException("url can not be empty")

        if (downloadJob != null) return// 正在下载

        shower.onDownloadPending()

        // 下载
        downloadJob = GlobalScope.launch(Dispatchers.Main) {
            downloader.downloadFile(url, downloadFile)
                .onCompletion {
                    if (it is CancellationException && it.message == PAUSE) {
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
                        DownloadInfo.Status.STATUS_SUCCESS -> {
                            context?.let {
                                ApkUtils.install(it, downloadFile)
                            }
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