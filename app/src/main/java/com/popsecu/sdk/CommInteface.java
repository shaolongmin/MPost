package com.popsecu.sdk;

import android.content.Context;

import java.sql.Time;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Created by xumin on 2015/9/10.
 */



public class CommInteface {

    private static CommInteface mInstance;
    private Context mContext;

    private ConcurrentLinkedQueue<TaskData> mTaskQueue;
    private Semaphore mWorkSem;
    private WorkThread mWorkThread;
    private boolean mWorkFlag;

    private CommInteface() {

    }

    public static CommInteface getInstance() {
        if (mInstance == null) {
            mInstance = new CommInteface();
        }

        return mInstance;
    }

    public void initCommInterface(Context context) {
        mContext = context;
        mWorkFlag = true;
        mWorkSem = new Semaphore(0);
        mTaskQueue = new ConcurrentLinkedQueue<TaskData>();
        mWorkThread = new WorkThread();
        mWorkThread.start();
    }

    public byte[] getInstallPackageInfo() {
        byte subCmd = 0x01;
        byte[] data = new byte[1];
        data[0] = (byte)CommProtocol.CMD_STC_CONTROL_PACKAGE_INFO;

        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, CommProtocol.CMD_STC_CONTROL,
                (byte)0, data, data.length);

        return null;
    }

    private int ReadLenData(byte[] buf, int ofs, int len) {
        return 0;
    }

    private class WorkThread extends Thread {
        @Override
        public void run() {
            while (mWorkFlag) {
                BleHandler bleHandler = null;

                while (mWorkFlag) {
                    bleHandler = new BleHandler(mContext);
                    if (bleHandler.conncet()) {
                        Misc.logd("connect dev success");
                        break;
                    }

                    Misc.logd("connect dev failed");
                    bleHandler.close();

                    Misc.logd("close ble handler failed");

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //for test
                byte[] buf = new byte[100];
                for (int i = 0; i < buf.length; i++) {
                    buf[i] = 'S';
                }
                while (true) {
                    int ret = bleHandler.send(buf, 0, buf.length);
                    if (ret == -1) {
                        Misc.logd("write data failed");
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /*
                try {
                    mWorkSem.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }

                TaskData task = mTaskQueue.poll();
                if (task == null) {
                    continue;
                }

                if (task.type == TaskData.TYPE_GET_PACKAGEINFO ) {
                    //Event evnt = new Event();
                }
                */

            }
        }
    }

    public class TaskData {
        public static final int TYPE_GET_PACKAGEINFO = 0;

        public int type;
        public byte[] data;
    }

    public interface CommIntefaceCallBack {
        void onCmdResponse(int subCmd, byte[] data);
    }
}
