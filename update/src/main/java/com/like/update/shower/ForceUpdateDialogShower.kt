package com.like.update.shower

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.like.common.base.BaseDialogFragment
import com.like.common.util.AppUtils
import com.like.floweventbus.FlowEventBus
import com.like.retrofit.util.getCustomNetworkMessage
import com.like.update.R
import com.like.update.databinding.DialogFragmentDownloadProgressBinding
import com.like.update.util.TAG_CANCEL
import com.like.update.util.TAG_CONTINUE
import com.like.update.util.TAG_PAUSE
import com.like.update.util.toDataStorageUnit
import kotlin.math.roundToInt

/**
 * 强制更新使用对话框显示进度条
 */
class ForceUpdateDialogShower(private val fragmentManager: FragmentManager) : IShower {
    private val downloadProgressDialog = DefaultDownloadProgressDialog.newInstance()

    override fun onDownloadPending() {
        downloadProgressDialog.show(fragmentManager)
        downloadProgressDialog.setTitle("正在连接服务器...")
        downloadProgressDialog.setMessage("")// 避免中途网络断开，然后重新连接后点击继续时，错误信息还是存在
    }

    override fun onDownloadRunning(currentSize: Long, totalSize: Long) {
        downloadProgressDialog.setTitle("下载中，请稍后...")
        downloadProgressDialog.setProgress(currentSize, totalSize)
    }

    override fun onDownloadPaused() {
        downloadProgressDialog.setTitle("已经暂停下载")
    }

    override fun onDownloadSuccess(totalSize: Long) {
        downloadProgressDialog.setTitle("下载完成！")
        downloadProgressDialog.setProgress(totalSize, totalSize)
    }

    override fun onDownloadFailed(throwable: Throwable?) {
        downloadProgressDialog.setTitle("下载失败！")
        downloadProgressDialog.setMessage(throwable.getCustomNetworkMessage())
    }

    class DefaultDownloadProgressDialog private constructor() : BaseDialogFragment() {
        companion object {
            fun newInstance(): DefaultDownloadProgressDialog {
                return DefaultDownloadProgressDialog()
            }
        }

        private var mBinding: DialogFragmentDownloadProgressBinding? = null
        private var mTitleText = "标题"
            set(value) {
                mBinding?.tvTitle?.text = value
                field = value
            }
        private var mTitleTextColor = Color.parseColor("#333333")
            set(value) {
                mBinding?.tvTitle?.setTextColor(value)
                field = value
            }
        private var mMessageText = "内容"
            set(value) {
                mBinding?.tvMessage?.visibility = if (value.isEmpty()) View.GONE else View.VISIBLE
                mBinding?.tvMessage?.text = value
                field = value
            }
        private var mMessageTextColor = Color.parseColor("#ef4a51")
            set(value) {
                mBinding?.tvMessage?.setTextColor(value)
                field = value
            }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val binding = DataBindingUtil.inflate<DialogFragmentDownloadProgressBinding>(
                inflater,
                R.layout.dialog_fragment_download_progress,
                container,
                false
            )
            mBinding = binding

            binding.btnPause.setOnClickListener {
                FlowEventBus.post(TAG_PAUSE)
            }
            binding.btnContinue.setOnClickListener {
                FlowEventBus.post(TAG_CONTINUE)
            }
            binding.ivClose.setOnClickListener {
                FlowEventBus.post(TAG_CANCEL)
                dismiss()
                AppUtils.exitApp(context)
            }
            isCancelable = false

            if (savedInstanceState != null) {
                setTitle(
                    savedInstanceState.getString("mTitle") ?: "",
                    savedInstanceState.getInt("mTitleTextColor")
                )
                setMessage(
                    savedInstanceState.getString("mMessage") ?: "",
                    savedInstanceState.getInt("mMessageTextColor")
                )
            } else {
                setTitle(mTitleText, mTitleTextColor)
                setMessage(mMessageText, mMessageTextColor)
            }
            return binding.root
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putString("mTitle", mTitleText)
            outState.putInt("mTitleTextColor", mTitleTextColor)
            outState.putString("mMessage", mMessageText)
            outState.putInt("mMessageTextColor", mMessageTextColor)
        }

        override fun initLayoutParams(layoutParams: WindowManager.LayoutParams) {
            super.initLayoutParams(layoutParams)
            resources.displayMetrics?.widthPixels?.let {
                layoutParams.width = (it * 0.9).toInt()
            }
        }

        @SuppressLint("SetTextI18n")
        fun setProgress(currentSize: Long, totalSize: Long) {
            mBinding?.apply {
                val progress = (currentSize.toFloat() / totalSize.toFloat() * 100).roundToInt()
                pbProgress.progress = progress
                tvPercent.text = "$progress%"
                tvSize.text = "${currentSize.toDataStorageUnit()}/${totalSize.toDataStorageUnit()}"
            }
        }

        fun setTitle(text: String = mTitleText, textColor: Int = mTitleTextColor) {
            mTitleText = text
            mTitleTextColor = textColor
        }

        fun setMessage(text: String = mMessageText, textColor: Int = mMessageTextColor) {
            mMessageText = text
            mMessageTextColor = textColor
        }

    }
}