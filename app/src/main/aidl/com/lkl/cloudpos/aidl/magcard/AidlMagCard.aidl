package com.lkl.cloudpos.aidl.magcard;

import com.lkl.cloudpos.aidl.magcard.MagCardListener;
import com.lkl.cloudpos.aidl.magcard.EncryptMagCardListener;

interface AidlMagCard{
	/** 寻卡*/
	void searchCard(int timeout, MagCardListener listener);
	/** 监听密文磁条刷卡动作*/
	void searchEncryptCard (int timeout, byte keyIndex,byte encryptFlag, in byte[] random,byte pinpadType, EncryptMagCardListener listener);
	/** 停止寻卡*/
	void stopSearch();

}
