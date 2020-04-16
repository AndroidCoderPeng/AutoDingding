package com.pengxh.autodingding.ui;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.DoubleClickExitActivity;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.NoScrollViewPager;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.BaseFragmentAdapter;
import com.pengxh.autodingding.service.TimeService;
import com.pengxh.autodingding.ui.fragment.OneDayFragment;
import com.pengxh.autodingding.ui.fragment.SettingsFragment;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends DoubleClickExitActivity {

    @BindView(R.id.mViewPager)
    NoScrollViewPager mViewPager;
    @BindView(R.id.mTabLayout)
    TabLayout mTableLayout;

    private List<String> mTabName = Arrays.asList("一天", "设置");
    private List<Fragment> fragmentList = new ArrayList<>();

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppThemeLight).init();
    }

    @Override
    public void initData() {
        startService(new Intent(this, TimeService.class));
        fragmentList.add(new OneDayFragment());
        fragmentList.add(new SettingsFragment());
    }

    @Override
    public void initEvent() {
        FragmentPagerAdapter fragmentAdapter = new BaseFragmentAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(fragmentAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                resetBtnState();
                selectBtnState(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTableLayout.setupWithViewPager(mViewPager);
        mTableLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        resetBtnState();
        selectBtnState(0);

        if (!Utils.isAppAvailable(Constant.DINGDING)) {
            new AlertView("温馨提示", "手机没有安装钉钉软件，无法自动打卡", null, new String[]{"确定"}, null, this, AlertView.Style.Alert,
                    (o, position) -> this.finish()).setCancelable(false).show();
        } else {
            boolean isFirst = (boolean) SaveKeyValues.getValue("isFirst", true);
            if (isFirst) {
                SaveKeyValues.putValue("isFirst", false);
                new AlertView("※温馨提醒※", "本软件仅供内部使用，严禁商用或者用作其他非法用途", null, new String[]{"确定"}, null, this, AlertView.Style.Alert, null).setCancelable(false).show();
            }
        }
    }

    private void selectBtnState(int index) {
        if (index < 0 || index >= 2) {
            return;
        }
        TabLayout.Tab tabAt = mTableLayout.getTabAt(index);
        if (tabAt != null) {
            switch (index) {
                case 0:
                    View v = tabAt.getCustomView();
                    if (v != null) {
                        ImageView imv_icon = v.findViewById(R.id.tab_icon);
                        imv_icon.setImageResource(R.mipmap.day_blue);
                        TextView tv_name = v.findViewById(R.id.tab_title);
                        tv_name.setTextColor(getResources().getColor(R.color.tab_selected_txtcolor));
                    }
                    break;
                case 1:
                    View v2 = tabAt.getCustomView();
                    if (v2 != null) {
                        ImageView imv_icon = v2.findViewById(R.id.tab_icon);
                        imv_icon.setImageResource(R.mipmap.settings_blue);
                        TextView tv_name = v2.findViewById(R.id.tab_title);
                        tv_name.setTextColor(getResources().getColor(R.color.tab_selected_txtcolor));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void resetBtnState() {
        TabLayout.Tab tabAt1 = mTableLayout.getTabAt(0);
        TabLayout.Tab tabAt2 = mTableLayout.getTabAt(1);

        if (tabAt1 != null) {
            if (tabAt1.getCustomView() == null) {
                View v = LayoutInflater.from(this).inflate(R.layout.item_tab, null);
                tabAt1.setCustomView(v);
            }
            ImageView imv_icon = tabAt1.getCustomView().findViewById(R.id.tab_icon);
            TextView tv_name = tabAt1.getCustomView().findViewById(R.id.tab_title);
            imv_icon.setImageResource(R.mipmap.day_gray);
            tv_name.setText(mTabName.get(0));
            tv_name.setTextColor(getResources().getColor(R.color.tab_txtcolor));
        }
        if (tabAt2 != null) {
            if (tabAt2.getCustomView() == null) {
                View v = LayoutInflater.from(this).inflate(R.layout.item_tab, null);
                tabAt2.setCustomView(v);
            }
            ImageView imv_icon = tabAt2.getCustomView().findViewById(R.id.tab_icon);
            TextView tv_name = tabAt2.getCustomView().findViewById(R.id.tab_title);
            imv_icon.setImageResource(R.mipmap.settings_gray);
            tv_name.setText(mTabName.get(1));
            tv_name.setTextColor(getResources().getColor(R.color.tab_txtcolor));
        }
    }

//    @Override
//    protected void onResume() {
//        //跳转制定fragment
//        int position = getIntent().getIntExtra("position", 0);
//        mViewPager.setCurrentItem(position);
//        super.onResume();
//    }
}