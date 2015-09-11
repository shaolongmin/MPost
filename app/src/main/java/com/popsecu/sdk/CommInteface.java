package com.popsecu.sdk;

import android.content.Context;

/**
 * Created by xumin on 2015/9/10.
 */



public class CommInteface {

    private static CommInteface mInstance;
    private Context mContext;

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
    }

    public void getInstallPackageInfo() {
        byte subCmd = 0x01;
        byte[] data = new byte[1];
        data[0] = (byte)CommProtocol.CMD_STC_CONTROL_PACKAGE_INFO;

        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, CommProtocol.CMD_STC_CONTROL,
                (byte)0, data, data.length);

        //
        //send data;
    }

    private int ReadLenData(byte[] buf, int ofs, int len) {
        return 0;
    }


    public interface CommIntefaceCallBack {
        void onCmdResponse(int subCmd, byte[] data);
    }
}
