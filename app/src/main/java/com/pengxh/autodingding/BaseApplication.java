package com.pengxh.autodingding;

import android.app.Application;

import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.utils.Utils;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2019/12/25 13:19
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        EasyToast.init(this);
//        SQLiteUtil.initDataBase(this);
        SaveKeyValues.initSharedPreferences(this);
    }
}
