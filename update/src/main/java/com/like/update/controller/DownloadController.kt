package com.like.update.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.like.floweventbus.FlowEventBus
import com.like.floweventbus_annotations.BusObserver
import com.like.retrofit.download.model.DownloadInfo
import com.like.update.downloader.IDownloader
import com.like.update.shower.IShower
import com.like.update.util.*
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
        FlowEventBus.init()
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
                                install(it, DownloadFileProvider.AUTHORITY, downloadFile)
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

    /**
     * 安装apk
     *
     * 1、需要权限：<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * 2、android8.0 需要安装未知来源应用的权限：<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />，
     * 这样会在App调用安装界面的同时，系统会自动询问用户完成授权。注意：此权限用下面的方法无法检查。
     */
    private suspend fun install(context: Context, authority: String, apkFile: File) {
        val fileName = apkFile.name
        val suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase()
        if ("apk" != suffix) return

        // 通过device file explorer来查看这个下载下来的文件时，发现这个文件本身的权限是600，即文件拥有者只有读写权限，而没有运行权限。所以无法进行安装
        withContext(Dispatchers.IO) {
            val process =
                Runtime.getRuntime().exec("chmod 755 $apkFile")// 将apk权限改为755就可以了。注意，必须调用waitFor。
            process.waitFor()
        }
        try {
            val installIntent = Intent()
            val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // android7.0 需要通过FileProvider来获取文件uri。并开始强制启用StrictMode“严苛模式”，这个策略禁止在app外暴露 “file://“URI。
                // 为了与其他应用共享文件，你应该发送"content://"URI ，并授予临时访问权限。授予这个临时访问权限的最签单方法就是使用FileProvider类。
                installIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION // 授予目录临时共享权限
                // android7.0 需要通过FileProvider来获取文件uri。并开始强制启用StrictMode“严苛模式”，这个策略禁止在app外暴露 “file://“URI。
                // 为了与其他应用共享文件，你应该发送"content://"URI ，并授予临时访问权限。授予这个临时访问权限的最签单方法就是使用FileProvider类。
                // content://com.like.update.downloadfileprovider/download_file_path/fileName
                FileProvider.getUriForFile(context.applicationContext, authority, apkFile)
            } else {
                Uri.fromFile(apkFile)
            }
            installIntent.action = Intent.ACTION_VIEW
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            installIntent.setDataAndType(fileUri, "application/vnd.android.package-archive")
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}