package com.popsecu.sdk;

import android.support.v4.app.NavUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by xumin on 2015/9/13.
 */
public class PackageInfo {
    private RandomAccessFile file;
    private int mItemCounts;

    public PackageInfo() {

    }

    public boolean openFile(String fileName) {
        try {
            file = new RandomAccessFile(fileName, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        mItemCounts = _getPackItemCounts();

        return true;
    }

    public void closeFile() {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPackItemCounts() {
        return mItemCounts;
    }

    public int _getPackItemCounts() {
        byte[] buf = new byte[44];
        int total = 0;
        int ret;

        try {
            file.readFully(buf, 0, buf.length);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        int counts = (buf[40] & 0xFF) |
                (buf[41] & 0xFF) << 8 ;

        return counts;
    }

    public byte[] getPackItemHeadData(int idx) {
        byte[] buf = new byte[100];

        try {
            file.seek(44 + idx * 100);
            file.readFully(buf, 0, buf.length);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return buf;
    }

    public byte[] getPackItemData(byte[] head) {
        if (head == null) {
            return null;
        }

        int len = (head[92] & 0xFF) |
                (head[93] & 0xFF) << 8 |
                (head[94] & 0xFF) << 16|
                (head[95] & 0xFF) << 32;
        int ofs = (head[96] & 0xFF) |
                (head[97] & 0xFF) << 8 |
                (head[98] & 0xFF) << 16|
                (head[99] & 0xFF) << 32;

        byte[] buf = new byte[len];
        try {
            file.seek(ofs + 44 + mItemCounts * 100);
            file.readFully(buf, 0, buf.length);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return buf;
    }

    public static class PackageHead {

    }

}
