package com.bamboo.gble;

import java.util.UUID;

/**
 * Created by weiwu on 2017/12/6.
 */

public class BleWritePackage {
    private UUID mService;
    private UUID mCharacteristic;
    private byte[] mData;
    private boolean mResp;
    public BleWritePackage(UUID service,UUID Characteristic, byte[] data, boolean resp){
        mService = service;
        mCharacteristic = Characteristic;
        mData = data;

        mResp = resp;
    }

    public UUID getService() {
        return mService;
    }

    public void setService(UUID service) {
        mService = service;
    }

    public UUID getCharacteristic() {
        return mCharacteristic;
    }

    public void setCharacteristic(UUID characteristic) {
        mCharacteristic = characteristic;
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        mData = data;
    }

    public boolean isResp() {
        return mResp;
    }

    public void setResp(boolean resp) {
        mResp = resp;
    }
}
