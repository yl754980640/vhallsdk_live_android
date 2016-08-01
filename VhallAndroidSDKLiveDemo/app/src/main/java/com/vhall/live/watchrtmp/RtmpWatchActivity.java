package com.vhall.live.watchrtmp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.vhall.live.R;
import com.vhall.live.data.Param;
import com.vhall.live.utils.ActivityUtils;

/**
 * 观看Rtmp界面
 *
 * @author qing
 */
public class RtmpWatchActivity extends FragmentActivity implements View.OnClickListener{

    private Button showPPT;
    private LinearLayout linearLayoutShow;
    private FrameLayout act_watch_document;
    private Param param;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.watch_rtmp_activity);

        init();

        /** 传参*/
        param = (Param) getIntent().getSerializableExtra("param");
        RtmpWatchFragment rtmpWatchFragment = (RtmpWatchFragment) getSupportFragmentManager().findFragmentById(R.id.act_watch_rtmp);
        if (rtmpWatchFragment == null) {
            rtmpWatchFragment = RtmpWatchFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    rtmpWatchFragment, R.id.act_watch_rtmp);
        }

        WatchDocumentFragment watchDocumentFragment = (WatchDocumentFragment) getSupportFragmentManager().findFragmentById(R.id.act_watch_document);
        if (watchDocumentFragment == null) {
            watchDocumentFragment = WatchDocumentFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    watchDocumentFragment, R.id.act_watch_document);
        }
        new RtmpWatchPresenter(param, rtmpWatchFragment, watchDocumentFragment);
    }

    private void init() {
        showPPT = (Button) this.findViewById(R.id.act_watch_ppt_show);
        showPPT.setOnClickListener(this);
        linearLayoutShow = (LinearLayout) this.findViewById(R.id.linear_layout_show);
        act_watch_document = (FrameLayout) this.findViewById(R.id.act_watch_document);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.act_watch_ppt_show:
                if (act_watch_document.getVisibility() == View.VISIBLE) {
                    act_watch_document.setVisibility(View.INVISIBLE);
                    showPPT.setText("显示PPT");
                } else {
                    act_watch_document.setVisibility(View.VISIBLE);
                    showPPT.setText("隐藏PPT");
                }
                break;
        }
    }
}
