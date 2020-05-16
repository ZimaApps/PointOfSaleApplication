package com.standard.app.settings;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.storage.MerchantInfo;

public class MerchantSettingActivity extends Activity implements View.OnClickListener{
    private static final String TAG = Utils.TAGPUBLIC + MerchantSettingActivity.class.getSimpleName();

    private EditText mEditMerId;
    private EditText mEditMerName;
    private EditText mEditTermId;
    private MerchantInfo mMerchantInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_setting);

        mMerchantInfo = new MerchantInfo(this);

        mEditMerId = (EditText) findViewById(R.id.merchine_id);
        mEditMerId.setText(mMerchantInfo.getMerchantId());
        mEditMerName = (EditText) findViewById(R.id.merchine_name);

        mEditMerName.setText(mMerchantInfo.getMerchantName());
        mEditTermId = (EditText) findViewById(R.id.term_id);
        mEditTermId.setText(mMerchantInfo.getTermId());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.term_para_cancle:
                finish();
                break;
            case R.id.term_para_save:
                String merid = mEditMerId.getText().toString();
                if (merid.length() == 15) {
                    mMerchantInfo.setMerchantId(mEditMerId.getText().toString());
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.tip_mer_id_changed_error), Toast.LENGTH_SHORT).show();
                }

                mMerchantInfo.setMerchantName(mEditMerName.getText().toString());

                String termid = mEditTermId.getText().toString();
                if (termid.length() == 8) {
                    mMerchantInfo.setTermId(mEditTermId.getText().toString());
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.tip_term_id_changed_error), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
