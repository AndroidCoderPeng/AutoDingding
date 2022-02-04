package com.pengxh.autodingding.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.pengxh.autodingding.R;

import java.util.List;

/**
 * @description: TODO 顶部下拉菜单
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/28 20:35
 */
public class EasyPopupWindow extends PopupWindow {

    private PopupWindowClickListener mClickListener;
    private final Context mContext;
    private final List<String> itemList;

    public EasyPopupWindow(Context context, List<String> stringList) {
        super(context);
        this.mContext = context;
        this.itemList = stringList;
        setWidth(400);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setAnimationStyle(R.style.PopupAnimation);
        View contentView = LayoutInflater.from(context).inflate(R.layout.easy_popup, null, false);
        setContentView(contentView);
        ListView popupListView = contentView.findViewById(R.id.popupListView);
        setupListView(popupListView);
    }

    //给PopupWindow列表绑定数据
    private void setupListView(ListView popupListView) {
        PopupAdapter adapter = new PopupAdapter(mContext, itemList);
        popupListView.setAdapter(adapter);
        popupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mClickListener != null) {
                    mClickListener.popupWindowClick(i);
                }
                dismiss();
            }
        });
    }

    public interface PopupWindowClickListener {
        void popupWindowClick(int position);
    }

    public void setPopupWindowClickListener(PopupWindowClickListener windowClickListener) {
        this.mClickListener = windowClickListener;
    }
}