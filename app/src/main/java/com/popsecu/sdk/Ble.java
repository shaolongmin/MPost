package com.popsecu.sdk;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

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
	public static final int BT_STATUS_CONNECT_FAILED = 3;
	
	private static final String TAG = "DevBle";

	public static final int REQUEST_ENABLE_BT = 1;

	private static final long SCAN_PERIOD = 10000; 

	private static final int MSG_SEND = 0;
	private static final int MSG_CONNECT = 1;
	private static final int MSG_CLEARDATA = 2;
	private static final int MSG_SCAN = 3 ;


	private String RECV_UUID = "0000fff4-0000-1000-8000-00805f9b34fb";
	private String SEND_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
	private String SERVICE_RECV_REGION = "0000fff0-0000-1000-8000-00805f9b34fb";
	private String SERVICE_SEND_REGION = "0000fff0-0000-1000-8000-00805f9b34fb";

	private boolean mScanning;
	private int mConnectionState = BT_STATUS_DISCONNECT;
	private String mBluetoothDeviceAddress;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt;
	private BluetoothGattService mBluetoothGattService;
	private BluetoothGattCharacteristic mBluetoothGattCharacter;

	// for debug
	private int write_callback_count;

	private Queue<byte[]> mQueue;

	private DevCallBack mBtCallBack;
	private Context mContext;

	private Runnable mScanRunnable;

	private Boolean mDeviceBusyKey = false;

	public Ble() {
		//mContext = context;
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

		mQueue = new LinkedList<byte[]>();
		mDeviceBusyKey = false;

		return 1;
	}

	public void uninitDev() {
		disconnect();
		close();
	}

	public int sendData(byte[] data) {
		Message.obtain(handler, MSG_SEND, data).sendToTarget();
		return 0;

	}

	private void recvData(byte[] recv) {
		// int bytes;
		// int len;
		//
		// while (true) {
		// byte checksum = 0;
		// len = ((int)recv[2]) & 0xFF;
		//
		// bytes = len + 3;
		// for (int i = 2; i < (bytes - 1); i++) {
		// checksum = (byte) (checksum ^ recv[i]);
		// }
		//
		// if (checksum != recv[bytes - 1]) {
		//
		// Misc.logd("bt checksum error");
		// continue;
		// }
		//
		// if (mBtRecv != null)
		// mBtRecv.onDataRecv(recv, 0, bytes);
		// }
	}

	public int getDevStatus() {
		return mConnectionState;
	}

	public String getAddress() {
		return mBluetoothDeviceAddress;
	}

	public void scanDevice(final boolean enable) {
		Message.obtain(handler, MSG_SCAN, enable).sendToTarget();
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Misc.logd("scan_result:" + device.getName() + ",uuid:"
					+ device.getAddress());
			if (device.getName().equals("POPSECU-DUAL SPP")) {
				mBtCallBack.onScanDevice(device);
				if (getDevStatus() == BT_STATUS_CONNECTED
						|| getDevStatus() == BT_STATUS_CONNECTING) {
					return;
				}

				//if (mLeDevices != null
						//&& mLeDevices.contains(device.getAddress())) {
				connect(device.getAddress());
				//}
			}
		}
	};

	private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (newState == BluetoothGatt.STATE_CONNECTED) {
				Misc.logd("ble connect success" + gatt.getDevice().getAddress());
				mConnectionState = BT_STATUS_CONNECTED;
				Message.obtain(handler, MSG_CLEARDATA).sendToTarget();
				gatt.discoverServices();
				mBtCallBack.onConnecting(gatt.getDevice(),
						BT_STATUS_CONNECTED);

			} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
				Misc.logd("ble connect failer" + gatt.getDevice().getAddress());
				mConnectionState = BT_STATUS_DISCONNECT;
				mBtCallBack.onConnecting(gatt.getDevice(),
						BT_STATUS_CONNECT_FAILED);
				disconnect();
				close();
			} else {
				mBtCallBack.onConnecting(gatt.getDevice(),
						BT_STATUS_CONNECT_FAILED);
			}
			
			//mBtCallBack.onStatusChange(Common.BluetoothVersionType.BLUETOOTH_LOW_ENERGY,
					//mConnectionState, gatt.getDevice().getAddress());
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			super.onServicesDiscovered(gatt, status);
			Misc.logd("service discovered " + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				scanCharacteristics(gatt);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				write_callback_count++;
				Misc.logd("send call count: " + write_callback_count);
			} else {
				Misc.logd("write_callback_count: failed");
			}

			if (mQueue.size() > 0) {
				writeData();
			} else {
				mDeviceBusyKey = false;
			}

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			// if (RECV_UUID.equals(characteristic.getUuid().toString())) {
			// onble_callback_count++;
			// recvData(characteristic.getValue());
			// Misc.logd(onble_callback_count + ",onCharacteristicChanged: "
			// + characteristic.getUuid() + ","
			// + characteristic.getValue());
			// }
		}

	};

	private void scanCharacteristics(BluetoothGatt gatt) {
		List<BluetoothGattService> list = gatt.getServices();
		for (BluetoothGattService service : list) {
			Misc.logd("serviceName:" + service.toString());
			List<BluetoothGattCharacteristic> characteristics = service
					.getCharacteristics();
			for (BluetoothGattCharacteristic characteristic : characteristics) {
				if (((characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0)
				/*
				 * && ((characteristic.getProperties() &
				 * BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0)
				 */) {

					SERVICE_SEND_REGION = characteristic.getService().getUuid()
							.toString();
					SEND_UUID = characteristic.getUuid().toString();
					Misc.logd("SEND_UUID:" + SEND_UUID);
					Misc.logd("SERVICE_SEND_REGION:" + SERVICE_SEND_REGION);
				}

				if ((characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_NOTIFY)) != 0) {

					SERVICE_RECV_REGION = characteristic.getService().getUuid()
							.toString();
					RECV_UUID = characteristic.getUuid().toString();
					Misc.logd("REC_UUID:" + RECV_UUID);
					// TODO
					gatt.setCharacteristicNotification(characteristic, true);
					// BluetoothGattDescriptor descriptor =
					// characteristic.getDescriptor(
					// UUID.fromString(SERVICE_SEND_REGION));
					// descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					// mBluetoothGatt.writeDescriptor(descriptor);
				}
			}
		}
	}

	public boolean connect(final String address) {

		if (mBluetoothAdapter == null || address == null) {
			return false;
		}

		if (mBluetoothDeviceAddress != null && mBluetoothGatt != null) {
			if (mBluetoothDeviceAddress.equals(address)) {

				if (mBluetoothGatt.connect()) {
					Misc.logd("has to connect the same connection");
					mConnectionState = BT_STATUS_CONNECTING;
					return true;
				} else {
					return false;
				}
			} else {
				if (mBluetoothGatt.connect()) {
					disconnect();
				}
			}
		}

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Misc.logd("device not found:" + address);
			return false;
		}

		mBluetoothGatt = device.connectGatt(mContext, false,
				mBluetoothGattCallback);
		Misc.logd("trying to connect: " + address);
		mBluetoothDeviceAddress = address;
		mConnectionState = BT_STATUS_CONNECTING;

		mBtCallBack.onConnecting(device, BT_STATUS_CONNECTING);

		return true;
	}

	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Misc.logd("disconnect fail, mBluetoothAdapter or mBluetoothGatt is null ");
			return;
		}

		mBluetoothGatt.disconnect();
		return;
	}

	public void clearData() {
		if (mQueue != null && mQueue.size() > 0) {
			mQueue.clear();
		}
	}

	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}
	
	public int writeData() {

		if (mBluetoothGatt == null || mBluetoothAdapter == null) {
			return 0;
		}

		mBluetoothGattService = mBluetoothGatt.getService(UUID
				.fromString(SERVICE_SEND_REGION));

		if (mBluetoothGattService == null) {
			return 0;
		} else {
			mBluetoothGattCharacter = mBluetoothGattService
					.getCharacteristic(UUID.fromString(SEND_UUID));
			if (mBluetoothGattCharacter == null) {
				return 0;
			}
		}

		if (mBluetoothGattCharacter != null) {
			mBluetoothGattCharacter.setValue(mQueue.poll());
			mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacter);
			return 1;
		}
		return 0;
	}
	
	private Handler handler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_SEND:
				byte[] byteData = (byte[]) msg.obj;
				if (mConnectionState == BT_STATUS_CONNECTED) {
					mQueue.add(byteData);
					if (!mDeviceBusyKey) {
						mDeviceBusyKey = true;
						writeData();
					}
				}
				break;
			case MSG_CONNECT:

				break;
			case MSG_CLEARDATA:
				clearData();
				break;
			case MSG_SCAN:
				boolean enable = (Boolean) msg.obj ;
				if (enable) {
					if (mScanRunnable == null) {
						mScanRunnable = new Runnable() {
							@Override
							public void run() {
								mScanning = false;
								mBluetoothAdapter.stopLeScan(mLeScanCallback);
								//onScanning(mScanning);
							}
						};
					} else {
						handler.removeCallbacks(mScanRunnable);
					}

					handler.postDelayed(mScanRunnable, SCAN_PERIOD);
					mScanning = true;
					mBluetoothAdapter.startLeScan(mLeScanCallback);
				} else {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
				//onScanning(mScanning);
				break ;
			default:
				break;
			}
		}

	};
	
	
	
	public interface DevCallBack{
		public void onDataRecv(byte[] buf, int offset, int count);
		public void onStatusChange(int type , int status , String address);
		public void onScanDevice(BluetoothDevice device);
		public void onScanning(boolean flag);
		public void onConnecting(BluetoothDevice device , int type) ;
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
