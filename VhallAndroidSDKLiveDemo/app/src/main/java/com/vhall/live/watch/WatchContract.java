package com.vhall.live.watch;

import android.app.Activity;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;

import java.util.HashMap;

/**
 * 观看页的接口类
 */
public class WatchContract {
    interface WatchView extends BaseView<Presenter> {
        void setShowDetail(boolean isShow);
    }

    interface DocumentView extends BaseView<Presenter> {
        void showDoc(String docUrl);
    }

    interface DetailView extends BaseView<Presenter> {
    }

    interface Presenter extends BasePresenter {

    }

    interface PlaybackView extends BaseView<PlaybackPresenter> {
        Activity getmActivity();
        void setPlayIcon(boolean isStop);
        void setProgressLabel(String text);
        void setSeekbarMax(int max);
        void setSeekbarCurrentPosition(int position);
        void showProgressbar(boolean show);
        RelativeLayout getContainer();
        void setScaleTypeText(String text);
        int changeScreenOri();
    }

    interface LiveView extends BaseView<LivePresenter> {
        Activity getmActivity();

        RelativeLayout getWatchLayout();
        void setPlayPicture(boolean state);
        void setDownSpeed(String text);
        void showLoading(boolean isShow);
        int changeOrientation();
        void showToast(String message);
        void showRadioButton(HashMap map);
        void setScaleButtonText(String text);
    }

    interface PlaybackPresenter extends BasePresenter {
        void onFragmentStop();
        void onFragmentDestory();

        void onPlayClick();


        void startPlay();
        void paushPlay();

        void stopPlay();

        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
        void onStopTrackingTouch(SeekBar seekBar);
        int changeScaleType();
        int changeScreenOri();
    }

    interface LivePresenter extends BasePresenter {


        void initWatch();

        void startWatch();

        void stopWatch();

        void onWatchBtnClick();


        void onSwitchPixel(int pixel);// 切换分辨率
        int setScaleType();
        int changeOriention();

        void onDestory();


    }
}
