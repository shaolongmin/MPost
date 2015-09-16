package com.popsecu.sdk;

public class CommProtocol {

	public static final byte HEADER = 0x01;

	public static final byte SBI = 'D';
	public static final byte OS = 'O';
	public static final byte KMM = 'K';
	public static final byte STD_CMD = 'S';
	
	public static final byte SETP_START = 0x01;
	public static final byte SETP_DATA = 0x02;
	public static final byte SETP_STOP = 0x03;

	public static final byte CMD_STC_CONTROL = 0x00;
	public static final byte CMD_STC_TRANS = 0x01;
	public static final byte CMD_STC_EXCHANGE = 0x02;

	public static final byte CMD_STC_CONTROL_PACKAGE_INFO = 0x01;
	public static final byte CMD_STC_CONTROL_PACKAGE_INSTALL = 0x02;
	public static final byte CMD_STC_CONTROL_CFG_INFO = 0x06;
	public static final byte CMD_STC_CONTROL_CFG_INSTALL = 0x05;
	public static final byte CMD_STC_CONTROL_TERM_INFO = 0x07;

    public static final byte CMD_STC_TEANS_SALE = 0x02;
    public static final byte CMD_STC_TEANS_BALANCE = 0x01;



	public static byte[] packageData(byte step, byte cmd, byte idx, byte[] data, int len) {
		int packLen = len + 8 + 1;
		//byte[] head = new byte[8];
		byte[] pack = new byte[packLen];
		byte checksum = 0;

		pack[0] = HEADER;
		pack[1] = step;
		pack[2] = cmd;
		pack[3] = idx;
		pack[4] = (byte)((len >> 24) & 0xFF);
		pack[5] = (byte)((len >> 16) & 0xFF);
		pack[6] = (byte)((len >> 8) & 0xFF);
		pack[7] = (byte)((len >> 0) & 0xFF);
		
        if (len > 0) {
            System.arraycopy(data, 0, pack, 8, len);
        }
		
        for (int i = 1; i < (packLen - 1); i++) {
            checksum = (byte) (checksum ^ pack[i]);
        }
        
        pack[packLen - 1] = checksum;
        
        return pack;
	}
	
	
	
}
