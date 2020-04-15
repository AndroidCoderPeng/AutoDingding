package com.pengxh.autodingding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.HistoryBean;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private List<HistoryBean> beanList;
    private LayoutInflater mInflater;

    public HistoryAdapter(Context mContext, List<HistoryBean> list) {
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
        HistoryViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_list, null);
            holder = new HistoryViewHolder();
            holder.noticeDate = convertView.findViewById(R.id.noticeDate);
            holder.noticeMessage = convertView.findViewById(R.id.noticeMessage);
            holder.tagView = convertView.findViewById(R.id.tagView);
            convertView.setTag(holder);
        } else {
            holder = (HistoryViewHolder) convertView.getTag();
        }
        holder.bindData(beanList.get(position));
        return convertView;
    }

    private static class HistoryViewHolder {
        private TextView noticeDate;
        private TextView noticeMessage;
        private ImageView tagView;

        void bindData(HistoryBean historyBean) {
            String message = historyBean.getMessage();
            if (!message.contains("成功")) {
                tagView.setBackgroundResource(R.drawable.bg_textview_error);
            }
            noticeMessage.setText(message);
            noticeDate.setText(historyBean.getDate());
        }
    }
}
