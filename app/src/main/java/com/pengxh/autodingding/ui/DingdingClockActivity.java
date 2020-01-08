package com.pengxh.autodingding.ui;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.autodingding.R;

import butterknife.BindView;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/8 12:37
 */
public class DingdingClockActivity extends BaseNormalActivity {

    @BindView(R.id.dingdingTitleRight)
    ImageView dingdingTitleRight;
    @BindView(R.id.emptyView)
    TextView emptyView;

    @Override
    public void initView() {
        setContentView(R.layout.activity_dingding);
    }

    @Override
    public void initData() {
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppTheme).init();
    }

    @Override
    public void initEvent() {
        dingdingTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DingdingClockActivity.this, AddClockActivity.class);
                startActivityForResult(intent, 123);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == 321) {

        }
    }
}
