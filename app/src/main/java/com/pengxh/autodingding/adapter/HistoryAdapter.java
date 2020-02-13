package com.pengxh.autodingding.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.HistoryBean;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private List<HistoryBean> beanList;
    private Context mContext;
    private LayoutInflater mInflater;

    public HistoryAdapter(Context mContext, List<HistoryBean> list) {
        this.beanList = list;
        this.mContext = mContext;
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
            holder.noticeTitle = convertView.findViewById(R.id.noticeTitle);
            holder.noticeDate = convertView.findViewById(R.id.noticeDate);
            holder.noticeTime = convertView.findViewById(R.id.noticeTime);
            holder.noticeMsg = convertView.findViewById(R.id.noticeMsg);
            convertView.setTag(holder);
        } else {
            holder = (HistoryViewHolder) convertView.getTag();
        }
        holder.bindData(beanList.get(position));
        return convertView;
    }

    private class HistoryViewHolder {
        private TextView noticeMsg;
        private TextView noticeTitle;
        private TextView noticeDate;
        private TextView noticeTime;

        void bindData(HistoryBean historyBean) {
            String message = historyBean.getMessage();
            if (message.contains("成功")) {
                noticeMsg.setTextColor(Color.WHITE);
                noticeMsg.setBackgroundResource(R.drawable.bg_textview);
            } else {
                noticeMsg.setTextColor(Color.parseColor("#0094FF"));
                noticeMsg.setBackgroundResource(R.drawable.bg_textview_error);
            }
            noticeMsg.setText(message);
            noticeTitle.setText(historyBean.getTitle());
            noticeDate.setText(historyBean.getDate());
            noticeTime.setText(historyBean.getTime());
        }
    }
}
