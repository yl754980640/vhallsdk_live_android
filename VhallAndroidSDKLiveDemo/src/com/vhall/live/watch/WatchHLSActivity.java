package com.vhall.live.watch;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.live.R;
import com.vhall.live.ScreenSizeUtil;
import com.vhall.netbase.constants.ZReqEngine;
import com.vhall.playersdk.player.TimeRange;
import com.vhall.playersdk.player.chunk.Format;
import com.vhall.playersdk.player.impl.HlsRendererBuilder;
import com.vhall.playersdk.player.impl.VhallHlsPlayer;
import com.vhall.playersdk.player.impl.VhallHlsPlayer.InfoListener;
import com.vhall.playersdk.player.util.Util;

/**
 * 观看回放
 * 
 * @author huanan
 * 
 */
public class WatchHLSActivity extends FragmentActivity implements
        ZReqEngine.FlashMsgListener {

    private static final String TAG = "MediaPlayerDemo";
    private VhallHlsPlayer mMediaPlayer;
    private VhallPlayerListener mVhallPlayerListener;
    private hlsSampleInfoListener mHlsSampleInfoListener;

    String video_url = "";
    String msg_server;
    String msg_token;
    // 文档信息 获取文档拼接Path host + "/" + doc + "/" + page + ".jpg"
    String host = null;// 文档根路径(获取视频信息时返回 )
    int layout = 0;// 界面布局1为单视频，2为单文档(语音 + 文档)，3为文档+视频
    String doc = null;// 文档名称(直播)（ 初始页获取视频信息时返回 更新文档FlashMsg消息中获取 ）
    int page = 0;// 文档当前页码(直播)（初始页码获取视频信息时返回 更新文档FlashMsg消息中获取 ）

    RadioGroup rg_tabs;
    RadioButton rb_detail, rb_doc;
    RelativeLayout rl_detail_container, rl_doc_container;
    BottomDetailFragment detailFragment = null;// 详情
    BottomDocumentFragment documentDetail = null;// 文档
    private FragmentManager mFragmentManager = null;

    SurfaceView surface;
    SurfaceHolder holder;
    ProgressBar pb;
    TextView audioShow;

    ZReqEngine.Attend attend;// 参会 用于统计参与人数 接收文档 聊天等即时消息
   
    //文档的延时计算
    private long hlsBuffer = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_watch_hls);
        mFragmentManager = getSupportFragmentManager();
        initView();
        video_url = getIntent().getStringExtra("url");
        msg_server = getIntent().getStringExtra("msg_server");
        msg_token = getIntent().getStringExtra("msg_token");
        host = getIntent().getStringExtra("host");// 文档根路径
        layout = getIntent().getIntExtra("layout", 0);// 界面布局1为单视频，2为单文档(语音 +
                                                      // 文档)，3为文档+视频
        doc = getIntent().getStringExtra("doc");// 文档名称(直播)
        page = getIntent().getIntExtra("page", 0);// 当前页

        if (!TextUtils.isEmpty(msg_server) && !TextUtils.isEmpty(msg_token)) {
            attend = new ZReqEngine().new Attend(msg_server, msg_token);
            attend.setFlashMsgListener(this);
        }

        isAudio(layout == 2);

        if (!TextUtils.isEmpty(video_url))
            playVideo(video_url);

    }

    private void initView() {
        surface = (SurfaceView) this.findViewById(R.id.surface);
        holder = surface.getHolder();
        pb = (ProgressBar) this.findViewById(R.id.pb);

        audioShow = (TextView) findViewById(R.id.audio_show);

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
    }

    /**
     * 是否为语音直播
     * 
     * @param isAudio
     */
    void isAudio(boolean isAudio) {
        if (isAudio)
            audioShow.setVisibility(View.VISIBLE);
        else {
            audioShow.setVisibility(View.GONE);
        }
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
            if (attend != null) // 参会
                attend.attend();
            // Create a new media player and set the listeners
            String userAgent = Util.getUserAgent(this, "VhallAPP");
            mVhallPlayerListener = new VhallPlayerListener();
            mMediaPlayer = new VhallHlsPlayer(new HlsRendererBuilder(this,
                    userAgent, path));
            mHlsSampleInfoListener = new hlsSampleInfoListener();
            mMediaPlayer.addListener(mVhallPlayerListener);
            mMediaPlayer.setInfoListener(mHlsSampleInfoListener);
            mMediaPlayer.prepare();
            mMediaPlayer.setSurface(holder.getSurface());
            if (!this.isFinishing()) {
                mMediaPlayer.setPlayWhenReady(true);
            } else {
                releaseMediaPlayer();
            }
            initBottom(layout > 1, null);
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
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
        if (!isHasDoc) {
            rb_doc.setVisibility(View.GONE);
        } else {
            rb_doc.setVisibility(View.VISIBLE);
            rl_doc_container.setVisibility(View.VISIBLE);
        }
    }

    private class hlsSampleInfoListener implements InfoListener {

        @Override
        public void onAudioFormatEnabled(Format arg0, int arg1, long arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAvailableRangeChanged(TimeRange arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onBandwidthSample(int arg0, long arg1, long arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDecoderInitialized(String arg0, long arg1, long arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDroppedFrames(int arg0, long arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadCompleted(int arg0, long arg1, int arg2, int arg3,
                Format arg4, long arg5, long arg6, long arg7, long arg8) {

            if(arg5 > 0 && arg6 > 0){
                hlsBuffer = (arg6 - arg5) + (arg6 - mMediaPlayer.getCurrentPosition()) + arg8;
            }
            
        }

        @Override
        public void onLoadStarted(int arg0, long arg1, int arg2, int arg3,
                Format arg4, long arg5, long arg6) {

        }

        @Override
        public void onVideoFormatEnabled(Format arg0, int arg1, long arg2) {
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
        public void onVideoSizeChanged(int width, int height,
                int unappliedRotationDegrees, float pixelWidthHeightRatio) {
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
        if (attend != null)
            attend.disAttend();
        releaseMediaPlayer();
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
        Log.e(TAG, "screenwidth:" + screenWidth + "screenheight:"
                + screenHeight + "videowidth:" + videoWidth + "videoheight:"
                + videoHeight + "fixwidth:" + fixWidth + "fixheight:"
                + fixHeight);
        // surfaceview.setLayoutParams(new RelativeLayout.LayoutParams(fixWidth,
        // fixHeight));
        if (surface != null)
            surface.getHolder().setFixedSize(fixWidth, fixHeight);
    }

    @Override
    public void onFlash(String flashMsg) {
        Log.d("flashMsg", flashMsg);
        if (flashMsg != null) {
            JSONObject obj;
            try {
                obj = new JSONObject(flashMsg);
                // int totalPage = obj.optInt("totalPage");// 总页数
                // String type = obj.optString("type");
                // String uid = obj.optString("uid");
                doc = obj.optString("doc");
                page = obj.optInt("page");// 当前页
                //
                final String cuePath = host + "/" + doc + "/" + page + ".jpg";
                Log.d(TAG, "hlsBuffer: " + hlsBuffer);
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if (!WatchHLSActivity.this.isFinishing())
                            if (documentDetail != null) {
                                if (TextUtils.isEmpty(doc)
                                        && !doc.equals("null")) {
                                    documentDetail.setNewDoc(null);
                                } else {
                                    documentDetail.setNewDoc(cuePath);
                                }
                            } else {
                                initBottom(true, cuePath);
                            }
                    }
                }, hlsBuffer > 0?hlsBuffer:16 * 1000);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
