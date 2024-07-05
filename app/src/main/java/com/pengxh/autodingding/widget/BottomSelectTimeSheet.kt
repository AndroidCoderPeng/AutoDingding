package com.pengxh.autodingding.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import com.pengxh.autodingding.databinding.BottomSelectTimeSheetBinding
import com.pengxh.kt.lite.R
import com.pengxh.kt.lite.extensions.binding
import com.pengxh.kt.lite.extensions.resetParams

class BottomSelectTimeSheet constructor(context: Context) :
    Dialog(context, R.style.UserDefinedActionStyle) {

    private val binding: BottomSelectTimeSheetBinding by binding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.resetParams(Gravity.BOTTOM, R.style.ActionSheetDialogAnimation, 1f)
        setCancelable(true)
        setCanceledOnTouchOutside(true)



        binding.sheetConfirmView.setOnClickListener {
            //保存数据
        }
    }
}