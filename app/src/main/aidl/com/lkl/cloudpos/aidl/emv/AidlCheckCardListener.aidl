package com.lkl.cloudpos.aidl.emv;
import com.lkl.cloudpos.aidl.magcard.TrackData;
interface AidlCheckCardListener{
	void onFindMagCard(in TrackData data);//检测到磁条卡
	void onSwipeCardFail();//刷卡失败
	void onFindICCard();//检测到接触式IC卡
	void onFindRFCard();//检测到RF卡
	void onTimeout();//检测超时
	void onCanceled();//被取消
	void onError(int errCode);//错误
}