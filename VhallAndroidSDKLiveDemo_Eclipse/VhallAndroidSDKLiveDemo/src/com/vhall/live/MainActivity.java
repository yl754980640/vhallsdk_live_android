package com.vhall.live;

import org.json.JSONException;
import org.json.JSONObject;

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

import com.vhall.live.broadcast.BroadcastActivity;
import com.vhall.live.watch.WatchHLSActivity;
import com.vhall.live.watch.WatchPlayBackActivity;
import com.vhall.live.watch.WatchRTMPActivity;
import com.vhall.netbase.constants.ZReqEngine;
import com.vhall.netbase.constants.ZReqEngine.ReqCallback;
import com.vinny.vinnylive.LiveParam;

/**
 * 主界面
 * 
 * @author huanan
 * 
 */
public class MainActivity extends Activity {

    EditText et_roomid, et_token, et_bitrate, et_delay, et_password;
    LinearLayout ll_password;
    TextView tv_roomid;
    RadioGroup rg_type;
    LiveParam param;
    private EditText mediaFrameRate;
    
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
        mediaFrameRate = (EditText) this.findViewById(R.id.et_frame_rate);
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
    private void initData() {
        tv_roomid.setText("活动id");
        ll_password.setVisibility(View.VISIBLE);
        et_roomid.setText("");
        et_token.setText("");
    }

    private boolean invalidate(int level, String id, String token,
            String bitrate, int delay ,  String mediaFrameRate) {

        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "id不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (level > 0) {
            if (TextUtils.isEmpty(bitrate)) {
                Toast.makeText(this, "码率不能为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (TextUtils.isEmpty(mediaFrameRate)){
				Toast.makeText(this, "帧率不能为空", Toast.LENGTH_SHORT).show();
				return false;
			}
            
            if (delay < 2) {
                Toast.makeText(getApplicationContext(), "延时最低2秒",
                        Toast.LENGTH_LONG).show();
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
      //帧率
      	String mediaFrameRateStr = mediaFrameRate.getText().toString();
        try {
            int delay = Integer.parseInt(delayStr);
            int bitrate = Integer.parseInt(bitrateStr);
            bitrate = bitrate * 1024;
            if (!invalidate(1, id, token, bitrateStr, delay , mediaFrameRateStr))
                return;
            int framerate = Integer.parseInt(mediaFrameRateStr);
			param.setFrame_rate(framerate);
            param.orientation = LiveParam.Screen_orientation_portrait;
            param.video_bitrate = bitrate;
            param.buffer_time = delay;

            Intent intent = new Intent(MainActivity.this,
                    BroadcastActivity.class);
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
      //帧率
      	String mediaFrameRateStr = mediaFrameRate.getText().toString();
        try {
            int delay = Integer.parseInt(delayStr);
            int bitrate = Integer.parseInt(bitrateStr);
            if (!invalidate(1, id, token, bitrateStr, delay , mediaFrameRateStr))
                return;
            int framerate = Integer.parseInt(mediaFrameRateStr);
			param.setFrame_rate(framerate);
            param.orientation = LiveParam.Screen_orientation_landscape;
            param.video_bitrate = bitrate;
            param.buffer_time = delay;
            Intent intent = new Intent(MainActivity.this,
                    BroadcastActivity.class);
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
        getWatchUrl(0);
    }

    public void onWatchHLS(View v) {
        getWatchUrl(1);
    }

    public void onPlayBack(View v) {
        getWatchUrl(2);
    }

    private void skipToWatchRtmp(String url, String msg_server,
            String msg_token, String host, int layout, String doc, int page) {
        Intent intent = new Intent(MainActivity.this, WatchRTMPActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("msg_server", msg_server);
        intent.putExtra("msg_token", msg_token);
        intent.putExtra("param", param);

        intent.putExtra("host", host);
        intent.putExtra("layout", layout);
        intent.putExtra("doc", doc);
        intent.putExtra("page", page);
        startActivity(intent);
    }

    private void skipToHLS(String url, String msg_server, String msg_token,
            String host, int layout,String doc,int page) {
        Intent intent = new Intent(MainActivity.this, WatchHLSActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("msg_server", msg_server);
        intent.putExtra("msg_token", msg_token);
        intent.putExtra("host", host);
        intent.putExtra("layout", layout);
        intent.putExtra("doc", doc);
        intent.putExtra("page", page);
        startActivity(intent);
    }

    private void skipToWatchPlayBack(String url, String docurl, int layout,
            String host) {
        Intent intent = new Intent(MainActivity.this,
                WatchPlayBackActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("param", param);
        intent.putExtra("docurl", docurl);
        intent.putExtra("layout", layout);
        intent.putExtra("host", host);
        startActivity(intent);
    }

    /**
     * 自动式获取播放地址
     * 
     * @param type
     */
    private void getWatchUrl(final int type) {
        String id = et_roomid.getText().toString();
        String token = et_token.getText().toString();
        String password = et_password.getText().toString();
        if (!invalidate(0, id, token, null, 0, null))
            return;
        mProcessDialog.show();
        ZReqEngine.watch(id, Constants.APP_KEY, Constants.APP_SECRET_KEY,
                "KoreaHank", "hanguoxin1989@sina.com", password,
                new ReqCallback() {

                    @Override
                    public void OnSuccess(final String data) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mProcessDialog.dismiss();
                                tv_param.setText(data);
                                Log.d("watchUrl", data);
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
                                Toast.makeText(getApplicationContext(),
                                        errorMsg, Toast.LENGTH_SHORT).show();
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

            String host = obj.optString("host");// 文档根路径
            int layout = obj.optInt("layout");// 界面布局1为单视频，2为单文档(语音 + 文档)，3为文档+视频
            String doc = obj.optString("doc");// 文档名称(直播)
            int page = obj.optInt("page");// 文档当前页码(直播)
            // String curpoint = obj.optString("curpoint");// 文档事件集合(回放)
            String docurl = obj.optString("docurl");// 文档 （ppt img）地址
            
            switch (type) {
            case 0:
                if (status != 1) {
                    Toast.makeText(getApplicationContext(),
                            Constants.getStatusStr(status), Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                skipToWatchRtmp(rtmp_video, msg_server, msg_token, host,
                        layout, doc, page);
                break;
            case 1:
                if (status != 1) {
                    Toast.makeText(getApplicationContext(),
                            Constants.getStatusStr(status), Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                skipToHLS(video, msg_server, msg_token, host, layout,doc,page);
                break;
            case 2:
                if (status != 4) {
                    Toast.makeText(getApplicationContext(),
                            Constants.getStatusStr(status), Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                skipToWatchPlayBack(video, docurl, layout, host);
                break;
            default:
                break;
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
