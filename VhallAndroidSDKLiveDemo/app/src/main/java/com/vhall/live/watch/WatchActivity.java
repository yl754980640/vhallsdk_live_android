package com.vhall.live.watch;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.vhall.live.R;
import com.vhall.live.data.Param;
import com.vhall.live.utils.ActivityUtils;

/**
 * 观看页的Activity
 */
public class WatchActivity extends FragmentActivity implements WatchContract.WatchView {
    private FrameLayout contentVideo, contentDoc, contentDetail;
    private RadioGroup rg_tabs;
    private LinearLayout ll_detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.watch_activity);
        Param param = (Param) getIntent().getSerializableExtra("param");
        initView();
        WatchLiveFragment liveFragment = (WatchLiveFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        WatchPlaybackFragment playbackFragment = (WatchPlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        DocumentFragment docFragment = (DocumentFragment) getSupportFragmentManager().findFragmentById(R.id.contentDoc);
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentDetail);
        if (docFragment == null) {
            docFragment = DocumentFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    docFragment, R.id.contentDoc);
        }
        if (detailFragment == null) {
            detailFragment = DetailFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    detailFragment, R.id.contentDetail);
        }

        if (liveFragment == null && param.watch_type == Param.WATCH_LIVE) {
            liveFragment = WatchLiveFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    liveFragment, R.id.contentVideo);
            new WatchLivePresenter(liveFragment, docFragment, this, param);
        }

        if (playbackFragment == null && param.watch_type == Param.WATCH_PLAYBACK) {
            playbackFragment = WatchPlaybackFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    playbackFragment, R.id.contentVideo);
            new WatchPlaybackPresenter(playbackFragment, docFragment, this, param);
        }

    }

    private void initView() {
        ll_detail = (LinearLayout) this.findViewById(R.id.ll_detail);
        contentVideo = (FrameLayout) findViewById(R.id.contentVideo);
        contentDoc = (FrameLayout) findViewById(R.id.contentDoc);
        contentDetail = (FrameLayout) findViewById(R.id.contentDetail);
        rg_tabs = (RadioGroup) findViewById(R.id.rg_tabs);
        rg_tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_doc:
                        contentDoc.setVisibility(View.VISIBLE);
                        contentDetail.setVisibility(View.GONE);
                        break;
                    case R.id.rb_detail:
                        contentDoc.setVisibility(View.GONE);
                        contentDetail.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void setShowDetail(boolean isShow) {
        if (isShow) {
            ll_detail.setVisibility(View.VISIBLE);
        } else {
            ll_detail.setVisibility(View.GONE);
        }
    }

    @Override
    public void setPresenter(WatchContract.Presenter presenter) {
    }
}
