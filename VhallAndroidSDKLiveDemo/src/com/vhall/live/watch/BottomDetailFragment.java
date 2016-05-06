package com.vhall.live.watch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vhall.live.R;

/**
 * 详情
 * 
 */
public class BottomDetailFragment extends Fragment implements View.OnClickListener {


    public static BottomDetailFragment newInstance() {
        BottomDetailFragment detailFragment = new BottomDetailFragment();
        return detailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

   
    @Override
    public void onClick(View view) {
        
    }

   
}
