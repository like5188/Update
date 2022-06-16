package com.like.update

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentActivity
import com.like.common.util.AppUtils
import com.like.retrofit.download.DownloadRetrofit
import com.like.update.downloader.RetrofitDownloader
import com.like.update.shower.ForceUpdateDialogShower
import com.like.update.shower.NotificationShower
import com.like.update.util.NotificationSettingsUtils

/**
 * app 更新工具类，对[Update]进行了封装，提供了强制更新和普通更新相关逻辑及对话框界面。
 *
 * @param smallNotificationIcon     通知栏小图标
 * @param bigNotificationIcon       通知栏大图标
 * @param jumpIntent                点击通知跳转到某个界面的 Intent
 */
class UpdateUtils(
    private val activity: FragmentActivity,
    downloadRetrofit: DownloadRetrofit,
    @DrawableRes smallNotificationIcon: Int,
    @DrawableRes bigNotificationIcon: Int,
    jumpIntent: Intent? = null,
) {
    private val mRetrofitDownloader by lazy {
        RetrofitDownloader(downloadRetrofit)
    }
    private val mForceUpdateDialogShower by lazy {
        ForceUpdateDialogShower(activity.supportFragmentManager)
    }
    private val mNotificationShower by lazy {
        object : NotificationShower(activity) {
            override fun onBuilderCreated(builder: NotificationCompat.Builder) {
                builder.setSmallIcon(smallNotificationIcon)
                if (jumpIntent != null) {
                    builder.setContentIntent(
                        // 通知点击行为
                        PendingIntent.getActivity(
                            activity,
                            2,
                            jumpIntent,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            } else {
                                PendingIntent.FLAG_UPDATE_CURRENT
                            }
                        )
                    )
                }
            }

            override fun onRemoteViewsCreated(remoteViews: RemoteViews) {
                remoteViews.setImageViewResource(R.id.iv_small_icon, smallNotificationIcon)
                remoteViews.setImageViewResource(R.id.iv_large_icon, bigNotificationIcon)
            }
        }
    }

    /**
     * 非强制更新
     */
    fun update(message: String, downUrl: String, versionName: String) {
        if (!NotificationSettingsUtils.areNotificationsEnabled(activity)) {
            AlertDialog.Builder(activity)
                .setTitle("您需要打开通知权限，才能在通知中看到下载进度！")
                .setCancelable(false)
                .setPositiveButton("去设置") { dialog, _ ->
                    NotificationSettingsUtils.openNotificationSettingsForApp(activity)
                    dialog.dismiss()
                }
                .setNegativeButton("下次再说") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            return
        }
        AlertDialog.Builder(activity)
            .setTitle("发现新版本，是否更新？")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("马上更新") { dialog, _ ->
                // 开始更新
                Update.shower(mNotificationShower)
                    .downloader(mRetrofitDownloader)
                    .download(activity, downUrl, versionName)
                dialog.dismiss()
            }
            .setNegativeButton("下次再说") { dialog, _ ->
                // 需要更新，但是不更新
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 强制更新
     */
    fun forceUpdate(message: String, downUrl: String, versionName: String) {
        AlertDialog.Builder(activity)
            .setTitle("发现新版本，您必须更新后才能继续使用！")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("马上更新") { dialog, _ ->
                // 开始更新
                Update.shower(mForceUpdateDialogShower)
                    .downloader(mRetrofitDownloader)
                    .download(activity, downUrl, versionName)
                dialog.dismiss()
            }.setNegativeButton("暂不使用") { dialog, _ ->
                // 需要强制更新，但是不更新
                dialog.dismiss()
                AppUtils.exitApp(activity)
            }
            .show()
    }
}