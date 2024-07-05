package com.pengxh.autodingding.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.pengxh.autodingding.extensions.createMail
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.model.DateDayModel
import com.pengxh.autodingding.retrofit.RetrofitServiceManager
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.BaseViewModel
import com.pengxh.kt.lite.extensions.launch
import com.pengxh.kt.lite.utils.SaveKeyValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class DateDayViewModel : BaseViewModel() {

    private val kTag = "DateDayViewModel"
    private val gson = Gson()
    val dayTypeResult = MutableLiveData<Boolean>()

    fun checkDayByDate(context: CoroutineContext) = launch({
        val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
        if (emailAddress.isEmpty()) {
            Log.d(kTag, "checkDayByDate: 邮箱地址为空")
            return@launch
        }

        val response = RetrofitServiceManager.checkDayByDate()
        if (response.isBlank()) {
            //返回值为空，发送邮件
            CoroutineScope(context).launch(Dispatchers.IO) {
                "工作日判断接口返回值异常，无法确定自动打卡日期，请及时手动打卡！".createMail(emailAddress)
                    .sendTextMail()
            }
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
            CoroutineScope(context).launch(Dispatchers.IO) {
                "工作日判断接口请求异常，错误码：${code}，无法确定自动打卡日期，请及时手动打卡！"
                    .createMail(emailAddress).sendTextMail()
            }
        }
    }, {
        it.printStackTrace()
    })
}