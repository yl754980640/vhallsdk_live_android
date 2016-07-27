package com.vhall.live.broadcast;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.live.Constants;
import com.vhall.live.R;
import com.vhall.netbase.constants.ZReqEngine;
import com.vhall.netbase.constants.ZReqEngine.ReqCallback;
import com.vinny.vinnylive.AudioRecordThread;
import com.vinny.vinnylive.CameraNewView;
import com.vinny.vinnylive.CameraNewView.CameraCallback;
import com.vinny.vinnylive.ConnectionChangeReceiver;
import com.vinny.vinnylive.LiveObs;
import com.vinny.vinnylive.LiveObs.LiveCallback;
import com.vinny.vinnylive.LiveParam;
import com.vinny.vinnylive.NativeLive;

/**
 * 发直播界面
 * 
 * @author huanan
 * 
 */
@SuppressLint("NewApi")
public class BroadcastActivity extends Activity {

	private static final String TAG = "BroadcastActivity";
	private boolean isPublishing, isAudioing;
	private ProgressDialog mProcessDialog;
	private Button mPublishBtn, mFlashBtn, mAudioBtn;
	private TextView tv_upload_speed;
	private AudioRecordThread mAudioRecordThread;
	// private GestureDetector mGestureDetector;
	private ConnectionChangeReceiver mConnectionChangeReceiver;
	private CameraNewView mCameraView;

	LiveParam param = null;
	String roomid = null;
	String access_token = null;
	String stream_token = null;
	String url = null;
	ZReqEngine.Attend attend;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		param = (LiveParam) getIntent().getSerializableExtra("param");
		// param.live_publish_type = 2;//0推流，1推流回调，2只回调不推流
		roomid = getIntent().getStringExtra("roomid");
		access_token = getIntent().getStringExtra("token");
		if (param.orientation == LiveParam.Screen_orientation_portrait)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// LightnessControl.setLightnessMax(BroadcastActivity.this);
		setContentView(R.layout.activity_broadcast);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mCameraView = (CameraNewView) this.findViewById(R.id.cameraview);
		mCameraView.init(param, this, new RelativeLayout.LayoutParams(0, 0));
		mCameraView.setmCameraCallback(new CameraCallback() {

			@Override
			public void onFirstFrame(String path) {
				Log.e(TAG, "path------------------>" + path);
			}
		});

		isPublishing = false;
		isAudioing = true;
		mAudioRecordThread = null;
		AudioRecordThread.mIsAudioRecording = true;
		mConnectionChangeReceiver = null;

		NativeLive.CreateVinnyLive();
		LiveObs.setCallback(mLiveCallBack);
		NativeLive.EnableDebug(true);
		NativeLive.AddObs();
		mProcessDialog = new ProgressDialog(BroadcastActivity.this);
		mProcessDialog.setCancelable(true);
		mProcessDialog.setCanceledOnTouchOutside(false);

