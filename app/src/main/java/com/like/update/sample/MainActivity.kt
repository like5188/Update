package com.like.update.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import com.like.common.util.AppUtils
import com.like.retrofit.RequestConfig
import com.like.retrofit.download.DownloadRetrofit
import com.like.update.Update
import com.like.update.downloader.RetrofitDownloader
import com.like.update.sample.databinding.ActivityMainBinding
import com.like.update.shower.ForceUpdateDialogShower
import com.like.update.shower.NotificationShower

class MainActivity : AppCompatActivity() {
    private val mBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val mRetrofitDownloader by lazy {
        RetrofitDownloader(DownloadRetrofit().init(RequestConfig(application)))
    }
    private val mForceUpdateDialogShower by lazy {
        ForceUpdateDialogShower(this.supportFragmentManager)
    }
    private val mNotificationShower by lazy {
        object : NotificationShower(this@MainActivity) {
            override fun onBuilderCreated(builder: NotificationCompat.Builder) {
                builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            this@MainActivity,
                            2,
                            Intent(this@MainActivity, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
            }

            override fun onRemoteViewsCreated(remoteViews: RemoteViews) {
                remoteViews.setImageViewResource(R.id.iv_small_icon, R.drawable.icon_0)
                remoteViews.setImageViewResource(R.id.iv_large_icon, R.drawable.banner)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
    }

    @SuppressLint("MissingPermission")
    fun update(view: View) {
        val updateInfo = UpdateInfo().apply {
            // 是否需要更新。0：不需要；1：需要；2：必须更新
            isUpdate = 1
            versionName = "1.0"
            versionCode = 1
            downUrl = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk"
            message = "bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改"
        }
        when (updateInfo.isUpdate) {
            // 需要更新
            1 -> AlertDialog.Builder(this)
                .setTitle("发现新版本，是否更新？")
                .setMessage(updateInfo.message)
                .setCancelable(false)
                .setPositiveButton("马上更新") { dialog, _ ->
                    // 开始更新
                    Update.shower(mNotificationShower)
                    Update.downloader(mRetrofitDownloader)
                    Update.download(this, updateInfo.downUrl, updateInfo.versionName)
                    dialog.dismiss()
                }
                .setNegativeButton("下次再说") { dialog, _ ->
                    // 需要更新，但是不更新
                    dialog.dismiss()
                }
                .show()
        }
    }

    @SuppressLint("MissingPermission")
    fun forceUpdate(view: View) {
        val updateInfo = UpdateInfo().apply {
            // 是否需要更新。0：不需要；1：需要；2：必须更新
            isUpdate = 2
            versionName = "1.0"
            versionCode = 1
            downUrl = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk"
            message = "bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改bug修改"
        }
        when (updateInfo.isUpdate) {
            // 必须强制更新
            2 -> AlertDialog.Builder(this)
                .setTitle("发现新版本，您必须更新后才能继续使用！")
                .setMessage(updateInfo.message)
                .setCancelable(false)
                .setPositiveButton("马上更新") { dialog, _ ->
                    // 开始更新
                    Update.shower(mForceUpdateDialogShower)
                    Update.downloader(mRetrofitDownloader)
                    Update.download(this, updateInfo.downUrl, updateInfo.versionName)
                    dialog.dismiss()
                }.setNegativeButton("暂不使用") { dialog, _ ->
                    // 需要强制更新，但是不更新
                    dialog.dismiss()
                    AppUtils.exitApp(this)
                }
                .show()
        }
    }
}
