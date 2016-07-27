package com.vhall.live.watchplayback;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;

/**
 * Created by huanan on 2016/7/8.
 */
public class PlaybackContract {

    interface PlaybackView extends BaseView<Presenter> {
        void showDocFragment();
        void showDetailFragment();
        Activity getActivity();
        void setShowDetail(boolean isShow);
    }

    interface DocumentView extends BaseView<Presenter> {
        void showDoc(String docUrl);
    }

    interface DetailView extends BaseView<Presenter> {
    }

    interface VideoView extends BaseView<Presenter> {
        SurfaceView getSurfaceView();

        void setPlayIcon(boolean isStop);

        void setProgressLabel(String text);

        void setSeekbarMax(int max);

        void setSeekbarCurrentPosition(int position);

        void showProgressbar(boolean show);

         RelativeLayout getContainer();

        void setScaleTypeText(String text);

    }

    interface Presenter extends BasePresenter {
        void onFragmentStop();

        void onFragmentDestory();

        void playVideo();

        void startPlay();

        void paushPlay();

        void releasePlayer();


        void onPlayClick();

        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

        void onStopTrackingTouch(SeekBar seekBar);

         int changeScaleType();

         int changeScreenOri();
    }


}
