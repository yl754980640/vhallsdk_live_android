package com.vhall.live.watch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vhall.live.R;
import com.vhall.live.ScreenSizeUtil;
import com.vhall.netbase.entity.DocumentDetail;
import com.vhall.netbase.entity.DocumentInfo;
import com.vhall.netbase.entity.MessageInfo;
import com.vhall.playersdk.player.impl.HlsRendererBuilder;
import com.vhall.playersdk.player.impl.VhallHlsPlayer;
import com.vhall.playersdk.player.util.Util;
import com.vinny.vinnylive.LiveParam;

/**
 * 观看回放
 * 
 * @author huanan
 * 
 */
public class WatchPlayBackActivity extends FragmentActivity {

    private final String PPTPATH = "vhall" + File.separator + "docCache";
    private static final String TAG = "MediaPlayerDemo";
    private VhallHlsPlayer mMediaPlayer;
    private VhallPlayerListener mVhallPlayerListener;
    private long playerCurrentPosition = 0L; // 度播放的当前标志，毫秒
    private long playerDuration;// 播放资源的时长，毫秒
    private String playerDurationTimeStr = "00:00:00";

    String video_url = "";
    String docurl = "";// 文档下载地址
    int layout;// 界面布局1为单视频，2为但文档(语音 + 文档)，3为文档+视频
    String host = null;// 文档根路径
    LiveParam param;

    SurfaceView surface;
    SurfaceHolder holder;
    LinearLayout ll_actions;
    ImageView iv_play;
    SeekBar seekbar;
    TextView tv_pos;
    ProgressBar pb;
    TextView audioShow;

    Timer timer;

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
        setContentView(R.layout.activity_play_back);
        mFragmentManager = getSupportFragmentManager();
        video_url = getIntent().getStringExtra("url");
        docurl = getIntent().getStringExtra("docurl");
        layout = getIntent().getIntExtra("layout", 0);
        host = getIntent().getStringExtra("host");// 文档根路径
        param = (LiveParam) getIntent().getSerializableExtra("param");

