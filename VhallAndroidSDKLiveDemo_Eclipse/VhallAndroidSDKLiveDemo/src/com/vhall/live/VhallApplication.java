package com.vhall.live;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.vhall.business.VhallSDK;

/**
 * Created by huanan on 2016/6/30.
 */

public class VhallApplication extends Application implements Constants{

    public static Context context;
    private static VhallApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        VhallSDK.init(APP_KEY,APP_SECRET_KEY);
    }

    public static VhallApplication getApp() {
        return app;
    }


}
