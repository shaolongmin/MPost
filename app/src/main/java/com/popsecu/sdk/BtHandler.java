package com.popsecu.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by xumin on 2015/9/17.
 */
public class BtHandler extends BtInterface {

    public static final int BT_STATUS_DISCONNECT = 0;
    public static final int BT_STATUS_CONNECTED = 2;

    private final int mTimeout = 15;

    private BluetoothAdapter mBluetoothAdapter;
    private String mstrUUID = "00001101-0000-1000-8000-00805F9B34FB";

    private int mConnStatus;
    private boolean mFlag;
    private BluetoothSocket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private ArrayList<BluetoothDevice> mListPairedDev;

    private Context mContext;

    public BtHandler(Context context) {
        mContext = context;
        mListPairedDev = new ArrayList<BluetoothDevice>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            mConnStatus = BT_STATUS_DISCONNECT;
            return;
        }

//        if (!mBluetoothAdapter.isEnabled()) {
//            mConnStatus = Common.DeviceKey.BT_STATUS_DISABLE;
//        } else {
//            mConnStatus = Common.DeviceKey.BT_STATUS_DISCONNECT;
//        }
    }
    @Override
    public boolean conncet() {
        return connectPairedBluetooth();
    }

    @Override
    public void close() {
        if (mSocket == null) {
            mSocket = null;
            return;
        }
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Misc.logd("bluetooth socket close error");
        }

        if (mInStream != null) {
            try {
                mInStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mInStream = null;
        }

        if (mOutStream != null) {
            try {
                mOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mOutStream = null;
        }
    }

    @Override
    public int send(byte[] data, int offset, int len) {
        if ((mOutStream == null))
            return -1;

        try {
            mOutStream.write(data);
        } catch (IOException e) {
            Misc.logd("bluetooth send data error " + e.getMessage());
            return -1;
        }

        return len;
    }

    @Override
    public int recv(byte[] buffer, int offset, int size) {
        int ret = -1;

        try {
            ret = mInStream.read(buffer, offset, size);
        } catch (IOException e) {
            Misc.logd("bluetooth read exception " + e.getMessage());
            return ret;
        }

        return ret;
    }

    @Override
    public int recv(byte[] buffer, int offset, int size, int miliseconds) {
        int ret = -1;

        try {
            ret = mInStream.read(buffer, offset, size);
        } catch (IOException e) {
            Misc.logd("bluetooth read exception " + e.getMessage());
            return ret;
        }

        return ret;
    }

    @Override
    public boolean isConnected() {
        if ((mSocket != null) && mSocket.isConnected()) {
            return true;
        }
        return false;
    }

    private boolean scanPairedBluetooth() {
        Set<BluetoothDevice> pairedDevices =
                mBluetoothAdapter.getBondedDevices();
        mListPairedDev.clear();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("ITON DM") ||
                        device.getName().equals("loxer")) {
                    mListPairedDev.add(device);
                }
            }
        }

        if (mListPairedDev.size() == 0) {
            Misc.logd("can not find bluetooth");
            return false;
        }

        return true;
    }

    private boolean initSocket(BluetoothDevice device) {
        try {
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(mstrUUID));
        } catch (IOException e) {
            Misc.logd("createRfcommSocketToServiceRecord failed");
            return false;
        }

        try {
            mSocket.connect();
        } catch (IOException e) {
            Misc.logd("connnect failed " + e.getMessage());
            return false;
        }

        try {
            mInStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();
        } catch (IOException e) {
            Misc.logd("get stream failed " + e.getMessage());
            return false;
        }

        return true;
    }

    private boolean connectPairedBluetooth() {
        int i;

        if (!scanPairedBluetooth()) {
            Misc.logd("scanBluetooth failed");
            return false;
        }

        for (i = 0; i < mListPairedDev.size(); i++) {
            if (initSocket(mListPairedDev.get(i))) {
                Misc.logd("paired: connect bluetooth success");
                return true;
            }
        }

        return false;
    }

    private boolean connectBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Misc.logd("Bluetooth disable");
            return false;
        }

        if (connectPairedBluetooth()) {
            Misc.logd("connect PairedBluetooth success");
            return true;
        }

        return  false;
    }

}
