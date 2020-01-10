package com.pengxh.autodingding.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.swipemenu.BaseSwipListAdapter;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.ClockBean;
import com.pengxh.autodingding.db.SQLiteUtil;
import com.pengxh.autodingding.ui.AddClockActivity;
import com.pengxh.autodingding.utils.AlarmReceiver;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/9 10:44
 */
public class ClockAdapter extends BaseSwipListAdapter {

    private static final String TAG = "ClockAdapter";
    private Context mContext;
    private List<ClockBean> clockList;
    private LayoutInflater inflater;

    public ClockAdapter(Context context, List<ClockBean> list) {
        this.mContext = context;
        this.clockList = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return clockList == null ? 0 : clockList.size();
    }

    @Override
    public Object getItem(int position) {
        return clockList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClockViewHolder itemHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_clock_swipelist, null);
            itemHolder = new ClockViewHolder();
            itemHolder.mItemLayout = convertView.findViewById(R.id.mItemLayout);
            itemHolder.clockText = convertView.findViewById(R.id.clockText);
            itemHolder.mSwitch = convertView.findViewById(R.id.mSwitch);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ClockViewHolder) convertView.getTag();
        }
        itemHolder.bindHolder(clockList.get(position));
        return convertView;
    }

    class ClockViewHolder {
        private RelativeLayout mItemLayout;
        private TextView clockText;
        private Switch mSwitch;
        private AlarmManager alarmManager;
        private PendingIntent pendingIntent;

        void bindHolder(ClockBean clockBean) {
            mItemLayout.setOnClickListener(v -> {
                String uuid = (String) SaveKeyValues.getValue("clock_uuid", "");
                Intent intent = new Intent(mContext, AddClockActivity.class);
                intent.putExtra("uuid", uuid);
                mContext.startActivity(intent);
            });
            clockText.setText(clockBean.getClockTime());
            mSwitch.setChecked((clockBean.getClockStatus() == 1));
            //修改状态
            mSwitch.setOnClickListener(v -> {
                boolean checked = mSwitch.isChecked();
                int status;
                if (checked) {
                    status = 1;
                    //打开闹钟
                    long alarmTime = getAlarmTime(clockBean.getClockTime()).getTimeInMillis() / 1000;
                    alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(mContext, AlarmReceiver.class);
                    intent.setAction("action.alarmWakeUp");
                    pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    if (Build.VERSION.SDK_INT < 19) {
                        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    }
                    Log.d(TAG, "bindHolder: 打开闹钟--->" + alarmTime);
                } else {
                    status = 0;
                    //关闭闹钟
                    alarmManager.cancel(pendingIntent);
                    Log.d(TAG, "bindHolder: 关闭闹钟");
                }
                SQLiteUtil.getInstance().updateClockStatus(clockBean.getUuid(), status);
            });
        }
    }

    private Calendar getAlarmTime(String time) {
        Log.d(TAG, "getAlarmTime: " + time);
        //获取当前秒值
        long systemTime = System.currentTimeMillis() / 1000;
        //12:00
        String[] split = time.split(":");
        Calendar calendar = Calendar.getInstance();
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(split[1]));
        calendar.set(Calendar.SECOND, 0);
        //获取上面设置的时分秒
        long selectTime = calendar.getTimeInMillis() / 1000;

        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if (systemTime > selectTime) {
            Log.d(TAG, "getAlarmTime: +1天");
            EasyToast.showToast("设置时间异常,+1天", EasyToast.ERROR);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar;
    }
}