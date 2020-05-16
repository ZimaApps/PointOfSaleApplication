package com.lkl.cloudpos.aidl.iccard;

//接触式IC卡
interface AidlICCard{
	/** 打开设备*/
	boolean open();
	/** 关闭设备*/
	boolean close();
	/** 复位卡片*/
	byte[] reset(int cardType);
	/** 卡片是否在位*/
	boolean isExist();
	/** 发送Apdu指令*/
	byte[] apduComm(in byte[] apdu);
	/** 断开*/
	int halt();
}
