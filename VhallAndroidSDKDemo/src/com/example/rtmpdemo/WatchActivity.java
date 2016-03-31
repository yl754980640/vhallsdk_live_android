package com.example.rtmpdemo;

import org.json.JSONException;
import org.json.JSONObject;

import com.vinny.vinnylive.AudioPlay;
import com.vinny.vinnylive.ConnectionChangeReceiver;
import com.vinny.vinnylive.LightnessControl;
import com.vinny.vinnylive.LiveObs;
import com.vinny.vinnylive.LiveObs.LiveCallback;
import com.vinny.vinnylive.LiveParam;
import com.vinny.vinnylive.NativeLive;
import com.vinny.vinnylive.PlayView;
import com.vinny.vinnylive.ZReqEngine;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.net.ConnectivityManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

/**
 * 看直播
 * 
 * @author huanan
 *
 */
public class WatchActivity extends Activity {

	private PlayView mPlayView;
	private AudioPlay mAudioPlay;
	private boolean isWatching;
	private ProgressDialog mProcessDialog;
	private ConnectionChangeReceiver mConnectionChangeReceiver;
	private final static String TAG = "WatchPortraitActivity";
	private TextView mTextViewDownloadSpeed;
	private FrameLayout mFrameLayout;
	private Button orientation_btn, watch_btn, back_btn;

	LiveParam param = null;
	String url;
	String msg_server;
	String msg_token;
	ZReqEngine.Attend attend;
	
	int videoWidth = 0;
	int videoHeight = 0;
	private int screenWidth = 0;
	private int screenHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// LightnessControl.setLightnessMax(this);
		setContentView(R.layout.activity_watch);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		param = (LiveParam) getIntent().getSerializableExtra("param");
		url = getIntent().getStringExtra("url");
		msg_server = getIntent().getStringExtra("msg_server");
		msg_token = getIntent().getStringExtra("msg_token");
		mFrameLayout = (FrameLayout) findViewById(R.id.play_view);
		mTextViewDownloadSpeed = (TextView) findViewById(R.id.textViewDownloadSpeed);
		orientation_btn = (Button) this.findViewById(R.id.orientation_btn);
		watch_btn = (Button) this.findViewById(R.id.watch_btn);
		back_btn = (Button) this.findViewById(R.id.back_btn);
		isWatching = false;
		mAudioPlay = null;
		mConnectionChangeReceiver = null;
		mPlayView = null;
		NativeLive.CreateVinnyLive();
		LiveObs.setCallback(mLiveCallBack);
		NativeLive.EnableDebug(false);
		NativeLive.AddObs();
		screenWidth = ScreenSizeUtil.getScreenWidth(this);
		screenHeight = ScreenSizeUtil.getScreenHeight(this);
		
		if (!TextUtils.isEmpty(msg_server) && !TextUtils.isEmpty(msg_token))
			attend = new ZReqEngine().new Attend(msg_server, msg_token);
		// NativeLive.Init();

		if (NativeLive.SetParam(param.getParamStr()) < 0) {
			showAlert("直播参数错误");
			finish();
		}

		mProcessDialog = new ProgressDialog(this);
		mProcessDialog.setCancelable(true);
		mProcessDialog.setCanceledOnTouchOutside(false);

