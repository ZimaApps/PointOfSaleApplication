package com.lkl.cloudpos.aidl.pinpad;
import com.lkl.cloudpos.aidl.pinpad.GetPinListener;

interface AidlPinpad{
	/** 输入PIN接口 */
	void getPin(in Bundle param, GetPinListener listener);
	
	/** 取消输入PIN */
	void stopGetPin();
	
	/** 确认PIN输入，lcd模拟确认键 */
	void confirmGetPin();
	
	/** 下装主密钥 */
	boolean loadMainkey(int keyID, in byte[] key, in byte[] checkvalue);
	
	/** 下装工作密钥 */
	boolean loadWorkKey(int keyType, int masterKeyId, int wkeyid, in byte[] keyvalue, in byte[] checkvalue);
	
	/** 计算MAC */
	int getMac(in Bundle param,out byte[] mac);
	
	/** TDK加密 */
	int encryptByTdk(int keyindex, byte mode, in byte[] random, in byte[] data, out byte[] encryptdata);
	
	/** 获取随机数 */
	byte[] getRandom();
	
	/** 密码键盘LED显示信息(外置)*/
	boolean display(String line1, String line2);

	/** 下装TEK **/
 boolean loadTEK(int keyID, in byte[] key, in byte[] checkvalue);

 /**下装TEK加密过的TWK**/
 boolean loadTWK(int keyType, int tekkeyid, int wkeyid, in byte[] keyvalue, in byte[] checkvalue);

 /** 下装TEK加密后的主密钥**/
 boolean loadEncryptMainkey(int tekkeyid, int keyID ,in byte[] key, in byte[] checkvalue);

 /** 下装TEK加密后的主密钥 mode为0：密码键盘每个键都是固定的；mode为1：密码键盘每个键是随机的**/
 boolean setPinKeyboardMode(int mode);

  /** 输入PIN接口 */
  	void getOfflinePin(int minlength,int maxlength, String amout,int timeout,GetPinListener listener);
}