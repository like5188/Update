package com.like.update.controller

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.like.common.util.ApkUtils
import com.like.update.TAG_CONTINUE
import com.like.update.TAG_PAUSE
import com.like.update.TAG_PAUSE_OR_CONTINUE
import com.like.update.downloader.IDownloader
import com.like.update.shower.IShower
import com.like.livedatabus.liveDataBusRegister
import com.like.livedatabus.liveDataBusUnRegister
import com.like.livedatabus_annotations.BusObserver
import com.like.retrofit.download.DownloadInfo
import com.like.retrofit.download.livedata.DownloadLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * 下载控制类。通过[com.like.livedatabus.LiveDataBus]来控制。
 *
 * Manifest.permission.INTERNET
 * Manifest.permission.WRITE_EXTERNAL_STORAGE
 */
@SuppressLint("MissingPermission")
class DownloadController {
    private var mCallLiveData: DownloadLiveData? = null
    private val mApkUtils: ApkUtils
    var mShower: IShower? = null
    var mDownloader: IDownloader? = null
    var mUrl: String = ""
    var mDownloadFile: File? = null

    constructor(fragmentActivity: FragmentActivity) {
        mApkUtils = ApkUtils(fragmentActivity)
        liveDataBusRegister()
    }

    constructor(fragment: Fragment) {
        mApkUtils = ApkUtils(fragment)
        liveDataBusRegister()
    }

    fun cancel() {
        mCallLiveData?.cancel()
        mCallLiveData = null
        liveDataBusUnRegister(TAG_PAUSE_OR_CONTINUE)
        liveDataBusUnRegister(TAG_PAUSE)
        liveDataBusUnRegister(TAG_CONTINUE)
    }

    /**
     * 如果是暂停，就继续下载；如果是下载中，就暂停
     */
    @BusObserver([TAG_PAUSE_OR_CONTINUE])
    fun pauseOrContinue() {
        if (mCallLiveData != null) {// 正在下载
            pause()
        } else {
            cont()
        }
    }

    @BusObserver([TAG_PAUSE])
    fun pause() {
        mCallLiveData?.pause()
        mCallLiveData = null
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @BusObserver([TAG_CONTINUE])
    fun cont() {
        val downloader = mDownloader ?: return
        val downloadFile = mDownloadFile ?: return
        val shower = mShower ?: return
        val url = mUrl
        if (url.isEmpty()) return

        if (mCallLiveData != null) return// 正在下载

        shower.onDownloadPending()

        // 下载
        GlobalScope.launch(Dispatchers.Main) {
            mCallLiveData = downloader.download(url, downloadFile, 3)
            mCallLiveData?.observeForever { downloadInfo ->
                when (downloadInfo.status) {
                    DownloadInfo.Status.STATUS_PENDING -> {
                    }
                    DownloadInfo.Status.STATUS_RUNNING -> {
                        shower.onDownloadRunning(downloadInfo.cachedSize, downloadInfo.totalSize)
                    }
                    DownloadInfo.Status.STATUS_PAUSED -> {
                        shower.onDownloadPaused(downloadInfo.cachedSize, downloadInfo.totalSize)
                    }
                    DownloadInfo.Status.STATUS_SUCCESSFUL -> {
                        mApkUtils.install(downloadFile)
                        mCallLiveData = null
                        shower.onDownloadSuccessful(downloadInfo.totalSize)
                    }
                    DownloadInfo.Status.STATUS_FAILED -> {
                        shower.onDownloadFailed(downloadInfo.throwable)
                        mCallLiveData = null// 用于点击继续重试
                    }
                }
            }
        }
    }

}