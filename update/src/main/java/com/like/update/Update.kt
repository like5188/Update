package com.like.update

import android.content.Context
import android.os.Environment
import com.like.common.util.storage.external.ExternalStoragePrivateUtils
import com.like.update.controller.DownloadController
import com.like.update.downloader.IDownloader
import com.like.update.shower.IShower
import java.io.File

/**
 * 文件下载路径：
 * context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
 * /storage/emulated/(0/1/...)/Android/data/packageName/files/Download
 */
object Update {
    private val mDownloadController by lazy {
        DownloadController()
    }

    /**
     *
     * @param shower        显示者。
     * 库中提供了：[com.like.update.shower.ForceUpdateDialogShower]、[com.like.update.shower.NotificationShower]
     */
    fun shower(shower: IShower): Update {
        mDownloadController.mShower = shower
        return this
    }

    /**
     *
     * @param downloader    下载工具类。
     * 库中提供了：[com.like.update.downloader.RetrofitDownloader]
     */
    fun downloader(downloader: IDownloader): Update {
        mDownloadController.mDownloader = downloader
        return this
    }

    /**
     *
     * @param url           下载地址。必须设置。可以是完整路径或者子路径
     * @param versionName   下载的文件的版本号。可以不设置。用于区分下载的文件的版本。如果url中包括了版本号，可以不传。
     */
    fun download(context: Context, url: String, versionName: String = "") {
        mDownloadController.apply {
            this.context = context
            this.mUrl = url
            this.mDownloadFile = createDownloadFile(context, url, versionName)
            this.cont()
        }
    }

    fun cancel() {
        mDownloadController.cancel()
    }

    private fun createDownloadFile(context: Context, url: String, versionName: String): File? =
        try {
            val downloadFileName = if (versionName.isNotEmpty()) {
                val lastPointPosition = url.lastIndexOf(".")// 最后一个"."的位置
                val fileName = url.substring(
                    url.lastIndexOf("/") + 1,
                    lastPointPosition
                )// 从url获取的文件名，不包括后缀。"xxx"
                val fileSuffix = url.substring(lastPointPosition)// 后缀。".apk"
                "$fileName-$versionName$fileSuffix"
            } else {
                url.substring(url.lastIndexOf("/") + 1)// 从url获取的文件名，包括后缀。"xxx.xxx"
            }
            File(
                ExternalStoragePrivateUtils.getExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS
                ),
                downloadFileName
            )
        } catch (e: Exception) {
            null
        }
}