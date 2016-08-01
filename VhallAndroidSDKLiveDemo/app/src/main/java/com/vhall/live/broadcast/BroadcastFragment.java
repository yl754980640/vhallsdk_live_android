package com.vhall.live.broadcast;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.VhallCameraView;
import com.vhall.live.R;



public class BroadcastFragment extends Fragment implements BroadcastContract.View, View.OnClickListener {

    public static final String TAG = "MainFragment";
    private BroadcastContract.Presenter mPresenter;
    VhallCameraView cameraview;
    TextView mSpeed;
    Button mPublish, mChangeCamera, mChangeFlash, mChangeAudio;

    public static BroadcastFragment newInstance() {
        return new BroadcastFragment();
    }

    @Override
    public void setPresenter(BroadcastContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.broadcast_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cameraview = (VhallCameraView) getView().findViewById(R.id.cameraview);
        mSpeed = (TextView) getView().findViewById(R.id.tv_upload_speed);
        mPublish = (Button) getView().findViewById(R.id.btn_publish);
        mPublish.setOnClickListener(this);
        mChangeCamera = (Button) getView().findViewById(R.id.btn_changeCamera);
        mChangeCamera.setOnClickListener(this);
        mChangeFlash = (Button) getView().findViewById(R.id.btn_changeFlash);
        mChangeFlash.setOnClickListener(this);
        mChangeAudio = (Button) getView().findViewById(R.id.btn_changeAudio);
        mChangeAudio.setOnClickListener(this);
        mPresenter.start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_publish:
                mPresenter.onstartBtnClick();
                break;
            case R.id.btn_changeAudio:
                mPresenter.changeAudio();
                break;
            case R.id.btn_changeCamera:
                mPresenter.changeCamera();
                break;
            case R.id.btn_changeFlash:
                mPresenter.changeFlash();
                break;
        }
    }

    @Override
    public VhallCameraView getCameraView() {
        return cameraview;
    }

    @Override
    public Activity getmActivity() {
        return getActivity();
    }

    @Override
    public void setStartBtnText(String text) {
        mPublish.setText(text);
    }

    @Override
    public void showErrorMsg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSpeedText(String text) {
        mSpeed.setText(text);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onResume();
    }
}
