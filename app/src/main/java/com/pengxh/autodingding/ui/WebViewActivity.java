package com.pengxh.autodingding.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.utils.StatusBarColorUtil;

import butterknife.BindView;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/7/21 9:53
 */
public class WebViewActivity extends BaseNormalActivity {

    private static final String TAG = "WebViewActivity";

    @BindView(R.id.mTitleLeftView)
    ImageView mTitleLeftView;
    @BindView(R.id.mTitleView)
    TextView mTitleView;
    @BindView(R.id.mTitleRightView)
    ImageView mTitleRightView;
    @BindView(R.id.mLinearLayout)
    LinearLayout mLinearLayout;

    public final static String UPDATE_URL = "https://cn.bing.com/";
    private AgentWeb mAgentWeb;

    @Override
    public int initLayoutView() {
        return R.layout.activity_webview;
    }

    @Override
    public void initData() {
        StatusBarColorUtil.setColor(this, Color.parseColor("#0094FF"));
        ImmersionBar.with(this).init();

        mTitleLeftView.setVisibility(View.GONE);
        mTitleView.setText("版本更新");
        mTitleRightView.setVisibility(View.GONE);
    }

    @Override
    public void initEvent() {
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(mLinearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
                .setSecurityType(AgentWeb.SecurityType.DEFAULT_CHECK)
                .createAgentWeb()
                .ready()
                .go(UPDATE_URL);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //do you  work
            Log.d(TAG, "onPageStarted: " + url);
        }
    };
    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //do you work
            Log.d(TAG, "onProgressChanged: " + newProgress);
        }
    };

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mAgentWeb.back()) {
            finish();
        }
    }
}
