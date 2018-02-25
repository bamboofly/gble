package com.bamboo.gble;

import java.util.UUID;

/**
 * Created by weiwu on 2017/12/8.
 */

public abstract class MultipleRespCallback extends SingleRespCallback {
    private boolean mRespOver;

    public MultipleRespCallback(UUID resp_uuid,int timeOut) {
        super(resp_uuid,timeOut);
    }

    @Override
    public boolean responeOver() {
        return mRespOver;
    }

    public void setRespOver(boolean b){
        mRespOver = b;
    }
}
