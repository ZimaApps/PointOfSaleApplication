package com.standard.app.card;

import com.standard.app.Utils;
import com.topwise.cloudpos.aidl.emv.EmvTransData;

public class EmvTransDataSub{
    private static final String TAG = Utils.TAGPUBLIC + EmvTransDataSub.class.getSimpleName();

    private byte mTranstype;//消费0x00 查询0x31 预授权0x03 退货0x20 消费撤销0x20 ...
    private byte mRequestAmtPosition;//请求输入金额位置 0x01:显示卡号前 0x02:显示卡号后
    private byte mEmvFlow;//0x01 – PBOC流程 0x02 – qPBOC流程
    private byte mSlotType;//0x00——接触 0x01——非接
    private byte[] mReserv;//保留供扩展使用 当交易类型是0xF4-卡片圈存日志查询时 Resv[0]值 0x00——逐笔读取 0x01——一次性读取

    private EmvTransData mEmvTransData;

    public EmvTransData getEmvTransData(boolean isIc) {
        mTranstype = 0x00;
        mRequestAmtPosition = 0x01;
        mReserv = new byte[3];

        if (isIc) {
            mEmvFlow = 0x01;
            mSlotType = 0x00;
        } else {
            mEmvFlow = 0x02;
            mSlotType = 0x01;
        }

        mEmvTransData = new EmvTransData(mTranstype, mRequestAmtPosition, false, true, true, mEmvFlow, mSlotType, mReserv);
        return mEmvTransData;
    }
}