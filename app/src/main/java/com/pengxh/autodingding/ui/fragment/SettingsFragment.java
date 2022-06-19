package com.pengxh.autodingding.ui.fragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationManagerCompat;

import com.pengxh.androidx.lite.base.AndroidxBaseFragment;
import com.pengxh.androidx.lite.utils.ContextUtil;
import com.pengxh.androidx.lite.utils.SaveKeyValues;
import com.pengxh.androidx.lite.widget.EasyToast;
import com.pengxh.androidx.lite.widget.dialog.AlertInputDialog;
import com.pengxh.androidx.lite.widget.dialog.AlertMessageDialog;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.BuildConfig;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.databinding.FragmentSettingsBinding;
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao;
import com.pengxh.autodingding.ui.HistoryRecordActivity;
import com.pengxh.autodingding.utils.Constant;

import java.util.Set;

import cn.bertsir.zbar.utils.QRUtils;

public class SettingsFragment extends AndroidxBaseFragment<FragmentSettingsBinding> {

    private static final String TAG = "SettingsFragment";
    private HistoryRecordBeanDao historyBeanDao;
    private NotificationManager notificationManager;

    @Override
    protected void setupTopBarLayout() {

    }

    @Override
    protected void initData() {
        historyBeanDao = BaseApplication.getDaoSession().getHistoryRecordBeanDao();
        notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String emailAddress = (String) SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "");
        if (!TextUtils.isEmpty(emailAddress)) {
            viewBinding.emailTextView.setText(emailAddress);
        }
        viewBinding.appVersion.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void initEvent() {
        viewBinding.emailLayout.setOnClickListener(v -> new AlertInputDialog.Builder()
                .setContext(requireContext())
                .setTitle("设置邮箱")
                .setHintMessage("请输入邮箱")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogButtonClickListener(new AlertInputDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onConfirmClick(String value) {
                        if (!TextUtils.isEmpty(value)) {
                            SaveKeyValues.putValue(Constant.EMAIL_ADDRESS, value);
                            viewBinding.emailTextView.setText(value);
                        } else {
                            EasyToast.show(requireContext(), "什么都还没输入呢！");
                        }
                    }

                    @Override
                    public void onCancelClick() {

                    }
                }).build().show());
        viewBinding.historyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextUtil.navigatePageTo(requireContext(), HistoryRecordActivity.class);
            }
        });
        viewBinding.introduceLayout.setOnClickListener(v -> new AlertMessageDialog.Builder()
                .setContext(requireContext())
                .setTitle("功能介绍")
                .setMessage(requireContext().getString(R.string.about))
                .setPositiveButton("看完了")
                .setOnDialogButtonClickListener(() -> {

                }).build().show());

        if (!notificationEnable()) {
            try {
                //打开通知监听设置页面
                requireContext().startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //创建常住通知栏
            createNotification();
        }

        //先识别出来备用
        try {
            String codeValue = QRUtils.getInstance().decodeQRcode(viewBinding.updateCodeView);
            SaveKeyValues.putValue("updateLink", codeValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        viewBinding.updateCodeView.setOnLongClickListener(v -> {
            String updateLink = (String) SaveKeyValues.getValue("updateLink", "https://www.pgyer.com/MBGt");
            new AlertMessageDialog.Builder()
                    .setContext(requireContext())
                    .setTitle("识别结果")
                    .setMessage(updateLink)
                    .setPositiveButton("前往更新页面(密码：123)")
                    .setOnDialogButtonClickListener(() -> {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(Uri.parse(updateLink));
                        startActivity(intent);
                    }).build().show();
            return true;
        });
    }

    /**
     * 每次切换到此页面都需要重新计算记录
     */
    @Override
    public void onResume() {
        Log.d(TAG, "recordSize ===> " + historyBeanDao.loadAll().size());
        viewBinding.recordSize.setText(String.valueOf(historyBeanDao.loadAll().size()));
        viewBinding.noticeCheckBox.setChecked(notificationEnable());
        super.onResume();
    }

    //检测通知监听服务是否被授权
    private boolean notificationEnable() {
        Set<String> packages = NotificationManagerCompat.getEnabledListenerPackages(requireContext());
//        for (String aPackage : packages) {
//            Log.d(TAG, "notificationEnable ===> "+aPackage);
//        }
        return packages.contains(requireContext().getPackageName());
    }

    private void createNotification() {
        //Android8.0以上必须添加 渠道 才能显示通知栏
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            String name = requireContext().getResources().getString(R.string.app_name);
            String id = name + "_DefaultNotificationChannel";
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setShowBadge(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300});
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);//设置锁屏可见
            notificationManager.createNotificationChannel(mChannel);
            builder = new Notification.Builder(requireContext(), id);
        } else {
            builder = new Notification.Builder(requireContext());
        }
        Bitmap bitmap = BitmapFactory.decodeResource(requireContext().getResources(), R.mipmap.logo_round);
        builder.setContentTitle("钉钉打卡通知监听已打开")
                .setContentText("如果通知消失，请重新开启应用")
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(bitmap)
                .setSmallIcon(R.mipmap.logo_round)
                .setAutoCancel(false);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        notificationManager.notify(Integer.MAX_VALUE, notification);
    }
}
