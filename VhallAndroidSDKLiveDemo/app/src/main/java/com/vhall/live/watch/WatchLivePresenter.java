package com.vhall.live.watch;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.util.Log;

import com.vhall.business.VhallPPT;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchLive;
import com.vhall.live.data.Param;
import com.vhall.live.utils.VhallUtil;


/**
 * 观看直播的Presenter
 */
public class WatchLivePresenter implements WatchContract.LivePresenter {
    private static final String TAG = "WatchLivePresenter";
    private Param params;
    private WatchContract.LiveView liveView;

    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;

    public boolean isWatching = false;
    private WatchLive watchLive;

    private VhallPPT vhallPPT;
    int[] scaleTypes = new int[]{WatchLive.FIT_DEFAULT, WatchLive.FIT_CENTER_INSIDE, WatchLive.FIT_X, WatchLive.FIT_Y, WatchLive.FIT_XY};
    int currentPos = 0;
    private int scaleType = WatchLive.FIT_DEFAULT;

    public WatchLivePresenter(WatchContract.LiveView liveView, WatchContract.DocumentView documentView, WatchContract.WatchView watchView, Param param) {
        this.params = param;
        this.liveView = liveView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.liveView.setPresenter(this);
    }

    @Override
    public void start() {
        VhallSDK.getInstance().setLogEnable(true);
        liveView.setScaleButtonText(VhallUtil.getFixType(scaleType));
        initWatch();
    }

    @Override
    public void onWatchBtnClick() {
        if (isWatching) {
            stopWatch();
        } else {
            if (getWatchLive().isAvaliable()) {
                startWatch();
            } else {
                initWatch();
            }
        }
    }

    @Override
    public void onSwitchPixel(int level) {
        if (isWatching) {
            stopWatch();
        }
        getWatchLive().setDefinition(level);
        /** 停止观看 不能立即重连 要延迟一秒重连*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isWatching) {
                    startWatch();
                }
            }
        }, 500);
    }


    @Override
    public int setScaleType() {
        scaleType = scaleTypes[(++currentPos) % scaleTypes.length];
        getWatchLive().setScaleType(scaleType);
        liveView.setScaleButtonText(VhallUtil.getFixType(scaleType));
        return scaleType;
    }

    @Override
    public int changeOriention() {
        int ori = liveView.changeOrientation();
        if (ori == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            watchView.setShowDetail(true);
        } else {
            watchView.setShowDetail(false);
        }
        getWatchLive().setScaleType(scaleType);
        return ori;
    }

    @Override
    public void onDestory() {
        getWatchLive().destory();
    }


    @Override
    public void initWatch() {
        VhallSDK.getInstance().initWatch(params.id, "test", "test@vhall.com", params.k, getWatchLive(), new VhallSDK.InitWatchCallback() {
            @Override
            public void initSuccess() {
                liveView.showRadioButton(getWatchLive().getDPIStatus());
                setPPT();
            }

            @Override
            public void initFailed(String msg) {
                liveView.showToast(msg);
            }
        });
    }

    @Override

    public void startWatch() {
        getWatchLive().start();
    }


    @Override
    public void stopWatch() {
        if (isWatching) {
            getWatchLive().stop();
            isWatching = false;
            liveView.setPlayPicture(isWatching);
        }
    }

    public WatchLive getWatchLive() {
        if (watchLive == null) {
            WatchLive.Builder builder = new WatchLive.Builder().context(liveView.getmActivity()).containerLayout(liveView.getWatchLayout()).bufferDelay(params.bufferSecond).callback(new WatchLive.WatchEventCallback() {
                @Override
                public void watchFailed(String reason) {
                    liveView.showToast(reason);
                }

                @Override
                public void watchConnectSuccess() {
                    isWatching = true;
                    liveView.setPlayPicture(isWatching);
                }

                @Override
                public void watchConnectFailed(String reason) {
                    isWatching = false;
                    liveView.showToast(reason);
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
                }

                @Override
                public void watchLoadingSpeed(String kbps) {
                    liveView.setDownSpeed("速率" + kbps + "/kbps");
                }

                @Override
                public void watchStartBuffering() {
                    if (isWatching) {
                        liveView.showLoading(true);
                    }
                }

                @Override
                public void watchStopBuffering() {
                    liveView.showLoading(false);
                }

            });
            watchLive = builder.build();
        }
        return watchLive;
    }

    //如果没有PPT业务,可忽略
    private void setPPT() {
        if (vhallPPT == null){
            vhallPPT = new VhallPPT();
        }
        vhallPPT.setCallback(new VhallPPT.PPTChangeCallback() {
            @Override
            public void onPPTChange(String url) {
                Log.v(TAG, " Document url -> " + url);
                documentView.showDoc(url);
            }
        });
        getWatchLive().setVhallPPT(vhallPPT);
    }
}
