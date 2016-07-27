package com.vhall.live.watchplayback;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vhall.live.BasePresenter;
import com.vhall.live.R;
import com.vhall.live.utils.VhallUtil;

/**
 * 详情
 */
public class VideoFragment extends Fragment implements PlaybackContract.VideoView {

    private static final String TAG = "VideoFragment";

    PlaybackContract.Presenter mPresenter;

    SurfaceView surface;
    ProgressBar pb;
    ImageView iv_play, iv_fullscreen;
    SeekBar seekbar;
    TextView tv_pos;

    RelativeLayout rl_hlscontainer;
    Button btn_changescaletype;

    public static VideoFragment newInstance() {
        VideoFragment articleFragment = new VideoFragment();
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.video_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        surface = (SurfaceView) getView().findViewById(R.id.surface);
        rl_hlscontainer = (RelativeLayout) getView().findViewById(R.id.rl_hlscontainer);
        btn_changescaletype = (Button) getView().findViewById(R.id.btn_changescaletype);
        pb = (ProgressBar) getView().findViewById(R.id.pb);
        iv_play = (ImageView) getView().findViewById(R.id.iv_play);
        iv_fullscreen = (ImageView) getView().findViewById(R.id.iv_fullscreen);
        seekbar = (SeekBar) getView().findViewById(R.id.seekbar);
        tv_pos = (TextView) getView().findViewById(R.id.tv_pos);
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onPlayClick();
            }
        });
        iv_fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.changeScreenOri();
            }
        });
        btn_changescaletype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.changeScaleType();
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPresenter.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.onStopTrackingTouch(seekBar);
            }
        });
    }

    @Override
    public void setPresenter(PlaybackContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public SurfaceView getSurfaceView() {
        return surface;
    }

    @Override
    public void setPlayIcon(boolean isStop) {
        if (isStop) {
            iv_play.setImageResource(R.drawable.icon_play_play);
        } else {
            iv_play.setImageResource(R.drawable.icon_play_pause);
        }
    }

    @Override
    public void setProgressLabel(String text) {
        tv_pos.setText(text);
    }

    @Override
    public void setSeekbarMax(int max) {
        seekbar.setMax(max);
    }

    @Override
    public void setSeekbarCurrentPosition(int position) {
        seekbar.setProgress(position);
    }

    @Override
    public void showProgressbar(boolean show) {
        if (show)
            pb.setVisibility(View.VISIBLE);
        else
            pb.setVisibility(View.GONE);
    }

    @Override
    public RelativeLayout getContainer() {
        return rl_hlscontainer;
    }

    @Override
    public void setScaleTypeText(String text) {
        btn_changescaletype.setText(text);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.onFragmentStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onFragmentDestory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
