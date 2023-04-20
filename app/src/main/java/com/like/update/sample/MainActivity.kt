package com.like.update.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.retrofit.RequestConfig
import com.like.retrofit.download.DownloadRetrofit
import com.like.update.UpdateUtils
import com.like.update.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val mBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }
    private val updateUtils by lazy {
        UpdateUtils(
            this,
            DownloadRetrofit().init(RequestConfig(application)),
            R.drawable.ic_1,
            R.drawable.icon_0
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
    }

    @SuppressLint("MissingPermission")
    fun update(view: View) {
        updateUtils.update(
            "bug修改",
            "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk",
            "1.0"
        )
    }

    @SuppressLint("MissingPermission")
    fun forceUpdate(view: View) {
        updateUtils.forceUpdate(
            "bug修改",
            "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk",
            "1.0"
        )
    }
}
