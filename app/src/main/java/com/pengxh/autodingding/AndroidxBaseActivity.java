package com.pengxh.autodingding;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.autodingding.utils.StatusBarColorUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AndroidxBaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    protected VB viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Type type = getClass().getGenericSuperclass();
        if (type == null) {
            throw new NullPointerException();
        }
        Class<?> cls = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        try {
            Method method = cls.getDeclaredMethod("inflate", LayoutInflater.class);
            viewBinding = (VB) method.invoke(null, getLayoutInflater());
            if (viewBinding == null) {
                throw new NullPointerException();
            }
            setContentView(viewBinding.getRoot());
            StatusBarColorUtil.setColor(this, ContextCompat.getColor(this, R.color.colorAppThemeLight));
            ImmersionBar.with(this).statusBarDarkFont(false).init();//沉浸式状态栏
            setupTopBarLayout();
            initData();
            initEvent();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化默认数据
     */
    protected abstract void initData();

    /**
     * 特定页面定制沉浸式状态栏
     */
    protected abstract void setupTopBarLayout();

    /**
     * 初始化业务逻辑
     */
    protected abstract void initEvent();

    @Override
    protected void onDestroy() {
        viewBinding = null;
        super.onDestroy();
    }
}
