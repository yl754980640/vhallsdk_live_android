package com.vhall.live.watchplayback;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.vhall.business.VhallPPT;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchRtmp;
import com.vhall.live.data.Param;
import com.vhall.live.utils.VhallUtil;
import com.vhall.playersdk.player.impl.HlsRendererBuilder;
import com.vhall.playersdk.player.impl.VhallHlsPlayer;
import com.vhall.playersdk.player.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class PlaybackPresenter implements PlaybackContract.Presenter {
    private static final String TAG = "PlaybackPresenter";

    private PlaybackContract.PlaybackView playbackView;
    private PlaybackContract.DocumentView documentView;
    private PlaybackContract.DetailView detailView;
    private PlaybackContract.VideoView videoView;
    private Param param;
    private String mediaUrl;

    private VhallPPT ppt;

    private VhallHlsPlayer mMediaPlayer;
    private VhallPlayerListener mVhallPlayerListener;
    private long playerCurrentPosition = 0L; // 度播放的当前标志，毫秒
    private long playerDuration;// 播放资源的时长，毫秒
    private String playerDurationTimeStr = "00:00:00";


    private int scaleType = WatchRtmp.DEFAULT;
    private int videoWidth = 0;
    private int videoHeight = 0;

    Timer timer;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                playerCurrentPosition = mMediaPlayer.getCurrentPosition();
                videoView.setSeekbarCurrentPosition((int) playerCurrentPosition);
                String playerCurrentPositionStr = VhallUtil.converLongTimeToStr(playerCurrentPosition);
                videoView.setProgressLabel(playerCurrentPositionStr + "/" + playerDurationTimeStr);
                if (ppt != null) {
                    String url = ppt.getPPT(playerCurrentPosition / 1000);
                    documentView.showDoc(url);
                }
            }
        }
    };


    public PlaybackPresenter(PlaybackContract.PlaybackView playbackView, PlaybackContract.DocumentView documentView, PlaybackContract.DetailView detailView, PlaybackContract.VideoView videoView, Param param) {
        this.playbackView = playbackView;
        this.documentView = documentView;
        this.detailView = detailView;
        this.videoView = videoView;
        this.param = param;
        this.playbackView.setPresenter(this);
        this.documentView.setPresenter(this);
        this.detailView.setPresenter(this);
        this.videoView.setPresenter(this);
    }

    @Override
    public void start() {
        videoView.setScaleTypeText(VhallUtil.getFixType(scaleType));
        VhallSDK.getInstance().getVideoURL(param.id, "test", "test@vhall.com", param.k, new VhallSDK.VideoURLCallback() {
            @Override
            public void getURLSuccess(String url) {
                Log.e(TAG, "url->" + url);
                mediaUrl = url;
                handlePosition();
            }

            @Override
            public void getURLFailed(String reason) {
                Log.e(TAG, "reason->" + reason);
                Toast.makeText(playbackView.getActivity(), reason, Toast.LENGTH_SHORT).show();
            }
        }, ppt = new VhallPPT());

    }


    @Override
    public void onFragmentStop() {
        releasePlayer();
    }

    @Override
    public void onFragmentDestory() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void playVideo() {
        String userAgent = Util.getUserAgent(playbackView.getActivity(), "VhallAPP");
        mVhallPlayerListener = new VhallPlayerListener();
        mMediaPlayer = new VhallHlsPlayer(new HlsRendererBuilder(playbackView.getActivity(),
                userAgent, mediaUrl));
        mMediaPlayer.addListener(mVhallPlayerListener);
        mMediaPlayer.prepare();
        mMediaPlayer.setSurface(videoView.getSurfaceView().getHolder().getSurface());
        mMediaPlayer.setPlayWhenReady(true);
    }

    @Override
    public void startPlay() {
        if (TextUtils.isEmpty(mediaUrl))
            return;
        if (mMediaPlayer == null)
            playVideo();
        else
            mMediaPlayer.start();
        videoView.setPlayIcon(false);
    }

    @Override
    public void paushPlay() {
        if (mMediaPlayer != null)
            mMediaPlayer.pause();
        videoView.setPlayIcon(true);
    }

    @Override
    public void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        videoView.setPlayIcon(true);
    }

    @Override
    public void onPlayClick() {
        if (mMediaPlayer == null) {
            startPlay();
        } else {
            if (mMediaPlayer.isPlaying()) {
                paushPlay();
            } else {
                startPlay();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        videoView.setProgressLabel(VhallUtil.converLongTimeToStr(progress) + "/" + playerDurationTimeStr);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        playerCurrentPosition = seekBar.getProgress();
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(playerCurrentPosition);
            startPlay();
        } else {
            startPlay();
            mMediaPlayer.seekTo(playerCurrentPosition);
        }
    }

    int[] scaleTypeList = new int[]{WatchRtmp.DEFAULT, WatchRtmp.CENTER_INSIDE, WatchRtmp.FIT_X, WatchRtmp.FIT_Y, WatchRtmp.FIT_XY};
    int currentPos = 0;

    @Override
    public int changeScaleType() {
        scaleType = scaleTypeList[(++currentPos) % scaleTypeList.length];
        setSurfaceFixSize();
        videoView.setScaleTypeText(VhallUtil.getFixType(scaleType));
        return scaleType;
    }

    @Override
    public int changeScreenOri() {
        if (playbackView.getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            playbackView.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            playbackView.setShowDetail(false);
        } else {
            playbackView.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            playbackView.setShowDetail(true);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setSurfaceFixSize();
            }
        }, 100);
        return playbackView.getActivity().getRequestedOrientation();
    }

    /**
     * 自定义播放器监听事件处理
     */

    private class VhallPlayerListener implements VhallHlsPlayer.Listener {
        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case VhallHlsPlayer.STATE_IDLE:
                    Log.e(TAG, "--------------------->STATE_IDLE");

                    break;
                case VhallHlsPlayer.STATE_PREPARING:
                    Log.e(TAG, "--------------------->STATE_PREPARING");
                    videoView.showProgressbar(true);
                    break;
                case VhallHlsPlayer.STATE_BUFFERING:
                    videoView.showProgressbar(true);
                    Log.e(TAG, "--------------------->STATE_BUFFERING");
                    break;
                case VhallHlsPlayer.STATE_READY:
                    videoView.showProgressbar(false);
                    playerDuration = mMediaPlayer.getDuration();
                    playerDurationTimeStr = VhallUtil.converLongTimeToStr(playerDuration);
                    videoView.setSeekbarMax((int) playerDuration);
                    Log.e(TAG, "--------------------->STATE_READY");
                    break;
                case VhallHlsPlayer.STATE_ENDED:
                    Log.e(TAG, "--------------------->STATE_ENDED");
                    releasePlayer();
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onError(Exception e) {
            releasePlayer();
        }

        @Override
        public void onVideoSizeChanged(int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            if (width == 0 || height == 0) {
                return;
            }
            videoWidth = width;
            videoHeight = height;
            setSurfaceFixSize();
        }
    }

    private void handlePosition() {
        if (timer != null)
            return;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 1000, 1000);
    }

    public void setSurfaceFixSize() {
        if (videoWidth == 0 || videoHeight == 0)
            return;
        int fixWidth = 0;
        int fixHeight = 0;
        switch (scaleType) {
            case WatchRtmp.DEFAULT:
                int frameWidth = videoView.getContainer().getWidth();
                int frameHeight = videoView.getContainer().getHeight();
                float framePercent = frameWidth * 1.0f / frameHeight;
                float videoPercent = videoWidth * 1.0f / videoHeight;
                if (framePercent < videoPercent) {//FIT_X
                    fixWidth = frameWidth;
                    fixHeight = fixWidth * videoHeight / videoWidth;
                } else {//FIT_Y
                    fixHeight = frameHeight;
                    fixWidth = videoWidth * fixHeight / videoHeight;
                }
                break;
            case WatchRtmp.CENTER_INSIDE:
                fixWidth = videoWidth;
                fixHeight = videoHeight;
                break;
            case WatchRtmp.FIT_X:
                fixWidth = videoView.getContainer().getWidth();
                fixHeight = fixWidth * videoHeight / videoWidth;
                break;
            case WatchRtmp.FIT_Y:
                fixHeight = videoView.getContainer().getHeight();
                fixWidth = videoWidth * fixHeight / videoHeight;
                break;
            case WatchRtmp.FIT_XY:
                fixWidth = videoView.getContainer().getWidth();
                fixHeight = videoView.getContainer().getHeight();
                break;
        }

        Log.e(TAG, "videowidth:" + videoWidth + "videoheight:"
                + videoHeight + "fixwidth:" + fixWidth + "fixheight:"
                + fixHeight);
        if (videoView.getSurfaceView() != null && fixWidth > 0 && fixHeight > 0)
            videoView.getSurfaceView().getHolder().setFixedSize(fixWidth, fixHeight);
    }
}
