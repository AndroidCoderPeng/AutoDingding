package com.pengxh.autodingding.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.pengxh.app.multilib.widget.EasyToast;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class WelcomeActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int PERMISSIONS_CODE = 999;
    private static final String[] USER_PERMISSIONS = {
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.DISABLE_KEYGUARD,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断是否有权限，如果版本大于5.1才需要判断（即6.0以上），其他则不需要判断。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(this, USER_PERMISSIONS)) {
                startMainActivity();
            } else {
                EasyPermissions.requestPermissions(this, "需要获取相关权限", PERMISSIONS_CODE, USER_PERMISSIONS);
            }
        } else {
            startMainActivity();
        }
    }

    private boolean checkPermission(Activity mActivity, String[] perms) {
        return EasyPermissions.hasPermissions(mActivity, perms);
    }

    private void startMainActivity() {
        startActivity(new Intent(this, DingDingClockActivity.class));
        finish();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        startMainActivity();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (perms.size() == USER_PERMISSIONS.length) {//授权全部失败，则提示用户
            EasyToast.showToast("授权失败", EasyToast.ERROR);
            handler.sendEmptyMessageDelayed(1, 1500);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                finish();
            }
            super.handleMessage(msg);
        }
    };
}