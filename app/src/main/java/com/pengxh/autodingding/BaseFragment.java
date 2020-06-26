package com.pengxh.autodingding;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/6/26 21:22
 */
public abstract class BaseFragment extends Fragment {

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(initLayoutView(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    //onViewCreated在onCreateView执行完后立即执行
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initEvent();
    }

    /**
     * 获取布局ID
     *
     * @return
     */
    protected abstract int initLayoutView();

    /**
     * 数据初始化操作
     */
    protected abstract void initData();

    /**
     * 业务逻辑操作
     */
    protected abstract void initEvent();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.bind(view).unbind();
    }
}
