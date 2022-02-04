package com.pengxh.autodingding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.utils.Constant;

import java.lang.ref.WeakReference;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class WelcomeActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private WeakReferenceHandler weakReferenceHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weakReferenceHandler = new WeakReferenceHandler(this);
        //判断是否有权限，如果版本大于5.1才需要判断（即6.0以上），其他则不需要判断。
        if (EasyPermissions.hasPermissions(this, Constant.USER_PERMISSIONS)) {
            startMainActivity();
        } else {
            EasyPermissions.requestPermissions(this, "需要获取相关权限", Constant.PERMISSIONS_CODE, Constant.USER_PERMISSIONS);
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        startMainActivity();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (perms.size() == Constant.USER_PERMISSIONS.length) {//授权全部失败，则提示用户
            EasyToast.showToast("授权失败", EasyToast.ERROR);
            weakReferenceHandler.sendEmptyMessageDelayed(20220104, 1500);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private static class WeakReferenceHandler extends Handler {

        private final WeakReference<WelcomeActivity> reference;

        private WeakReferenceHandler(WelcomeActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            WelcomeActivity activity = reference.get();
            if (msg.what == 20220104) {
                activity.finish();
            }
        }
    }
}