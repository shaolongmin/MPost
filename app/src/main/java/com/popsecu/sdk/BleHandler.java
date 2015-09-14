package com.popsecu.sdk;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xumin on 2015/9/12.
 */

@SuppressLint("NewApi")
public class BleHandler {
    public static final int BT_STATUS_DISCONNECT = 0;
    //public static final int BT_STATUS_CONNECTING = 1;
    public static final int BT_STATUS_CONNECTED = 2;
    private static final long SCAN_PERIOD = 20000;
    private static final int CONNECT_TIMEOUT = 20;

    private int mConnectionState = BT_STATUS_DISCONNECT;
    private Context mContext;

//    private final String UUID_COMM_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
//    private final String UUID_CHARACTER_WRITE = "0000fff1-0000-1000-8000-00805f9b34fb";
//    private final String UUID_CHARACTER_NOTIFY = "0000fff4-0000-1000-8000-00805f9b34fb";

//    private final String UUID_COMM_SERVICE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
//    private final String UUID_CHARACTER_WRITE = "49535343-6daa-4d02-abf6-19569aca69fe";
//    private final String UUID_CHARACTER_NOTIFY = "49535343-aca3-481c-91ec-d85e28a60318";

    private final String UUID_COMM_SERVICE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    private final String UUID_CHARACTER_WRITE = "49535343-8841-43f4-a8d4-ecbe34729bb3";
    private final String UUID_CHARACTER_NOTIFY = "49535343-1e4d-4bd9-ba61-23c647249616";

    private BoundedBuffer mSendBuf = new BoundedBuffer("SEND_BUF");
    private BoundedBuffer mRecvBuf = new BoundedBuffer("RECV_BUF");
    private boolean mThreadFlag = true;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mWriteCharacter;
    private BluetoothGattCharacteristic mNotifyCharacter;

    private Semaphore mSemSend;
    private Semaphore mSemConnect;
    private SendThread mSendThread;

    public BleHandler(Context context) {
        mContext = context;
        if (!context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            return;
        }

        final BluetoothManager manager = (BluetoothManager) (context
                .getSystemService(Context.BLUETOOTH_SERVICE));
        mBluetoothAdapter = manager.getAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }

