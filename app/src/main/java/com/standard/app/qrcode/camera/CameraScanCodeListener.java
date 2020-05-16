package com.standard.app.qrcode.camera;

/**
 * @author wangfubao.
 * @date 18-5-2.
 */
public interface CameraScanCodeListener {
    void onSuccess(String code);
    void onFail(int errorCode);
}
