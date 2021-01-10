package com.pengxh.autodingding.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.InputDialog;
import com.pengxh.autodingding.BaseFragment;
import com.pengxh.autodingding.BuildConfig;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.service.NotificationMonitorService;
import com.pengxh.autodingding.ui.HistoryActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.StatusBarColorUtil;
import com.pengxh.autodingding.utils.Utils;

import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bertsir.zbar.utils.QRUtils;

public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "SettingsFragment";

    @BindView(R.id.mTitleLeftView)
    ImageView mTitleLeftView;
    @BindView(R.id.mTitleView)
    TextView mTitleView;
    @BindView(R.id.mTitleRightView)
    ImageView mTitleRightView;
    @BindView(R.id.emailTextView)
    TextView emailTextView;
    @BindView(R.id.noticeCheckBox)
    CheckBox noticeCheckBox;
    @BindView(R.id.recordSize)
    TextView recordSize;
    @BindView(R.id.appVersion)
    TextView appVersion;
    @BindView(R.id.updateCodeView)
    ImageView updateCodeView;

    private BroadcastManager broadcastManager;
    private SQLiteUtil sqLiteUtil;
    private Context context;
    private FragmentActivity activity;

    @Override
    protected int initLayoutView() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void initData() {
        context = getContext();
        activity = getActivity();

        mTitleLeftView.setVisibility(View.GONE);
        mTitleView.setText("其他设置");
        mTitleRightView.setVisibility(View.GONE);

        broadcastManager = BroadcastManager.getInstance(context);
        sqLiteUtil = SQLiteUtil.getInstance();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initEvent() {
        String emailAddress = Utils.readEmailAddress();
        if (!emailAddress.equals("")) {
            emailTextView.setText(emailAddress);
        }
        recordSize.setText(sqLiteUtil.loadHistory().size() + "");
        broadcastManager.addAction(Constant.ACTION_UPDATE, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(Constant.ACTION_UPDATE)) {
                    recordSize.setText(sqLiteUtil.loadHistory().size() + "");
                }
            }
        });
        appVersion.setText(BuildConfig.VERSION_NAME);

        boolean enabled = isNotificationListenerEnabled();
        noticeCheckBox.setChecked(enabled);
        if (!enabled) {
            openNotificationListenSettings();
        }
        toggleNotificationListenerService();
        //创建常住通知栏
        Utils.createNotification();

        //先识别出来备用
        try {
            String codeValue = QRUtils.getInstance().decodeQRcode(updateCodeView);
            SaveKeyValues.putValue("updateLink", codeValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateCodeView.setOnLongClickListener(v -> {
            String updateLink = (String) SaveKeyValues.getValue("updateLink", "https://www.pgyer.com/MBGt");
            Utils.showAlertDialog(activity, "识别结果", updateLink, "前往更新页面(密码：123)", true,
                    (dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(updateLink);
                        intent.setData(content_url);
                        startActivity(intent);
                    });
            return true;
        });
    }

    @Override
    public void initImmersionBar() {
        StatusBarColorUtil.setColor(activity, Color.parseColor("#0094FF"));
        ImmersionBar.with(this).init();
    }

    //检测通知监听服务是否被授权
    private boolean isNotificationListenerEnabled() {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packageNames.contains(context.getPackageName());
    }

    //打开通知监听设置页面
    private void openNotificationListenSettings() {
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
        ComponentName componentName = new ComponentName(context, NotificationMonitorService.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @OnClick({R.id.emailLayout, R.id.historyLayout, R.id.introduceLayout})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emailLayout:
                new InputDialog.Builder().setContext(context).setTitle("设置邮箱").setNegativeButton("取消").setPositiveButton("确定").setOnDialogClickListener(new InputDialog.onDialogClickListener() {
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
                startActivity(new Intent(context, HistoryActivity.class));
                break;
            case R.id.introduceLayout:
                Utils.showAlertDialog(activity, "功能介绍", context.getString(R.string.about), "看完了", true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        broadcastManager.destroy(Constant.ACTION_UPDATE);
    }
}
