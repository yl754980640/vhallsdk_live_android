package com.vhall.live.main;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.vhall.live.data.Param;

/**
 * Created by huanan on 2016/6/30.
 */

public class MainPresenter implements MainContract.Presenter {

    private MainContract.View mView;

    public MainPresenter(MainContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public boolean checkInput(boolean isStart) {
        if (TextUtils.isEmpty(mView.getID())) {
            mView.showErrorMsg("请填写ID");
            return false;
        }
        if (TextUtils.isEmpty(mView.getToken())) {
            mView.showErrorMsg("请填写token");
            return false;
        }
        if (mView.getBufferSecond() <= 0) {
            mView.showErrorMsg("缓冲时间必须大于0");
            return false;
        }
        if (isStart) {
            if (mView.getvideoBitrate() <= 0) {
                mView.showErrorMsg("码率必须大于0");
                return false;
            }
            if (mView.getFrameRate() <= 0) {
                mView.showErrorMsg("帧率必须大于0");
                return false;
            }
        }
        return true;
    }

    @Override
    public void startBroadcast(int ori) {
        if (!checkInput(true))
            return;
        mView.skipStart(getParam(ori));
    }

    @Override
    public void startWatch() {
        if(!checkInput(false))
            return;
        mView.skipWatch(getParam(0));
    }

    @Override
    public void watchPlayback() {
        if(!checkInput(false))
            return;
        mView.skipPlayback(getParam(0));
    }

    private Param getParam(int ori){
       Param params = new Param();
        params.id = mView.getID();
        params.token = mView.getToken();
        params.videoBitrate = mView.getvideoBitrate();
        params.frameRate = mView.getFrameRate();
        params.bufferSecond = mView.getBufferSecond();
        params.k = mView.getK();
        params.pixel_type = mView.getdpi();
        params.screenOri = ori;
        return params;
    }

    @Override
    public void start() {

    }
}
