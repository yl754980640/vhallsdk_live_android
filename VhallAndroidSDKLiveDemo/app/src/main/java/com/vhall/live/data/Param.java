package com.vhall.live.data;

import com.vhall.business.VhallCameraView;

import java.io.Serializable;



public class Param implements Serializable{

    public static final int HDPI = VhallCameraView.TYPE_HDPI;
    public static final int XHDPI = VhallCameraView.TYPE_XHDPI;

    public String id;
    public String token;
    public int videoBitrate;
    public int frameRate;
    public int bufferSecond;
    public String k;
    public int pixel_type = VhallCameraView.TYPE_HDPI;


    public int screenOri;


}
