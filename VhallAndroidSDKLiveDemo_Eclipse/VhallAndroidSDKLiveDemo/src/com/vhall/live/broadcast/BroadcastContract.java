package com.vhall.live.broadcast;

import android.app.Activity;

import com.vhall.business.VhallCameraView;
import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;
import com.vhall.live.data.Param;

/**
 * Created by huanan on 2016/6/30.
 */

public class BroadcastContract {

    interface View extends BaseView<Presenter> {

        VhallCameraView getCameraView();

        Activity getmActivity();

        void setStartBtnText(String text);

        void showErrorMsg(String msg);

        void setSpeedText(String text);


    }

    interface Presenter extends BasePresenter {

         void onstartBtnClick();

         void startBroadCast();

         void stopBroadcast();

         void changeFlash();

         void changeCamera();

         void changeAudio();

         void onPause();

        void onResume();

    }
}
