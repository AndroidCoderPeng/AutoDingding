package com.pengxh.autodingding.ui;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.WeekdaysAdapter;
import com.pengxh.autodingding.utils.LiveDataBus;
import com.pengxh.autodingding.utils.OnCheckedListener;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/8 13:21
 */
public class RepeatActivity extends BaseNormalActivity implements OnCheckedListener {

    private static final String TAG = "RepeatActivity";
    private static final List<String> WEEKDAYS = Arrays.asList("每周一", "每周二", "每周三", "每周四", "每周五");

    @BindView(R.id.repeatList)
    ListView repeatList;
    private List<String> clockList;

    @Override
    public void initView() {
        setContentView(R.layout.activity_repeat);
    }

    @Override
    public void initData() {
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppTheme).init();
    }

    @Override
    public void initEvent() {
        WeekdaysAdapter adapter = new WeekdaysAdapter(this, WEEKDAYS);
        adapter.setOnCheckedListener(this);
        repeatList.setAdapter(adapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onKeyDown: " + clockList);
            LiveDataBus.get().with("clockList").setValue(clockList);
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void getDataList(List<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        this.clockList = list;
    }
}
