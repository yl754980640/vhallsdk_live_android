package com.example.rtmpdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * 欢迎页
 * @author huanan
 *
 */
public class LaunchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
	}

	//自助式
	public void selfServiceClick(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		Constants.sdk_type = Constants.TYPE_SELF;
		startActivity(intent);
	}
	//一站式
	public void streamServiceClick(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		Constants.sdk_type = Constants.TYPE_STREAM;
		startActivity(intent);
	}

}
