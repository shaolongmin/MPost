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

    public void updateFirmware(String filePath) {
        TaskData task = new CommInteface.TaskData();
        task.type = TaskData.TaskType.TYPE_UPATA_FW;
        task.strParam = filePath;
        addTask(task);
    }

    public void sale(String value) {
        TaskData task = new CommInteface.TaskData();
        task.type = TaskData.TaskType.TYPE_TRADE_SALE;

        int i;
        byte[] buf = new byte[value.length() + 1];
        for (i = 0; i < value.length(); i++) {
            buf[i] = value.getBytes()[i];
        }
        buf[i] = 0;
        task.data = buf;

        addTask(task);
    }

    public void getBalance() {
        TaskData task = new CommInteface.TaskData();
        task.type = TaskData.TaskType.TYPE_TRADE_SELECT;
        addTask(task);
    }

    private void addTask(TaskData task) {
        mTaskQueue.add(task);
        mWorkSem.release();
    }

    public byte[] _getBalance(BtInterface handler) {
        byte[] conf;
        conf = loadInfoFromDev(handler, CommProtocol.CMD_STC_TRANS,
                CommProtocol.CMD_STC_TEANS_BALANCE, null);
        return conf;
    }

    public int _sale(BtInterface handler, TaskData task) {
        int ret;

        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, CommProtocol.CMD_STC_TRANS,
                (byte)0, null, 0);

        ret = downInfoToDev(handler, CommProtocol.CMD_STC_TRANS,
                CommProtocol.CMD_STC_TEANS_SALE, sendData, task.data);

        return ret;
    }

    private byte[] _getInstallPackageInfo(BtInterface handler) {
        byte[] conf;
        conf = loadInfoFromDev(handler, CommProtocol.CMD_STC_CONTROL,
                CommProtocol.CMD_STC_CONTROL_PACKAGE_INFO, null);
        return conf;
    }

    private byte[] _getTerminalInfo(BtInterface handler) {
        byte[] conf;
        conf = loadInfoFromDev(handler, CommProtocol.CMD_STC_CONTROL,
                CommProtocol.CMD_STC_CONTROL_TERM_INFO, null);
        return conf;
    }

    private byte[] _getUserConfig(BtInterface handler) {
        byte[] conf;
        conf = loadInfoFromDev(handler, CommProtocol.CMD_STC_CONTROL,
                CommProtocol.CMD_STC_CONTROL_CFG_INFO, null);
        return conf;
    }

    private int _setUserConfig(BtInterface handler, byte[] cfgData) {
        int ret;
        String fileName = "USERCFG\0";
        byte[] startData = new byte[4 + fileName.length()];
        startData[1] = 0;
        startData[2] = 0;
        startData[3] = 0;
        startData[4] = 0;
        for (int i = 0; i < fileName.length(); i++) {
            startData[4 + i] = fileName.getBytes()[i];
        }

        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, CommProtocol.CMD_STC_CONTROL,
                (byte) 0, startData, startData.length);

        ret = downInfoToDev(handler, CommProtocol.CMD_STC_CONTROL,
                CommProtocol.CMD_STC_CONTROL_CFG_INSTALL, startData, cfgData);

        return ret;
    }

    private int _updateFirmware(BtInterface handler, String filePath) {
        PackageInfo pack = new PackageInfo();
        //if (!pack.openFile("/sdcard/package0.bin")) {
        if (!pack.openFile(filePath)) {
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

            int ret = downInfoToDev(handler, CommProtocol.CMD_STC_CONTROL,
                    CommProtocol.CMD_STC_CONTROL_PACKAGE_INSTALL, head, data);
            if (ret != 0) {
                Misc.logd("update firmware error");
                return -1;
            }
        }

        return 0;
    }

    private byte[] loadInfoFromDev(BtInterface handler, byte cmd, byte subCmd, byte[] startData) {
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

        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START,cmd,
                (byte)0, data, data.length);
        ret = handler.send(sendData, 0, sendData.length);
        if (ret == -1) {
            return  null;
        }

        ret = recvResponseData(handler, buf, buf.length);
        if (ret == -1) {
            return null;
        }

        dataLen = ret - 9 - 1;
        if (dataLen <= 0) {
            return null;
        }

        byte[] recvData = new byte[dataLen];
        System.arraycopy(buf, 9, recvData, 0, dataLen);
        return recvData;
    }

    private int downInfoToDev(BtInterface handler, byte cmd, byte subCmd, byte[] startData, byte[] dataData) {
        int ret;
        final  int PACK_SIZE = 1024 * 7;
        byte[] buf = new byte[1024];
        byte[] data = new byte[startData.length + 1];
        data[0] = subCmd;
        System.arraycopy(startData, 0, data, 1, startData.length);
        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, cmd,
                (byte)0, data, data.length);
        ret = handler.send(sendData, 0, sendData.length);
        if (ret == -1) {
            return  1;
        }
        ret = recvResponseData(handler, buf, buf.length);
        if (ret == -1) {
            return 1;
        }
        if (buf[2] != cmd) {
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
            sendData = CommProtocol.packageData(CommProtocol.SETP_DATA, cmd,
                    (byte)i, data, data.length);
            ret = handler.send(sendData, 0, sendData.length);
            if (ret == -1) {
                return -1;
            }
            ret = recvResponseData(handler, buf, buf.length);
            if (ret == -1) {
                return -1;
            }
            if (buf[2] != cmd) {
                return -1;
            }

            if (buf[8] != 0) {
                return -1;
            }
        }

        data = new byte[1];
        data[0] = subCmd;
        sendData = CommProtocol.packageData(CommProtocol.SETP_STOP, cmd,
                (byte) 0, data, data.length);
        ret = handler.send(sendData, 0, sendData.length);
        if (ret == -1) {
            return -1;
        }
        ret = recvResponseData(handler, buf, buf.length);
        if (ret == -1) {
            return -1;
        }
        if (buf[2] != cmd) {
            return -1;
        }

        return 0;
    }


    private boolean readLenData(BtInterface handler, byte[] buf, int ofs, int len) {
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

    private int recvResponseData(BtInterface handler, byte[] buf, int size) {
        int len;
        byte lrc = 0;

        if (!readLenData(handler, buf, 0, 1)) {
            Misc.logd("recv response header error");
            return -1;
        }

        if (buf[0] != CommProtocol.HEADER) {
            Misc.logd("recv response header not HEADER");
            return -1;
        }

        if (!readLenData(handler, buf, 1, 7)) {
            Misc.logd("recv response header data error");
            return -1;
        }

        len = ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
        if (len > size) {
            Misc.logd("recv response len > buffer size");
            return -1;
        }

        if (!readLenData(handler, buf, 8, len + 1)) {
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

    private boolean handShake(BtInterface hander) {
        byte[] shake = new byte[1];
        byte[] buf = new byte[1];
        shake[0] = 'S';
        int counts = 3;

        while (counts-- > 0) {
            if (hander.send(shake, 0, 1) == -1) {
                return false;
            }

            if (hander.recv(buf, 0, buf.length, 2000) == -1) {
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
//                BleHandler bleHandler = null;
                BtInterface handler = null;

                while (mWorkFlag) {
                    updateBleStatus(0);
//                    handler = new BleHandler(mContext);
                    handler = new BtHandler(mContext);
                    if (handler.conncet()) {
                        Misc.logd("connect dev success");
                        updateBleStatus(1);
                        break;
                    }
                    Misc.logd("connect dev failed");
                    handler.close();
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
//                byte[] buf = new byte[10];
//                for (int i = 0; i < buf.length; i++) {
//                    buf[i] = 'S';
//                }
                byte[] buf = ctrlDev(0, 1, 0, 0);
                while (true) {
                    ret = handler.send(buf, 0, buf.length);
                    if (ret == -1) {
                        Misc.logd("write data failed");
                        break;
                    }

                    ret =  handler.recv(rbuf, 0, rbuf.length);
                    Misc.logd("read data len " + ret);
//
//                    if (ret != -1) {
//                        ret = handler.send(buf, 0, buf.length);
//                        if (ret == -1) {
//                            Misc.logd("write data failed");
//                            break;
//                        }
//                    } else {
//                        break;
//                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
               //*/


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

                    if (!hasTask && !handler.isConnected()) {
                        break;
                    }

                    TaskData task = mTaskQueue.poll();
                    if (task == null) {
                        continue;
                    }

                    if (!handShake(handler)) {
                        break;
                    }

                    if (task.type == TaskData.TaskType.TYPE_GET_PACKAGEINFO ) {
                        //Event evnt = new Event();
                    } else if (task.type == TaskData.TaskType.TYPE_GET_USERINFO) {
                        Event evnt = new Event(Event.EventType.GET_USER_CFG);
                        byte[] config = _getUserConfig(handler);
                        if (config != null) {
                            evnt.setIntParam(0);
                            evnt.setObjectParam(config);
                        } else {
                            evnt.setIntParam(1);
                        }

                        EventCenter.getInstance().notifyEvent(evnt);
                    } else if (task.type == TaskData.TaskType.TYPE_SET_USERINFO) {
                        Event evnt = new Event(Event.EventType.SET_USER_CFG);
                        ret = _setUserConfig(handler, task.data);
                        if (ret == 0) {
                            evnt.setIntParam(0);
                        } else {
                            evnt.setIntParam(1);
                        }
                        EventCenter.getInstance().notifyEvent(evnt);
                    } else if (task.type == TaskData.TaskType.TYPE_GET_TERMINAL) {
                        byte[] ter = _getTerminalInfo(handler);
                    } else if (task.type == TaskData.TaskType.TYPE_UPATA_FW) {
                        Event evnt = new Event(Event.EventType.UPATA_FW);
                        ret = _updateFirmware(handler, task.strParam);
                        if (ret == 0) {
                            evnt.setIntParam(0);
                        } else {
                            evnt.setIntParam(1);
                        }
                        EventCenter.getInstance().notifyEvent(evnt);
                    } else if (task.type == TaskData.TaskType.TYPE_TRADE_SALE) {
                        Event evnt = new Event(Event.EventType.SALE);
                        ret = _sale(handler, task);
                        if (ret == 0) {
                            evnt.setIntParam(0);
                        } else {
                            evnt.setIntParam(1);
                        }
                        EventCenter.getInstance().notifyEvent(evnt);
                    } else if (task.type == TaskData.TaskType.TYPE_TRADE_SELECT) {
                        Event evnt = new Event(Event.EventType.SELECT);
                        byte[] result = _getBalance(handler);
                        evnt.setObjectParam(result);
                        if (result != null) {
                            evnt.setIntParam(0);
                        } else {
                            evnt.setIntParam(1);
                        }
                        EventCenter.getInstance().notifyEvent(evnt);
                    }
                }

                if (handler != null) {
                    handler.close();
                }

                //*/

            }
        }
    }

    //for test
    public byte[] ctrlDev(int enable, int num, int mode, int pwm) {
        byte[] send;
        byte[] data;

        data = new byte[1];
        data[0] = (byte) 0x01;
//        data[1] = (byte) enable;
//        data[2] = (byte) mode;
//        data[3] = (byte) pwm;

        send = dataPackage((byte)0x03, data);

        return send;
    }

    private byte[] dataPackage(byte cmd, byte[] data) {
        byte dataLen;
        byte packLen;
        byte checksum = 0;
        byte[] pack;

        if (data != null) {
            dataLen = (byte)(data.length + 2);
        } else {
            dataLen = 2;
        }

        packLen = (byte)(dataLen + 3);
        pack = new byte[packLen];
        pack[0] = (byte)0xAA;
        pack[1] = (byte)0xBB;
        pack[2] = dataLen;
        pack[3] = cmd;

        if (data !=  null) {
            System.arraycopy(data, 0, pack, 4, data.length);
        }

        for (int i = 2; i < packLen - 1; i++) {
            checksum = (byte) (checksum ^ pack[i]);
        }

        pack[packLen - 1] = checksum;

        return pack;
    }

    private static class TaskData {
        public enum TaskType {
            TYPE_GET_PACKAGEINFO,
            TYPE_GET_USERINFO,
            TYPE_SET_USERINFO,
            TYPE_GET_TERMINAL,
            TYPE_UPATA_FW,
            TYPE_TRADE_SALE,
            TYPE_TRADE_SELECT,
            TYPE_TRADE_CANCEL
        };

        public TaskType type;
        public byte[] data;
        public String strParam;
    }

//    public interface CommIntefaceCallBack {
//        void onCmdResponse(int subCmd, byte[] data);
//    }
}
