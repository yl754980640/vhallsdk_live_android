package com.vhall.live.main;

import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;
import com.vhall.live.data.Param;

/**
 * 程序主界面的View接口类
 */
public class MainContract {
    interface View extends BaseView<Presenter> {
        String getID();
        String getToken();
        int getvideoBitrate();
        int getFrameRate();
        int getBufferSecond();
        String getK();
        int getdpi();
        void skipBroadcast(Param param);
        void skipWatch(Param param);
        void showErrorMsg(String msg);
    }

    interface Presenter extends BasePresenter {
        boolean checkInput(boolean isStart);
        void startBroadcast(int ori);
        void startWatch(int watch_type);
    }
}
