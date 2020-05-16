package com.lkl.cloudpos.aidl.magcard;

interface EncryptMagCardListener{
	/** 超时*/
	void onTimeout();
	/** 设备模块错误*/
	void onError(int errorCode);
	/** 被取消*/
	void onCanceled(); 
	/** 刷卡成功，返回加密的刷卡数据 */
	void onSuccess(in String[] trackData);
	/** 刷卡失败，读取磁道失败*/
	void onGetTrackFail();
}