        initView();
        isAudio(layout == 2);
        initBottom(layout > 1, null);
    }

    private void initView() {
        surface = (SurfaceView) this.findViewById(R.id.surface);
        holder = surface.getHolder();
        ll_actions = (LinearLayout) this.findViewById(R.id.ll_actions);
        iv_play = (ImageView) this.findViewById(R.id.iv_play);
        seekbar = (SeekBar) this.findViewById(R.id.seekbar);
        tv_pos = (TextView) this.findViewById(R.id.tv_pos);
        pb = (ProgressBar) this.findViewById(R.id.pb);
        audioShow = (TextView) findViewById(R.id.audio_show);

        iv_play.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(video_url))
                    return;
                if (mMediaPlayer == null) {
                    playVideo(video_url);
                    iv_play.setImageResource(R.drawable.icon_play_pause);
                    if (audioShow != null && layout == 2) {
                        audioShow.setText("语音回放中 ");
                    }
                } else {
                    if (mMediaPlayer.isPlaying()) {
                        // mMediaPlayer.pause();
                        mMediaPlayer.setPlayWhenReady(false);
                        iv_play.setImageResource(R.drawable.icon_play_play);
                        if (audioShow != null && layout == 2) {
                            audioShow.setText("已暂停语音回放");
                        }
                    } else {
                        // mMediaPlayer.start();
                        mMediaPlayer.setPlayWhenReady(true);
                        iv_play.setImageResource(R.drawable.icon_play_pause);
                        if (audioShow != null && layout == 2) {
                            audioShow.setText("语音回放中");
                        }
                    }

                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    playerCurrentPosition = seekBar.getProgress();
                    seekChangePage(playerCurrentPosition / 1000);
                    mMediaPlayer.seekTo(playerCurrentPosition);
                    mMediaPlayer.start();
                    iv_play.setImageResource(R.drawable.icon_play_pause);
                    if (audioShow != null && layout == 2) {
                        audioShow.setText("语音回放中");
                    }
                } else {
                    seekBar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                tv_pos.setText(converLongTimeToStr(progress) + "/"
                        + playerDurationTimeStr);
            }
        });

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
     * 是否为语音回放
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

    private void initBottom(boolean isHasDoc, String docPath) {
        // 详情
        detailFragment = BottomDetailFragment.newInstance();
        mFragmentManager.beginTransaction()
                .replace(R.id.rl_detail_container, detailFragment).commit();

        // 文档
        documentDetail = BottomDocumentFragment.newInstance(docPath);
        mFragmentManager.beginTransaction()
                .replace(R.id.rl_doc_container, documentDetail).commit();
        if (!isHasDoc) {
            rb_doc.setVisibility(View.GONE);
        } else {
            dealVideoDoc();
            rb_doc.setVisibility(View.VISIBLE);
            rl_doc_container.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1:
                DocumentDetail detail = (DocumentDetail) msg.obj;
                changePPT(detail);
                break;
            }
        }
    };
    private String imgUrlFormat = "%s/%s/%d.jpg";

    public void changePPT(DocumentDetail detail) {
        try {
            if (detail != null && !TextUtils.isEmpty(detail.doc)
                    && !TextUtils.isEmpty(host)) {
                String imgurl;
                if (detail.doc.length() > 1 && !detail.doc.equals("null")) {
                    imgurl = String.format(imgUrlFormat, host, detail.doc,
                            detail.page);
                } else {
                    imgurl = null;
                }
                if (documentDetail != null) {
                    documentDetail.setNewDoc(imgurl);
                } else {
                    initBottom(true, imgurl);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录播情况下载解析文档
     */
    private void dealVideoDoc() {
        String fileName = docurl.substring(docurl.lastIndexOf("/"));
        String footDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        String cachPath = footDir + File.separator + PPTPATH;
        File cachFile = new File(cachPath);
        cachFile.mkdirs();
        String localDocPath = cachFile.getAbsolutePath() + fileName;
        File file = new File(localDocPath);
        if (file.exists()) {
            file.delete();
        }
        downloadTimeFile(file, docurl);
    }

    private void downloadTimeFile(final File localFile,
            final String romateFilePath) {
        new Thread() {
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(romateFilePath);
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    StatusLine statusLine = httpResponse.getStatusLine();
                    if (statusLine.getStatusCode() == 200) {
                        FileOutputStream outputStream = new FileOutputStream(
                                localFile);
                        InputStream inputStream = httpResponse.getEntity()
                                .getContent();
                        byte b[] = new byte[1024];
                        int j = 0;
                        while ((j = inputStream.read(b)) != -1) {
                            outputStream.write(b, 0, j);
                        }
                        outputStream.flush();
                        outputStream.close();
                        readFile(localFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }
            }
        }.start();
    }

    public void readFile(String filePath) {
        try {
            // String encoding = "utf-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = fis.read(buffer)) != -1) {// 4
                    outStream.write(buffer, 0, length);
                }
                byte[] b = outStream.toByteArray();

                fis.close();
                outStream.flush();
                outStream.close();
                String s = new String(b);

                parserFile(s);
                b = null;
                s = null;
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }

    List<MessageInfo> documents = new ArrayList<MessageInfo>();// 录播情况下翻页信息集合

    public void parserFile(String fileJson) {
        if (fileJson != null && fileJson.startsWith("\ufeff")) {
            fileJson = fileJson.substring(1);
        }
        DocumentInfo info = null;
        try {
            info = new Gson().fromJson(fileJson, DocumentInfo.class);
            if (info == null) {
                return;
            }
            List<MessageInfo> eventinfo = info.cuepoint;
            if (eventinfo != null && eventinfo.size() > 0) {
                for (int i = 0; i < eventinfo.size(); i++) {
                    if (eventinfo.get(i).event.equals("flashMsg")) {
                        documents.add(eventinfo.get(i));
                        // Log.d("watch data", "doc String :  " +
                        // eventinfo.get(i).content);
                    }
                }
            }
            // 初始化展示第一张，如果当前已经播放到第二张则取消操作
            initPPT();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPPT() {
        if (currentPos == 0) {
            if (documents == null || documents.size() <= currentPos + 1)
                return;
            String content = documents.get(0).content;
            try {
                detail = new Gson().fromJson(content, DocumentDetail.class);
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = detail;
                handler.sendMessageDelayed(msg, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 回放当前位置，判断是否需要翻页
     * 
     * @param timestamp
     */
    int currentPos = 0;
    DocumentDetail detail = null;

    public void dealChangePage(long timestamp) {
        if (documents == null || documents.size() <= currentPos + 1)
            return;
        long nextTime = (long) documents.get(currentPos + 1).created_at;
        String content;
        while (timestamp >= nextTime) {
            currentPos++;
            if (documents.size() > currentPos + 1) {
                nextTime = (long) documents.get(currentPos + 1).created_at;
            } else {
                break;
            }
        }
        if (timestamp == (long) documents.get(currentPos).created_at) {
            content = documents.get(currentPos).content;
            try {
                detail = new Gson().fromJson(content, DocumentDetail.class);
                changePPT(detail);
            } catch (Exception e) {
                e.printStackTrace();
                if (documentDetail != null) {
                    documentDetail.setNewDoc(null);
                } else {
                    initBottom(true, null);
                }
            }
        }
    }

    /**
     * seek操作重新计算pos
     */
    public void seekChangePage(long timestamp) {
        Log.e(TAG, "timestamp----------->" + timestamp);
        if (documents == null || documents.size() <= 0)
            return;
        int pos = documents.size() - 1;
        for (int i = 0; i < documents.size(); i++) {
            long time = (long) documents.get(i).created_at;
            if (time > timestamp) {
                pos = i;
                break;
            }
        }
        currentPos = pos - 1;// 小于0说明初始化没有PPT，默认显示第一张
        if (currentPos < 0)
            currentPos = 0;
        String content = documents.get(currentPos).content;
        try {
            detail = new Gson().fromJson(content, DocumentDetail.class);
            changePPT(detail);
        } catch (Exception e) {
            if (documentDetail != null) {
                documentDetail.setNewDoc(null);
            } else {
                initBottom(true, null);
            }
            e.printStackTrace();
        }
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
                            playerCurrentPosition = mMediaPlayer
                                    .getCurrentPosition();
                            seekbar.setProgress((int) playerCurrentPosition);
                            dealChangePage(playerCurrentPosition / 1000);
                            String playerCurrentPositionStr = converLongTimeToStr(playerCurrentPosition);
                            tv_pos.setText(playerCurrentPositionStr + "/"
                                    + playerDurationTimeStr);
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
            mMediaPlayer = new VhallHlsPlayer(new HlsRendererBuilder(this,
                    userAgent, path));
            mMediaPlayer.addListener(mVhallPlayerListener);
            mMediaPlayer.seekTo(playerCurrentPosition);
            if (playerCurrentPosition == 0) {
                if (documentDetail != null) {
                    documentDetail.setNewDoc(null);
                } else {
                    initBottom(true, null);
                }
            } else {
                dealChangePage(playerCurrentPosition / 1000);
            }
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
                playerDuration = mMediaPlayer.getDuration();
                playerDurationTimeStr = converLongTimeToStr(playerDuration);
                seekbar.setMax((int) playerDuration);
                initTimer();
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
            currentPos = 0;
            playerCurrentPosition = 0;
            iv_play.setImageResource(R.drawable.icon_play_play);
            if (audioShow != null && layout == 2) {
                audioShow.setText("已暂停语音回放");
            }
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
        Log.e(TAG, "screenwidth:" + screenWidth + "screenheight:"
                + screenHeight + "videowidth:" + videoWidth + "videoheight:"
                + videoHeight + "fixwidth:" + fixWidth + "fixheight:"
                + fixHeight);
        // surfaceview.setLayoutParams(new RelativeLayout.LayoutParams(fixWidth,
        // fixHeight));
        if (surface != null)
            surface.getHolder().setFixedSize(fixWidth, fixHeight);
    }

}
