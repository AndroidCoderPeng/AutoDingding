package com.pengxh.autodingding

import android.app.Application
import com.pengxh.autodingding.greendao.DaoMaster
import com.pengxh.autodingding.greendao.DaoSession
import com.pengxh.kt.lite.utils.SaveKeyValues
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
        initDataBase()
    }

    private fun initDataBase() {
        val helper = DaoMaster.DevOpenHelper(this, "DingRecord.db")
        val daoMaster = DaoMaster(helper.writableDatabase)
        daoSession = daoMaster.newSession()
    }
}