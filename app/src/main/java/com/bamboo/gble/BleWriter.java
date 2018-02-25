package com.bamboo.gble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by weiwu on 2017/12/8.
 */

public abstract class BleWriter {
    protected WriteCallback mCallback;
    protected BluetoothGatt mGatt;

    public BleWriter(BluetoothGatt gatt, WriteCallback callback){
        mGatt = gatt;
        mCallback = callback;

    }


    public boolean write(byte[] bytes, UUID ser_uuid, UUID cha_uuid){
        if (bytes == null){
            mCallback.onWritFailure("data will be null!");
            return false;
        }

        if (bytes.length > 20){
            mCallback.onWritFailure("one data lenght will be not over 20");
            return false;
        }

        BluetoothGattService service = mGatt.getService(ser_uuid);

        if (service == null){
            mCallback.onWritFailure("BluetoothGattService not found!");
            return false;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(cha_uuid);

        if (characteristic == null){
            mCallback.onWritFailure("BluetoothGattCharacteristic not found!");
            return false;
        }

        characteristic.setValue(bytes);

        beforeWrite();

        return mGatt.writeCharacteristic(characteristic);
    }

    public abstract void beforeWrite();
}
