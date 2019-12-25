package com.pengxh.audodingding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.pengxh.app.multilib.widget.EasyToast;

public class WelcomeActivity extends AppCompatActivity {

    private static final int PERMISSIONS_CODE = 999;
    private static final String[] USER_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断是否有权限，如果版本大于5.1才需要判断（即6.0以上），其他则不需要判断。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasAllPermission()) {
                startMainActivity();
            } else {
                ActivityCompat.requestPermissions(this, USER_PERMISSIONS, PERMISSIONS_CODE);
            }
        } else {
            startMainActivity();
        }
    }

    private boolean hasAllPermission() {
        for (String permission : USER_PERMISSIONS) {
            int i = ActivityCompat.checkSelfPermission(this, permission);
            return i == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_CODE) {
            //权限全部获取,grantResults数组里面元素：[0,0.......,0]
            //权限拒绝,grantResults数组里面元素：[-1,-1.......,-1]
            boolean grant = true;
            if (grantResults.length == 0) {
                grant = false;
            }
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    grant = false;
                }
            }
            if (grant) {
                startMainActivity();
            } else {
                EasyToast.showToast("权限被拒绝", EasyToast.ERROR);
                handler.sendEmptyMessageDelayed(1, 1500);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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