package com.vhall.live.watchplayback;

import android.app.Activity;
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

public class PlaybackActivity extends FragmentActivity implements PlaybackContract.PlaybackView {

    FrameLayout contentVideo, contentDoc, contentDetail;
    RadioGroup rg_tabs;
    PlaybackContract.Presenter mPresenter;
    LinearLayout ll_detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.playback_activity);
        ll_detail = (LinearLayout) this.findViewById(R.id.ll_detail);
        Param param = (Param) getIntent().getSerializableExtra("param");

        contentVideo = (FrameLayout) findViewById(R.id.contentVideo);
        contentDoc = (FrameLayout) findViewById(R.id.contentDoc);
        contentDetail = (FrameLayout) findViewById(R.id.contentDetail);
        rg_tabs = (RadioGroup) findViewById(R.id.rg_tabs);
        rg_tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_doc:
                        showDocFragment();
                        break;
                    case R.id.rb_detail:
                        showDetailFragment();
                        break;
                }
            }
        });

        VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        DocumentFragment docFragment = (DocumentFragment) getSupportFragmentManager().findFragmentById(R.id.contentDoc);
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentDetail);
        if (videoFragment == null) {
            videoFragment = VideoFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    videoFragment, R.id.contentVideo);
        }
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



        new PlaybackPresenter(this,docFragment,detailFragment,videoFragment,param);
    }

    @Override
    public void showDocFragment() {
        contentDoc.setVisibility(View.VISIBLE);
        contentDetail.setVisibility(View.GONE);
    }

    @Override
    public void showDetailFragment() {
        contentDoc.setVisibility(View.GONE);
        contentDetail.setVisibility(View.VISIBLE);
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void setShowDetail(boolean isShow) {
        if(isShow){
            ll_detail.setVisibility(View.VISIBLE);
        }else{
            ll_detail.setVisibility(View.GONE);
        }

    }

    @Override
    public void setPresenter(PlaybackContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
