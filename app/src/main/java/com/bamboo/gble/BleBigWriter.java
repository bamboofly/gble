package com.bamboo.gble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.util.Log;

import java.util.UUID;

/**
 * Created by lianghuan on 2017/12/8.
 * a writer that send to device with large data.
 */

public class BleBigWriter extends BleWriter implements Runnable, CharacteristicChangeListener, CharacteristicWriteListener {

    private BigWriteCallback mRespCallback;
    private UUID mSer_uuid;
    private UUID mCha_uuid;
    private long mWriteTime;
    private Handler mHandler;
    private volatile boolean mWriteSuccess;
    private volatile boolean mResponed;
    private volatile boolean mTimeOut;
    private int mRunCount;
    private WriteListenerContrl mWriteContrl;

    public BleBigWriter(Handler handler, UUID ser_uuid, UUID cha_uuid, BluetoothGatt gatt, WriteListenerContrl contrl, BigWriteCallback callback){
        super(gatt,callback);
        mRespCallback = callback;
        mSer_uuid = ser_uuid;
        mCha_uuid = cha_uuid;
        mHandler = handler;
        mWriteContrl = contrl;
    }

    @Override
    public void characteristicChange(byte[] value) {
        if(mRespCallback.canNext(mRunCount,value)){
            Log.d("lianghuan","characteristicChange mRuncount = "+mRunCount);
            mHandler.postDelayed(this,mRespCallback.getWriteDuration());
        }else {
            mHandler.removeCallbacks(mTimeOutRunnable);
            mWriteContrl.removeCharacteristicChangeListener(mRespCallback.getRespUUID());
            mWriteContrl.removeCharacteristicWriteListener(mCha_uuid);
        }
    }

    @Override
    public void characteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS){
            if (!mRespCallback.isResp()){
                if (mRespCallback.canNext(mRunCount)){
                    mWriteSuccess = false;
                    mHandler.postDelayed(this,mRespCallback.getWriteDuration());
                }else {
                    mHandler.removeCallbacks(mTimeOutRunnable);
                    mWriteContrl.removeCharacteristicWriteListener(mCha_uuid);
                }
            }
        }else {
            mHandler.removeCallbacks(mTimeOutRunnable);
            mRespCallback.onWritFailure("write failure");
            mWriteContrl.removeCharacteristicWriteListener(mCha_uuid);
            if (mRespCallback.isResp()){
                mWriteContrl.removeCharacteristicChangeListener(mCha_uuid);
            }
        }
    }

    @Override
    public void run() {

        if (mRunCount == 0){
            boolean jus = mWriteContrl.characteristicJustWrite(mCha_uuid,mRespCallback.getRespUUID());
            if (jus){
                mCallback.onWritFailure("the characteristic is working");
                return;
            }
        }

        byte[] write = mRespCallback.write(mRunCount);

        if (!write(write,mSer_uuid,mCha_uuid)){
            mCallback.onWritFailure("faile");
            mWriteContrl.removeCharacteristicWriteListener(mCha_uuid);
            mWriteContrl.removeCharacteristicChangeListener(mRespCallback.getRespUUID());
            return;
        }else {
            mWriteSuccess = true;
            mWriteTime = System.currentTimeMillis();
            if (mRunCount == 0){
                mHandler.postDelayed(mTimeOutRunnable,mRespCallback.getTimeOut());
            }
        }

        mRunCount++;
    }

    @Override
    public void beforeWrite() {
        if (mRunCount == 0){
            mWriteContrl.addCharacteristicWriteListener(this,mCha_uuid);
            if (mRespCallback.isResp()){
                mWriteContrl.addCharacteristicChangeListener(this,mRespCallback.getRespUUID());
            }
        }
    }

    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long space = currentTime - mWriteTime;
            if (space > mRespCallback.getTimeOut()){
                mTimeOut = true;
                if (mRespCallback.isResp()){
                    mWriteContrl.removeCharacteristicChangeListener(mRespCallback.getRespUUID());
                }
                mWriteContrl.removeCharacteristicWriteListener(mCha_uuid);
                mRespCallback.onWritFailure("timeOut");
            }else {
                mHandler.postDelayed(this,mRespCallback.getTimeOut());
            }
        }
    };
}
