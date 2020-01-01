package com.like.update.shower

/**
 * 显示者
 */
interface IShower {

    /**
     * 即将开始下载数据
     */
    fun onDownloadPending()

    /**
     * 正在下载
     */
    fun onDownloadRunning(currentSize: Long, totalSize: Long)

    /**
     * 暂停下载
     */
    fun onDownloadPaused(currentSize: Long, totalSize: Long)

    /**
     * 下载成功
     */
    fun onDownloadSuccessful(totalSize: Long)

    /**
     * 下载失败
     */
    fun onDownloadFailed(throwable: Throwable?)

}