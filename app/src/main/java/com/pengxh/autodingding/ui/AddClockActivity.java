package com.pengxh.autodingding.ui;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.ClockBean;
import com.pengxh.autodingding.db.SQLiteUtil;
import com.pengxh.autodingding.utils.LiveDataBus;
import com.pengxh.autodingding.utils.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/8 12:59
 */
public class AddClockActivity extends BaseNormalActivity implements View.OnClickListener {

    private static final String TAG = "AddClockActivity";
    @BindView(R.id.addTitleLeft)
    TextView addTitleLeft;
    @BindView(R.id.addTitleRight)
    TextView addTitleRight;
    @BindView(R.id.mTimePicker)
    TimePicker mTimePicker;

    private NumberFormat numberFormat = new DecimalFormat("00");
    private SQLiteUtil sqLiteUtil = SQLiteUtil.getInstance();

    @Override
    public void initView() {
        setContentView(R.layout.activity_addclock);
    }

    @Override
    public void initData() {
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppTheme).init();
    }

    @Override
    public void initEvent() {
        mTimePicker.setIs24HourView(true);
    }

    @OnClick({R.id.addTitleLeft, R.id.addTitleRight})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addTitleLeft:
                finish();
                break;
            case R.id.addTitleRight:
                String hour = numberFormat.format(mTimePicker.getHour());
                String minute = numberFormat.format(mTimePicker.getMinute());
                String clockTime = hour + ":" + minute;

                String uuid = getIntent().getStringExtra("uuid");
                if (uuid != null && !uuid.equals("")) {
                    Log.d(TAG, "更新时间,当前时间" + clockTime);
                    sqLiteUtil.updateClockTime(uuid, clockTime);
                } else {
                    Log.d(TAG, "保存新闹钟,当前时间" + clockTime);
                    ClockBean clockBean = new ClockBean();
                    clockBean.setUuid(Utils.uuid());
                    clockBean.setClockTime(clockTime);
                    clockBean.setClockStatus(0);
                    sqLiteUtil.saveClock(clockBean);
                }
                LiveDataBus.get().with("notifyDataSetChanged").setValue("");
                finish();
                break;
            default:
                break;
        }
    }
}