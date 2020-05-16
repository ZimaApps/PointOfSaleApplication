package com.lkl.cloudpos.aidl.system;
import com.lkl.cloudpos.aidl.system.InstallAppObserver;
//系统设备
interface AidlSystem{
	//获取终端序列号
	String getSerialNo();
	//安装APP
	void installApp(String filePath,InstallAppObserver observer);
	//读取KSN号
	String getKsn();
	//获取驱动版本信息
	String getDriverVersion();
	//获取当前接口版本信息
	String getCurSdkVersion();
	//更新系统时间
	boolean updateSysTime(String dateStr);
	//增加存储路径
	String getStoragePath();
	//更新OS或驱动包，此接口在本规范2.0.0已废除
	void update(int updateType);
	String getIMSI();
	String getIMEI();
	String getHardWireVersion();
	String getSecurityDriverVersion();
	String getManufacture();
	String getModel();
	String getAndroidOsVersion();
	String getRomVersion();
	String getAndroidKernelVersion();
	void reboot();
	String getICCID();
	//获取拉卡拉对安卓系统的定制规范版本
 String getLKLOSSpecsVersion();
 //更新固件
 String updateFirmware(int updateType, String packageName);
 //查询固件更新状态
 int getUpdateFirmwareState(String updateId);
 //设置APN
 boolean setAPN(String name, String apn, String userName, String password);
}