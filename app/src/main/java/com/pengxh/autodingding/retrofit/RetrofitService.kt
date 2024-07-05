package com.pengxh.autodingding.retrofit

import retrofit2.http.GET

interface RetrofitService {
    /**
     * 工作日判断
     */
    @GET("/work")
    suspend fun checkDayByDate(): String
}