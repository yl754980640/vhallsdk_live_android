package com.example.rtmpdemo;

public class Constants {
	
	
	public static final int TYPE_SELF = 0;//自助式
	public static final int TYPE_STREAM = 1;//流式
	public static int sdk_type = -1;
	public static final String APP_KEY = "";
	public static final String APP_SECRET_KEY = "";
	
	
	//1	直播2	预约3	结束4	回放
	public static String getStatusStr(int type){
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
		
		return "当前直播处在"+statusStr+"状态！";
	}
	

}
