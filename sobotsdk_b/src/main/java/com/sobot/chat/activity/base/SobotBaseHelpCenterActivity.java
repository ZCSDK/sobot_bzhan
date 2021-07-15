package com.sobot.chat.activity.base;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sobot.chat.api.model.Information;
import com.sobot.chat.utils.ZhiChiConstant;

import java.io.Serializable;

/**
 * 帮助中心基类
 */
public abstract class SobotBaseHelpCenterActivity extends SobotBaseActivity {
    protected Bundle mInformationBundle;
    protected Information mInfo;

    @Override
    protected void initBundleData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mInformationBundle = getIntent().getBundleExtra(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
        } else {
            mInformationBundle = savedInstanceState.getBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
        }
        if (mInformationBundle != null) {
            Serializable sobot_info = mInformationBundle.getSerializable(ZhiChiConstant.SOBOT_BUNDLE_INFO);
            if (sobot_info instanceof Information) {
                mInfo = (Information) sobot_info;
            }
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        //销毁前缓存数据
        outState.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, mInformationBundle);
        super.onSaveInstanceState(outState);
    }

    protected int getStatusBarColor() {
        return getResColor("sobot_help_center_status_bar_color");
    }

    protected void setUpToolBar() {
        View toolBar = getToolBar();
        if (toolBar == null) {
            return;
        }

        toolBar.setBackgroundColor(getResColor("sobot_help_center_status_bar_color"));
    }

    protected void setUpToolBarLeftMenu() {
        if (getLeftMenu() != null) {
            TextView leftMenu = getLeftMenu();
            leftMenu.setTextColor(getResColor("sobot_color_title_bar_back_help_center"));
            leftMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLeftMenuClick(v);
                }
            });
        }
    }

    public void setTitle(CharSequence title) {
        View tmpMenu = getTitleView();
        if (!(tmpMenu instanceof TextView)) {
            return;
        }
        TextView tvTitle = (TextView) tmpMenu;
        tvTitle.setText(title);
        tvTitle.setTextColor(getResColor("sobot_color_title_bar_title_help_center"));
    }

}