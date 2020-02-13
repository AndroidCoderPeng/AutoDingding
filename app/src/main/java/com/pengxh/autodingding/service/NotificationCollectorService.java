package com.pengxh.autodingding.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.Utils;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO 检查通知栏监听服务是否运行中。应用进程被杀后再次启动时，服务不生效，导致通知栏有内容变更，服务无法感知
 * @date: 2020/2/13 20:07
 */
public class NotificationCollectorService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Utils.isServiceAlive(Constant.SERVICE_NAME)) {
            Log.d("CollectorService", "onCreate: 通知栏监听服务正在运行中");
            return;
        }
        toggleNotificationListenerService();
    }

    //重启通知栏监听服务
    private void toggleNotificationListenerService() {
        ComponentName componentName = new ComponentName(this, NotificationMonitorService.class);
        PackageManager packageManager = getPackageManager();
        //先禁用
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        //再启用
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
