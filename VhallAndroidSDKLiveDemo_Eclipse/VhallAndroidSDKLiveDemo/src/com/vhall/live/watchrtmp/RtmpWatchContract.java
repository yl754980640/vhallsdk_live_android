package com.vhall.live.watchrtmp;

import android.app.Activity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huanan on 2016/6/30.
 */

public class RtmpWatchContract {
    interface View extends BaseView<Presenter> {

        Activity getWatchActivity();

        RelativeLayout getWatchLayout();

        void setWatchButtonText(String text);

        void setDownSpeed(String text);

        void showLoading(boolean isShow);

        int changeOrientation();

        void showToast(String message);

        void showRadioButton(HashMap map);

        void setScaleButtonText(String text);
    }

    interface DocumentView extends BaseView<Presenter> {
        void showDoc(String docUrl);
    }

    interface Presenter extends BasePresenter {

        void watchStart(int pixel);

        void watchStop();

        void onWatchBtnClick(int pixel);

        void onWatchBack();

        void onSwitchPixel(int pixel);// 切换分辨率

        int setScaleType();

        int changeOriention();

    }
}
