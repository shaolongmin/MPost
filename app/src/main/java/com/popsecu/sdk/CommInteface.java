package com.popsecu.sdk;

import android.content.Context;

import java.sql.Time;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
    private int mBleStatus;

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

    public int getBleStatus() {
        return mBleStatus;
    }

    public void getInstallPackageInfo() {
        TaskData task = new TaskData();
        task.type = TaskData.TaskType.TYPE_GET_PACKAGEINFO;
        addTask(task);
    }

    public void getUserConfig() {
        TaskData task = new CommInteface.TaskData();
        task.type = TaskData.TaskType.TYPE_GET_USERINFO;
        addTask(task);
    }

    public void setUserConfig(byte[] cfgData) {
        TaskData task = new CommInteface.TaskData();
        task.type = TaskData.TaskType.TYPE_SET_USERINFO;
        task.data = cfgData;
        addTask(task);
    }

    public void getTerminalInfo() {
        TaskData task = new CommInteface.TaskData();
        task.type = TaskData.TaskType.TYPE_GET_TERMINAL;
        addTask(task);
    }

    public void updateFirmware() {
        TaskData task = new CommInteface.TaskData();
        task.type = TaskData.TaskType.TYPE_UPATA_FW;
        addTask(task);
    }

    private void addTask(TaskData task) {
        mTaskQueue.add(task);
        mWorkSem.release();
    }

    private byte[] _getInstallPackageInfo(BleHandler bleHandler) {
        byte[] conf;
        conf = loadInfoFromDev(bleHandler, CommProtocol.CMD_STC_CONTROL_PACKAGE_INFO, null);
        return conf;
    }

    private byte[] _getTerminalInfo(BleHandler bleHandler) {
        byte[] conf;
        conf = loadInfoFromDev(bleHandler, CommProtocol.CMD_STC_CONTROL_TERM_INFO, null);
        return conf;
    }

    private byte[] _getUserConfig(BleHandler bleHandler) {
        byte[] conf;
        conf = loadInfoFromDev(bleHandler, CommProtocol.CMD_STC_CONTROL_CFG_INFO, null);
        return conf;
    }

    private int _setUserConfig(BleHandler bleHandler, byte[] cfgData) {
        int ret;
        String fileName = "USERCFG\0";
        byte[] startData = new byte[4 + fileName.length()];
        startData[1] = 0;
        startData[2] = 0;
        startData[3] = 0;
        startData[4] = 0;
        for (int i = 0; i < fileName.length(); i++) {
            startData[5 + i] = fileName.getBytes()[i];
        }

        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, CommProtocol.CMD_STC_CONTROL,
                (byte)0, startData, startData.length);

        ret = downInfoToDev(bleHandler, CommProtocol.CMD_STC_CONTROL_CFG_INSTALL, startData, cfgData);

        return ret;
    }

    private int _updateFirmware(BleHandler handler) {
        PackageInfo pack = new PackageInfo();
        if (!pack.openFile("/sdcard/package0.bin")) {
            Misc.logd("read file error");
            return -1;
        }

        int counts = pack.getPackItemCounts();
        for (int i = 0; i < counts; i++) {
            byte[] head = pack.getPackItemHeadData(i);
            byte[] data = pack.getPackItemData(head);
            if (data == null) {
                Misc.logd("read package item error");
                return -1;
            }

            int ret = downInfoToDev(handler, CommProtocol.CMD_STC_CONTROL_PACKAGE_INSTALL, head, data);
            if (ret != 0) {
                Misc.logd("update firmware error");
                return -1;
            }
        }

        return 0;
    }

    private byte[] loadInfoFromDev(BleHandler bleHandler, byte subCmd, byte[] startData) {
        int ret;
        byte[] buf = new byte[1024 * 10];

        int dataLen;
        if (startData != null) {
            dataLen = startData.length;
        } else {
            dataLen = 0;
        }
        byte[] data = new byte[dataLen + 1];
        data[0] = subCmd;
        if (startData != null) {
            System.arraycopy(startData, 0, data, 1, startData.length);
        }

        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, CommProtocol.CMD_STC_CONTROL,
                (byte)0, data, data.length);
        ret = bleHandler.send(sendData, 0, sendData.length);
        if (ret == -1) {
            return  null;
        }

        ret = recvResponseData(bleHandler, buf, buf.length);
        if (ret == -1) {
            return null;
        }

        dataLen = ret - 9 - 1;
        byte[] recvData = new byte[dataLen];
        System.arraycopy(buf, 9, recvData, 0, dataLen);
        return recvData;
    }

    private int downInfoToDev(BleHandler bleHandler, byte subCmd, byte[] startData, byte[] dataData) {
        int ret;
        final  int PACK_SIZE = 1024 * 4;
        byte[] buf = new byte[1024];
        byte[] data = new byte[startData.length + 1];
        data[0] = subCmd;
        System.arraycopy(startData, 0, data, 1, startData.length);
        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, CommProtocol.CMD_STC_CONTROL,
                (byte)0, data, data.length);
        ret = bleHandler.send(sendData, 0, sendData.length);
        if (ret == -1) {
            return  1;
        }
        ret = recvResponseData(bleHandler, buf, buf.length);
        if (ret == -1) {
            return 1;
        }
        if (buf[2] != CommProtocol.CMD_STC_CONTROL) {
            return -1;
        }

        int pack = (dataData.length + PACK_SIZE - 1) / PACK_SIZE;
        for (int i = 0; i < pack; i++) {
            int sendLen;
            if (i == (pack -1)) {
                sendLen = dataData.length - i * PACK_SIZE;
            } else {
                sendLen = PACK_SIZE;
            }

            data = new byte[sendLen + 1];
            data[0] = subCmd;
            System.arraycopy(dataData, i * PACK_SIZE, data, 1, sendLen);
            sendData = CommProtocol.packageData(CommProtocol.SETP_DATA, CommProtocol.CMD_STC_CONTROL,
                    (byte)i, data, data.length);
            ret = bleHandler.send(sendData, 0, sendData.length);
            if (ret == -1) {
                return -1;
            }
            ret = recvResponseData(bleHandler, buf, buf.length);
            if (ret == -1) {
                return -1;
            }
            if (buf[2] != CommProtocol.CMD_STC_CONTROL) {
                return -1;
            }
        }

        data = new byte[1];
        data[0] = subCmd;
        sendData = CommProtocol.packageData(CommProtocol.SETP_STOP, CommProtocol.CMD_STC_CONTROL,
                (byte) 0, data, data.length);
        ret = bleHandler.send(sendData, 0, sendData.length);
        if (ret == -1) {
            return -1;
        }
        ret = recvResponseData(bleHandler, buf, buf.length);
        if (ret == -1) {
            return -1;
        }
        if (buf[2] != CommProtocol.CMD_STC_CONTROL) {
            return -1;
        }

        return 0;
    }


    private boolean readLenData(BleHandler handler, byte[] buf, int ofs, int len) {
        int ret;
        int total = 0;

        while (total < len) {
            if ((ret = handler.recv(buf, ofs + total, len - total)) == -1) {
                return false;
            }

            total += ret;
        }

        return true;
    }

    private int recvResponseData(BleHandler hander, byte[] buf, int size) {
        int ret;
        int len;
        byte lrc = 0;

        if (!readLenData(hander, buf, 0, 1)) {
            Misc.logd("recv response header error");
            return -1;
        }

        if (buf[0] != CommProtocol.HEADER) {
            Misc.logd("recv response header not HEADER");
            return -1;
        }

        if (!readLenData(hander, buf, 1, 7)) {
            Misc.logd("recv response header data error");
            return -1;
        }

        len = ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
        if (len > size) {
            Misc.logd("recv response len > buffer size");
            return -1;
        }

        if (!readLenData(hander, buf, 8, len + 1)) {
            Misc.logd("recv response data error");
            return -1;
        }

        for (int i = 1; i < len + 9; i++)
        {
            lrc ^= buf[i];
        }

        if (lrc != 0)
        {
            Misc.logd("check sum error");
            return -1;
        }

        Misc.logd("read response success");

        return  len + 9;
    }

    private boolean handShake(BleHandler handler) {
        byte[] shake = new byte[1];
        byte[] buf = new byte[1];
        shake[0] = 'S';
        int counts = 3;

        while (counts-- > 0) {
            if (handler.send(shake, 0, 1) == -1) {
                return false;
            }

            if (handler.recv(buf, 0, buf.length, 2000) == -1) {
                return false;
            }

            if (buf[0] == 'A')
            {
                return true;
            }
        }

        return false;
    }

    private void updateBleStatus(int status) {
        Event evnt = new Event(Event.EventType.BLE_STATUS_CHANGED);
        evnt.setIntParam(status);
        EventCenter.getInstance().notifyEvent(evnt);
    }

    private class WorkThread extends Thread {
        @Override
        public void run() {
            //byte[] recvBuf = new byte[1024];
            int ret;

            while (mWorkFlag) {
                BleHandler bleHandler = null;

                while (mWorkFlag) {
                    updateBleStatus(0);
                    bleHandler = new BleHandler(mContext);
                    if (bleHandler.conncet()) {
                        Misc.logd("connect dev success");
                        updateBleStatus(1);
                        break;
                    }
                    Misc.logd("connect dev failed");
                    bleHandler.close();
                    Misc.logd("close ble handler");

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



                //for test
                /*
                byte[] rbuf = new byte[10];
                byte[] buf = new byte[10];
                for (int i = 0; i < buf.length; i++) {
                    buf[i] = 'S';
                }
                while (true) {
//                    ret = bleHandler.send(buf, 0, buf.length);
//                    if (ret == -1) {
//                        Misc.logd("write data failed");
//                        break;
//                    }

                    ret =  bleHandler.recv(rbuf, 0, rbuf.length);
                    Misc.logd("read data len " + ret);

                    if (ret != -1) {
                        ret = bleHandler.send(buf, 0, buf.length);
                        if (ret == -1) {
                            Misc.logd("write data failed");
                            break;
                        }
                    } else {
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                */


                ///*
                while (mWorkFlag) {
                    boolean hasTask;
                    try {
                        //mWorkSem.acquire();
                        hasTask = mWorkSem.tryAcquire(3, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (!hasTask && !bleHandler.isConnected()) {
                        break;
                    }

                    TaskData task = mTaskQueue.poll();
                    if (task == null) {
                        continue;
                    }

                    if (!handShake(bleHandler)) {
                        break;
                    }

                    if (task.type == TaskData.TaskType.TYPE_GET_PACKAGEINFO ) {
                        //Event evnt = new Event();
                    } else if (task.type == TaskData.TaskType.TYPE_GET_USERINFO) {
                        Event evnt = new Event(Event.EventType.GET_USER_CFG);
                        byte[] config = _getUserConfig(bleHandler);
                        setUserConfig(config);
                        if (config != null) {
                            evnt.setIntParam(0);
                            evnt.setObjectParam(config);
                        } else {
                            evnt.setIntParam(1);
                        }

                        EventCenter.getInstance().notifyEvent(evnt);
                    } else if (task.type == TaskData.TaskType.TYPE_SET_USERINFO) {
                        Event evnt = new Event(Event.EventType.SET_USER_CFG);
                        ret = _setUserConfig(bleHandler, task.data);
                        if (ret == 0) {
                            evnt.setIntParam(0);
                        } else {
                            evnt.setIntParam(1);
                        }
                        EventCenter.getInstance().notifyEvent(evnt);
                    } else if (task.type == TaskData.TaskType.TYPE_GET_TERMINAL) {
                        byte[] ter = _getTerminalInfo(bleHandler);
                    } else if (task.type == TaskData.TaskType.TYPE_UPATA_FW) {
                        Event evnt = new Event(Event.EventType.SET_USER_CFG);
                        ret = _updateFirmware(bleHandler);
                        if (ret == 0) {
                            evnt.setIntParam(0);
                        } else {
                            evnt.setIntParam(1);
                        }
                        EventCenter.getInstance().notifyEvent(evnt);
                    }
                }

                if (bleHandler != null) {
                    bleHandler.close();
                }

                //*/

            }
        }
    }

    private static class TaskData {
        public enum TaskType {
            TYPE_GET_PACKAGEINFO,
            TYPE_GET_USERINFO,
            TYPE_SET_USERINFO,
            TYPE_GET_TERMINAL,
            TYPE_UPATA_FW,
        };

        public TaskType type;
        public byte[] data;
    }

//    public interface CommIntefaceCallBack {
//        void onCmdResponse(int subCmd, byte[] data);
//    }
}
