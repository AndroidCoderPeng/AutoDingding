package com.pengxh.autodingding.retrofit

import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.utils.RetrofitFactory

object RetrofitServiceManager {
    private val api by lazy {
        RetrofitFactory.createRetrofit<RetrofitService>(Constant.DATE_DAY_API)
    }

    /**
     * 工作日判断
     */
    suspend fun checkDayByDate(): String {
        return api.checkDayByDate()
    }
}