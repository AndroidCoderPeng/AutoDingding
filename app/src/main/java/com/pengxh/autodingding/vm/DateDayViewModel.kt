package com.pengxh.autodingding.vm

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.pengxh.autodingding.model.DateDayModel
import com.pengxh.autodingding.retrofit.RetrofitServiceManager
import com.pengxh.kt.lite.base.BaseViewModel
import com.pengxh.kt.lite.extensions.launch

class DateDayViewModel : BaseViewModel() {
    private val gson = Gson()
    val dayTypeResult = MutableLiveData<Boolean>()

    fun checkDayByDate() = launch({
        val response = RetrofitServiceManager.checkDayByDate()
        if (response.isBlank()) {
            //返回值为空，发送邮件

        }

        val element = JsonParser.parseString(response)
        val jsonObject = element.asJsonObject
        val code = jsonObject.get("code").asInt
        if (code == 200) {
            val result = gson.fromJson<DateDayModel>(
                response, object : TypeToken<DateDayModel>() {}.type
            )

            dayTypeResult.value = result.data.isWork
        } else {
            //请求失败，发送邮件

        }
    }, {
        it.printStackTrace()
    })
}