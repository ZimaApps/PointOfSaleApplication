package com.lkl.cloudpos.aidl.psam;
//PSAM卡设备接口
interface AidlPsam{
   /** 打开设备 */
   boolean open();
   /** 关闭设备 */
   boolean close();
   /** 复位 */
   byte[] reset(int cardType);
   /** 发送APDU指令 */
   byte[] apduComm(in byte[] apdu);
   /** 设置ETU时间 */
   boolean setETU(byte etuVal);
}