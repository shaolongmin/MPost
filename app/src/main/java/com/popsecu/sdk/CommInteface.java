package com.popsecu.sdk;

/**
 * Created by xumin on 2015/9/10.
 */



public class CommInteface {

    public void getInstallPackageInfo() {
        byte subCmd = 0x01;
        byte[] data = new byte[1];
        data[0] = (byte)CommProtocol.CMD_STC_CONTROL_PACKAGE_INFO;

        byte[] sendData = CommProtocol.packageData(CommProtocol.SETP_START, CommProtocol.CMD_STC_CONTROL,
                (byte)0, data, data.length);

        //
        //send data;
    }
}
