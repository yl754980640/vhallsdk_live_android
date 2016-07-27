package com.vhall.live.broadcast;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.vhall.live.R;
import com.vhall.live.data.Param;
import com.vhall.live.main.MainFragment;
import com.vhall.live.utils.ActivityUtils;

/**
 * 主界面
 *
 * @author huanan
 */
public class BroadcastActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Param param = (Param) getIntent().getSerializableExtra("param");
        if(param!=null)
            setRequestedOrientation(param.screenOri);
        setContentView(R.layout.main_activity);
        BroadcastFragment mainFragment = (BroadcastFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mainFragment == null) {
            mainFragment = BroadcastFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    mainFragment, R.id.contentFrame);
        }
        new BroadcastPresenter(param,mainFragment);
    }


}
