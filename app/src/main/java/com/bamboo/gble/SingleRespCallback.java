package com.bamboo.gble;

import java.util.UUID;

/**
 * Created by weiwu on 2017/12/6.
 */

public abstract class SingleRespCallback implements WriteCallback {
    private UUID mUUID;
    private int mTimeOut;

    public SingleRespCallback(UUID resp_uuid,int timeOut){
        mUUID = resp_uuid;
        mTimeOut = timeOut;
    }

    public UUID getRespUUID(){
        return mUUID;
    }

    public int getTimeOut(){
        return mTimeOut;
    }

    @Override
    public void onWritSuccess() {

    }

    public boolean hasResp() {
        return true;
    }

    public boolean responeOver(){
        return true;
    }

    public abstract void onRespone(byte[] data);
}
