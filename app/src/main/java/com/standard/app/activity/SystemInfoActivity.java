package com.standard.app.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.standard.app.DeviceTopUsdkServiceManager;
import com.standard.app.R;
import com.standard.app.Utils;
import com.topwise.cloudpos.aidl.system.AidlSystem;

public class SystemInfoActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener{
    private static final String TAG = Utils.TAGPUBLIC + SystemInfoActivity.class.getSimpleName();

    private static final String KEY_INSTALL_APP = "other_info_silent_install_app";
    private static final String KEY_SET_APN = "other_info_set_apn";
    private static final String KEY_UPDATE_SYS_TIME = "other_info_update_system_time";
    private static final String KEY_UPDATE = "other_info_firmware_upgrade";
    private static final String KEY_UPDATE_FIRMWARE = "other_info_firmware_update";
    private static final String KEY_GET_FIRMWARE_STATUE = "other_info_firmware_update_status";
    private static final String KEY_REBOOT = "other_info_reboot";


    private Preference mDeviceInfoVendor;
    private Preference mDeviceInfoModel;
    private Preference mDeviceInfoUniquecode;
    private Preference mDeviceInfoHardwareVer;
    private Preference mDeviceInfoKernelVer;
    private Preference mDeviceRomVer;
    private Preference mDeviceFirmwareVer;
    private Preference mDeviceSecureFirmwareVer;
    private Preference mDeviceKSN;
    private Preference mDeviceIMSI;
    private Preference mDeviceIMEI;

    private Preference mSoftAndroidVer;
    private Preference mSoftLKLIVer;
    private Preference mSoftLKLToAndroidVer;
    private Preference mSoftAppVer;

    private Preference mOthersSavePath;
    private Preference mOthersSimICCID;
    private Preference mOthersSilentInApp;
    private Preference mOthersSetApn;
    private Preference mOthersUpdateSysTime;
    private Preference mOthersFirmUpgrade;
    private Preference mOthersFirmUpdate;
    private Preference mOthersFirmUpdateStatus;
    private Preference mOthersReboot;

    private AidlSystem mSystemManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_version_info);

        mSystemManager = DeviceTopUsdkServiceManager.getInstance().getSystemManager();

        mDeviceInfoVendor = (Preference) findPreference("device_info_vendor");
        mDeviceInfoModel = (Preference) findPreference("device_info_model");
        mDeviceInfoUniquecode = (Preference) findPreference("device_info_uniqueCode");
        mDeviceInfoHardwareVer = (Preference) findPreference("device_info_hardware_ver");
        mDeviceInfoKernelVer = (Preference) findPreference("device_info_kernel_ver");
        mDeviceRomVer = (Preference) findPreference("device_info_rom_ver");
        mDeviceFirmwareVer = (Preference) findPreference("device_info_firmware_ver");
        mDeviceSecureFirmwareVer = (Preference) findPreference("device_info_secure_firmware_ver");
        mDeviceKSN = (Preference) findPreference("device_info_ksn");
        mDeviceIMSI = (Preference) findPreference("device_info_imsi");
        mDeviceIMEI = (Preference) findPreference("device_info_imer");

        mSoftAndroidVer = (Preference) findPreference("software_info_android_version");
        //mSoftLKLIVer = (Preference) findPreference("software_info_lkl_version");
        //mSoftLKLToAndroidVer = (Preference) findPreference("software_info_lkl_to_android_version");
        mSoftAppVer = (Preference) findPreference("software_info_app_version");

        mOthersSavePath = (Preference) findPreference("other_info_save_path");
        mOthersSimICCID = (Preference) findPreference("other_info_sim_iccId");
        mOthersSilentInApp = (Preference) findPreference("other_info_silent_install_app");
        mOthersSilentInApp.setOnPreferenceClickListener(this);
        mOthersSetApn = (Preference) findPreference("other_info_set_apn");
        mOthersSetApn.setOnPreferenceClickListener(this);
        mOthersUpdateSysTime = (Preference) findPreference("other_info_update_system_time");
        mOthersUpdateSysTime.setOnPreferenceClickListener(this);
        mOthersFirmUpgrade = (Preference) findPreference("other_info_firmware_upgrade");
        mOthersFirmUpgrade.setOnPreferenceClickListener(this);
        mOthersFirmUpdate = (Preference) findPreference("other_info_firmware_update");
        mOthersFirmUpdate.setOnPreferenceClickListener(this);
        mOthersFirmUpdateStatus = (Preference) findPreference("other_info_firmware_update_status");
        mOthersFirmUpdateStatus.setOnPreferenceClickListener(this);
        mOthersReboot = (Preference) findPreference("other_info_reboot");
        mOthersReboot.setOnPreferenceClickListener(this);


        try {
            mDeviceInfoVendor.setSummary(mSystemManager.getManufacture());
            mDeviceInfoModel.setSummary(mSystemManager.getModel());
            mDeviceInfoUniquecode.setSummary(mSystemManager.getSerialNo());
            mDeviceInfoHardwareVer.setSummary(mSystemManager.getHardWireVersion());
            mDeviceInfoKernelVer.setSummary(mSystemManager.getAndroidKernelVersion());
            mDeviceRomVer.setSummary(mSystemManager.getRomVersion());
            mDeviceFirmwareVer.setSummary(mSystemManager.getDriverVersion());
            mDeviceSecureFirmwareVer.setSummary(mSystemManager.getSecurityDriverVersion());
            mDeviceKSN.setSummary(mSystemManager.getKsn());
            mDeviceIMSI.setSummary(mSystemManager.getIMSI());
            mDeviceIMEI.setSummary(mSystemManager.getIMEI());

            mSoftAndroidVer.setSummary(mSystemManager.getAndroidOsVersion());
            //mSoftLKLIVer.setSummary(mSystemManager.getCurSdkVersion());
            //mSoftLKLToAndroidVer.setSummary(mSystemManager.getLKLOSSpecsVersion());
            mSoftAppVer.setSummary(getPayAppVer());

            mOthersSavePath.setSummary(mSystemManager.getStoragePath());
            mOthersSimICCID.setSummary(mSystemManager.getICCID());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String getPayAppVer() {
        String verName = null;
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            if (pi != null) {
                verName = pi.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return (verName != null) ? verName : getString(R.string.device_info_default);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        try {
            switch (preference.getKey()) {
                case KEY_INSTALL_APP:
//                    mSystemManager.installApp()
                    break;
                case KEY_SET_APN:
//                    mSystemManager.setAPN()
                    break;
                case KEY_UPDATE_SYS_TIME:
//                    mSystemManager.updateSysTime();
                    break;
                case KEY_UPDATE:
//                    mSystemManager.update();
                    break;
                case KEY_UPDATE_FIRMWARE:
//                    mSystemManager.updateFirmware();
                    break;
                case KEY_GET_FIRMWARE_STATUE:
//                    mSystemManager.getUpdateFirmwareState();
                    break;
                case KEY_REBOOT:
                    mSystemManager.reboot();
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }
}
