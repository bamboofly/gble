package com.bamboo.gble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by weiwu on 2017/12/7.
 */

public interface CharacteristicWriteListener {

    void characteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
}
