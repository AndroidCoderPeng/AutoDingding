package com.pengxh.autodingding.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.InputDialog;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.service.NotificationMonitorService;
import com.pengxh.autodingding.ui.HistoryActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingsFragment";

    @BindView(R.id.emailTextView)
    TextView emailTextView;
    @BindView(R.id.noticeSwitch)
    Switch noticeSwitch;
    @BindView(R.id.appVersion)
    TextView appVersion;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.fragment_settings, null);
        unbinder = ButterKnife.bind(this, view);
        initEvent(getActivity());
        return view;
    }

    private void initEvent(Activity mActivity) {
        String emailAddress = Utils.readEmailAddress();
        if (!emailAddress.equals("")) {
            emailTextView.setText(emailAddress);
        }

        PackageManager manager = mActivity.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(mActivity.getPackageName(), 0);
            appVersion.setText(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        noticeSwitch.setChecked(Utils.isServiceAlive(Constant.SERVICE_NAME));
        noticeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "onCheckedChanged: 打开通知监听");
                    String string = Settings.Secure.getString(mActivity.getContentResolver(), "enabled_notification_listeners");
                    if (!string.contains(NotificationMonitorService.class.getName())) {
                        Utils.openNotificationSettings();
                        return;
                    }
                    mActivity.startService(new Intent(mActivity, NotificationMonitorService.class));
                }
            }
        });
    }

    @OnClick({R.id.emailLayout, R.id.historyLayout, R.id.introduceLayout, R.id.updateLayout})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emailLayout:
                new InputDialog.Builder().setContext(getContext()).setTitle("设置邮箱").setNegativeButton("取消").setPositiveButton("确定").setOnDialogClickListener(new InputDialog.onDialogClickListener() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String input) {
                        if (!input.isEmpty()) {
                            Utils.saveEmailAddress(input);
                            emailTextView.setText(input);
                            dialog.dismiss();
                        } else {
                            EasyToast.showToast("什么都还没输入呢！", EasyToast.ERROR);
                        }
                    }

                    @Override
                    public void onCancelClick(Dialog dialog) {
                        dialog.dismiss();
                    }
                }).build().show();
                break;
            case R.id.historyLayout:
                startActivity(new Intent(getActivity(), HistoryActivity.class));
                break;
            case R.id.introduceLayout:
                new AlertView("功能介绍", getResources().getString(R.string.about),
                        null, new String[]{"确定"}, null,
                        getContext(), AlertView.Style.Alert,
                        null).setCancelable(false).show();
                break;
            case R.id.updateLayout:
                Utils.showProgress(getActivity(), "正在检查更新......");
                new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        Utils.hideProgress();
                        EasyToast.showToast("已是最新版本", EasyToast.SUCCESS);
                    }
                }.start();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
