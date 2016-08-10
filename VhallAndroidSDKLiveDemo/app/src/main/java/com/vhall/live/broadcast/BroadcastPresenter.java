package com.vhall.live.broadcast;

import android.util.Log;
import android.widget.RelativeLayout;

import com.vhall.business.Broadcast;
import com.vhall.business.VhallSDK;
import com.vhall.live.data.Param;

/**
 * 发直播的Presenter
 */
public class BroadcastPresenter implements BroadcastContract.Presenter {
    private static final String TAG = "BroadcastPresenter";

    private Param param;
    private BroadcastContract.View mView;
    private Broadcast broadcast;
    private boolean ispublishing = false;

    public BroadcastPresenter(Param params, BroadcastContract.View mView) {
        this.param = params;
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        mView.getCameraView().init(param.pixel_type, mView.getmActivity(),
                new RelativeLayout.LayoutParams(0, 0));
        getBroadcast().setAudioing(true);
        VhallSDK.getInstance().setLogEnable(false);
    }


    @Override
    public void onstartBtnClick() {
        if (ispublishing) {
            finishBroadcast();
        } else {
            if (getBroadcast().isAvaliable()) {
                startBroadcast();
            } else {
                initBroadcast();
            }
        }
    }

    @Override
    public void initBroadcast() {
        VhallSDK.getInstance().initBroadcast(param.id, param.token, getBroadcast(), new VhallSDK.InitBroadcastCallback() {
            @Override
            public void initBroadcastSuccess() {
                startBroadcast();
            }

            @Override
            public void initBroadcastFailed(String reason) {
                mView.showErrorMsg("initBroadcastFailed：" + reason);
            }
        });
    }

    @Override
    public void startBroadcast() {//发起直播
        getBroadcast().start();
    }

    @Override
    public void stopBroadcast() {//停止直播
        getBroadcast().stop();
    }

    @Override
    public void finishBroadcast() {
        VhallSDK.getInstance().finishBroadcast(param.id, param.token, getBroadcast(), new VhallSDK.FinishBroadcastCallback() {
            @Override
            public void finishSuccess() {
                Log.e(TAG, "finishSuccess");
            }

            @Override
            public void finishFailed(String reason) {
                Log.e(TAG, "finishFailed：" + reason);
            }
        });
    }

    @Override
    public void changeFlash() {
        getBroadcast().changeFlash();
    }

    @Override
    public void changeCamera() {
        getBroadcast().changeCamera();
    }

    @Override
    public void changeAudio() {
        boolean isAudioRecord = getBroadcast().isAudioing();
        getBroadcast().setAudioing(!isAudioRecord);
    }

    @Override
    public void onPause() {
        if (ispublishing)
            stopBroadcast();
    }

    @Override
    public void onResume() {
        //异常中断（HOME/PHONE）返回，是否自动继续直播
//        if (!ispublishing && getBroadcast().isAvaliable())
//            getBroadcast().start();
    }

    private Broadcast getBroadcast() {
        if (broadcast == null) {
            Broadcast.Builder builder = new Broadcast.Builder().cameraView(mView.getCameraView()).frameRate(param.frameRate).videoBitrate(param.videoBitrate).callback(new Broadcast.BroadcastEventCallback() {
                @Override
                public void startFailed(String reason) {
                    mView.showErrorMsg(reason);
                }

                @Override
                public void onConnectSuccess() {
                    mView.showErrorMsg("连接成功！");
                    ispublishing = true;
                    mView.setStartBtnText("结束直播");
                }

                @Override
                public void onErrorConnect() {
                    mView.showErrorMsg("连接失败！");
                }

                @Override
                public void onErrorParam() {
                    mView.showErrorMsg("直播参数错误！");
                }

                @Override
                public void onErrorSendData() {
                    mView.showErrorMsg("数据传输失败！");
                }

                @Override
                public void uploadSpeed(String kbps) {
                    mView.setSpeedText(kbps + "/kbps");
                }

                @Override
                public void onNetworkWeek() {
                    mView.showErrorMsg("网络环境差！");
                }

                @Override
                public void onNetworkFluency() {
                    mView.showErrorMsg("网络通畅！");
                }

                @Override
                public void onStop() {
                    ispublishing = false;
                    mView.setStartBtnText("开始直播");
                }
            });
            broadcast = builder.build();
        }
        return broadcast;
    }
}
