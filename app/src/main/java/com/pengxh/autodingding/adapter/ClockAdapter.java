package com.pengxh.autodingding.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.swipemenu.BaseSwipListAdapter;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.ClockBean;
import com.pengxh.autodingding.db.SQLiteUtil;
import com.pengxh.autodingding.ui.AddClockActivity;

import java.util.List;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/9 10:44
 */
public class ClockAdapter extends BaseSwipListAdapter {

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
                } else {
                    status = 0;
                }
                SQLiteUtil.getInstance().updateClockStatus(clockBean.getUuid(), status);
            });
        }
    }
}
