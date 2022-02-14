package com.pengxh.autodingding.ui;

import android.view.MenuItem;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.dialog.AlertMessageDialog;
import com.pengxh.autodingding.AndroidxBaseActivity;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.BaseFragmentAdapter;
import com.pengxh.autodingding.databinding.ActivityMainBinding;
import com.pengxh.autodingding.ui.fragment.AutoDingDingFragment;
import com.pengxh.autodingding.ui.fragment.SettingsFragment;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.StatusBarColorUtil;
import com.pengxh.autodingding.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AndroidxBaseActivity<ActivityMainBinding> {

    private MenuItem menuItem = null;
    private final List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void setupTopBarLayout() {
        StatusBarColorUtil.setColor(this, ContextCompat.getColor(this, R.color.colorAppThemeLight));
        ImmersionBar.with(this).statusBarDarkFont(false).init();
        viewBinding.titleView.setText("自动打卡");
    }

    @Override
    protected void initData() {
        fragmentList.add(new AutoDingDingFragment());
        fragmentList.add(new SettingsFragment());
    }

    @Override
    public void initEvent() {
        viewBinding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_clock) {
                viewBinding.mViewPager.setCurrentItem(0);
                viewBinding.titleView.setText("自动打卡");
            } else if (itemId == R.id.nav_settings) {
                viewBinding.mViewPager.setCurrentItem(1);
                viewBinding.titleView.setText("其他设置");
            }
            return false;
        });
        BaseFragmentAdapter fragmentAdapter = new BaseFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewBinding.mViewPager.setAdapter(fragmentAdapter);
        viewBinding.mViewPager.setOffscreenPageLimit(fragmentList.size());
        viewBinding.mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        if (!Utils.isAppAvailable(Constant.DINGDING)) {
            new AlertMessageDialog.Builder()
                    .setContext(this)
                    .setTitle("温馨提醒")
                    .setMessage("手机没有安装钉钉软件，无法自动打卡")
                    .setPositiveButton("退出")
                    .setOnDialogButtonClickListener(new AlertMessageDialog.OnDialogButtonClickListener() {
                        @Override
                        public void onConfirmClick() {
                            finish();
                        }
                    }).build().show();
        } else {
            boolean isFirst = (boolean) SaveKeyValues.getValue("isFirst", true);
            if (isFirst) {
                new AlertMessageDialog.Builder()
                        .setContext(this)
                        .setTitle("温馨提醒")
                        .setMessage("本软件仅供内部使用，严禁商用或者用作其他非法用途")
                        .setPositiveButton("知道了")
                        .setOnDialogButtonClickListener(new AlertMessageDialog.OnDialogButtonClickListener() {
                            @Override
                            public void onConfirmClick() {
                                SaveKeyValues.putValue("isFirst", false);
                            }
                        }).build().show();
            }
        }
    }
}