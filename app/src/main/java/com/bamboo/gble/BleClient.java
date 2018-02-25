package com.bamboo.gble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by weiwu on 2017/12/6.
 */

public class BleClient {

    private final ScheduledExecutorService mThreadPool;
    private final BluetoothAdapter mAdapter;
    BluetoothGatt mGatt;
    private GattCallback mGattCallback;
    private Context mContext;
    private ConcurrentHashMap<UUID,CharacteristicWriteListener> mWriteListeners;
    private ConcurrentHashMap<UUID,CharacteristicChangeListener> mChangeListeners;
    private Handler mWriterHandle;
    private ArrayList<BluetoothGattCharacteristic> mNeedEnableCharacteristic;
    private ConcurrentHashMap<UUID,ArrayList<CharacteristicChangeListener>> mListenerHashMap;
    private BleConnectCallback mConnectCallback;


    public BleClient(Context context){
        mThreadPool = Executors.newScheduledThreadPool(4);
        mGattCallback = new GattCallback();
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        WriteThread writer = new WriteThread("writer");
        writer.start();
        Looper looper = writer.getLooper();
        mWriterHandle = new Handler(looper);

        mWriteListeners = new ConcurrentHashMap<>();
        mChangeListeners = new ConcurrentHashMap<>();
        mListenerHashMap = new ConcurrentHashMap<>();
    }

    public void connectDeviceByMac(final String mac,int timeOut, final BleConnectCallback callback){
        mAdapter.startLeScan(new FindDeviceCallback(mac,timeOut) {
            @Override
            public void onFind(BluetoothDevice device) {
                mAdapter.stopLeScan(this);
                callback.connectStart(device);
                mGatt = device.connectGatt(mContext,true,mGattCallback);
                mConnectCallback = callback;
            }

            @Override
            public void onFailure() {
                mAdapter.stopLeScan(this);
                callback.connectFailure("Do not find device with mac address "+mac);
            }
        });

    }

    public void getRssi(){
        if (mGatt != null){
            mGatt.readRemoteRssi();
        }
    }

    public void registCharacteristicListener(UUID cha_uuid,CharacteristicChangeListener listener){
        ArrayList<CharacteristicChangeListener> arrayList = mListenerHashMap.get(cha_uuid);
        if (arrayList == null){
            arrayList = new ArrayList<>();
        }
        arrayList.add(listener);
    }

    public void unregistCharacteristicListener(CharacteristicWriteListener listener){
        Collection<ArrayList<CharacteristicChangeListener>> values = mListenerHashMap.values();
        for (ArrayList<CharacteristicChangeListener> al : values) {
        	if (al.remove(listener)){
                return;
            }
        }
    }

    public void disconnect(){
        if (mGatt != null){
            mGatt.disconnect();
        }
    }

    public void close(){
        if (mGatt != null){
            mGatt.close();
        }
    }

    public <T extends SingleRespCallback> void writeCharacteric(BleWritePackage writePackage,T callback){
        if (mGatt == null){
            callback.onWritFailure("not connect any device");
            return;
        }

        mWriterHandle.post(new BleOneWriter(mWriterHandle,writePackage,mGatt,mWriteContrl,callback));
    }

    public <T extends BigWriteCallback> void writeCharacteric(UUID ser_uuid, UUID cha_uuid, T callback){
        if (mGatt == null){
            callback.onWritFailure("not connect any device");
            return;
        }

        mWriterHandle.post(new BleBigWriter(mWriterHandle,ser_uuid,cha_uuid,mGatt,mWriteContrl,callback));
    }


