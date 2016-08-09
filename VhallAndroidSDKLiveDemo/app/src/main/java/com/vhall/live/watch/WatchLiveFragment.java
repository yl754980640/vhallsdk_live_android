package com.vhall.live.watch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.WatchLive;
import com.vhall.live.R;
import com.vhall.live.VhallApplication;


import java.util.HashMap;

/**
 * 观看直播的Fragment
 */
public class WatchLiveFragment extends Fragment implements WatchContract.LiveView, View.OnClickListener {

    private WatchContract.LivePresenter mPresenter;

    private ImageView clickStart;
    private ImageView clickOrientation;
    private RadioButton radio_button_sd;
    private RadioButton radio_button_hd;
    private RadioButton radio_button_uhd;
    private RadioGroup radioChoose;
    private TextView fragmentDownloadSpeed;
    private ProgressDialog mProcessDialog;
    private RelativeLayout mContainerLayout;
    private Button btn_change_scaletype;
    /***
     * 控制显示Toast
     */
    public static boolean isShowToast = true;

    public static WatchLiveFragment newInstance() {
        return new WatchLiveFragment();
    }

    @Override
    public void setPresenter(WatchContract.LivePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.watch_live_fragment, container, false);
        initView(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stopWatch();
    }

    private void initView(View root) {
        clickStart = (ImageView) root.findViewById(R.id.click_rtmp_watch);
        clickStart.setOnClickListener(this);
        clickOrientation = (ImageView) root.findViewById(R.id.click_rtmp_orientation);
        clickOrientation.setOnClickListener(this);
        radioChoose = (RadioGroup) root.findViewById(R.id.radio_choose);
        radioChoose.setOnCheckedChangeListener(checkListener);
        radio_button_sd = (RadioButton) root.findViewById(R.id.radio_btn_sd);
        radio_button_hd = (RadioButton) root.findViewById(R.id.radio_btn_hd);
        radio_button_uhd = (RadioButton) root.findViewById(R.id.radio_btn_uhd);
        mContainerLayout = (RelativeLayout) root.findViewById(R.id.rl_container);
        fragmentDownloadSpeed = (TextView) root.findViewById(R.id.fragment_download_speed);

        btn_change_scaletype = (Button) root.findViewById(R.id.btn_change_scaletype);
        btn_change_scaletype.setOnClickListener(this);
        /** 初始化Dialog*/
        mProcessDialog = new ProgressDialog(this.getActivity());
        mProcessDialog.setCancelable(true);
        mProcessDialog.setCanceledOnTouchOutside(false);
        mPresenter.start();
    }

    @Override
    public Activity getmActivity() {
        return this.getActivity();
    }

    @Override
    public RelativeLayout getWatchLayout() {
        return mContainerLayout;
    }

    @Override
    public void setPlayPicture(boolean state) {
        if (state) {
            clickStart.setBackgroundResource(R.drawable.pause);
        } else {
            clickStart.setBackgroundResource(R.drawable.play);
        }
    }

    @Override
    public void setDownSpeed(String text) {
        fragmentDownloadSpeed.setText(text);
    }

    @Override
    public void showLoading(boolean isShow) {
        if (isShow) {
            mProcessDialog.show();
        } else {
            mProcessDialog.dismiss();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.click_rtmp_watch:
                mPresenter.onWatchBtnClick();
                break;
            case R.id.click_rtmp_orientation:
                mPresenter.changeOriention();
                break;
            case R.id.btn_change_scaletype:
                mPresenter.setScaleType();
                break;
            default:
                break;
        }
    }

    @Override
    public void showToast(String message) {
        if (isShowToast) {
            Toast.makeText(VhallApplication.getApp(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 切换分辨率
     *
     * @param map 0 : 无效不可用  1 ：有效可用
     */
    @Override
    public void showRadioButton(HashMap map) {
        if (map == null)
            return;
        if ((Integer) map.get("SD") == 1) {
            radio_button_sd.setVisibility(View.VISIBLE);
        } else {
            radio_button_sd.setVisibility(View.GONE);
        }
        if ((Integer) map.get("HD") == 1) {
            radio_button_hd.setVisibility(View.VISIBLE);
        } else {
            radio_button_hd.setVisibility(View.GONE);
        }
        if ((Integer) map.get("UHD") == 1) {
            radio_button_uhd.setVisibility(View.VISIBLE);
        } else {
            radio_button_uhd.setVisibility(View.GONE);
        }
    }

    @Override
    public void setScaleButtonText(String text) {
        btn_change_scaletype.setText(text);
    }

    @Override
    public int changeOrientation() {
        if (this.getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        return getActivity().getRequestedOrientation();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public RadioGroup.OnCheckedChangeListener checkListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i) {
                case R.id.radio_btn_sd:
                    mPresenter.onSwitchPixel(WatchLive.DPI_SD);
                    break;
                case R.id.radio_btn_hd:
                    mPresenter.onSwitchPixel(WatchLive.DPI_HD);
                    break;
                case R.id.radio_btn_uhd:
                    mPresenter.onSwitchPixel(WatchLive.DPI_UHD);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestory();
    }
}
