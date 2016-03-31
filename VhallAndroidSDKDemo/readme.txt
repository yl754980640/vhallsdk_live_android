重写cameraview使用新APITextureView，重写SDK层LiveParam/NativeLive
重写本地接口，对接新的本地库
重写demo界面，对接新的功能
//添加createVinny/destoryVinnyJNI接口
vinny_android_jni.cpp

//新建一个独立的编译工程，编译底层库及JNI对接层
//完成底层库编译工作与JNI层对接工作，至此，JNI层已完成，开始SDK层编写
//重写demo界面，更新demo功能、添加动态设置缓冲时间等
//测试横竖屏发直播，标清与高清，VLC观看无问题
//动态获取初步宽高，渲染播放
//观看回放4825600bca7f5e6897b6528993d9c3b6d203e6c


//定制SDK，支持视频数据回调
//增加第一帧视频回调
//添加三种视频回调方式
161186852 39a8237420daa067ba99f35069390e3b
e0b151ca0d69664e32cfc009c2338d26 123456

//改动部分2.1.0

1、底层库替换 VinnyLive.so ffmpeg.so

2、观看界面语音回调方法修改
public int notifyAudioData(byte[] data, int size) {
			// Log.e(TAG, "audioData:" + data.length + "size:" + size);
			if (!isWatching)
				return 1;
			if (mAudioPlay != null) {
				mAudioPlay.play(data, size);
			}
//去掉
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

3、替换VhallSDK2.1.0.jar包






