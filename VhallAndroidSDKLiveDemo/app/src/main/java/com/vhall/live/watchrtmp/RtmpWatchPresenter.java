package com.vhall.live.watchrtmp;

import android.os.Handler;
import android.util.Log;

import com.vhall.business.VhallPPT;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchRtmp;
import com.vhall.live.data.Param;

import java.util.HashMap;

/**
 * Created by huanan on 2016/6/30.
 */

public class RtmpWatchPresenter implements RtmpWatchContract.Presenter {
    private static final String TAG = "RtmpWatchPresenter";

    private Param params;
    private RtmpWatchContract.View watchView;
    private RtmpWatchContract.DocumentView documentView;
    public static boolean isWatching = false;
    private int pixel = 0;

    private WatchRtmp watchRtmp;
    private VhallPPT ppt;

    public RtmpWatchPresenter(Param param, RtmpWatchContract.View view, RtmpWatchContract.DocumentView documentView) {
        this.params = param;
        this.watchView = view;
        this.documentView = documentView;
        watchView.setPresenter(this);
    }

    @Override
    public void start() {
        VhallSDK.getInstance().setLogEnable(true);
    }

    @Override
    public void onWatchBtnClick(int level) {
        if (!isWatching) {
            watchStart(level);
        } else {
            watchStop();
        }
    }

    @Override
    public void onWatchBack() {
        watchView.getWatchActivity().finish();
    }

    @Override
    public void onSwitchPixel(final int level) {
        if (isWatching) {
            watchStop();
        }
        /** 停止观看 不能立即重连 要延迟一秒重连*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isWatching) {
                    watchStart(level);
                }
            }
        }, 1000);
    }

    @Override
    public int setScaleType(int type) {
        getWatchRtmp().setScaleType(type);
        return type;
    }


    @Override
    public void watchStart(int pixel) {
        this.pixel = pixel;
        ppt = new VhallPPT();
        ppt.setCallback(new VhallPPT.PPTChangeCallback() {
            @Override
            public void onPPTChage(String url) {
                Log.v(TAG, " Document url === " + url);
                documentView.showDoc(url);
            }
        });
        VhallSDK.getInstance().watchRtmpVideo(getWatchRtmp(), params.id, "test", "test@vhall.com", params.k, ppt, new VhallSDK.WatchRtmpCallback() {
            @Override
            public void watchSuccess() {

            }

            @Override
            public void watchFailed(String msg) {
                watchView.showToast(msg);
            }

            @Override
            public void watchGetPixelAvailable(HashMap map) {
                watchView.showRadioButton(map);
            }
        });
    }


    @Override
    public void watchStop() {
        if(isWatching)
        VhallSDK.getInstance().watchStopVideo(watchRtmp);
    }

    public WatchRtmp getWatchRtmp() {
        if (pixel < 0) {
            return null;
        }
        if (watchRtmp == null) {
            WatchRtmp.Builder builder = new WatchRtmp.Builder().context(watchView.getWatchActivity()).containerLayout(watchView.getWatchLayout()).bufferDelay(params.bufferSecond).callback(new WatchRtmp.WatchEventCallback() {
                @Override
                public void watchFailed(String reason) {
                    watchView.showToast(reason);
                }

                @Override
                public void watchConnectSuccess() {
                    //mWatchView.showToast("观看连接成功");
                    isWatching = true;
                    watchView.setWatchButtonText("停止");
                }

                @Override
                public void watchConnectFailed(String reason) {
                    watchView.showToast(reason);
                    watchView.showLoading(false);
                }

                @Override
                public void watchLoadingSpeed(String kbps) {
                    watchView.setDownSpeed("速率" + kbps + "/kbps");
                }

                @Override
                public void watchStartBuffering() {
                    if (isWatching) {
                        watchView.showLoading(true);
                    }
                }

                @Override
                public void watchStopBuffering() {
                    watchView.showLoading(false);
                }

                @Override
                public void watchStopVideoSuccess(String reson) {
                    isWatching = false;
                    watchView.setWatchButtonText("观看");
                    watchView.showToast(reson);
                }

                @Override
                public void watchStopVideoFailed(String reson) {
                    watchView.showToast(reson);
                }

                @Override
                public void watchSwitchPixelSuccess() {
                    watchView.showToast("切换成功");
                }

                @Override
                public void watchSwitchPixelFailed(String reson) {
                    watchView.showToast(reson);
                }

            });
            watchRtmp = builder.build();
        }
        watchRtmp.setDefinition(pixel);
        return watchRtmp;
    }
}
