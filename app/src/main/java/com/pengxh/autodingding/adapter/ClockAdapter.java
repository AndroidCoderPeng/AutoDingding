package com.pengxh.autodingding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.pengxh.app.multilib.widget.swipemenu.BaseSwipListAdapter;
import com.pengxh.autodingding.R;

import java.util.List;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/9 10:44
 */
public class ClockAdapter extends BaseSwipListAdapter {

    private List<String> clockList;
    private LayoutInflater inflater;

    public ClockAdapter(Context context, List<String> list) {
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
        private TextView clockText;
        private Switch mSwitch;

        void bindHolder(String clock) {
            clockText.setText(clock);
        }
    }
}
