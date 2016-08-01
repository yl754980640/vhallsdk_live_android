package com.vhall.live.watchplayback;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vhall.live.R;

/**
 * 详情
 */
public class DetailFragment extends Fragment implements PlaybackContract.DetailView {

    PlaybackContract.Presenter mPresenter;

    public static DetailFragment newInstance() {
        DetailFragment articleFragment = new DetailFragment();
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setPresenter(PlaybackContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
