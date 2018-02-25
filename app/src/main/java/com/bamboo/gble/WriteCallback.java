package com.bamboo.gble;

/**
 * Created by weiwu on 2017/12/6.
 */

public interface WriteCallback {

    void onWritSuccess();

    void onWritFailure(String errMsg);

}
