package com.bamboo.gble;

import java.util.UUID;

/**
 * Created by lianghuan on 2017/12/8.
 */

public abstract class BigWriteCallback implements WriteCallback {

    private UUID mUUID;
    private int mTimeOut;

    public BigWriteCallback(UUID resp_uuid,int timeOut){
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
    public final void onWritSuccess() {

    }

    public abstract boolean isResp();

    public abstract int getWriteDuration();

    public abstract boolean canNext(int count, byte[] resp);

    public abstract boolean canNext(int count);

    public abstract byte[] write(int writeCount);
}
