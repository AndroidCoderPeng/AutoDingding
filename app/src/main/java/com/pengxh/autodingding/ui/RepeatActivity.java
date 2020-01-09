package com.pengxh.autodingding.ui;

import android.view.KeyEvent;
import android.widget.ListView;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.WeekdaysAdapter;
import com.pengxh.autodingding.utils.LiveDataBus;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/8 13:21
 */
public class RepeatActivity extends BaseNormalActivity {

    private static final List<String> WEEKDAYS = Arrays.asList("每周一", "每周二", "每周三", "每周四", "每周五");

    @BindView(R.id.repeatList)
    ListView repeatList;

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
        repeatList.setAdapter(new WeekdaysAdapter(this, WEEKDAYS));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LiveDataBus.get().with("updateWeek").setValue("");
            finish();
            return true;
        }
        return false;
    }
}