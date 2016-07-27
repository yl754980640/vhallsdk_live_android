package com.vhall.live.utils;

import com.vhall.business.WatchRtmp;

/**
 * Created by huanan on 2016/6/30.
 */

public class VhallUtil {

    //1	直播2	预约3	结束4	回放
    public static String getStatusStr(int type) {
        String statusStr = "";
        switch (type) {
            case 1:
                statusStr = "直播";
                break;
            case 2:
                statusStr = "预约";
                break;
            case 3:
                statusStr = "结束";
                break;
            case 4:
                statusStr = "回放";
                break;

            default:
                break;
        }

        return "当前直播处在" + statusStr + "状态！";
    }


    public static String getFixType(int type){
        String typeStr = "";
        switch (type){
            case WatchRtmp.CENTER_INSIDE:
                typeStr = "CENTER_INSIDE";
                break;
            case WatchRtmp.FIT_X:
                typeStr = "FIT_X";
                break;
            case WatchRtmp.FIT_Y:
                typeStr = "FIT_Y";
                break;
            case WatchRtmp.FIT_XY:
                typeStr = "FIT_XY";
                break;
        }
        return typeStr;
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


}
