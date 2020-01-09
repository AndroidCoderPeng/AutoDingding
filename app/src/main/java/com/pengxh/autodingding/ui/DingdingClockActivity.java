package com.pengxh.autodingding.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.DensityUtil;
import com.pengxh.app.multilib.widget.swipemenu.SwipeMenuItem;
import com.pengxh.app.multilib.widget.swipemenu.SwipeMenuListView;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.ClockAdapter;
import com.pengxh.autodingding.utils.LiveDataBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/8 12:37
 */
public class DingdingClockActivity extends BaseNormalActivity implements View.OnClickListener {

    private static final String TAG = "DingdingClockActivity";
    @BindView(R.id.dingdingTitleRight)
    ImageView dingdingTitleRight;
    @BindView(R.id.emptyView)
    TextView emptyView;
    @BindView(R.id.clockListView)
    SwipeMenuListView clockListView;
    private MutableLiveData<String> addClock;
    private Observer<String> addClockObserver;
    private List<String> clockList;

    @Override
    public void initView() {
        setContentView(R.layout.activity_dingding);
    }

    @Override
    public void initData() {
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppTheme).init();
    }

    @Override
    public void initEvent() {
        clockList = new ArrayList<>();
        addClock = LiveDataBus.get().with("addClock", String.class);
        addClockObserver = s -> {
            Log.d(TAG, "onChanged: " + s);
            if (s == null) {
                Log.e(TAG, "", new Throwable());
                return;
            }
            if (!s.isEmpty()) {
                emptyView.setVisibility(View.GONE);
                clockList.add(s);

                ClockAdapter adapter = new ClockAdapter(DingdingClockActivity.this, clockList);
                clockListView.setAdapter(adapter);
                clockListView.setMenuCreator(menu -> {
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    openItem.setBackground(new ColorDrawable(Color.rgb(255, 0, 0)));
                    openItem.setWidth(DensityUtil.dp2px(getApplicationContext(), 90.0f));
                    openItem.setTitle("删除");
                    openItem.setTitleSize(18);
                    openItem.setTitleColor(Color.WHITE);
                    menu.addMenuItem(openItem);
                });
                clockListView.setOnMenuItemClickListener((position, menu, index) -> {
                    switch (index) {
                        case 0:
                            clockList.remove(position);
                            adapter.notifyDataSetChanged();
                            break;
                    }
                    return true;
                });
            }
        };
        addClock.observeForever(addClockObserver);
    }

    @OnClick({R.id.dingdingTitleRight})
    @Override
    public void onClick(View v) {
        startActivity(new Intent(DingdingClockActivity.this, AddClockActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addClock.removeObserver(addClockObserver);
    }
}
