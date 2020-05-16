package com.lkl.cloudpos.aidl.rfcard;
//非接卡设备
interface AidlRFCard{
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
	/** 获取卡类型 */
	int getCardType();

	/*
	以下接口只针对M1卡有效
	*/
	/** 认证  */
	int auth(int type, byte blockaddr, in byte[] keydata, in byte[] resetRes);
	
	/** 读数据 */
	int readBlock(byte blockaddr, out byte[] blockdata);
	
	/** 写数据 */
	int writeBlock(byte blockaddr, in byte[] data);
	
	/** 加值 */
	int addValue(byte blockaddr,in byte[] data);
	
	/** 减值 */
	int reduceValue(byte blockaddr, in byte[] data);

	/** 读数据, 成功返回实际字节数组，失败返回null   */
  	byte[] readBlockX(byte blockaddr);

 //获取卡片内码
 byte[] getCardCode();
}
