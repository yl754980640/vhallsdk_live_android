package com.example.rtmpdemo;

import java.util.Timer;
import java.util.TimerTask;
import com.vhall.playersdk.player.impl.HlsRendererBuilder;
import com.vhall.playersdk.player.impl.VhallHlsPlayer;
import com.vhall.playersdk.player.util.Util;
import com.vinny.vinnylive.ZReqEngine;
import com.vinny.vinnylive.ZReqEngine.ReqCallback;
import com.vinny.vinnylive.LiveParam;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 观看回放
 * 
 * @author huanan
 *
 */
public class WatchHLSActivity extends Activity {

	private static final String TAG = "MediaPlayerDemo";
	private VhallHlsPlayer mMediaPlayer;
	private VhallPlayerListener mVhallPlayerListener;
	private long playerCurrentPosition = 0L; // 度播放的当前标志，毫秒
	private long playerDuration;// 播放资源的时长，毫秒
	private String playerDurationTimeStr = "00:00:00";

	String type = "";
	String video_url = "";
	LiveParam param;

	SurfaceView surface;
	SurfaceHolder holder;
	LinearLayout ll_actions;
	ImageView iv_play;
	SeekBar seekbar;
	TextView tv_pos;
	ProgressBar pb;

	Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.play_back);
		surface = (SurfaceView) this.findViewById(R.id.surface);
		holder = surface.getHolder();
		ll_actions = (LinearLayout) this.findViewById(R.id.ll_actions);
		iv_play = (ImageView) this.findViewById(R.id.iv_play);
		seekbar = (SeekBar) this.findViewById(R.id.seekbar);
		tv_pos = (TextView) this.findViewById(R.id.tv_pos);
		pb = (ProgressBar) this.findViewById(R.id.pb);
		type = getIntent().getStringExtra("type");
		video_url = getIntent().getStringExtra("url");
		param = (LiveParam) getIntent().getSerializableExtra("param");
		iv_play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(video_url))
					return;
				if (mMediaPlayer == null) {
					playVideo(video_url);
					iv_play.setImageResource(R.drawable.icon_play_pause);
				} else {
					if (mMediaPlayer.isPlaying()) {
						// mMediaPlayer.pause();
						mMediaPlayer.setPlayWhenReady(false);
						iv_play.setImageResource(R.drawable.icon_play_play);
					} else {
						// mMediaPlayer.start();
						mMediaPlayer.setPlayWhenReady(true);
						iv_play.setImageResource(R.drawable.icon_play_pause);
					}

				}
			}
		});

		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				playerCurrentPosition = seekBar.getProgress();
				if (mMediaPlayer != null) {
					mMediaPlayer.seekTo(playerCurrentPosition);
					mMediaPlayer.start();
				} else {
					// TODO
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tv_pos.setText(converLongTimeToStr(progress) + "/" + playerDurationTimeStr);
			}
		});

		if (type.equals("direct")) {
			iv_play.performClick();
		} else if (type.equals("video")) {
			ll_actions.setVisibility(View.VISIBLE);
		}

		// getReq();

	}
	private void initTimer() {
		if (timer != null)
			return;
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {

				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
							playerCurrentPosition = mMediaPlayer.getCurrentPosition();
							seekbar.setProgress((int) playerCurrentPosition);
							String playerCurrentPositionStr = converLongTimeToStr(playerCurrentPosition);
							tv_pos.setText(playerCurrentPositionStr + "/" + playerDurationTimeStr);
						}
					}
				});

			}
		}, 1000, 1000);
	}

	/**
	 * 创建播放器,并播放
	 * 
	 * @param path
	 */
	private void playVideo(String path) {
		try {
			if (path == "") {
				return;
			}
			// Create a new media player and set the listeners
			String userAgent = Util.getUserAgent(this, "VhallAPP");
			mVhallPlayerListener = new VhallPlayerListener();
			mMediaPlayer = new VhallHlsPlayer(new HlsRendererBuilder(this, userAgent, path));
			mMediaPlayer.addListener(mVhallPlayerListener);
			mMediaPlayer.seekTo(playerCurrentPosition);
			mMediaPlayer.prepare();
			mMediaPlayer.setSurface(holder.getSurface());
			if (!this.isFinishing()) {
				mMediaPlayer.setPlayWhenReady(true);
			} else {
				releaseMediaPlayer();
			}

		} catch (Exception e) {
			Log.e(TAG, "error: " + e.getMessage(), e);
		}
	}

	/**
	 * 自定义播放器监听事件处理
	 */

	private class VhallPlayerListener implements VhallHlsPlayer.Listener {
		@Override
		public void onStateChanged(boolean playWhenReady, int playbackState) {
			switch (playbackState) {
			case VhallHlsPlayer.STATE_IDLE:
				Log.e(TAG, "--------------------->STATE_IDLE");
				break;
			case VhallHlsPlayer.STATE_PREPARING:
				pb.setVisibility(View.VISIBLE);
				Log.e(TAG, "--------------------->STATE_PREPARING");
				break;
			case VhallHlsPlayer.STATE_BUFFERING:
				pb.setVisibility(View.VISIBLE);
				Log.e(TAG, "--------------------->STATE_BUFFERING");
				break;
			case VhallHlsPlayer.STATE_READY:
				Log.e(TAG, "--------------------->STATE_READY");
				pb.setVisibility(View.GONE);
				if (type.equals("video")) {
					playerDuration = mMediaPlayer.getDuration();
					playerDurationTimeStr = converLongTimeToStr(playerDuration);
					seekbar.setMax((int) playerDuration);
					initTimer();
				}
				break;
			case VhallHlsPlayer.STATE_ENDED:
				Log.e(TAG, "--------------------->STATE_ENDED");
				pb.setVisibility(View.GONE);
				releaseMediaPlayer();
				break;
			default:
				break;
			}

		}

		@Override
		public void onError(Exception e) {
			releaseMediaPlayer();
		}

		@Override
		public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
				float pixelWidthHeightRatio) {
			if (width == 0 || height == 0) {
				return;
			}
			Log.e(TAG, "width:" + width + "---" + "height:" + height);
			videoWidth = width;
			videoHeight = height;
			setSurfaceFixSize();
		}
	}

	/**
	 * 释放播放器
	 */
	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
			playerCurrentPosition = 0;
			iv_play.setImageResource(R.drawable.icon_play_play);
		}
	}

	/**
	 * 将长整型值转化成字符串
	 * 
	 * @param time
	 * @return
	 */
	public static String converLongTimeToStr(long time) {
		int ss = 1000;
		int mi = ss * 60;
		int hh = mi * 60;

		long hour = (time) / hh;
		long minute = (time - hour * hh) / mi;
		long second = (time - hour * hh - minute * mi) / ss;

		String strHour = hour < 10 ? "0" + hour : "" + hour;
		String strMinute = minute < 10 ? "0" + minute : "" + minute;
		String strSecond = second < 10 ? "0" + second : "" + second;
		if (hour > 0) {
			return strHour + ":" + strMinute + ":" + strSecond;
		} else {
			return "00:" + strMinute + ":" + strSecond;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		releaseMediaPlayer();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private int screenWidth = 0;
	private int screenHeight = 0;
	private int videoWidth = 0;
	private int videoHeight = 0;

	public void setSurfaceFixSize() {
		if (videoWidth == 0 || videoHeight == 0)
			return;
		int fixWidth = 0;
		int fixHeight = 0;
		fixHeight = ScreenSizeUtil.Dp2Px(this, 200);
		fixWidth = videoWidth * fixHeight / videoHeight;
		if (fixWidth <= 0 || fixHeight <= 0)
			return;
		Log.e(TAG, "screenwidth:" + screenWidth + "screenheight:" + screenHeight + "videowidth:" + videoWidth
				+ "videoheight:" + videoHeight + "fixwidth:" + fixWidth + "fixheight:" + fixHeight);
		// surfaceview.setLayoutParams(new RelativeLayout.LayoutParams(fixWidth,
		// fixHeight));
		if (surface != null)
			surface.getHolder().setFixedSize(fixWidth, fixHeight);
	}
	
}
