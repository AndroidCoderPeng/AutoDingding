package com.pengxh.autodingding.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.InputDialog;
import com.pengxh.autodingding.BuildConfig;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.service.NotificationMonitorService;
import com.pengxh.autodingding.ui.HistoryActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.Utils;

import java.util.Objects;
import java.util.Set;

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
    @BindView(R.id.recordSize)
    TextView recordSize;
    @BindView(R.id.appVersion)
    TextView appVersion;
    Unbinder unbinder;
    private BroadcastManager broadcastManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.fragment_settings, null);
        unbinder = ButterKnife.bind(this, view);
        broadcastManager = BroadcastManager.getInstance(getContext());
        initEvent(getActivity());
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initEvent(Activity activity) {
        String emailAddress = Utils.readEmailAddress();
        if (!emailAddress.equals("")) {
            emailTextView.setText(emailAddress);
        }
        recordSize.setText(SQLiteUtil.getInstance().loadHistory().size() + "");
        broadcastManager.addAction(Constant.ACTION_UPDATE, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(Constant.ACTION_UPDATE)) {
                    recordSize.setText(SQLiteUtil.getInstance().loadHistory().size() + "");
                }
            }
        });
        appVersion.setText(BuildConfig.VERSION_NAME);

        boolean enabled = isNotificationListenerEnabled(activity.getApplicationContext());
        noticeSwitch.setChecked(enabled);
        if (!enabled) {
            openNotificationListenSettings();
        }
        toggleNotificationListenerService();
        Utils.createNotification();
    }

    //检测通知监听服务是否被授权
    public boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packageNames.contains(context.getPackageName());
    }

    //打开通知监听设置页面
    public void openNotificationListenSettings() {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //把应用的NotificationListenerService实现类disable再enable，即可触发系统rebind操作
    private void toggleNotificationListenerService() {
        Context context = Objects.requireNonNull(getContext());
        ComponentName componentName = new ComponentName(context, NotificationMonitorService.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
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
        broadcastManager.destroy(Constant.ACTION_UPDATE);
    }
}
