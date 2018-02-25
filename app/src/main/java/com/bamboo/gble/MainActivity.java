package com.bamboo.gble;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BleClient mBleClient;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler.post(mRunnable);
        Log.d("lianghuan","oncreat");
    }

    public void connect(View view){
        if (mBleClient == null){
            mBleClient = new BleClient(this);
        }
        mBleClient.connectDeviceByMac("F7:04:C4:66:86:95", 10 * 1000, new BleConnectCallback() {
            @Override
            public void connectStart(BluetoothDevice device) {

            }

            @Override
            public void connectSuccess(BluetoothDevice device) {

            }

            @Override
            public void channelOpened(BluetoothDevice device) {

            }

            @Override
            public void connectFailure(String errMsg) {

            }
        });
    }

    int i;

    UUID ser_uuid = UUID.fromString("0a10cb10-0000-0000-00cb-075582842602");
    UUID cha_uuid = UUID.fromString("0a10cb12-0000-0000-00cb-075582842602");
    UUID resp_uuid = UUID.fromString("0a10cb11-0000-0000-00cb-075582842602");

    UUID ser2_uuid = UUID.fromString("0a10cb30-0000-0000-00cb-075582842602");
    UUID cha2_uuid = UUID.fromString("0a10cb34-0000-0000-00cb-075582842602");
    UUID resp2_uuid = UUID.fromString("0a10cb33-0000-0000-00cb-075582842602");
    int count = 0;
    public void send(View view){
        count = 0;
        for (;i < 2;){
            i++;
            mBleClient.writeCharacteric(new BleWritePackage(ser2_uuid,cha2_uuid
                    , new byte[]{0x58}, true), new NoneRespCallback(resp2_uuid) {
                @Override
                public void onRespone(byte[] bytes) {
                    Log.d("lianghuan","data = "+StringUtil.bytesToHexString(bytes));
                    count ++;
                    if (count >= 2){
//                        setRespOver(true);
                    }
                }

                @Override
                public void onWritSuccess() {
                    Log.d("lianghuan","onWriteSuccess thread = "+Thread.currentThread().getName());
                }

                @Override
                public void onWritFailure(String errMsg) {
                    Log.d("lianghuan","onWriteFailure msg = "+errMsg);
                }
            });
//            SystemClock.sleep(10);
        }
        for (;i < 4;){
            i++;
            mBleClient.writeCharacteric(new BleWritePackage(ser_uuid,cha_uuid
                    , new byte[]{0x2d, (byte)(i % 2 + 1)}, true), new SingleRespCallback(resp_uuid,1000) {
                @Override
                public void onRespone(byte[] bytes) {
                    Log.d("lianghuan","data = "+StringUtil.bytesToHexString(bytes));
                }

                @Override
                public void onWritSuccess() {
                    Log.d("lianghuan","onWriteSuccess thread = "+Thread.currentThread().getName());
                }

                @Override
                public void onWritFailure(String errMsg) {
                    Log.d("lianghuan","onWriteFailure msg = "+errMsg);
                }
            });
//            SystemClock.sleep(10);
        }


        i = 0;
    }

    UUID cha3_uuid = UUID.fromString("0a10cb14-0000-0000-00cb-075582842602");
    UUID resp3_uuid = UUID.fromString("0a10cb13-0000-0000-00cb-075582842602");
    public void send2(View view){
        String str = "hello,my name is liming.what is your name?";
        final byte[] bytes = str.getBytes();

        final int leng = bytes.length % 15 == 0 ? bytes.length / 15 : bytes.length / 15 + 1;
        final byte highbyte = (byte) ((bytes.length >> 8) & 0xff);
        final byte lowbyte = (byte) (bytes.length & 0xff);
        mBleClient.writeCharacteric(ser_uuid, cha3_uuid, new BigWriteRespCallback(resp3_uuid,1000) {

            @Override
            public boolean canNext(int count, byte[] resp) {
                Log.d("lianghuan","canNext count = "+count+" leng = "+leng+" resp = "+StringUtil.bytesToHexString(resp));
                if (count >= leng){
                    return false;
                }
                if (resp[0] == 0x7f && resp[1] == 0x39){
                    if (resp[2] == 0x00){
                        return true;
                    }
                }
                return false;
            }

            @Override
            public byte[] write(int writeCount) {
                byte[] by = new byte[20];
                by[0] = 0x39;
                by[1] = 0x05;
                by[2] = highbyte;
                by[3] = lowbyte;
                by[4] = (byte)writeCount;
                int copyLength = bytes.length - writeCount * 15 >= 15 ? 15 : bytes.length - writeCount * 15;
                System.arraycopy(bytes,writeCount * 15,by,5,copyLength);
                return by;
            }

            @Override
            public void onWritFailure(String errMsg) {
                Log.d("lianghuan","onWriteFailure errMsg = "+errMsg);
            }
        });
    }

    public void disconnect(View view){
        if (mBleClient != null){
            mBleClient.disconnect();
            mBleClient = null;
            Log.d("lianghuan","disconnect");
        }
    }

    public void close(View view){
        if (mBleClient != null){
            mBleClient.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mBleClient != null){
//            mBleClient.disconnect();
//            mBleClient = null;
//            Log.d("lianghuan","disconnect");
//        }
        mHandler.removeCallbacks(mRunnable);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBleClient != null){
                send2(null);
            }
            mHandler.postDelayed(this,2000);
        }
    };
}
