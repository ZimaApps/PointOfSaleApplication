// AidlPedestal.aidl
package com.lkl.cloudpos.aidl.pedestal;

// Declare any non-default types here with import statements

interface AidlPedestal {
    //判断底座与POS机是否连接
    boolean isCoupled();


    /** 获取底座串口管理操作实例*/
    	IBinder getSerialManager();
}
