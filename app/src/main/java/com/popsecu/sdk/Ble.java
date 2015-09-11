package com.popsecu.sdk;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.popsecu.sdk.Misc;

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


@SuppressLint("NewApi")
public class Ble {
	public static final int BT_STATUS_DISCONNECT = 0;
	public static final int BT_STATUS_CONNECTING = 1;
	public static final int BT_STATUS_CONNECTED = 2;

	private static final String TAG = "DevBle";
	public static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 10000; 

	private static final int MSG_STATUS = 0;
	private static final int MSG_SCAN = 1;
	private static final int MSG_CLEARDATA = 2;

	private String UUID_COMM_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
	private String UUID_CHARACTER_WRITE = "0000fff1-0000-1000-8000-00805f9b34fb";
	private String UUID_CHARACTER_NOTIFY = "0000fff4-0000-1000-8000-00805f9b34fb";

	private boolean mScanning;
	private int mConnectionState = BT_STATUS_DISCONNECT;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt;
	private BluetoothGattService mGattService;
	private BluetoothGattCharacteristic mWriteCharacter;
	private BluetoothGattCharacteristic mNotifyCharacter;

	private DevCallBack mBtCallBack;
	private Context mContext;
	private Boolean mDeviceBusyKey = false;

	private final int COUNTS_MAX = 1024 * 10;
	private byte[] mRecvBuffer = new byte[COUNTS_MAX];
	private byte[] mSendBuffer = new byte[COUNTS_MAX];
	private int mIdxWriteRecv;
	private int mIdxReadRecv;
	private Semaphore mSemSend;
	private Semaphore mWorkStopSem;
	private SendThread mSendThread;

	private BoundedBuffer mSendBuf = new BoundedBuffer();
	private BoundedBuffer mRecvBuf = new BoundedBuffer();

	private static Ble mInstance;

	private Ble() {
	}

	public static Ble getInstance() {
        if (mInstance == null) {
			mInstance = new Ble();
		}

		return mInstance;
    }

	public int initDev(Context context, DevCallBack recv) {
		if (!context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return 0;
		}

		final BluetoothManager manager = (BluetoothManager) (context
				.getSystemService(Context.BLUETOOTH_SERVICE));
		mBluetoothAdapter = manager.getAdapter();
		if (mBluetoothAdapter == null) {
			return 0;
		}
		
		mBtCallBack = recv;

		mSemSend = new Semaphore(1);
		mSendThread = new SendThread();
		mSendThread.start();

		return 1;
	}

	public void uninitDev() {
	}

	public void sendData(byte[] data, int ofs, int len) {
		try {
			mSendBuf.put(data, ofs, len);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private int recvData(byte[] recv, int ofs, int len) {
		int ret = 0;
		try {
			ret = mRecvBuf.take(recv, ofs, len);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public int getDevStatus() {
		return mConnectionState;
	}

	private class SendThread extends Thread {
		@Override
		public void run() {
			byte[] buf = new byte[20];
			while (true) {
				int count = 0;
				try {
					count = mSendBuf.take(buf, 0, buf.length);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (count == 0) continue;
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

	public void scanDevice(boolean enable) {
		if (mConnectionState != BT_STATUS_DISCONNECT) {
			return;
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				mScanning = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		};
		//if (enable) {
	    //}

		handler.postDelayed(runnable, SCAN_PERIOD);
		mScanning = true;
		mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Misc.logd("scan_result:" + device.getName() + ",uuid:"
					+ device.getAddress());
			if (device.getName().equals("POPSECU-DUAL SPP") |
					device.getName().equals("loxer")) {

				Message msg = handler.obtainMessage(MSG_SCAN, device);
				handler.sendMessage(msg);

				if (mConnectionState == BT_STATUS_CONNECTED
						|| mConnectionState == BT_STATUS_CONNECTING) {
					return;
				}

				mBluetoothGatt = device.connectGatt(mContext, true, mBluetoothGattCallback);
				mConnectionState = BT_STATUS_CONNECTING;
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
				//mConnectionState = BT_STATUS_CONNECTED;
				gatt.discoverServices();
			} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
				Misc.logd("ble connect failer" + gatt.getDevice().getAddress());
				mConnectionState = BT_STATUS_DISCONNECT;
				gatt.close();
			} else {
				mConnectionState = BT_STATUS_DISCONNECT;
			}

			Message msg = handler.obtainMessage(MSG_STATUS, mConnectionState, 0);
			handler.sendMessage(msg);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			Misc.logd("service discovered " + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (scanCharacteristics(gatt)) {
					mConnectionState = BT_STATUS_CONNECTED;
					Message msg = handler.obtainMessage(MSG_STATUS, mConnectionState, 0);
					handler.sendMessage(msg);
				}
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Misc.logd("ble write success");
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
		}

	};

	private boolean scanCharacteristics(BluetoothGatt gatt) {
		boolean flag = false;

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

	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Misc.logd("disconnect fail, mBluetoothAdapter or mBluetoothGatt is null ");
			return;
		}

		mBluetoothGatt.disconnect();
		return;
	}

	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			if (mBtCallBack == null) {
				return;
			}

			switch (msg.what) {
				case MSG_STATUS:
					mBtCallBack.onStatusChange(msg.arg1);
					break;
				case MSG_SCAN:
					mBtCallBack.onScanDevice((BluetoothDevice)msg.obj);
					break;
			}
		}
	};

	class BoundedBuffer {
		final int MAX_COUNTS = 1024 * 10;
		final Lock lock = new ReentrantLock();
		final Condition notFull = lock.newCondition();
		final Condition notEmpty = lock.newCondition();

		final byte[] items = new byte[MAX_COUNTS];
		int putptr, takeptr, count;

		public void reset() {
			putptr = 0;
			takeptr = 0;
			count = 0;
		}

		public void put(byte[] buf, int ofs, int len) throws InterruptedException {
			int total = 0;

			lock.lock();
			try {
				while (total < len) {
					while (count == MAX_COUNTS) {
						notFull.await();
					}

					if (count == 0) {
						notEmpty.signal();
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
			} finally {
				lock.unlock();
			}
		}

		public int take(byte[] buf, int ofs, int size) throws InterruptedException {
			lock.lock();
			try {
				while (count == 0) {
					notEmpty.await();
				}

				if (count == MAX_COUNTS) {
					notFull.signal();
				}

				int rc = count > size ? size : count;
				for (int i = 0; i < rc; i++) {
					buf[ofs + i] = items[takeptr];
					takeptr = (takeptr + 1) % MAX_COUNTS;
				}

				count -= rc;
				return rc;
			} finally {
				lock.unlock();
			}
		}
	}

	public interface DevCallBack{
		public void onStatusChange(int status);
		public void onScanDevice(BluetoothDevice device);
	};
}

/*
 class BoundedBuffer {
   final Lock lock = new ReentrantLock();
   final Condition notFull  = lock.newCondition();
   final Condition notEmpty = lock.newCondition();

   final Object[] items = new Object[100];
   int putptr, takeptr, count;

   public void put(Object x) throws InterruptedException {
     lock.lock();
     try {
       while (count == items.length)
         notFull.await();
       items[putptr] = x;
       if (++putptr == items.length) putptr = 0;
       ++count;
       notEmpty.signal();
     } finally {
       lock.unlock();
     }
   }

   public Object take() throws InterruptedException {
     lock.lock();
     try {
       while (count == 0)
         notEmpty.await();
       Object x = items[takeptr];
       if (++takeptr == items.length) takeptr = 0;
       --count;
       notFull.signal();
       return x;
     } finally {
       lock.unlock();
     }
   }
 }
 */
