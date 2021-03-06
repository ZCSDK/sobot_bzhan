package com.sobot.chat.conversation;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sobot.chat.activity.base.SobotBaseActivity;
import com.sobot.chat.utils.ZhiChiConstant;

public class SobotChatActivity extends SobotBaseActivity {

    Bundle informationBundle;
    SobotChatFragment chatFragment;

    @Override
    protected int getContentViewResId() {
        return getResLayoutId("sobot_chat_act");
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            informationBundle = getIntent().getBundleExtra(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
        } else {
            informationBundle = savedInstanceState.getBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        //销毁前缓存数据
        outState.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, informationBundle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void initView() {
        chatFragment = (SobotChatFragment) getSupportFragmentManager()
                .findFragmentById(getResId("sobot_contentFrame"));
        if (chatFragment == null) {
            chatFragment = SobotChatFragment.newInstance(informationBundle);

            addFragmentToActivity(getSupportFragmentManager(),
                    chatFragment, getResId("sobot_contentFrame"));
        }
    }

    public static void addFragmentToActivity(FragmentManager fragmentManager,
                                             Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.commit();
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onBackPressed() {
        if (chatFragment != null) {
            chatFragment.onBackPress();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        //全屏切换成小窗口时，重建界面
        recreate();
    }
}