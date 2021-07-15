package com.sobot.demo;

import android.app.Application;

import com.sobot.chat.SobotApi;
import com.sobot.chat.api.apiUtils.SobotBaseUrl;
import com.sobot.chat.utils.LogUtils;

/**
 * Created by Administrator on 2017/12/29.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        SobotBaseUrl.setHost("https://new-service.biliapi.net");
        SobotBaseUrl.setHost("https://test-service.biliapi.net");

        String appkey="a683f49da89e457ea86a09b663a74c0e";
        SobotApi.initSobotSDK(this, appkey, SobotSPUtil.getStringData(this, "sobot_partnerId", ""));
        LogUtils.isDebug=true;
        LogUtils.allowI=true;
        LogUtils.allowD=true;
    }



}