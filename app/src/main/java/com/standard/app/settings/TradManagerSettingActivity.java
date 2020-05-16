package com.standard.app.settings;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.storage.TradManagerInfo;

public class TradManagerSettingActivity extends Activity implements View.OnClickListener{
    private static final String TAG = Utils.TAGPUBLIC + TradManagerSettingActivity.class.getSimpleName();

    private Switch mSWIsNoPin;
    private Switch mSWIsNoSign;
    private EditText mNoPinAmount;

    private TradManagerInfo mTradManagerInfo;
    private String mNoPinAmountMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trad_manager);

        mTradManagerInfo = new TradManagerInfo(this);
        mNoPinAmountMax = mTradManagerInfo.getNoPinAmt();

        mSWIsNoPin = (Switch) findViewById(R.id.switch_no_pin);
        mSWIsNoSign = (Switch) findViewById(R.id.switch_no_sign);
        mNoPinAmount = (EditText) findViewById(R.id.max_amount_no_pin);

        mSWIsNoPin.setChecked(mTradManagerInfo.getIsRfNoPin());
        mSWIsNoPin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNoPinAmount.setEnabled(isChecked);
            }
        });

        mSWIsNoSign.setChecked(mTradManagerInfo.getIsNoSign());

        mNoPinAmount.setEnabled(mTradManagerInfo.getIsRfNoPin());
        mNoPinAmount.setText(mNoPinAmountMax);
        mNoPinAmount.setSelection(mNoPinAmountMax.length());

        mNoPinAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String amount = s.toString();
                if (amount != null) {
                    if (!isPointRight(amount)) {
                        String fixAmount = getFixedAmount(amount);
                        mNoPinAmount.setText(fixAmount);
                        mNoPinAmount.setSelection(fixAmount.length());
                    }
                }
            }
        });
    }

    private boolean isPointRight(String tempStr) {
        if (tempStr.length() > 3) {
            String subStr = tempStr.substring(tempStr.length() - 3, tempStr.length() - 2);
            Log.i(TAG, "subStr = " + subStr);
            if (subStr.equals(".")) {
                return true;
            }
        }
        return false;
    }

    private String getFixedAmount(String amount) {
        String amountStr = amount.replace(".", "");
        Log.i(TAG, "amountStr = " + amountStr);

        if (amountStr.length() > 3) {
            String subStr3 = amountStr.substring(0, 3);
            String subStr2 = amountStr.substring(0, 2);
            String subStr1 = amountStr.substring(0, 1);
            if (subStr3.equals("000")) {
                amountStr = amountStr.substring(3, amountStr.length());
            } else if (subStr2.equals("00")) {
                amountStr = amountStr.substring(2, amountStr.length());
            } else if (subStr1.equals("0")) {
                amountStr = amountStr.substring(1, amountStr.length());
            }
        }

        String temp = amountStr;
        for (int i = 0 ; i < 3 - amountStr.length(); i++) {
            temp = "0" + temp;
        }

        StringBuilder amountBuilder = new StringBuilder(temp);
        amountBuilder.insert(amountBuilder.length()-2, ".");
        Log.i(TAG, "amountBuilder = " + amountBuilder);

        return amountBuilder.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trad_para_cancle:
                finish();
                break;
            case R.id.trad_para_save:
                mTradManagerInfo.setIsRfNoPin(mSWIsNoPin.isChecked());
                mTradManagerInfo.setIsNoSign(mSWIsNoSign.isChecked());
                mTradManagerInfo.setNoPinAmt(mNoPinAmount.getText().toString());
                finish();
                break;
            default:
                break;
        }
    }
}
