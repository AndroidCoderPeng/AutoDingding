package com.pengxh.autodingding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.NotificationBean;

import java.util.List;

public class NotificationRecordAdapter extends BaseAdapter {

    private final List<NotificationBean> beanList;
    private final LayoutInflater mInflater;

    public NotificationRecordAdapter(Context mContext, List<NotificationBean> list) {
        this.beanList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return beanList == null ? 0 : beanList.size();
    }

    @Override
    public Object getItem(int position) {
        return beanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NotificationViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_list_notification, null);
            holder = new NotificationViewHolder();
            holder.titleView = convertView.findViewById(R.id.titleView);
            holder.packageNameView = convertView.findViewById(R.id.packageNameView);
            holder.messageView = convertView.findViewById(R.id.messageView);
            holder.postTimeView = convertView.findViewById(R.id.postTimeView);
            convertView.setTag(holder);
        } else {
            holder = (NotificationViewHolder) convertView.getTag();
        }
        holder.bindData(beanList.get(position));
        return convertView;
    }

    private static class NotificationViewHolder {
        private TextView titleView;
        private TextView packageNameView;
        private TextView messageView;
        private TextView postTimeView;

        void bindData(NotificationBean bean) {
            titleView.setText("标题：" + bean.getNotificationTitle());
            packageNameView.setText("包名：" + bean.getPackageName());
            messageView.setText("内容：" + bean.getNotificationMsg());
            postTimeView.setText(bean.getPostTime());
        }
    }
}
