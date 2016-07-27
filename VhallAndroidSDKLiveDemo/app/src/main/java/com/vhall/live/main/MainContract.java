package com.vhall.live.main;

import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;
import com.vhall.live.data.Param;

/**
 * Created by huanan on 2016/6/30.
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

        void skipStart(Param param);
        void skipWatch(Param param);
        void skipPlayback(Param param);

        void showErrorMsg(String msg);

    }

    interface Presenter extends BasePresenter {

        boolean checkInput(boolean isStart);

        void startBroadcast(int ori);

        void startWatch();

        void watchPlayback();


    }
}
