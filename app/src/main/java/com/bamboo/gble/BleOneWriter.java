package com.bamboo.gble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;

/**
 * Created by weiwu on 2017/12/6.
 */

public class BleOneWriter extends BleWriter implements Runnable, CharacteristicChangeListener, CharacteristicWriteListener {
    private BleWritePackage mPackage;
    private SingleRespCallback mRespCallback;
    private Handler mHandler;
    private volatile boolean mWriteSuccess;
    private volatile boolean mResponed;
    private volatile boolean mTimeOut;
    private int mRunCount;
    private WriteListenerContrl mWriteContrl;

    public BleOneWriter(Handler handler, BleWritePackage writePackage, BluetoothGatt gatt, WriteListenerContrl contrl, SingleRespCallback callback){
        super(gatt,callback);
        mPackage = writePackage;
        mRespCallback = callback;
        mHandler = handler;
        mWriteContrl = contrl;
    }

    @Override
    public void beforeWrite() {
        mWriteContrl.addCharacteristicWriteListener(this,mPackage.getCharacteristic());
        if (mRespCallback.hasResp()){
            mWriteContrl.addCharacteristicChangeListener(this,mRespCallback.getRespUUID());
        }
    }

    @Override
    public void run() {


        if (!mWriteSuccess){
            boolean jus = mWriteContrl.characteristicJustWrite(mPackage.getCharacteristic(),mRespCallback.getRespUUID());
            if (jus){
                mCallback.onWritFailure("the characteristic is working");
                return;
            }

            if (!write(mPackage.getData(),mPackage.getService(),mPackage.getCharacteristic())){
                mCallback.onWritFailure("faile");
                mWriteContrl.removeCharacteristicWriteListener(mPackage.getCharacteristic());
                mWriteContrl.removeCharacteristicChangeListener(mRespCallback.getRespUUID());
                return;
            }else {
                mWriteSuccess = true;
            }
        }

        if (mRunCount > 0){
            if (!mResponed){
                mTimeOut = true;
                mWriteContrl.removeCharacteristicChangeListener(mRespCallback.getRespUUID());
                mCallback.onWritFailure("respone time out");
            }
        }


        mRunCount++;
    }

    @Override
    public void characteristicChange(byte[] value) {

        if (!mTimeOut){
            mRespCallback.onRespone(value);
        }
        if (mRespCallback.responeOver()){
            mResponed = true;
            mHandler.removeCallbacks(this);
        }

        if (mRespCallback.responeOver()){
            mWriteContrl.removeCharacteristicChangeListener(mRespCallback.getRespUUID());
        }
    }

    @Override
    public void characteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getUuid().compareTo(mPackage.getCharacteristic()) == 0){
            mWriteContrl.removeCharacteristicWriteListener(mPackage.getCharacteristic());
            if (status == BluetoothGatt.GATT_SUCCESS){
                mCallback.onWritSuccess();
                if (mRespCallback.hasResp()){
                    mHandler.postDelayed(this,mRespCallback.getTimeOut());
                }
            }else {
                mWriteContrl.removeCharacteristicChangeListener(mRespCallback.getRespUUID());
                mCallback.onWritFailure(""+status);
            }
        }
    }
}
