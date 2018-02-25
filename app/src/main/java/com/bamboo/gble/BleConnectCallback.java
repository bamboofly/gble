package com.bamboo.gble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by weiwu on 2017/12/6.
 */

public interface BleConnectCallback {


    void connectStart(BluetoothDevice device);

    void connectSuccess(BluetoothDevice device);

    void channelOpened(BluetoothDevice device);

    void connectFailure(String errMsg);
}
