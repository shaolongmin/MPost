package com.popsecu.sdk;

public class CommProtocol {
	
	private static final byte HEADER = 0x01;
	
	private static final byte SBI = 'D';
	private static final byte OS = 'D';
	private static final byte KMM = 'D';
	private static final byte STD_CMD = 'D';
	
	public static final byte SETP_START = 0x01;
	public static final byte SETP_DATA = 0x02;
	public static final byte SETP_STOP = 0x03;
	
//	public boolean checkHeader(byte hearder) {
//		return true;
//	}
	
//	public boolean connBle() {
//		
//	}
	
	public byte[] packageData(byte step, byte cmd, byte type, byte idx, byte[] data, int len) {
		int packLen = len + 8 + 1;
		byte[] head = new byte[8];
		byte[] pack = new byte[packLen];
		byte checksum = 0; 
		
		head[0] = 0x01;
		head[1] = step;
		head[2] = cmd;
		head[3] = idx;
		head[5] = (byte)((len >> 24) & 0xFF);
		head[6] = (byte)((len >> 16) & 0xFF);
		head[6] = (byte)((len >> 8) & 0xFF);
		head[6] = (byte)((len >> 0) & 0xFF);
		
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
