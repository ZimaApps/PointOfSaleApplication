// AidlPedestalSerialManager.aidl
package com.lkl.cloudpos.aidl.pedestal;

// Declare any non-default types here with import statements

interface AidlPedestalSerialManager {
    //统计底座串口数目
    int getCount();

				//枚举底座串口，返回串口设备名数组
				List<String> enumSerialports();

    /** 获取串口操作AidlSerialport接口实例 */
    	IBinder getSerialport(String portName);
}
