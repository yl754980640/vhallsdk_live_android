package com.vhall.live.watch;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.live.R;
import com.vhall.netbase.entity.DocumentInfo;

/**
 * 文档
 * 
 */
public class BottomDocumentFragment extends Fragment {

    DocumentInfo docInfo = null;
    boolean isFinish = false;
    View view;

    private ImageView pptContainer = null;// 显示PPT
    private TextView noDoc = null;

    @SuppressLint("HandlerLeak")
    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 0:
                noDoc.setVisibility(View.GONE);
                break;
            case 1:
                pptContainer.setVisibility(View.GONE);
                noDoc.setVisibility(View.VISIBLE);
                break;
            default:
                break;
            }
        }
    };

    // 处理直播情况
    public static BottomDocumentFragment newInstance(String cueDoc) {
        BottomDocumentFragment articleFragment = new BottomDocumentFragment();
        Bundle args = new Bundle();
        args.putString("cueDoc", cueDoc);
        articleFragment.setArguments(args);
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.fragment_document, null);
        initPage();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(lp);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        String cueDoc = bundle.getString("cueDoc");
        setNewDoc(cueDoc);
    }

    private void initPage() {
        pptContainer = (ImageView) view.findViewById(R.id.iv_doc);
        noDoc = (TextView) view.findViewById(R.id.tv_fragment_document_name);
    }

    public void setNewDoc(String docPath) {
        if (docPath != null) {
            PPTDownloadTask docDownload = new PPTDownloadTask();
            docDownload.setView(pptContainer);
            docDownload.execute(docPath);
            mHandle.sendEmptyMessage(0);
        } else {
            mHandle.sendEmptyMessage(1);
        }
    }

}
