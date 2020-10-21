#### 最新版本

模块|Update
---|---
最新版本|[![Download](https://jitpack.io/v/like5188/Update.svg)](https://jitpack.io/#like5188/Update)

## 功能介绍

1、App 更新功能。

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
        implementation 'com.github.like5188:Common:6.1.3'
        implementation 'com.github.like5188.LiveDataBus:livedatabus:2.0.6'
        implementation 'com.github.like5188.LiveDataBus:livedatabus_annotations:2.0.6'

        implementation 'com.github.like5188:Update:版本号'
    }
```

2、使用
```java
    Update.shower(mNotificationShower).downloader(mRetrofitDownloader).download(this, downUrl, versionName)
```