package com.bamboo.gble;

/**
 * Created by weiwu on 2017/12/7.
 */

public interface CharacteristicChangeListener {
    void characteristicChange(byte[] value);
}
