package com.example.rtmpdemo;

import org.json.JSONException;
import org.json.JSONObject;

import com.vinny.vinnylive.LiveParam;
import com.vinny.vinnylive.ZReqEngine;
import com.vinny.vinnylive.ZReqEngine.ReqCallback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 主界面
 * 
 * @author huanan
 *
 */
public class MainActivity extends Activity {

	EditText et_roomid, et_token, et_bitrate, et_delay,et_password;
	LinearLayout ll_password;
	TextView tv_roomid;
	RadioGroup rg_type;
	LiveParam param;
	int delay = 2;
	TextView tv_param;
	private ProgressDialog mProcessDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		et_roomid = (EditText) this.findViewById(R.id.et_roomid);
		tv_roomid = (TextView) this.findViewById(R.id.tv_roomid);
		et_token = (EditText) this.findViewById(R.id.et_token);
		et_bitrate = (EditText) this.findViewById(R.id.et_bitrate);
		et_delay = (EditText) this.findViewById(R.id.et_delay);
		et_password = (EditText) this.findViewById(R.id.et_password);
		ll_password = (LinearLayout) this.findViewById(R.id.ll_password);
		rg_type = (RadioGroup) this.findViewById(R.id.rg_type);
		tv_param = (TextView) this.findViewById(R.id.tv_param);
		mProcessDialog = new ProgressDialog(MainActivity.this);
		mProcessDialog.setCancelable(true);
		mProcessDialog.setCanceledOnTouchOutside(false);
		param = LiveParam.getParam(LiveParam.TYPE_HDPI);
		tv_param.setText(param.getParamStr());
		initData();
		rg_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_hdpi:
					param = LiveParam.getParam(LiveParam.TYPE_HDPI);
					tv_param.setText(param.getParamStr());
					break;
				case R.id.rb_xhdpi:
					param = LiveParam.getParam(LiveParam.TYPE_XHDPI);
					tv_param.setText(param.getParamStr());
					break;
				case R.id.rb_xxhdpi:
					param = LiveParam.getParam(LiveParam.TYPE_XXHDPI);
					tv_param.setText(param.getParamStr());
					break;

