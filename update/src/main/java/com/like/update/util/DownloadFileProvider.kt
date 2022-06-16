package com.like.update.util

import androidx.core.content.FileProvider

/*
Manifest merger failed : Attribute provider#androidx.core.content.FileProvider@authorities value=(com.like.update.downloadfileprovider) from [:update] AndroidManifest.xml:15:13-71
	is also present at [com.github.like5188:Common:6.9.2] AndroidManifest.xml:53:13-64 value=(com.like.update.sample.fileprovider).
 */
class DownloadFileProvider : FileProvider(){
    companion object{
        const val AUTHORITY = "com.like.update.downloadfileprovider"
    }
}