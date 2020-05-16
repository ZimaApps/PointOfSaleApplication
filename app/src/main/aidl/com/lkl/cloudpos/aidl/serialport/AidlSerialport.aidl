package com.lkl.cloudpos.aidl.serialport;
//串口设备
interface AidlSerialport{
	//打开串口
	boolean open();
	//初始化
	boolean init(int botratebyte,byte dataBits,byte parity,byte StopBits);
	//发送数据
	boolean sendData(in byte[] data,int timeout);
	//读取数据
	byte[] readData(int timeout );
	//关闭
	boolean close();
}