				default:
					break;
				}
			}
		});
	}
	
	/**
	 * 初始化显示和参数
	 */
	private void initData(){
		Constants.sdk_type = Constants.TYPE_SELF;
		if(Constants.sdk_type == Constants.TYPE_SELF){
			tv_roomid.setText("活动id");
			ll_password.setVisibility(View.VISIBLE);
			et_roomid.setText("");
			et_token.setText("");
		}else if(Constants.sdk_type == Constants.TYPE_STREAM){
			tv_roomid.setText("streamName");
			ll_password.setVisibility(View.GONE);
			et_roomid.setText("");
			et_token.setText("");
		}
	}

	private boolean invalidate(int level, String id, String token, String bitrate, int delay) {

		if (TextUtils.isEmpty(id)) {
			Toast.makeText(this, "id不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}

		if (level > 0) {
			if (TextUtils.isEmpty(bitrate)) {
				Toast.makeText(this, "码率不能为空", Toast.LENGTH_SHORT).show();
				return false;
			}
			if (delay < 2) {
				Toast.makeText(getApplicationContext(), "延时最低2秒", Toast.LENGTH_LONG).show();
				return false;
			}
		} else {
			if (TextUtils.isEmpty(token)) {
				Toast.makeText(this, "token不能为空", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

	public void onBroadcast(View v) {
		String id = et_roomid.getText().toString();
		String token = et_token.getText().toString();
		String bitrateStr = et_bitrate.getText().toString();
		String delayStr = et_delay.getText().toString();
		try {
			int delay = Integer.parseInt(delayStr);
			int bitrate = Integer.parseInt(bitrateStr);
			bitrate = bitrate*1024;
			if (!invalidate(1, id, token, bitrateStr, delay))
				return;
			param.orientation = LiveParam.Screen_orientation_portrait;
			param.video_bitrate = bitrate;
			param.buffer_time = delay;

			Intent intent = new Intent(MainActivity.this, BroadcastActivity.class);
			intent.putExtra("roomid", id);
			intent.putExtra("token", token);
			intent.putExtra("param", param);
			startActivity(intent);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onBroadcastLand(View v) {
		String id = et_roomid.getText().toString();
		String token = et_token.getText().toString();
		String bitrateStr = et_bitrate.getText().toString();
		String delayStr = et_delay.getText().toString();
		try {
			int delay = Integer.parseInt(delayStr);
			int bitrate = Integer.parseInt(bitrateStr);
			if (!invalidate(1, id, token, bitrateStr, delay))
				return;
			param.orientation = LiveParam.Screen_orientation_landscape;
			param.video_bitrate = bitrate;
			param.buffer_time = delay;
			Intent intent = new Intent(MainActivity.this, BroadcastActivity.class);
			intent.putExtra("roomid", id);
			intent.putExtra("token", token);
			intent.putExtra("param", param);
			startActivity(intent);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onWatch(View v) {
		if (Constants.sdk_type == Constants.TYPE_SELF) {
			getWatchUrl(0);
		} else if (Constants.sdk_type == Constants.TYPE_STREAM) {
			String id = et_roomid.getText().toString();
			if (TextUtils.isEmpty(id))
				return;
			String url = LiveParam.rtmpWatchBaseUrl + id;
			skipToWatch(url,"","");
		}
	}

	public void onWatchHLS(View v) {
		if (Constants.sdk_type == Constants.TYPE_SELF) {
			getWatchUrl(1);
		} else if (Constants.sdk_type == Constants.TYPE_STREAM) {
			String id = et_roomid.getText().toString();
			if (TextUtils.isEmpty(id))
				return;
			getDirectHLSURL(id);
		}
	}

	public void onPlayBack(View v) {

		if (Constants.sdk_type == Constants.TYPE_SELF) {
			getWatchUrl(2);
		} else if (Constants.sdk_type == Constants.TYPE_STREAM) {
			String id = et_roomid.getText().toString();
			if (TextUtils.isEmpty(id))
				return;
			getVideoHLSURL(id);
		}
	}

	private void skipToWatch(String url,String msg_server,String msg_token) {
		Intent intent = new Intent(MainActivity.this, WatchActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("msg_server", msg_server);
		intent.putExtra("msg_token", msg_token);
		intent.putExtra("param", param);
		startActivity(intent);
	}

	private void skipToHLS(String url, String type) {
		Intent intent = new Intent(MainActivity.this, WatchHLSActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("type", type);
		intent.putExtra("param", param);
		startActivity(intent);
	}

	/**
	 * 自动式获取播放地址
	 * @param type
	 */
	private void getWatchUrl(final int type) {
		String id = et_roomid.getText().toString();
		String token = et_token.getText().toString();
		String password = et_password.getText().toString();
		if (!invalidate(0, id, token, null, 0))
			return;
		mProcessDialog.show();
		ZReqEngine.watch(id, Constants.APP_KEY, Constants.APP_SECRET_KEY, "KoreaHank", "hanguoxin1989@sina.com",
				password, new ReqCallback() {

					@Override
					public void OnSuccess(final String data) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mProcessDialog.dismiss();
								tv_param.setText(data);
								Log.e("MainActivity", data);
								dealResult(type, data);
							}
						});
					}

					@Override
					public void OnFail(final String errorMsg) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mProcessDialog.dismiss();
								Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
							}
						});
					}
				});

	}

	private void dealResult(int type, String data) {
		try {
			JSONObject obj = new JSONObject(data);
			String rtmp_video = obj.optString("rtmp_video");
			String video = obj.optString("video");
			int status = obj.optInt("status");// 1 直播2 预约3 结束4 回放
			String msg_server = obj.optString("msg_server");
			String msg_token = obj.optString("msg_token");

			switch (type) {
			case 0:
				if (status != 1) {
					Toast.makeText(getApplicationContext(), Constants.getStatusStr(status), Toast.LENGTH_LONG).show();
					return;
				}
				skipToWatch(rtmp_video,msg_server,msg_token);
				break;
			case 1:
				if (status != 1) {
					Toast.makeText(getApplicationContext(), Constants.getStatusStr(status), Toast.LENGTH_LONG).show();
					return;
				}
				skipToHLS(video, "direct");
				break;
			case 2:
				if (status != 4) {
					Toast.makeText(getApplicationContext(), Constants.getStatusStr(status), Toast.LENGTH_LONG).show();
					return;
				}
				skipToHLS(video, "video");
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 一站式网络直播获取直播HLS地址
	 */
	private void getDirectHLSURL(String id) {
		mProcessDialog.show();
		ZReqEngine.getDirectUrl(id, new ReqCallback() {

			@Override
			public void OnSuccess(final String data) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mProcessDialog.dismiss();
						skipToHLS(data, "direct");
					}
				});
			}

			@Override
			public void OnFail(final String errorMsg) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mProcessDialog.dismiss();
						Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/**
	 * 一站式获取回放地址
	 * @param id
	 */
	private void getVideoHLSURL(String id) {
		mProcessDialog.show();
		ZReqEngine.getVideoUrl(id, new ReqCallback() {

			@Override
			public void OnSuccess(final String data) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mProcessDialog.dismiss();
						skipToHLS(data, "video");
					}
				});
			}

			@Override
			public void OnFail(final String errorMsg) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mProcessDialog.dismiss();
						Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

}