        mSemSend = new Semaphore(1);
        mSemConnect = new Semaphore(0);
    }

    public boolean conncet() {
        mConnectionState = BT_STATUS_DISCONNECT;
        scanDevice();
        try {
            //mSemConnect.tryAcquire(CONNECT_TIMEOUT, TimeUnit.SECONDS);
            mSemConnect.tryAcquire(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        if (mConnectionState != BT_STATUS_CONNECTED) {
            return false;
        }

        mSendThread = new SendThread();
        mSendThread.start();
        return true;
    }

    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        procDisconnect();

        mThreadFlag = false;
        if (mSendThread != null) {
            try {
                mSendThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSendThread = null;
        }
    }

    public boolean isConnected() {
        if (mConnectionState == BT_STATUS_CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

    public int send(byte[] data, int offset, int len) {
        int ret = -1;
        try {
            ret = mSendBuf.put(data, offset, len);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public int recv(byte[] buffer, int offset, int size) {
        int ret = -1;
        try {
            ret = mRecvBuf.take(buffer, offset, size);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int recv(byte[] buffer, int offset, int size, int miliseconds) {
        int ret = -1;
        try {
            ret = mRecvBuf.take(buffer, offset, size, miliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private void procDisconnect() {
        mSendBuf.close();
        mRecvBuf.close();
        mSemSend.release();
    }

    private void scanDevice() {
        if (mConnectionState != BT_STATUS_DISCONNECT) {
            return;
        }

//        Handler handler = new Handler(Looper.getMainLooper());
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            }
//        };
//        handler.postDelayed(runnable, SCAN_PERIOD);
//        mBluetoothAdapter.startLeScan(mLeScanCallback);
        Message msg = handler.obtainMessage(0);
        handler.sendMessage(msg);
        msg = handler.obtainMessage(1);
        handler.sendMessageDelayed(msg, SCAN_PERIOD);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == 0) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                Misc.logd("start scan ble device");
            } else if (what == 1) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Misc.logd("stop scan ble device");
            }
        }
    };

    private class SendThread extends Thread {
        @Override
        public void run() {
            byte[] buf = new byte[20];
            while (mThreadFlag) {
                int count = 0;
                try {
                    count = mSendBuf.take(buf, 0, buf.length);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                if (count <= 0) {
                    return;
                }

                try {
                    mSemSend.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] sendBuf = new byte[count];
                System.arraycopy(buf, 0, sendBuf, 0, count);
                mWriteCharacter.setValue(sendBuf);
                mBluetoothGatt.writeCharacteristic(mWriteCharacter);
            }
        }
    }

    class BoundedBuffer {
        private final int MAX_COUNTS = 1024 * 10;
        private final Lock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        private String name;
        private boolean flag = true;
        private  final byte[] items = new byte[MAX_COUNTS];
        private int putptr;
        private int takeptr;
        private int count;

        public BoundedBuffer(String name) {
            this.name = name;
        }

        public void close() {
            lock.lock();
            flag = false;
            notFull.signal();
            notEmpty.signal();
            lock.unlock();
        }

        public int put(byte[] buf, int ofs, int len) throws InterruptedException {
            lock.lock();
            try {
                int total = 0;
                while ((total < len) && flag) {
                    while ((count == MAX_COUNTS) &&  flag) {
                        Misc.logd(name + ": " + "buffer full, need to wait");
                        notFull.await();
                        Misc.logd(name + ": " +  "buffer not full, can write");
                    }

                    if (count == 0) {
                        notEmpty.signal();
                        Misc.logd(name + ": " + "buf not empty, notify to read");
                    }

                    int wc = MAX_COUNTS - count;
                    wc = wc > len ? len : wc;
                    for (int i = 0; i < wc; i++) {
                        items[putptr] = buf[ofs + total + i];
                        putptr = (putptr + 1) % MAX_COUNTS;
                    }

                    count += wc;
                    total += wc;
                }

                if (flag) {
                    return  total;
                } else {
                    return -1;
                }
            } finally {
                lock.unlock();
            }
        }

        public int take(byte[] buf, int ofs, int size) throws InterruptedException {
            lock.lock();
            try {
                while ((count == 0) && flag) {
                    Misc.logd(name + ": " +  "buffer empty, need to wait");
                    notEmpty.await();
                    Misc.logd(name + ": " +  "buffer not empty, can read");
                }

                if (count == MAX_COUNTS) {
                    notFull.signal();
                    Misc.logd(name + ": " + "buffer not full, notify to write");
                }

                int rc = count > size ? size : count;
                for (int i = 0; i < rc; i++) {
                    buf[ofs + i] = items[takeptr];
                    takeptr = (takeptr + 1) % MAX_COUNTS;
                }

                count -= rc;
                if (flag) {
                    return  rc;
                } else {
                    return -1;
                }
            } finally {
                lock.unlock();
            }
        }

        public int take(byte[] buf, int ofs, int size, int milliseconds) throws InterruptedException {
            lock.lock();
            try {
                while ((count == 0) && flag) {
                    Misc.logd(name + ": " +  "buffer empty, need to wait");
                    if (!notEmpty.await(milliseconds, TimeUnit.MILLISECONDS)) {
                        Misc.logd(name + ": " + "buffer empty, read timeout");
                        return -1;
                    }
                    Misc.logd(name + ": " + "buffer not empty, can read");
                }

                if (count == MAX_COUNTS) {
                    notFull.signal();
                    Misc.logd(name + ": " + "buffer not full, notify to write");
                }

                int rc = count > size ? size : count;
                for (int i = 0; i < rc; i++) {
                    buf[ofs + i] = items[takeptr];
                    takeptr = (takeptr + 1) % MAX_COUNTS;
                }

                count -= rc;
                if (flag) {
                    return  rc;
                } else {
                    return -1;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private boolean scanCharacteristics(BluetoothGatt gatt) {
        boolean flag = false;
        List<BluetoothGattService> tmp = gatt.getServices();

        mGattService = gatt.getService(UUID.fromString(UUID_COMM_SERVICE));
        if (mGattService != null) {
            mWriteCharacter = mGattService.getCharacteristic(
                    UUID.fromString(UUID_CHARACTER_WRITE));
            mNotifyCharacter = mGattService.getCharacteristic(
                    UUID.fromString(UUID_CHARACTER_NOTIFY));
            if ((mWriteCharacter != null) && (mNotifyCharacter != null)) {
                flag = gatt.setCharacteristicNotification(mNotifyCharacter, true);;
            }
        }

        return flag;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Misc.logd("scan_result:" + device.getName() + ",uuid:"
                            + device.getAddress());
                    //if (device.getName().equals("Dual-SPP") |
                    if (device.getName().equals("ITON DM") |
                            device.getName().equals("loxer")) {
                        handler.removeMessages(1);
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
                        //mBluetoothGatt = device.connectGatt(mContext, true, mBluetoothGattCallback);
                        //mConnectionState = BT_STATUS_CONNECTING;
                        Misc.logd("find ble device: " + device.getName());
                    }
                }
            };

    private final BluetoothGattCallback mBluetoothGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt,
                                                    int status, int newState) {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        Misc.logd("ble connect success" + gatt.getDevice().getAddress());
                        gatt.discoverServices();
                    } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                        Misc.logd("ble connect failed " + gatt.getDevice().getAddress());
                        mConnectionState = BT_STATUS_DISCONNECT;
                        //gatt.close();
                        mSemConnect.release();
                        procDisconnect();
                    } else {
                        Misc.logd("ble connect failed " + gatt.getDevice().getAddress());
                        mConnectionState = BT_STATUS_DISCONNECT;
                        mSemConnect.release();
                        procDisconnect();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Misc.logd("service discovered " + status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (scanCharacteristics(gatt)) {
                            mConnectionState = BT_STATUS_CONNECTED;
                            Misc.logd("scanCharacteristics success");
                        } else {
                            mConnectionState = BT_STATUS_DISCONNECT;
                            Misc.logd("scanCharacteristics failed");
                        }
                    } else {
                        mConnectionState = BT_STATUS_DISCONNECT;
                    }

                    mSemConnect.release();
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //Misc.logd("ble write success");
                    } else {
                        Misc.loge("ble write failed");
                    }

                    mSemSend.release();
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    byte[] recv = characteristic.getValue();
                    try {
                        mRecvBuf.put(recv, 0, recv.length);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //Misc.loge("ble recv notify data, len " + recv.length);
                }

            };
}
