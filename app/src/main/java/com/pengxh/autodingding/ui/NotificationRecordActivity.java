package com.pengxh.autodingding.ui;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.androidx.lite.base.AndroidxBaseActivity;
import com.pengxh.androidx.lite.utils.ColorUtil;
import com.pengxh.androidx.lite.utils.ImmerseStatusBarUtil;
import com.pengxh.androidx.lite.utils.WeakReferenceHandler;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.NotificationRecordAdapter;
import com.pengxh.autodingding.bean.NotificationBean;
import com.pengxh.autodingding.databinding.ActivityNotificationBinding;
import com.pengxh.autodingding.greendao.DaoSession;

import java.util.ArrayList;
import java.util.List;

public class NotificationRecordActivity extends AndroidxBaseActivity<ActivityNotificationBinding> {

    private WeakReferenceHandler weakReferenceHandler;
    private DaoSession daoSession;
    private List<NotificationBean> dataBeans = new ArrayList<>();
    private boolean isRefresh = false;
    private boolean isLoadMore = false;
    private int offset = 0;// 本地数据库分页从0开始
    private NotificationRecordAdapter notificationAdapter;

    @Override
    protected void setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(this, ColorUtil.convertColor(this, R.color.colorAppThemeLight));
        ImmersionBar.with(this).statusBarDarkFont(false).init();
        viewBinding.titleView.setText("所有通知");
    }

    @Override
    public void initData() {
        weakReferenceHandler = new WeakReferenceHandler(callback);
        daoSession = BaseApplication.getInstance().getDaoSession();

        dataBeans = queryNotificationRecord();
        weakReferenceHandler.sendEmptyMessage(2022061901);
    }

    @Override
    public void initEvent() {
        viewBinding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            isRefresh = true;
            new CountDownTimer(1000, 500) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    isRefresh = false;
                    dataBeans.clear();
                    offset = 0;
                    dataBeans = queryNotificationRecord();

                    refreshLayout.finishRefresh();
                    weakReferenceHandler.sendEmptyMessage(2022061901);
                }
            }.start();
        });

        viewBinding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            isLoadMore = true;
            new CountDownTimer(1000, 500) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    isLoadMore = false;
                    offset++;
                    dataBeans.addAll(queryNotificationRecord());

                    refreshLayout.finishLoadMore();
                    weakReferenceHandler.sendEmptyMessage(2022061901);
                }
            }.start();
        });
    }

    private final Handler.Callback callback = msg -> {
        if (msg.what == 2022061901) {
            if (isRefresh || isLoadMore) {
                notificationAdapter.notifyDataSetChanged();
            } else { //首次加载数据
                if (dataBeans.size() == 0) {
                    viewBinding.emptyView.setVisibility(View.VISIBLE);
                } else {
                    viewBinding.emptyView.setVisibility(View.GONE);
                    notificationAdapter = new NotificationRecordAdapter(this, dataBeans);
                    viewBinding.notificationView.setLayoutManager(new LinearLayoutManager(this));
                    viewBinding.notificationView.setAdapter(notificationAdapter);
                }
            }
        }
        return true;
    };

    private List<NotificationBean> queryNotificationRecord() {
        return daoSession.queryBuilder(NotificationBean.class).offset(offset * 15).limit(15).list();
    }
}
