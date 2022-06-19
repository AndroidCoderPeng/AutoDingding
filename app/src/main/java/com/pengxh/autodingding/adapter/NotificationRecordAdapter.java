package com.pengxh.autodingding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.NotificationBean;

import java.util.List;

public class NotificationRecordAdapter extends RecyclerView.Adapter<NotificationRecordAdapter.ItemViewHolder> {

    private final List<NotificationBean> dataRows;
    private final LayoutInflater layoutInflater;

    public NotificationRecordAdapter(Context mContext, List<NotificationBean> list) {
        this.dataRows = list;
        layoutInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_list_notification, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bindView(dataRows.get(position));
    }

    @Override
    public int getItemCount() {
        return dataRows.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;
        private final TextView packageNameView;
        private final TextView messageView;
        private final TextView postTimeView;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.titleView);
            packageNameView = itemView.findViewById(R.id.packageNameView);
            messageView = itemView.findViewById(R.id.messageView);
            postTimeView = itemView.findViewById(R.id.postTimeView);
        }

        void bindView(NotificationBean bean) {
            titleView.setText("标题：" + bean.getNotificationTitle());
            packageNameView.setText("包名：" + bean.getPackageName());
            messageView.setText("内容：" + bean.getNotificationMsg());
            postTimeView.setText(bean.getPostTime());
        }
    }
}
