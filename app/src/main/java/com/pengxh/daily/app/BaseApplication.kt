package com.pengxh.daily.app

import android.app.Application
import com.pengxh.daily.app.greendao.DaoMaster
import com.pengxh.daily.app.greendao.DaoSession
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.tencent.bugly.crashreport.CrashReport
import kotlin.properties.Delegates

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 13:19
 */
class BaseApplication : Application() {

    companion object {
        private var application: BaseApplication by Delegates.notNull()

        fun get() = application
    }

    lateinit var daoSession: DaoSession

    override fun onCreate() {
        super.onCreate()
        application = this
        SaveKeyValues.initSharedPreferences(this)
        CrashReport.initCrashReport(this, "ce38195468", false)
        initDataBase()
    }

    private fun initDataBase() {
        val helper = DaoMaster.DevOpenHelper(this, "DingRecord.db")
        val daoMaster = DaoMaster(helper.writableDatabase)
        daoSession = daoMaster.newSession()
    }
}