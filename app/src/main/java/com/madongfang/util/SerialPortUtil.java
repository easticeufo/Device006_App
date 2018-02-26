/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.madongfang.util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class SerialPortUtil {

    static {
        System.loadLibrary("SerialPort");
    }

    public static void send(byte[] buffer)
    {
        if (serialPortUtil.mFileOutputStream == null)
        {
            Log.e(TAG, "串口发送失败: mFileOutputStream == null");
            return;
        }

        try {
            serialPortUtil.mFileOutputStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "串口发送异常:", e);
        }
    }

    public static void send(byte[] buffer, int off, int len)
    {
        if (serialPortUtil.mFileOutputStream == null)
        {
            Log.e(TAG, "串口发送失败: mFileOutputStream == null");
            return;
        }

        try {
            serialPortUtil.mFileOutputStream.write(buffer, off, len);
        } catch (IOException e) {
            Log.e(TAG, "串口发送异常:", e);
        }
    }

    public static int recv(byte[] buffer)
    {
        if (serialPortUtil.mFileInputStream == null)
        {
            Log.e(TAG, "串口接收失败: mFileInputStream == null");
            return -1;
        }

        try {
            return serialPortUtil.mFileInputStream.read(buffer);
        } catch (IOException e) {
            Log.e(TAG, "串口接收异常:", e);
            return -1;
        }
    }

    public static int recv(byte[] buffer, int off, int len)
    {
        if (serialPortUtil.mFileInputStream == null)
        {
            Log.e(TAG, "串口接收失败: mFileInputStream == null");
            return -1;
        }

        try {
            return serialPortUtil.mFileInputStream.read(buffer, off, len);
        } catch (IOException e) {
            Log.e(TAG, "串口接收异常:", e);
            return -1;
        }
    }

    private static final String TAG = "SerialPortUtil";

    private static SerialPortUtil serialPortUtil = new SerialPortUtil(new File("/dev/ttyS0"), 115200);

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileInputStream mFileInputStream = null;
    private FileOutputStream mFileOutputStream = null;

    private SerialPortUtil(File device, int baudrate){

		/* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
				/* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    Log.e(TAG, "异常");
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "catch Exception:", e);
                return;
            }
        }

        FileDescriptor mFd = open(device.getAbsolutePath(), baudrate);
        if (mFd == null) {
            Log.e(TAG, "串口打开失败");
            return;
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    private native static FileDescriptor open(String path, int baudrate);
    private native void close();
}
