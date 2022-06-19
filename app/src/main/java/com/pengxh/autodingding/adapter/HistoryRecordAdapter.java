package com.pengxh.autodingding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.HistoryRecordBean;

import java.util.List;

public class HistoryRecordAdapter extends RecyclerView.Adapter<HistoryRecordAdapter.ItemViewHolder> {

    private final List<HistoryRecordBean> dataRows;
    private final LayoutInflater layoutInflater;

    public HistoryRecordAdapter(Context mContext, List<HistoryRecordBean> list) {
        this.dataRows = list;
        layoutInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_list, parent, false);
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

        private final TextView noticeDate;
        private final TextView noticeMessage;
        private final ImageView tagView;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            noticeDate = itemView.findViewById(R.id.noticeDate);
            noticeMessage = itemView.findViewById(R.id.noticeMessage);
            tagView = itemView.findViewById(R.id.tagView);
        }

        void bindView(HistoryRecordBean bean) {
            String message = bean.getMessage();
            if (!message.contains("成功")) {
                tagView.setBackgroundResource(R.drawable.bg_textview_error);
            }
            noticeMessage.setText(message);
            noticeDate.setText(bean.getDate());
        }
    }
}
