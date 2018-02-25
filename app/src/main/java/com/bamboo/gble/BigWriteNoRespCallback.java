package com.bamboo.gble;

import java.util.UUID;

/**
 * Created by weiwu on 2017/12/11.
 */

public abstract class BigWriteNoRespCallback extends BigWriteCallback {
    private int mWriteDuration;

    public BigWriteNoRespCallback(UUID resp_uuid, int timeOut) {
        super(resp_uuid, timeOut);
        mWriteDuration = 0;
    }
    public BigWriteNoRespCallback(UUID resp_uuid, int timeOut, int writeDuration){
        this(resp_uuid,timeOut);
        mWriteDuration = writeDuration;
    }

    @Override
    public boolean isResp() {
        return false;
    }

    @Override
    public final boolean canNext(int count, byte[] resp) {
        return false;
    }
}
