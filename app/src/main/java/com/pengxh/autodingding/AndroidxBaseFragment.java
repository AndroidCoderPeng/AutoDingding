package com.pengxh.autodingding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AndroidxBaseFragment<VB extends ViewBinding> extends Fragment {

    protected VB viewBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Type type = getClass().getGenericSuperclass();
        if (type == null) {
            throw new NullPointerException();
        }
        Class<?> cls = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        try {
            Method method = cls.getDeclaredMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
            viewBinding = (VB) method.invoke(null, getLayoutInflater(), container, false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (viewBinding == null) {
            throw new NullPointerException();
        }
        setupTopBarLayout();
        initData();
        initEvent();
        return viewBinding.getRoot();
    }

    protected abstract void setupTopBarLayout();

    /**
     * 初始化默认数据
     */
    protected abstract void initData();

    /**
     * 初始化业务逻辑
     */
    protected abstract void initEvent();

    @Override
    public void onDestroyView() {
        viewBinding = null;
        super.onDestroyView();
    }
}