		watch_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isWatching) {
					stopWatch();
				} else {
					startWatch();
				}
			}
		});
		back_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		orientation_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		});

		

	}

	private void startWatch() {
//		url = "rtmp://cnrtmplive01.e.vhall.com/vhall/906807302";
		if (NativeLive.StartRecv(url) < 0) {
			// Toast.makeText(MainActivity.this, "StartRecv error",
			// Toast.LENGTH_SHORT).show();
			showAlert("开始观看失败");
			finish();
		} else {
			mProcessDialog.show();
		}
	}

	long firstTimestamp = 0;
	long lastTimestamp = 0;
	long count = 0;
	double perSizeTimeMillis = 0;
	int bitsPerSample = 0;
	int numOfChannels = 0;
	int sampleRate = 0;

	private LiveCallback mLiveCallBack = new LiveCallback() {
		@Override
		public void notifyVideoData(byte[] data) {
			// Log.e(TAG, "videoData:" + data.length);
			if (mPlayView != null && mPlayView.isReady()) {
				mPlayView.UpdateScreenAll(data);
			}
		}

		@Override
		public int notifyAudioData(byte[] data, int size) {
			// Log.e(TAG, "audioData:" + data.length + "size:" + size);
			if (!isWatching)
				return 1;
			if (mAudioPlay != null) {
				mAudioPlay.play(data, size);
			}
			// count++;
			// // if (perSizeTimeMillis == 0)
			// // perSizeTimeMillis = size * 1000 / (numOfChannels *
			// // bitsPerSample / 8 * sampleRate);
			// if (firstTimestamp == 0) {
			// firstTimestamp = System.currentTimeMillis();
			// return 1;
			// } else {
			// long currentTimestamp = System.currentTimeMillis();
			// long time = currentTimestamp - firstTimestamp;
			// // perSizeTimeMillis = (size * 1000 / (numOfChannels *
			// // bitsPerSample / 8 * sampleRate));
			// int frame = (int) ((count * (size * 1000 / (numOfChannels *
			// bitsPerSample / 8 * sampleRate)) - time)
			// / (size * 1000 / (numOfChannels * bitsPerSample / 8 *
			// sampleRate)));
			// Log.e("frame", "frame-------------------" + frame);
			// return (frame>=1)?frame:1;
			// }
			// } else {
			// return 1;
			// }
			return 1;

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

		}

	};

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LiveObs.OK_WatchConnect: {
//				mProcessDialog.dismiss();
				isWatching = true;
				watch_btn.setText("停止观看");
				if (attend != null) // 参会
					attend.attend();
			}
				break;
			case LiveObs.ERROR_WatchConnect: {
				stopWatch();
				int netState = ConnectionChangeReceiver.ConnectionDetect(WatchActivity.this);
				switch (netState) {
				case ConnectionChangeReceiver.NET_ERROR:
					showAlert("没有可以使用的网络");
					break;
				default:
					showAlert("连接服务器失败");
					break;
				}
			}
				break;
			case LiveObs.ERROR_NeedReconnect: {
				stopWatch();
				int netState = ConnectionChangeReceiver.ConnectionDetect(WatchActivity.this);
				switch (netState) {
				case ConnectionChangeReceiver.NET_ERROR:
					showAlert("没有可以使用的网络");
					break;
				default:
					showAlert("对方已经停止直播");
					break;
				}
			}
				break;
			case LiveObs.StartBuffering: {
				Log.e(TAG, "开始缓冲");
				if (!WatchActivity.this.isFinishing())
					mProcessDialog.show();
			}
				break;
			case LiveObs.StopBuffering: {
				Log.e(TAG, "停止缓冲");
				mProcessDialog.dismiss();
			}
				break;
			case LiveObs.INFO_Speed_Download: {
				String content = (String) msg.obj;
				mTextViewDownloadSpeed.setText("速率: " + content + "kbps");
			}
				break;
			case LiveObs.INFO_Decoded_Video: {
				String content = (String) msg.obj;
				Log.e(TAG, "INFO_Decoded_Video------------>" + content);
				try {
					JSONObject obj = new JSONObject(content);
					int width = obj.optInt("width");
					int height = obj.optInt("height");
					initWH(width, height);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
				break;
			case LiveObs.INFO_Decoded_Audio:

				String content = (String) msg.obj;
				Log.e(TAG, "INFO_Decoded_Audio------------>" + content);
				try {
					JSONObject obj = new JSONObject(content);
					bitsPerSample = obj.optInt("bitsPerSample");
					numOfChannels = obj.optInt("numOfChannels");
					sampleRate = obj.optInt("samplesPerSecond");
					if (bitsPerSample == 16) {
						bitsPerSample = AudioFormat.ENCODING_PCM_16BIT;
					} else {
						bitsPerSample = AudioFormat.ENCODING_PCM_8BIT;
					}

					if (numOfChannels == 1) {
						numOfChannels = AudioFormat.CHANNEL_OUT_MONO;
					} else {
						numOfChannels = AudioFormat.CHANNEL_OUT_STEREO;
					}
					Log.e(TAG, "sampleRate:" + sampleRate + "numOfChannels:" + numOfChannels + "bitsPerSameple:"
							+ bitsPerSample);
					startAudioPlay(sampleRate, numOfChannels, bitsPerSample);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void stopWatch() {
		if (isWatching) {
			int num = NativeLive.StopRecv();
			isWatching = false;
			watch_btn.setText("开始观看");
			mProcessDialog.dismiss();
		}
		if (attend != null)
			attend.disAttend();

	}

	private void startAudioPlay(int sampleRate, int channelConfig, int audioFormat) {
		if (mAudioPlay == null) {
			mAudioPlay = new AudioPlay();
			mAudioPlay.init(sampleRate, channelConfig, audioFormat);
		}
	}

	@SuppressWarnings("unused")
	private void stopAudioPlay() {
		if (mAudioPlay != null) {
			mAudioPlay.destroy();
			mAudioPlay = null;
		}
	}

	public void onBackBtn(View v) {
		finish();
	}

	private void registerConnectionChangeReceiver() {
		if (mConnectionChangeReceiver == null) {
			mConnectionChangeReceiver = new ConnectionChangeReceiver();
		}
		registerReceiver(mConnectionChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	private void unregisterConnectionChangeReceiver() {
		unregisterReceiver(mConnectionChangeReceiver);
	}

	private void showAlert(String info) {
		if (this.isFinishing())
			return;
		new AlertDialog.Builder(WatchActivity.this).setTitle("错误").setMessage(info)
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
	protected void onStop() {
		super.onStop();
		stopWatch();
		unregisterConnectionChangeReceiver();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		screenWidth = ScreenSizeUtil.getScreenWidth(this);
		screenHeight = ScreenSizeUtil.getScreenHeight(this);
		initWH(videoWidth, videoHeight);
	}

	
	private GLSurfaceView glSurfaceview;

	private void initWH(int width, int height) {
		if (width == 0 || height == 0)
			return;
		if (glSurfaceview == null) {
			glSurfaceview = new GLSurfaceView(WatchActivity.this);
			mFrameLayout.removeAllViews();
			mFrameLayout.addView(glSurfaceview);
			mPlayView = new PlayView(glSurfaceview);
		} else if (width != videoWidth || height != videoHeight) {
			mFrameLayout.removeAllViews();
			glSurfaceview = new GLSurfaceView(WatchActivity.this);
			mFrameLayout.addView(glSurfaceview);
			mPlayView = new PlayView(glSurfaceview);
		}
		videoWidth = width;
		videoHeight = height;
		int fixWidth = 0;
		int fixHeight = 0;
//		if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//			if (videoWidth < videoHeight) {
//				fixHeight = screenHeight;
//				fixWidth = (screenHeight * videoWidth) / videoHeight;
//				mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(fixWidth, fixHeight));
//			} else {
//				fixWidth = screenWidth;
//				fixHeight = (videoHeight * screenWidth) / videoWidth;
//				mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(fixWidth, fixHeight));
//			}
//		} else {
//			if (videoWidth > videoHeight) {
//				fixHeight = screenWidth;
//				fixWidth = (screenWidth * videoWidth) / videoHeight;
//				mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(fixWidth, fixHeight));
//			} else {
//				fixHeight = screenWidth;
//				fixWidth = (screenWidth * videoWidth) / videoHeight;
//				mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(fixWidth, fixHeight));
//			}
//		}
		
		double videoPix = videoWidth*1.0/videoHeight;
		double screenPix = screenWidth*1.0/screenHeight;
		if(videoPix>screenPix){//视频的宽高比大于屏幕的宽高比，那么适配高
			fixHeight = screenHeight;
			fixWidth = (screenHeight * videoWidth) / videoHeight;
		}else{
			fixWidth = screenWidth;
			fixHeight = (screenWidth*videoHeight)/videoWidth;
		}
		mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(fixWidth, fixHeight));
		Log.e(TAG, "screenwidth:" + screenWidth + "screenheight:" + screenHeight + "videowidth:" + videoWidth
				+ "videoheight:" + videoHeight + "fixwidth:" + fixWidth + "fixheight:" + fixHeight);

		mPlayView.init(videoWidth, videoHeight);
	}
}