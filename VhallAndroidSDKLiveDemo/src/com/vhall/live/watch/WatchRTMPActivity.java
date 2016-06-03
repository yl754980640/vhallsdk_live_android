package com.vhall.live.watch;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioFormat;
import android.net.ConnectivityManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.live.R;
import com.vhall.live.ScreenSizeUtil;
import com.vhall.netbase.constants.ZReqEngine;
import com.vinny.vinnylive.AudioPlay;
import com.vinny.vinnylive.ConnectionChangeReceiver;
import com.vinny.vinnylive.LiveObs;
import com.vinny.vinnylive.LiveObs.LiveCallback;
import com.vinny.vinnylive.LiveParam;
import com.vinny.vinnylive.NativeLive;
import com.vinny.vinnylive.PlayView;

/**
 * 看直播
 * 
 * @author huanan
 * 
 */
public class WatchRTMPActivity extends FragmentActivity implements
        ZReqEngine.FlashMsgListener {

    private PlayView mPlayView;
    private AudioPlay mAudioPlay;
    private boolean isWatching;
    private ProgressDialog mProcessDialog;
    private ConnectionChangeReceiver mConnectionChangeReceiver;
    private final static String TAG = "WatchPortraitActivity";
    private TextView mTextViewDownloadSpeed;
    private RelativeLayout mFrameLayout;
    private TextView orientationbtn, watchBtn, audioShow;
    private ImageView iconBack;

    LiveParam param = null;
    String url;
    String msg_server;
    String msg_token;

    // 文档信息 获取文档拼接Path host + "/" + doc + "/" + page + ".jpg"
    String host = null;// 文档根路径(获取视频信息时返回 )
    int layout = 0;// 界面布局1为单视频，2为单文档(语音 + 文档)，3为文档+视频
    String doc = null;// 文档名称(直播)（ 初始页获取视频信息时返回 更新文档FlashMsg消息中获取 ）
    int page = 0;// 文档当前页码(直播)（初始页码获取视频信息时返回 更新文档FlashMsg消息中获取 ）

    ZReqEngine.Attend attend;// 参会 用于统计参与人数 接收文档 聊天等即时消息
    int videoWidth = 0;
    int videoHeight = 0;

    // show PPT View
    private RelativeLayout rl_bottom_container = null;

    RadioGroup rg_tabs;
    RadioButton rb_detail, rb_doc;
    RelativeLayout rl_detail_container, rl_doc_container;
    BottomDetailFragment detailFragment = null;// 详情
    BottomDocumentFragment documentDetail = null;// 文档
    private FragmentManager mFragmentManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // LightnessControl.setLightnessMax(this);
        setContentView(R.layout.activity_watch_rtmp);
        mFragmentManager = getSupportFragmentManager();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        param = (LiveParam) getIntent().getSerializableExtra("param");
        url = getIntent().getStringExtra("url");
        msg_server = getIntent().getStringExtra("msg_server");
        msg_token = getIntent().getStringExtra("msg_token");

        host = getIntent().getStringExtra("host");// 文档根路径
        layout = getIntent().getIntExtra("layout", 0);// 界面布局1为单视频，2为单文档(语音 +
                                                      // 文档)，3为文档+视频
        doc = getIntent().getStringExtra("doc");// 文档名称(直播)
        page = getIntent().getIntExtra("page", 0);// 当前页

        screenWidth = ScreenSizeUtil.getScreenWidth(this);
        screenHeight = ScreenSizeUtil.getScreenHeight(this);

        initView();

        isWatching = false;
        mAudioPlay = null;
        mConnectionChangeReceiver = null;
        mPlayView = null;
        NativeLive.CreateVinnyLive();
        LiveObs.setCallback(mLiveCallBack);
        NativeLive.EnableDebug(false);
        NativeLive.AddObs();
        if (!TextUtils.isEmpty(msg_server) && !TextUtils.isEmpty(msg_token)) {
            attend = new ZReqEngine().new Attend(msg_server, msg_token);
            attend.setFlashMsgListener(this);
        }
        // NativeLive.Init();

        if (NativeLive.SetParam(param.getParamStr()) < 0) {
            showAlert("直播参数错误");
            finish();
        }

        mProcessDialog = new ProgressDialog(this);
        mProcessDialog.setCancelable(true);
        mProcessDialog.setCanceledOnTouchOutside(false);

    }

    private void initView() {
        mFrameLayout = (RelativeLayout) findViewById(R.id.play_view);
        mTextViewDownloadSpeed = (TextView) findViewById(R.id.textViewDownloadSpeed);

        rl_bottom_container = (RelativeLayout) this
                .findViewById(R.id.doc_container);

        rg_tabs = (RadioGroup) findViewById(R.id.rg_tabs);
        rb_detail = (RadioButton) findViewById(R.id.rb_detail);
        rb_doc = (RadioButton) findViewById(R.id.rb_doc);
        rl_detail_container = (RelativeLayout) findViewById(R.id.rl_detail_container);
        rl_doc_container = (RelativeLayout) findViewById(R.id.rl_doc_container);
        rg_tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                case R.id.rb_detail:
                    rl_detail_container.setVisibility(View.VISIBLE);
                    rl_doc_container.setVisibility(View.GONE);
                    break;
                case R.id.rb_doc:
                    rl_detail_container.setVisibility(View.GONE);
                    rl_doc_container.setVisibility(View.VISIBLE);
                    break;
                }
            }
        });
        initPlayView();
    }

    private void initPlayView() {

        glSurfaceview = new GLSurfaceView(WatchRTMPActivity.this);
        mFrameLayout.removeAllViews();
        mFrameLayout.addView(glSurfaceview);
        initBtn();
        mPlayView = new PlayView(glSurfaceview);

        mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                screenWidth, ScreenSizeUtil.Dp2Px(this, 210)));
        mPlayView.init(screenWidth, ScreenSizeUtil.Dp2Px(this, 210));
    }

    void initBtn() {
        iconBack = new ImageView(this);
        iconBack.setImageDrawable(getResources().getDrawable(
                R.drawable.icon_back));

        orientationbtn = new TextView(this);
        orientationbtn.setText("横竖屏切换");
        orientationbtn.setTextColor(Color.WHITE);
        orientationbtn.setTextSize(ScreenSizeUtil.sp2px(6, this));

        watchBtn = new TextView(this);
        watchBtn.setText("开始观看");
        watchBtn.setTextColor(Color.WHITE);
        watchBtn.setTextSize(ScreenSizeUtil.sp2px(6, this));

        audioShow = new TextView(this);
        audioShow.setText("语音直播");
        audioShow.setTextColor(Color.BLUE);
        audioShow.setTextSize(ScreenSizeUtil.sp2px(6, this));

        RelativeLayout.LayoutParams startLayoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        startLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        startLayoutParams.setMargins(ScreenSizeUtil.Dp2Px(this, 14), 0, 0,
                ScreenSizeUtil.Dp2Px(this, 14));

        RelativeLayout.LayoutParams orientatioLayoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        orientatioLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        orientatioLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        orientatioLayoutParams.setMargins(0, 0, ScreenSizeUtil.Dp2Px(this, 14),
                ScreenSizeUtil.Dp2Px(this, 14));

        RelativeLayout.LayoutParams audioShowLayoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        audioShowLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        RelativeLayout.LayoutParams iconBackLayoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        iconBackLayoutParams.setMargins(ScreenSizeUtil.Dp2Px(this, 14),
                ScreenSizeUtil.Dp2Px(this, 14), 0, 0);

        orientationbtn.setLayoutParams(orientatioLayoutParams);
        audioShow.setLayoutParams(audioShowLayoutParams);
        watchBtn.setLayoutParams(startLayoutParams);
        iconBack.setLayoutParams(iconBackLayoutParams);

        iconBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        watchBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isWatching) {
                    stopWatch();
                } else {
                    startWatch();
                }
            }
        });

        orientationbtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    rl_bottom_container.setVisibility(View.GONE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    rl_bottom_container.setVisibility(View.VISIBLE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        addBtn();
    }

    void addBtn() {
        if (layout == 2) {
            mFrameLayout.addView(audioShow);
        }
        mFrameLayout.addView(orientationbtn);
        mFrameLayout.addView(watchBtn);
        mFrameLayout.addView(iconBack);
    }

    private void initBottom(boolean isHasDoc, String cuePath) {
        // 详情
        detailFragment = BottomDetailFragment.newInstance();
        mFragmentManager.beginTransaction()
                .replace(R.id.rl_detail_container, detailFragment).commit();

        // 文档
        String docPath = null;
        if (host != null && !TextUtils.isEmpty(doc) && !doc.equals("null")) {
            docPath = host + "/" + doc + "/" + page + ".jpg";
        }

        if (cuePath != null) {
            docPath = cuePath;
        }
        documentDetail = BottomDocumentFragment.newInstance(docPath);
        mFragmentManager.beginTransaction()
                .replace(R.id.rl_doc_container, documentDetail).commit();
        rl_bottom_container.setVisibility(View.VISIBLE);
        if (!isHasDoc) {
            rb_doc.setVisibility(View.GONE);
        } else {
            rb_doc.setVisibility(View.VISIBLE);
            rl_doc_container.setVisibility(View.VISIBLE);
        }
    }

    private void startWatch() {
        // url = "rtmp://cnrtmplive01.e.vhall.com/vhall/906807302";
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
                Log.d(TAG, "LiveCallback resultCode: " + resultCode
                        + "  content: " + content);
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
                mProcessDialog.dismiss();
                isWatching = true;
                watchBtn.setText("停止观看");
                if (attend != null) // 参会
                    attend.attend();
                if (detailFragment == null || documentDetail == null)
                    if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        initBottom(layout > 1, null);
                if (audioShow != null && layout == 2) {
                    audioShow.setText("语音直播中");
                }
            }
                break;
            case LiveObs.ERROR_WatchConnect: {
                stopWatch();
                int netState = ConnectionChangeReceiver
                        .ConnectionDetect(WatchRTMPActivity.this);
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
                int netState = ConnectionChangeReceiver
                        .ConnectionDetect(WatchRTMPActivity.this);
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
                if (!WatchRTMPActivity.this.isFinishing())
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
                    Log.e(TAG, "sampleRate:" + sampleRate + "numOfChannels:"
                            + numOfChannels + "bitsPerSameple:" + bitsPerSample);
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

    @SuppressWarnings("unused")
    private void stopWatch() {
        if (isWatching) {
            int num = NativeLive.StopRecv();
            isWatching = false;
            watchBtn.setText("开始观看");
            if (audioShow != null && layout == 2) {
                audioShow.setText("已暂停语音直播");
            }
            mProcessDialog.dismiss();
        }

    }

    private void startAudioPlay(int sampleRate, int channelConfig,
            int audioFormat) {
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
        registerReceiver(mConnectionChangeReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterConnectionChangeReceiver() {
        unregisterReceiver(mConnectionChangeReceiver);
    }

    private void showAlert(String info) {
        if (this.isFinishing())
            return;
        new AlertDialog.Builder(WatchRTMPActivity.this).setTitle("错误")
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
    protected void onStop() {
        super.onStop();
        stopWatch();
        unregisterConnectionChangeReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (attend != null)
            attend.disAttend();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initWH(videoWidth, videoHeight);
    }

    private int screenWidth = 0;
    private int screenHeight = 0;
    private GLSurfaceView glSurfaceview;

    private void initWH(int width, int height) {
        if (width == 0 || height == 0)
            return;
        if (glSurfaceview == null) {
            glSurfaceview = new GLSurfaceView(WatchRTMPActivity.this);
            mFrameLayout.removeAllViews();
            mFrameLayout.addView(glSurfaceview);
            if (watchBtn != null && orientationbtn != null) {
                addBtn();
            } else {
                initBtn();
            }
            mPlayView = new PlayView(glSurfaceview);
        } else if (width != videoWidth || height != videoHeight) {
            mFrameLayout.removeAllViews();
            glSurfaceview = new GLSurfaceView(WatchRTMPActivity.this);
            mFrameLayout.addView(glSurfaceview);
            if (watchBtn != null && orientationbtn != null) {
                addBtn();
            } else {
                initBtn();
            }
            mPlayView = new PlayView(glSurfaceview);
        }
        videoWidth = width;
        videoHeight = height;
        int fixWidth = 0;
        int fixHeight = 0;
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if (videoWidth < videoHeight) {
                fixHeight = screenHeight;
                fixWidth = (screenHeight * videoWidth) / videoHeight;
                mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                        fixWidth, fixHeight));
            } else {
                fixWidth = screenWidth;
                fixHeight = (videoHeight * screenWidth) / videoWidth;
                mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                        fixWidth, fixHeight));
            }
        } else {
            if (videoWidth > videoHeight) {
                fixHeight = screenWidth;
                fixWidth = (screenWidth * videoWidth) / videoHeight;
                mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                        fixWidth, fixHeight));
            } else {
                fixHeight = screenWidth;
                fixWidth = (screenWidth * videoWidth) / videoHeight;
                mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                        fixWidth, fixHeight));
            }
        }
        Log.e(TAG, "screenwidth:" + screenWidth + "screenheight:"
                + screenHeight + "videowidth:" + videoWidth + "videoheight:"
                + videoHeight + "fixwidth:" + fixWidth + "fixheight:"
                + fixHeight);

        mPlayView.init(videoWidth, videoHeight);
    }

    @Override
    public void onFlash(String flashMsg) {
        if (flashMsg != null) {
            JSONObject obj;
            try {
                obj = new JSONObject(flashMsg);
                // int totalPage = obj.optInt("totalPage");// 总页数
                // String type = obj.optString("type");.//直播模式
                // String uid = obj.optString("uid");
                doc = obj.optString("doc");//文档名
                page = obj.optInt("page");// 当前页
                Log.d("flashMsg", "flashMsg: " + flashMsg);
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if (!WatchRTMPActivity.this.isFinishing()) {
                            if (documentDetail != null) {
                                if (TextUtils.isEmpty(doc)
                                        && !doc.equals("null")) {
                                    documentDetail.setNewDoc(null);
                                } else {
                                    documentDetail.setNewDoc(host + "/" + doc
                                            + "/" + page + ".jpg");
                                }
                            } else {
                                initBottom(true, host + "/" + doc + "/" + page
                                        + ".jpg");
                            }
                        }
                    }
                }, NativeLive.GetRealityBufferTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}