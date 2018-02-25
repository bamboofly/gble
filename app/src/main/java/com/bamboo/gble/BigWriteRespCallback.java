package com.bamboo.gble;

import java.util.UUID;

/**
 * Created by weiwu on 2017/12/11.
 */

public abstract class BigWriteRespCallback extends BigWriteCallback {
    private int mWriteDuration;

    public BigWriteRespCallback(UUID resp_uuid, int timeOut) {
        super(resp_uuid, timeOut);
        mWriteDuration = 0;
    }

    public BigWriteRespCallback(UUID resp_uuid, int timeOut, int writeDuration){
        this(resp_uuid,timeOut);
        mWriteDuration = writeDuration;
    }

    @Override
    public int getWriteDuration() {
        return mWriteDuration;
    }

    @Override
    public boolean isResp() {
        return true;
    }

    @Override
    public final boolean canNext(int count) {
        return false;
    }
}
