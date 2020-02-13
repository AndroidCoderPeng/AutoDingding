package com.pengxh.autodingding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public abstract class BaseLazyFragment extends Fragment {

    private boolean isPrepared;//该页面是否已经准备完毕，onCreateView已走完
    private boolean isLazyLoaded;//该Fragment是否已经懒加载过

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isPrepared = true;
        lazyLoaded();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        lazyLoaded();
    }

    private void lazyLoaded() {
        if (getUserVisibleHint() && isPrepared && isLazyLoaded) {
            onLazyLoad();
            isLazyLoaded = true;
        }
    }

    public abstract void onLazyLoad();
}
