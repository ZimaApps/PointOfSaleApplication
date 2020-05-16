// AidlCameraScanCodeListener.aidl
package com.lkl.cloudpos.aidl.camera;

interface AidlCameraScanCodeListener {
    // 解码失败,错误码:1硬件故障  2解码失败
    void onFail(int errorCode);
    // 解码成功
    void onSuccess(String code);
}
