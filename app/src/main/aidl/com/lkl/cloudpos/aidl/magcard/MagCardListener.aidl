package com.lkl.cloudpos.aidl.magcard;

import com.lkl.cloudpos.aidl.magcard.TrackData;

interface MagCardListener {
	/** 超时*/
	void onTimeout();
	/** 设备模块错误*/
	void onError(int errorCode);
	/** 被取消*/
	void onCanceled(); 
	/** 刷卡成功，实现序列化接口 */
	void onSuccess(in TrackData trackData);
	/** 刷卡失败，读取磁道失败*/
	void onGetTrackFail();
}
