package com.bamboo.gble;

import android.os.HandlerThread;

/**
 * Created by weiwu on 2017/12/6.
 */

public class WriteThread extends HandlerThread {
    public WriteThread(String name) {
        super(name);
    }

    public WriteThread(String name, int priority) {
        super(name, priority);
    }


}
