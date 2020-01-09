package com.pengxh.autodingding.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.DoubleClickExitActivity;
import com.pengxh.app.multilib.utils.DensityUtil;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.swipemenu.SwipeMenuItem;
import com.pengxh.app.multilib.widget.swipemenu.SwipeMenuListView;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.ClockAdapter;
import com.pengxh.autodingding.bean.ClockBean;
import com.pengxh.autodingding.db.SQLiteUtil;
import com.pengxh.autodingding.utils.LiveDataBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/8 12:37
 */
public class DingdingClockActivity extends DoubleClickExitActivity implements View.OnClickListener {

    private static final String TAG = "DingdingClockActivity";
    @BindView(R.id.dingdingTitleRight)
    ImageView dingdingTitleRight;
    @BindView(R.id.emptyView)
    TextView emptyView;
    @BindView(R.id.clockListView)
    SwipeMenuListView clockListView;
    private MutableLiveData<String> addClock;
    private Observer<String> addClockObserver;
    private SQLiteUtil sqLiteUtil;

    @Override
    public void initView() {
        setContentView(R.layout.activity_dingding);
    }

    @Override
    public void initData() {
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppTheme).init();
        sqLiteUtil = SQLiteUtil.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LiveDataBus.get().with("notifyDataSetChanged").setValue("");
    }

    @Override
    public void initEvent() {
        addClock = LiveDataBus.get().with("notifyDataSetChanged", String.class);
        addClockObserver = s -> {
            List<ClockBean> clockBeanList = sqLiteUtil.loadAllClock();
            if (clockBeanList == null || clockBeanList.size() == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);

                ClockAdapter adapter = new ClockAdapter(DingdingClockActivity.this, clockBeanList);
                clockListView.setAdapter(adapter);
                clockListView.setMenuCreator(menu -> {
                    SwipeMenuItem updateItem = new SwipeMenuItem(getApplicationContext());
                    updateItem.setBackground(new ColorDrawable(Color.rgb(0, 255, 0)));
                    updateItem.setWidth(DensityUtil.dp2px(getApplicationContext(), 80.0f));
                    updateItem.setTitle("修改");
                    updateItem.setTitleSize(18);
                    updateItem.setTitleColor(Color.WHITE);
                    menu.addMenuItem(updateItem);

                    SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(255, 0, 0)));
                    deleteItem.setWidth(DensityUtil.dp2px(getApplicationContext(), 80.0f));
                    deleteItem.setTitle("删除");
                    deleteItem.setTitleSize(18);
                    deleteItem.setTitleColor(Color.WHITE);
                    menu.addMenuItem(deleteItem);
                });
                clockListView.setOnMenuItemClickListener((position, menu, index) -> {
                    String uuid = clockBeanList.get(position).getUuid();
                    switch (index) {
                        case 0:
                            SaveKeyValues.putValue("update", uuid);
                            Intent intent = new Intent(DingdingClockActivity.this, AddClockActivity.class);
                            intent.putExtra("uuid", uuid);
                            startActivity(intent);
                            break;
                        case 1:
                            sqLiteUtil.deleteClockByUUid(uuid);
//                            sqLiteUtil.deleteWeek();
                            clockBeanList.remove(position);
                            adapter.notifyDataSetChanged();
                            LiveDataBus.get().with("notifyDataSetChanged").setValue("");
                            break;

                    }
                    return false;//不拦截滑动事件
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
