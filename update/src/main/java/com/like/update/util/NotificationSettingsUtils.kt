package com.like.update.util

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

object NotificationSettingsUtils {
    /**
     * 检查是否有通知权限
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * 打开通知权限设置界面
     */
    fun openNotificationSettingsForApp(context: Context) {
        // Links to this app's notification settings.
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", context.packageName)
        intent.putExtra("app_uid", context.applicationInfo.uid)
        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
        context.startActivity(intent)
    }
}