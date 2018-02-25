package com.bamboo.gble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

/**
 * Created by weiwu on 2017/12/7.
 */

public abstract class FindDeviceCallback implements BluetoothAdapter.LeScanCallback {
    private String mMac;
    private int mTimeout;
    private Handler mHandler = new Handler(Looper.myLooper());

    public FindDeviceCallback(String mac,int timeout){
        mMac = mac;
        mTimeout = timeout;
        mHandler.postDelayed(mRunnable,mTimeout);
    }

    public FindDeviceCallback(BluetoothDevice device, int timeout){
        this(device.getAddress(),timeout);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (TextUtils.equals(device.getAddress(),mMac)){
            mHandler.removeCallbacks(mRunnable);
            onFind(device);
        }
    }

    public abstract void onFind(BluetoothDevice device);

    public abstract void onFailure();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            onFailure();
        }
    };
}
