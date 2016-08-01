package com.vhall.live.watchrtmp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.vhall.live.R;

/**
 * 文档
 */
public class WatchDocumentFragment extends Fragment implements RtmpWatchContract.DocumentView {
    private ImageView iv_doc;
    private RtmpWatchContract.Presenter mPresenter;

    public static WatchDocumentFragment newInstance() {
        WatchDocumentFragment articleFragment = new WatchDocumentFragment();
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.document_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iv_doc = (ImageView) getView().findViewById(R.id.iv_doc);
    }

    @Override
    public void showDoc(final String docUrl) {
        Glide.with(WatchDocumentFragment.this).load(docUrl).asBitmap().into(iv_doc);
    }

    @Override
    public void setPresenter(RtmpWatchContract.Presenter presenter) {

    }
}
