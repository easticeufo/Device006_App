package com.madongfang.util;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by madongfang on 17/10/16.
 */

public class DeviceControlUtil {

    public static final int CHANNEL_NUMBER = 4;

    public static final int STATE_STANDBY = 0; // 待机

    public static final int STATE_READY = 1; // 就绪：收到容量命令，指示灯亮

    public static final int STATE_OUTFLOW = 2; // 出水

    public static final int STATE_FAULT = 4; // 故障

    public static final int STATE_FINISH = 5; // 完成

    public static final int STATE_CANCEL = 6; // 取消

    public static final int STATE_PAUSE = 7; // 暂停

    public static void init()
    {
        deviceReceiveThread.start();
    }

    public static int getState(int channel)
    {
        ProtocolData protocolData = new ProtocolData();
        protocolData.channel = channel;
        protocolData.cmd = CMD_QUERY_STATE;

        if (!sendRecv(protocolData))
        {
            Log.w(TAG, "getState: sendRecv failed");
            return -1;
        }

        return protocolData.state;
    }

    private static final String TAG = "DeviceControlUtil";

    private static final int CMD_QUERY_STATE = 10; // 状态查询

    private static final int CMD_SET_QUANTITY = 11; // 设置购买量

    private static final int CMD_STOP = 12; // 停止

    private static final int CMD_READ_CALIBRATION = 13; // 写校准值

    private static final int CMD_WRITE_CALIBRATION = 14; // 读校准值

    private static DeviceReceiveThread deviceReceiveThread = new DeviceReceiveThread();

    private static BlockingQueue<ProtocolData> recvQueue = new LinkedBlockingQueue<>();

    private static boolean sendRecv(ProtocolData protocolData)
    {
        recvQueue.clear();

        if (!sendCmd(protocolData.channel, protocolData.cmd, protocolData.data))
        {
            Log.e(TAG, "sendCmd failed");
            return false;
        }

        while (true)
        {
            try {
                ProtocolData recvData = recvQueue.poll(200, TimeUnit.MILLISECONDS);
                if (recvData == null)
                {
                    Log.w(TAG, "sendRecv: 无串口响应数据");
                    return false;
                }
                if (recvData.cmd == protocolData.cmd && recvData.channel == protocolData.channel)
                {
                    protocolData = recvData;
                    return true;
                }
                else
                {
                    Log.w(TAG, "sendRecv: 接收到的响应数据错误:recvData="+recvData);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "sendRecv catch InterruptedException:", e);
            }
        }

    }

    private static boolean sendCmd(int channel, int cmd, int data)
    {
        if (channel < 0 || channel >= CHANNEL_NUMBER)
        {
            Log.e(TAG, "通道号错误: channel=" + channel);
            return false;
        }

        byte[] buffer = new byte[9];

        buffer[0] = (byte) (0xFF & (0xE1 + channel));
        buffer[1] = (byte) (0xFF & cmd);
        buffer[2] = (byte) 0xFF;
        buffer[3] = (byte) (0xFF & data);
        data >>= 8;
        buffer[4] = (byte) (0xFF & data);
        data >>= 8;
        buffer[5] = (byte) (0xFF & data);
        data >>= 8;
        buffer[6] = (byte) (0xFF & data);
        buffer[7] = (byte) (0xFF & getChecksum(buffer, 0, 7));
        buffer[8] = (byte) (0xFF & 0xEC);

        SerialPortUtil.send(buffer);

        return true;
    }

    private static int getChecksum(byte[] buffer, int off, int len)
    {
        int checksum = 0;
        for (int i = off; i < off+len; i++)
        {
            checksum += buffer[i];
        }
        return checksum;
    }

    private static class ProtocolData {
        public int channel;
        public int cmd;
        public int state;
        public int data;

        @Override
        public String toString() {
            return "ProtocolData{" +
                    "channel=" + channel +
                    ", cmd=" + cmd +
                    ", state=" + state +
                    ", data=" + data +
                    '}';
        }
    }

    private static class DeviceReceiveThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[9];
            byte[] ch = new byte[1];
            int len = 0;

            while (true)
            {
                if (SerialPortUtil.recv(ch) != 1)
                {
                    Log.e(TAG, "SerialPortUtil.recv error");
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if (len == buffer.length)
                {
                    for (int i = 0; i < len-1; i++)
                    {
                        buffer[i] = buffer[i+1];
                    }
                    buffer[len-1] = ch[0];
                }
                else
                {
                    buffer[len] = ch[0];
                    len++;
                }

                if (len == buffer.length && (0xFF & buffer[len-1]) == 0xEC)
                {
                    if (buffer[0] > 0xE0 && buffer[0] <= (0xE0 + CHANNEL_NUMBER)
                            && ((0xFF & getChecksum(buffer, 0, 7)) == buffer[7]))
                    {
                        Log.d(TAG, "串口收到数据:"+buffer);
                        len = 0;

                        ProtocolData protocolData = new ProtocolData();
                        protocolData.channel = (0xFF & buffer[0]) - 0xE1;
                        protocolData.cmd = 0xFF & buffer[1];
                        protocolData.state = 0xFF & buffer[2];
                        int data = 0xFF & buffer[6];
                        data <<= 8;
                        data |= (0xFF & buffer[5]);
                        data <<= 8;
                        data |= (0xFF & buffer[4]);
                        data <<= 8;
                        data |= (0xFF & buffer[3]);
                        protocolData.data = data;
                        if (!recvQueue.offer(protocolData))
                        {
                            Log.e(TAG, "recvQueue 添加数据失败");
                        }
                    }
                }
            }
        }
    }
}
