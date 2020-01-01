package com.like.update

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.like.common.util.PermissionUtils
import com.like.update.controller.DownloadController
import com.like.update.downloader.IDownloader
import com.like.update.downloader.RetrofitDownloader
import com.like.update.shower.ForceUpdateDialogShower
import com.like.update.shower.IShower
import java.io.File

class Update {
    private val mContext: Context?
    private val mDownloadController: DownloadController
    private val mPermissionUtils: PermissionUtils

    constructor(fragmentActivity: FragmentActivity) {
        mContext = fragmentActivity.applicationContext
        mDownloadController = DownloadController(fragmentActivity)
        mPermissionUtils = PermissionUtils(fragmentActivity)
        init(fragmentActivity.supportFragmentManager, fragmentActivity.application)
    }

    constructor(fragment: Fragment) {
        mContext = fragment.context?.applicationContext ?: throw IllegalArgumentException("can not get context from fragment")
        mDownloadController = DownloadController(fragment)
        mPermissionUtils = PermissionUtils(fragment)
        fragment.activity?.application?.let {
            init(fragment.childFragmentManager, it)
        }
    }

    private fun init(fragmentManager: FragmentManager, application: Application) {
        setShower(ForceUpdateDialogShower(fragmentManager))
        setDownloader(RetrofitDownloader(application))
    }

    /**
     *
     * @param shower        显示者。默认为[com.like.update.shower.ForceUpdateDialogShower]
     */
    fun setShower(shower: IShower) {
        mDownloadController.mShower = shower
    }

    /**
     *
     * @param downloader    下载工具类。默认为[com.like.update.downloader.RetrofitDownloader]
     */
    fun setDownloader(downloader: IDownloader) {
        mDownloadController.mDownloader = downloader
    }

    /**
     *
     * @param url           下载地址。必须设置。可以是完整路径或者子路径
     * @param versionName   下载的文件的版本号。可以不设置。用于区分下载的文件的版本。如果url中包括了版本号，可以不传。
     */
    fun setUrl(url: String, versionName: String = "") {
        require(url.isNotEmpty()) { "url can not be empty" }
        val context = mContext ?: throw IllegalArgumentException("can not get context from fragment")
        val downloadFile = createDownloadFile(context, url, versionName) ?: throw IllegalArgumentException("wrong download url")
        mDownloadController.mUrl = url
        mDownloadController.mDownloadFile = downloadFile
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun download() {
        mPermissionUtils.checkStoragePermissionGroup {
            mDownloadController.cont()
        }
    }

    fun cancel() {
        mDownloadController.cancel()
    }

    private fun createDownloadFile(context: Context, url: String, versionName: String): File? = try {
        val downloadFileName = if (versionName.isNotEmpty()) {
            val lastPointPosition = url.lastIndexOf(".")// 最后一个"."的位置
            val fileName = url.substring(url.lastIndexOf("/") + 1, lastPointPosition)// 从url获取的文件名，不包括后缀。"xxx"
            val fileSuffix = url.substring(lastPointPosition)// 后缀。".apk"
            "$fileName-$versionName$fileSuffix"
        } else {
            url.substring(url.lastIndexOf("/") + 1)// 从url获取的文件名，包括后缀。"xxx.xxx"
        }
        File(context.cacheDir, downloadFileName)
    } catch (e: Exception) {
        null
    }
}