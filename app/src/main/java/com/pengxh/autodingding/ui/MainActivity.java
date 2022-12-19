package com.pengxh.autodingding.ui;

import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.androidx.lite.base.AndroidxBaseActivity;
import com.pengxh.androidx.lite.utils.ColorUtil;
import com.pengxh.androidx.lite.utils.ImmerseStatusBarUtil;
import com.pengxh.androidx.lite.utils.SaveKeyValues;
import com.pengxh.androidx.lite.widget.dialog.AlertMessageDialog;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.BaseFragmentAdapter;
import com.pengxh.autodingding.databinding.ActivityMainBinding;
import com.pengxh.autodingding.fragment.DingDingFragment;
import com.pengxh.autodingding.fragment.SettingsFragment;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.DingDingUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AndroidxBaseActivity<ActivityMainBinding> {

    private MenuItem menuItem = null;
    private final List<Fragment> fragmentPages = new ArrayList<>();

    @Override
    protected void setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(this, ColorUtil.convertColor(this, R.color.colorAppThemeLight));
        ImmersionBar.with(this).statusBarDarkFont(false).init();
        viewBinding.titleView.setText("钉钉打卡");
    }

    @Override
    protected void initData() {
        fragmentPages.add(new DingDingFragment());
        fragmentPages.add(new SettingsFragment());
    }

    @Override
    public void initEvent() {
        viewBinding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dingding) {
                if (DingDingUtil.isAppAvailable(Constant.DINGDING)) {
                    viewBinding.viewPager.setCurrentItem(0);
                    viewBinding.titleView.setText("钉钉打卡");
                } else {
                    showAlertDialog("手机没有安装《钉钉》软件，无法自动打卡");
                }
            } else if (itemId == R.id.nav_wechat) {
                if (DingDingUtil.isAppAvailable(Constant.WECHAT)) {
                    viewBinding.viewPager.setCurrentItem(1);
                    viewBinding.titleView.setText("企业微信设置");
                } else {
                    showAlertDialog("手机没有安装《企业微信》，无法自动打卡");
                }
            } else if (itemId == R.id.nav_settings) {
                viewBinding.viewPager.setCurrentItem(2);
                viewBinding.titleView.setText("其他设置");
            }
            return false;
        });
        BaseFragmentAdapter fragmentAdapter = new BaseFragmentAdapter(getSupportFragmentManager(), fragmentPages);
        viewBinding.viewPager.setAdapter(fragmentAdapter);
        viewBinding.viewPager.setOffscreenPageLimit(fragmentPages.size());
        viewBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    viewBinding.bottomNavigation.getMenu().getItem(0).setChecked(false);
                }
                menuItem = viewBinding.bottomNavigation.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        boolean isFirst = (boolean) SaveKeyValues.getValue("isFirst", true);
        if (isFirst) {
            new AlertMessageDialog.Builder()
                    .setContext(this)
                    .setTitle("温馨提醒")
                    .setMessage("本软件仅供内部使用，严禁商用或者用作其他非法用途")
                    .setPositiveButton("知道了")
                    .setOnDialogButtonClickListener(() -> SaveKeyValues.putValue("isFirst", false)).build().show();
        }
    }

    private void showAlertDialog(String message) {
        new AlertMessageDialog.Builder()
                .setContext(this)
                .setTitle("温馨提醒")
                .setMessage(message)
                .setPositiveButton("知道了")
                .setOnDialogButtonClickListener(() -> {

                }).build().show();
    }
}