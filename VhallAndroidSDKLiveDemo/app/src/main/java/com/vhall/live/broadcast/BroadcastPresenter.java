package com.vhall.live.broadcast;

import android.util.Log;
import android.widget.RelativeLayout;

import com.vhall.business.Broadcast;
import com.vhall.business.VhallSDK;
import com.vhall.live.data.Param;


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
        mView.getCameraView().init(param.pixel_type, mView.getmActivity(), new RelativeLayout.LayoutParams(0, 0));
        getBroadcast().setAudioing(true);
        VhallSDK.getInstance().setLogEnable(true);
    }


    @Override
    public void onstartBtnClick() {
        if (ispublishing) {
            stopBroadcast();
        } else {
            startBroadCast();
        }
    }

    @Override
    public void startBroadCast() {
        VhallSDK.getInstance().startBroadcast(param.id, param.token, getBroadcast(), new VhallSDK.BroadcastCallback() {
            @Override
            public void getStreaminfoSuccess() {
                Log.e(TAG, "getStreaminfoSuccess");
            }

            @Override
            public void getStreaminfoFailed(String reason) {
                Log.e(TAG, "getStreaminfoFailed：" + reason);
                mView.showErrorMsg("getStreaminfoFailed：" + reason);
            }
        });
    }

    @Override
    public void stopBroadcast() {
        VhallSDK.getInstance().stopBroadcast(param.id, param.token, getBroadcast(), new VhallSDK.StopBroadcastCallback() {
            @Override
            public void stopSuccess() {
                Log.e(TAG, "stopSuccess");
            }

            @Override
            public void stopFailed(String reason) {
                Log.e(TAG, "stopFailed：" + reason);
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
        getBroadcast().pause();
    }

    @Override
    public void onResume() {
        getBroadcast().resume();
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
