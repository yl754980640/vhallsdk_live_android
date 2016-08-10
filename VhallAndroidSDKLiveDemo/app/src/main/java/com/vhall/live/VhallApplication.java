package com.vhall.live;

import android.app.Application;
import android.content.Context;

import com.vhall.business.VhallSDK;

/**
 * 主Application类
 */
public class VhallApplication extends Application implements Constants {

    private static VhallApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        VhallSDK.init(APP_KEY, APP_SECRET_KEY);
    }

    public static VhallApplication getApp() {
        return app;
    }
}
