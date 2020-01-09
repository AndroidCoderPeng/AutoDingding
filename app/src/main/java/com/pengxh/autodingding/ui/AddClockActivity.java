package com.pengxh.autodingding.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.ClockBean;
import com.pengxh.autodingding.db.SQLiteUtil;
import com.pengxh.autodingding.utils.LiveDataBus;
import com.pengxh.autodingding.utils.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

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
    @BindView(R.id.repeatView)
    TextView repeatView;
    @BindView(R.id.repeatLayout)
    RelativeLayout repeatLayout;
    @BindView(R.id.mTimePicker)
    TimePicker mTimePicker;

    private NumberFormat numberFormat = new DecimalFormat("00");
    private Observer<Object> clockListObserver;
    private MutableLiveData<Object> clockListLiveData;

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
        clockListLiveData = LiveDataBus.get().with("clockList", Object.class);
        clockListObserver = o -> {
            List<String> clockList = (List<String>) o;
            if (clockList.size() == 5) {
                repeatView.setText("每天（不包括周末）");
            } else {
                repeatView.setText(clockList.toString());
            }
        };
        clockListLiveData.observeForever(clockListObserver);

        mTimePicker.setIs24HourView(true);
        mTimePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
    }

    @OnClick({R.id.addTitleLeft, R.id.addTitleRight, R.id.repeatLayout})
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
                Log.d(TAG, "当前时间" + clockTime);

                String value = (String) SaveKeyValues.getValue("update", "");
                if (!value.equals("update")) {
                    Log.d(TAG, "更新时间: ");
                    String uuid = getIntent().getStringExtra("uuid");
                    SQLiteUtil.getInstance().updateClockTime(uuid, clockTime);
                } else {
                    Log.d(TAG, "保存新闹钟: ");
                    ClockBean clockBean = new ClockBean();
                    clockBean.setUuid(Utils.uuid());
                    clockBean.setClockTime(clockTime);
                    clockBean.setClockStatus(1);
                    SQLiteUtil.getInstance().saveClock(clockBean);
                }
                LiveDataBus.get().with("notifyDataSetChanged").setValue("");
                finish();
                break;
            case R.id.repeatLayout:
                startActivity(new Intent(this, RepeatActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockListLiveData.removeObserver(clockListObserver);
    }
}