    private ArrayList<BluetoothGattCharacteristic> getAllCharacteristic(BluetoothGatt gatt){
        ArrayList<BluetoothGattCharacteristic> al = new ArrayList<>();
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services){
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                al.add(characteristic);

            }
        }
        return al;
    }

    private ArrayList<BluetoothGattCharacteristic> getNeedEnableCharacteristic(BluetoothGatt gatt){
        ArrayList<BluetoothGattCharacteristic> al = new ArrayList<>();
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services){
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                int properties = characteristic.getProperties();
                if (properties == BluetoothGattCharacteristic.PROPERTY_INDICATE || properties == BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                    al.add(characteristic);
                }
            }
        }
        return al;
    }

    private boolean enableCharacteristic(BluetoothGattCharacteristic characteristic){
        int properties = characteristic.getProperties();
        if (properties == BluetoothGattCharacteristic.PROPERTY_NOTIFY){
            boolean b = mGatt.setCharacteristicNotification(characteristic,true);
            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            if (descriptors.size() > 0){
                BluetoothGattDescriptor descriptor = descriptors.get(0);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                b = mGatt.writeDescriptor(descriptor);
                Log.d("lianghuan", "enableCharacteristic descriptor = "+descriptor);
            }
            return b;

        }

        if (properties == BluetoothGattCharacteristic.PROPERTY_INDICATE){
            boolean b = mGatt.setCharacteristicNotification(characteristic,true);
            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            if (descriptors.size() > 0){
                BluetoothGattDescriptor descriptor = descriptors.get(0);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                b = mGatt.writeDescriptor(descriptor);
                Log.d("lianghuan", "enableCharacteristic descriptor = "+descriptor.getUuid());
            }
            return b;
        }
        return false;
    }


    private class ScanCallbackAndConnect implements BluetoothAdapter.LeScanCallback {

        private String mMac;
        private boolean mAutoConnect;

        public ScanCallbackAndConnect(String mac){
            this(mac,true);
        }

        public ScanCallbackAndConnect(String mac, boolean autoConnect){
            mMac = mac;
            mAutoConnect = autoConnect;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (TextUtils.equals(device.getAddress(),mMac)){
                device.connectGatt(mContext, mAutoConnect,mGattCallback);
            }
        }
    }

    private class GattCallback extends BleGattCallback{
        private int mLastConnectState = -1;
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d("lianghuan","onConnectionStateChange status = "+status+" newState = "+newState);
            if (status == 0){
//                if (mLastConnectState != newState){
                    mLastConnectState = newState;
                    if (newState == BluetoothProfile.STATE_CONNECTED){
                        gatt.discoverServices();
//                    }

                }

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("lianghuan", "onServicesDiscovered status = "+status);
            if (status == 0){
                mNeedEnableCharacteristic = getNeedEnableCharacteristic(gatt);
                if (mNeedEnableCharacteristic.size() > 0){
                    enableCharacteristic(mNeedEnableCharacteristic.get(mNeedEnableCharacteristic.size() - 1));
                }

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("lianghuan","onCharacteristicRead uuid = "+characteristic.getUuid()+" status = "+status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("lianghuan","onCharacteristicWrite uuid = "+characteristic.getUuid()+" status = "+status);
            CharacteristicWriteListener characteristicWriteListener = mWriteListeners.get(characteristic.getUuid());
            if (characteristicWriteListener != null){
                characteristicWriteListener.characteristicWrite(gatt,characteristic,status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("lianghuan","onCharacteristicChanged uuid = "+characteristic+" mChangeListeners size = "+mChangeListeners.size()+" value = "+StringUtil.bytesToHexString(characteristic.getValue()));
            CharacteristicChangeListener characteristicChangeListener = mChangeListeners.get(characteristic.getUuid());
            if (characteristicChangeListener != null){
                characteristicChangeListener.characteristicChange(characteristic.getValue());
            }
            ArrayList<CharacteristicChangeListener> arrayList = mListenerHashMap.get(characteristic.getUuid());
            if (arrayList != null){
                for (int i = 0; i < arrayList.size(); i++) {
                    CharacteristicChangeListener changeListener = arrayList.get(i);
                    changeListener.characteristicChange(characteristic.getValue());
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d("lianghuan","onDescriptorRead uuid = "+descriptor+" status = "+status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d("lianghuan","onDescriptorWrite uuid = "+descriptor.getUuid()+" status = "+status);
            if (mNeedEnableCharacteristic.size() > 0){
                BluetoothGattCharacteristic characteristic = mNeedEnableCharacteristic.get(mNeedEnableCharacteristic.size() - 1);
                Log.d("lianghuan","uuid = "+ characteristic.getUuid());
                BluetoothGattDescriptor descriptor1 = characteristic.getDescriptor(descriptor.getUuid());
                if (descriptor1 != null){
                    if (status == BluetoothGatt.GATT_SUCCESS){
                        mNeedEnableCharacteristic.remove(characteristic);
                        enableCharacteristic(mNeedEnableCharacteristic.get(mNeedEnableCharacteristic.size() - 1));
                    }else {
                        //TODO
                    }
                }
            }

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d("lianghuan","rssi = "+rssi+" status = "+status);
        }
    }

    private WriteListenerContrl mWriteContrl = new WriteListenerContrl() {

        @Override
        public void addCharacteristicChangeListener(CharacteristicChangeListener listener, UUID cha_uuid) {
            mChangeListeners.put(cha_uuid,listener);
        }

        @Override
        public void addCharacteristicWriteListener(CharacteristicWriteListener listener, UUID cha_uuid) {
            mWriteListeners.put(cha_uuid,listener);
        }

        @Override
        public void removeCharacteristicChangeListener(UUID cha_uuid) {
            mChangeListeners.remove(cha_uuid);
        }

        @Override
        public void removeCharacteristicWriteListener(UUID cha_uuid) {
            mWriteListeners.remove(cha_uuid);
        }


        @Override
        public boolean characteristicJustWrite(UUID write_uuid,UUID cha_uuid) {
            if (mChangeListeners.containsKey(cha_uuid) || mWriteListeners.containsKey(write_uuid)){
                return true;
            }else {
                return false;
            }
        }

    };

}
