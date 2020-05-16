// AidlFacingbackCamera.aidl
package com.lkl.cloudpos.aidl.camera;
import com.lkl.cloudpos.aidl.camera.AidlCameraScanCodeListener;
interface AidlFacingbackCamera {
    //打开前置摄像头,成功true， 失败false
    boolean open();
    //关闭前置摄像头
    void close();
    //扫码
    void scanCode(AidlCameraScanCodeListener listener);
}
