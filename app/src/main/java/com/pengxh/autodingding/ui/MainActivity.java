package com.pengxh.autodingding.ui;

import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pengxh.app.multilib.base.DoubleClickExitActivity;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.NoScrollViewPager;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.BaseFragmentAdapter;
import com.pengxh.autodingding.ui.fragment.AutoDingDingFragment;
import com.pengxh.autodingding.ui.fragment.SettingsFragment;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;

public class MainActivity extends DoubleClickExitActivity {

    @BindView(R.id.mViewPager)
    NoScrollViewPager mViewPager;
    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottomNavigation;

    private MenuItem menuItem = null;
    private List<Fragment> fragmentList = new ArrayList<>();

    @Override
    public int initLayoutView() {
        return R.layout.activity_main;
    }

    @Override
    public void initData() {
        fragmentList.add(new AutoDingDingFragment());
        fragmentList.add(new SettingsFragment());
    }

    @Override
    public void initEvent() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_clock:
                    mViewPager.setCurrentItem(0);
                    break;
                case R.id.nav_settings:
                    mViewPager.setCurrentItem(1);
                    break;
            }
            return false;
        });
        FragmentPagerAdapter fragmentAdapter = new BaseFragmentAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(fragmentAdapter);
        mViewPager.setOffscreenPageLimit(fragmentList.size());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigation.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigation.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (!Utils.isAppAvailable(Constant.DINGDING)) {
            Utils.showAlertDialog(this, "温馨提示", "手机没有安装钉钉软件，无法自动打卡", "退出", (dialog, which) -> finish());
        } else {
            boolean isFirst = (boolean) SaveKeyValues.getValue("isFirst", true);
            if (isFirst) {
                SaveKeyValues.putValue("isFirst", false);
                Utils.showAlertDialog(this, "温馨提醒", "本软件仅供内部使用，严禁商用或者用作其他非法用途", "知道了");
            }
        }
    }
}