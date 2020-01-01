#### 最新版本

模块|Update
---|---
最新版本|[![Download](https://jitpack.io/v/like5188/Update.svg)](https://jitpack.io/#like5188/Update)

## 功能介绍

1、App 更新功能。

2、已经处理了权限。

## 使用方法：

1、引用

在Project的gradle中加入：
```groovy
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```
在Module的gradle中加入：
```groovy
    dependencies {
        // 这个库的引用不能用 compileOnly 否则 gradle:3.5.2 编译不过，但是 gradle:3.2.1 能编译通过。
        implementation 'com.github.like5188:Common:5.2.5'

        compileOnly 'com.github.like5188:LibRetrofit:2.0.6'
        compileOnly 'com.github.like5188.LiveDataBus:livedatabus:2.0.5'
    //    gradle 3.2.1 不需要添加这个引用 ， 升级到 3.5.2 后必须添加 ， 否则会提示找不到livedatabus_annotations中的类 。
        compileOnly 'com.github.like5188.LiveDataBus:livedatabus_annotations:2.0.5'
        kapt 'com.github.like5188.LiveDataBus:livedatabus_compiler:2.0.5'

        implementation 'com.github.like5188:Update:版本号'
    }
```

2、使用
```java
    // 初始化
    private val mUpdate: Update by lazy { Update(this) }
    // 非强制更新。通知栏显示更新进度
    mUpdate.setUrl(updateInfo.downUrl, updateInfo.versionName)
    mUpdate.setShower(
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
    )
    mUpdate.download()
    // 强制更新。对话框显示更新进度
    mUpdate.setUrl(updateInfo.downUrl, updateInfo.versionName)
    mUpdate.setShower(ForceUpdateDialogShower(this.supportFragmentManager))
    mUpdate.download()
```