package com.bamboo.gble;

import java.util.UUID;

/**
 * Created by weiwu on 2017/12/8.
 */

public abstract class NoneRespCallback extends SingleRespCallback {

    public NoneRespCallback(UUID resp_uuid) {
        super(resp_uuid,0);
    }

    public NoneRespCallback(){
        this(null);
    }

    @Override
    public boolean hasResp() {
        return false;
    }

    @Override
    public void onRespone(byte[] data) {

    }
}