		mAudioBtn = (Button) findViewById(R.id.audioBtn);
		mFlashBtn = (Button) findViewById(R.id.flashBtn);
		mPublishBtn = (Button) findViewById(R.id.publish_btn);
		tv_upload_speed = (TextView) this.findViewById(R.id.tv_upload_speed);
		mPublishBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isPublishing) {
					stopBroadcast();
				} else {
					// 请求网络
					/* Check 3G or WiFi */
					int netState = ConnectionChangeReceiver
							.ConnectionDetect(BroadcastActivity.this);
					switch (netState) {
					case ConnectionChangeReceiver.NET_ERROR:
						break;
					case ConnectionChangeReceiver.NET_UNKNOWN: {
						param.crf = LiveParam.CRF_WIFI;
						startBrocast();
					}
						break;
					case ConnectionChangeReceiver.NET_2G3G: {
						param.crf = LiveParam.CRF_2G3G;
						startBrocast();
					}
						break;
					case ConnectionChangeReceiver.NET_WIFI: {
						param.crf = LiveParam.CRF_WIFI;
						startBrocast();
					}
						break;
					default:
						break;
					}
				}
			}
		});
	}

	/**
	 * 发起直播
	 */
	private void startPublish() {
		if (param == null)
			return;
		if (NativeLive.SetParam(param.getParamStr()) < 0) {
			showAlert("直播参数错误");
			finish();
		}
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(stream_token)
				|| TextUtils.isEmpty(roomid)) {
			showAlert("参数为空");
			return;
		}
		String publishUrl = url + "?token=" + stream_token + "/" + roomid;
		/** 连接推流*/
		if (NativeLive.StartPublish(publishUrl) < 0) {
			showAlert("开始直播失败");
			finish();
		} else {
			mProcessDialog.show();
			if (attend != null)
				attend.attend();
		}
	}

	/**
	 * 直播过程中的回调
	 */
	private LiveCallback mLiveCallBack = new LiveCallback() {

		@Override
		public void notifyVideoData(byte[] data) {
		}

		@Override
		public int notifyAudioData(byte[] data, int size) {
			return 0;
		}

		@Override
		public void notifyEvent(int resultCode, String content) {
			if (handler != null) {
				try {
					Message message = new Message();
					message.what = resultCode;
					message.obj = content;
					handler.sendMessage(message);
				} catch (Exception e) {

				}
			}
		}

		@Override
		public void onH264Video(byte[] data, int size, int type) {
			Log.e("h264data", "长度--------------->" + data.length
					+ "类型--------------->" + type);
		}
	};

	/**
	 * 直播回调处理
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Log.d("LiveCallBack", "msg.what code:" + msg.what
					+ " -- msg.obj msg:" + msg.obj.toString());
			switch (msg.what) {
			case LiveObs.OK_PublishConnect: {
				mProcessDialog.dismiss();
				isPublishing = true;
				mPublishBtn.setText("停止直播");
				mCameraView.startPublish();
				startAudioCapture();
			}
				break;
			case LiveObs.ERROR_PublishConnect: {
				mProcessDialog.show();
				stopPublish();
				int netState = ConnectionChangeReceiver
						.ConnectionDetect(BroadcastActivity.this);
				switch (netState) {
				case ConnectionChangeReceiver.NET_ERROR:
					showAlert("没有可以使用的网络");
					break;
				default:
					showAlert("服务器连接失败");
					break;
				}
			}
				break;
			case LiveObs.ERROR_Send: {
				mProcessDialog.show();
				stopPublish();
				int netState = ConnectionChangeReceiver
						.ConnectionDetect(BroadcastActivity.this);
				switch (netState) {
				case ConnectionChangeReceiver.NET_ERROR:
					showAlert("没有可以使用的网络");
					break;
				default:
					showAlert("网断了，请重试！");
					break;
				}
			}
				break;
			case LiveObs.INFO_Speed_Upload: {
				String content = (String) msg.obj;
				tv_upload_speed.setText(content + "kbps");
				// Log.e(TAG, "上传速度: " + content + "kbps");
			}
				break;
			case LiveObs.ERROR_Param:
				Log.e(TAG, "ERROR_Param------------------------------------->"
						+ param.getParamStr());
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 切换摄像头
	 * 
	 * @param v
	 */
	public void onChangeCamera(View v) {
		if (Camera.getNumberOfCameras() <= 1) {
			Toast.makeText(getApplicationContext(), "只有一个摄像头!",
					Toast.LENGTH_LONG).show();
			return;
		}
		if (mCameraView != null) {
			mCameraView.changeCamera();
		}
	}

	/**
	 * 切换闪光灯
	 * 
	 * @param v
	 */
	public void onFlash(View v) {
		if (mCameraView != null) {
			boolean ret = mCameraView.changeFlash();
			if (ret) {
				mFlashBtn.setText("关闭闪光灯");
			} else {
				mFlashBtn.setText("开启闪光灯");
			}
		}
	}

	/**
	 * 切换静音
	 * 
	 * @param v
	 */
	public void onAudioSwitch(View v) {
		if (isAudioing) {
			closeAudio();
			isAudioing = false;
			mAudioBtn.setText("已静音");
		} else {
			openAudio();
			isAudioing = true;
			mAudioBtn.setText("静音");
		}
	}

	private void openAudio() {
		AudioRecordThread.openAudio();
	}

	private void closeAudio() {
		AudioRecordThread.closeAudio();
	}

	/**
	 * 停止直播
	 */
	private void stopPublish() {
		if (isPublishing) {
			mProcessDialog.dismiss();
			mCameraView.stopPublish();
			stopAudioCapture();
			NativeLive.StopPublish();
		}
		isPublishing = false;
		mPublishBtn.setText("开始直播");
		if (attend != null)
			attend.disAttend();
	}

	/**
	 * 开启音频录制线程
	 */

	private void startAudioCapture() {
		if (mAudioRecordThread == null) {
			mAudioRecordThread = new AudioRecordThread();
			mAudioRecordThread.init();
			mAudioRecordThread.start();
		}
	}

	/**
	 * 关闭音频录制线程
	 */
	private void stopAudioCapture() {
		if (mAudioRecordThread != null) {
			mAudioRecordThread.stopThread();
			mAudioRecordThread = null;
		}
	}

	private void registerConnectionChangeReceiver() {
		if (mConnectionChangeReceiver == null) {
			mConnectionChangeReceiver = new ConnectionChangeReceiver();
		}
		registerReceiver(mConnectionChangeReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}

	private void unregisterConnectionChangeReceiver() {
		unregisterReceiver(mConnectionChangeReceiver);
	}

	private void showAlert(String info) {
		if (this.isFinishing())
			return;
		new AlertDialog.Builder(BroadcastActivity.this).setTitle("错误")
				.setMessage(info)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
					}
				}).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerConnectionChangeReceiver();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mCameraView.destroyCamera();
		stopPublish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopPublish();
		unregisterConnectionChangeReceiver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 开始直播前获取地址
	 */
	private void startBrocast() {
		if (!mCameraView.mIsPreviewing) {
			Toast.makeText(getApplicationContext(), "预览失败，无法直播！",
					Toast.LENGTH_LONG).show();
			return;
		}
		mProcessDialog.show();
		ZReqEngine.start2(roomid, Constants.APP_KEY, Constants.APP_SECRET_KEY,
				access_token, new ReqCallback() {

					@Override
					public void OnSuccess(final String data) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mProcessDialog.dismiss();
								Log.e(TAG, data);
								try {
									JSONObject obj = new JSONObject(data);
									url = obj.optString("media_srv");
									stream_token = obj.optString("streamtoken");
									String msg_server = obj
											.optString("msg_server");
									String msg_token = obj
											.optString("msg_token");
									attend = new ZReqEngine().new Attend(
											msg_server, msg_token);
									startPublish();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
					}

					@Override
					public void OnFail(final String errorMsg) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mProcessDialog.dismiss();
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_SHORT).show();
							}
						});
					}
				});

	}

	/**
	 * 结束直播请求
	 */
	private void stopBroadcast() {
		mProcessDialog.show();
		ZReqEngine.stop(roomid, Constants.APP_KEY, Constants.APP_SECRET_KEY,
				access_token, new ReqCallback() {

					@Override
					public void OnSuccess(final String data) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mProcessDialog.dismiss();
								stopPublish();
							}
						});
					}

					@Override
					public void OnFail(final String errorMsg) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mProcessDialog.dismiss();
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_SHORT).show();
							}
						});

					}
				});

	}

}