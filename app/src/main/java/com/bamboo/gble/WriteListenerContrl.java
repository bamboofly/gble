package com.bamboo.gble;

import java.util.UUID;

/**
 * Created by weiwu on 2017/12/7.
 */

public interface WriteListenerContrl {

    void addCharacteristicChangeListener(CharacteristicChangeListener listener, UUID cha_uuid);
    void addCharacteristicWriteListener(CharacteristicWriteListener listener, UUID cha_uuid);

    void removeCharacteristicChangeListener(UUID cha_uuid);
    void removeCharacteristicWriteListener(UUID cha_uuid);

    boolean characteristicJustWrite(UUID write_uuid, UUID cha_uuid);
}
