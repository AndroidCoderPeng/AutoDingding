package com.pengxh.autodingding.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pengxh.autodingding.R;

import java.util.List;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/28 20:46
 */
public class PopupAdapter extends BaseAdapter {

    private List<String> itemList;
    private Context mContext;
    private LayoutInflater inflater;

    public PopupAdapter(Context mContext, List<String> stringList) {
        this.itemList = stringList;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return itemList == null ? 0 : itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PopupWindowHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_easy_popup, null);
            holder = new PopupWindowHolder();
            holder.itemIcon = convertView.findViewById(R.id.itemIcon);
            holder.itemName = convertView.findViewById(R.id.itemName);
            convertView.setTag(holder);
        } else {
            holder = (PopupWindowHolder) convertView.getTag();
        }
        holder.bindData(itemList.get(position), position);
        return convertView;
    }

    private class PopupWindowHolder {
        private ImageView itemIcon;
        private TextView itemName;

        void bindData(String s, int index) {
            itemIcon.setImageResource(R.drawable.settings);
            itemName.setText(s);
        }
    }
}
