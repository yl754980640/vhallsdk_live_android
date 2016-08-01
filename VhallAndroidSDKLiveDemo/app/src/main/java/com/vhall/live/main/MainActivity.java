package com.vhall.live.main;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.vhall.live.R;
import com.vhall.live.utils.ActivityUtils;

/**
 * 主界面
 */
public class MainActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    mainFragment, R.id.contentFrame);
        }
        new MainPresenter(mainFragment);
    }


}
