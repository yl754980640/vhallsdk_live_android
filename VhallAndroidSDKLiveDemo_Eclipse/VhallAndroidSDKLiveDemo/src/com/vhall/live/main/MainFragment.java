package com.vhall.live.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.vhall.live.R;
import com.vhall.live.broadcast.BroadcastActivity;
import com.vhall.live.data.Param;
import com.vhall.live.watchplayback.PlaybackActivity;
import com.vhall.live.watchrtmp.RtmpWatchActivity;

/**
 * Created by huanan on 2016/6/30.
 */

public class MainFragment extends Fragment implements MainContract.View, View.OnClickListener {

    public static final String TAG = "MainFragment";
    private MainContract.Presenter mPresenter;

    private EditText mId, mToken, mVideoBitrate, mFrameRate, mBufferTime, mK;
    RadioGroup mType;
    RadioButton mType_hdpi, mType_xhdpi;

    private Button mStartPotrait, mStartLandspace, mWatchRTMP, mWatchHLS;


    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        mId = (EditText) getView().findViewById(R.id.et_id);
        mToken = (EditText) getView().findViewById(R.id.et_token);
        mVideoBitrate = (EditText) getView().findViewById(R.id.et_bitrate);
        mFrameRate = (EditText) getView().findViewById(R.id.et_frame_rate);
        mBufferTime = (EditText) getView().findViewById(R.id.et_buffertime);
        mK = (EditText) getView().findViewById(R.id.et_k);
        mType = (RadioGroup) getView().findViewById(R.id.rg_type);
        mType_hdpi = (RadioButton) getView().findViewById(R.id.rb_hdpi);
        mType_xhdpi = (RadioButton) getView().findViewById(R.id.rb_xhdpi);

        mStartPotrait = (Button) getView().findViewById(R.id.btn_start_potrait);
        mStartLandspace = (Button) getView().findViewById(R.id.btn_start_landspace);
        mWatchRTMP = (Button) getView().findViewById(R.id.btn_watch_rtmp);
        mWatchHLS = (Button) getView().findViewById(R.id.btn_watch_hls);

        mStartPotrait.setOnClickListener(this);
        mStartLandspace.setOnClickListener(this);
        mStartLandspace.setOnClickListener(this);
        mWatchRTMP.setOnClickListener(this);
        mWatchHLS.setOnClickListener(this);

    }

    @Override
    public String getID() {
        return mId.getText().toString();
    }

    @Override
    public String getToken() {
        return mToken.getText().toString();
    }

    @Override
    public int getvideoBitrate() {
        String codeRate = mVideoBitrate.getText().toString();
        int code = 0;
        try {
            code = Integer.parseInt(codeRate);
            code = code * 1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return code;
    }

    @Override
    public int getFrameRate() {
        String frameRate = mFrameRate.getText().toString();
        int frame = 0;
        try {
            frame = Integer.parseInt(frameRate);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return frame;
    }

    @Override
    public int getBufferSecond() {
        String bufferSecond = mBufferTime.getText().toString();
        int buffer = 0;
        try {
            buffer = Integer.parseInt(bufferSecond);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    @Override
    public String getK() {
        return mK.getText().toString();
    }

    @Override
    public int getdpi() {
        if (mType_hdpi.isChecked()) {
            return Param.HDPI;
        } else if (mType_xhdpi.isChecked()) {
            return Param.XHDPI;
        } else {
            return Param.HDPI;
        }
    }

    @Override
    public void skipStart(Param param) {
        Intent intent = new Intent(getActivity(), BroadcastActivity.class);
        intent.putExtra("param", param);
        startActivity(intent);
    }

    @Override
    public void skipWatch(Param param) {
        Intent intent = new Intent(getActivity(), RtmpWatchActivity.class);
        intent.putExtra("param", param);
        startActivity(intent);
    }

    @Override
    public void skipPlayback(Param param) {
        Intent intent = new Intent(getActivity(), PlaybackActivity.class);
        intent.putExtra("param", param);
        startActivity(intent);
    }

    @Override
    public void showErrorMsg(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_watch_rtmp:
                mPresenter.startWatch();
                break;
            case R.id.btn_start_potrait:
                mPresenter.startBroadcast(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.btn_start_landspace:
                mPresenter.startBroadcast(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.btn_watch_hls:
                mPresenter.watchPlayback();
                break;
        }
    }
}
