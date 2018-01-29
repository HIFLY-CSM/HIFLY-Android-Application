package kr.ac.hansung.hifly;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class HIFLYStreamingSocket extends Thread {

    private final String SERVER_ADDRESS = "192.168.0.10";
    private final int SERVER_PORT = 10123;
    private DatagramSocket dsocket;
    private int udpport = -1;

    private final byte dummyByte[] = {(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x27, (byte) 0x64, (byte) 0x00,
            (byte) 0x28, (byte) 0xAC, (byte) 0xB4, (byte) 0x02, (byte) 0x80, (byte) 0x2D, (byte) 0xD8, (byte) 0x0B,
            (byte) 0x50, (byte) 0x10, (byte) 0x10, (byte) 0x14, (byte) 0x00, (byte) 0x00, (byte) 0x0F, (byte) 0xA4,
            (byte) 0x00, (byte) 0x03, (byte) 0xA9, (byte) 0x83, (byte) 0xA1, (byte) 0x80, (byte) 0x1E, (byte) 0x84,
            (byte) 0x00, (byte) 0x08, (byte) 0x95, (byte) 0x4B, (byte) 0xBC, (byte) 0xB8, (byte) 0xD0, (byte) 0xC0,
            (byte) 0x0F, (byte) 0x42, (byte) 0x00, (byte) 0x04, (byte) 0x4A, (byte) 0xA5, (byte) 0xDE, (byte) 0x5C,
            (byte) 0x3E, (byte) 0x11, (byte) 0x08, (byte) 0xA3, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x01, (byte) 0x28, (byte) 0xEE, (byte) 0x38, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x01, (byte) 0x25, (byte) 0xB8, (byte) 0x20, (byte) 0x2D, (byte) 0xCC, (byte) 0xA4, (byte) 0x7B,
            (byte) 0x8F, (byte) 0x0D, (byte) 0xFF, (byte) 0xFC, (byte) 0x6A, (byte) 0x4B, (byte) 0x85, (byte) 0x9D,
            (byte) 0x1B, (byte) 0x52, (byte) 0xCB, (byte) 0x96, (byte) 0xB0, (byte) 0xC8, (byte) 0x5E, (byte) 0x64,
            (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00,
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x0C, (byte) 0xB9, (byte) 0xC5, (byte) 0xDC,
            (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x01, (byte) 0x62,
            (byte) 0x00, (byte) 0x05, (byte) 0x40, (byte) 0x00, (byte) 0x15, (byte) 0xC0, (byte) 0x00, (byte) 0x7F,
            (byte) 0x00, (byte) 0x02, (byte) 0x64, (byte) 0x00, (byte) 0x10, (byte) 0xD0, (byte) 0x00, (byte) 0xFB,
            (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x25, (byte) 0x00, (byte) 0x5A,
            (byte) 0x2E, (byte) 0x08, (byte) 0x0B, (byte) 0x7F, (byte) 0xCC, (byte) 0xA4, (byte) 0x7B, (byte) 0x8F,
            (byte) 0x0D, (byte) 0xFF, (byte) 0xFC, (byte) 0x6A, (byte) 0x4B, (byte) 0x85, (byte) 0x9D, (byte) 0x1B,
            (byte) 0x52, (byte) 0xCB, (byte) 0x96, (byte) 0xB0, (byte) 0xC8, (byte) 0x5E, (byte) 0x64, (byte) 0x00,
            (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x03,
            (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x0C, (byte) 0xB9, (byte) 0xC5, (byte) 0xDC, (byte) 0x00,
            (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x01, (byte) 0x62, (byte) 0x00,
            (byte) 0x05, (byte) 0x40, (byte) 0x00, (byte) 0x15, (byte) 0xC0, (byte) 0x00, (byte) 0x7F, (byte) 0x00,
            (byte) 0x02, (byte) 0x64, (byte) 0x00, (byte) 0x10, (byte) 0xD0, (byte) 0x00, (byte) 0xFB, (byte) 0x80,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x25, (byte) 0x00, (byte) 0x2D, (byte) 0x0B,
            (byte) 0x82, (byte) 0x02, (byte) 0xDF, (byte) 0xCC, (byte) 0xA4, (byte) 0x7B, (byte) 0x8F, (byte) 0x0D,
            (byte) 0xFF, (byte) 0xFC, (byte) 0x6A, (byte) 0x4B, (byte) 0x85, (byte) 0x9D, (byte) 0x1B, (byte) 0x52,
            (byte) 0xCB, (byte) 0x96, (byte) 0xB0, (byte) 0xC8, (byte) 0x5E, (byte) 0x64, (byte) 0x00, (byte) 0x00,
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00,
            (byte) 0x00, (byte) 0x07, (byte) 0x0C, (byte) 0xB9, (byte) 0xC5, (byte) 0xDC, (byte) 0x00, (byte) 0x00,
            (byte) 0x03, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x01, (byte) 0x62, (byte) 0x00, (byte) 0x05,
            (byte) 0x40, (byte) 0x00, (byte) 0x15, (byte) 0xC0, (byte) 0x00, (byte) 0x7F, (byte) 0x00, (byte) 0x02,
            (byte) 0x64, (byte) 0x00, (byte) 0x10, (byte) 0xD0, (byte) 0x00, (byte) 0xFB, (byte) 0x80, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x25, (byte) 0x00, (byte) 0x10, (byte) 0xE2, (byte) 0xE0,
            (byte) 0x80, (byte) 0xB7, (byte) 0xCC, (byte) 0xA4, (byte) 0x7B, (byte) 0x8F, (byte) 0x0D, (byte) 0xFF,
            (byte) 0xFC, (byte) 0x6A, (byte) 0x4B, (byte) 0x85, (byte) 0x9D, (byte) 0x1B, (byte) 0x52, (byte) 0xCB,
            (byte) 0x96, (byte) 0xB0, (byte) 0xC8, (byte) 0x5E, (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x03,
            (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00,
            (byte) 0x07, (byte) 0x0C, (byte) 0xB9, (byte) 0xC5, (byte) 0xDC, (byte) 0x00, (byte) 0x00, (byte) 0x03,
            (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x01, (byte) 0x62, (byte) 0x00, (byte) 0x05, (byte) 0x40,
            (byte) 0x00, (byte) 0x15, (byte) 0xC0, (byte) 0x00, (byte) 0x7F, (byte) 0x00, (byte) 0x02, (byte) 0x64,
            (byte) 0x00, (byte) 0x10, (byte) 0xD0, (byte) 0x00, (byte) 0xFB, (byte) 0x80, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x25, (byte) 0x00, (byte) 0x16, (byte) 0x82, (byte) 0xE0, (byte) 0x80,
            (byte) 0xB7, (byte) 0xCC, (byte) 0xA4, (byte) 0x7B, (byte) 0x8F, (byte) 0x0D, (byte) 0xFF, (byte) 0xFC,
            (byte) 0x6A, (byte) 0x4B, (byte) 0x85, (byte) 0x9D, (byte) 0x1B, (byte) 0x52, (byte) 0xCB, (byte) 0x96,
            (byte) 0xB0, (byte) 0xC8, (byte) 0x5E, (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00,
            (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x07,
            (byte) 0x0C, (byte) 0xB9, (byte) 0xC5, (byte) 0xDC, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00,
            (byte) 0x82, (byte) 0x00, (byte) 0x01, (byte) 0x62, (byte) 0x00, (byte) 0x05, (byte) 0x40, (byte) 0x00,
            (byte) 0x15, (byte) 0xC0, (byte) 0x00, (byte) 0x7F, (byte) 0x00, (byte) 0x02, (byte) 0x64, (byte) 0x00,
            (byte) 0x10, (byte) 0xD0, (byte) 0x00, (byte) 0xFB, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x01, (byte) 0x09, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x09,
            (byte) 0x10};// First Drone 1280x720p Input Data

    private InetSocketAddress inetSocketAddress;
    private boolean isSocketConnected = false;
    private BlockingQueue<byte[]> blockingQueue;
    //private Handler handler;
    Thread sendThread = this;

    public HIFLYStreamingSocket(BlockingQueue<byte[]> blockingQueue, final Handler handler) {

        //this.handler = handler;
        this.blockingQueue = blockingQueue;
        Log.i("minmin","contructor run");
        initSocket();
    }

    public void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dsocket = new DatagramSocket();
                    inetSocketAddress = new InetSocketAddress(SERVER_ADDRESS, udpport);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }

    public void setUDPPort(int portNum) {
        udpport = portNum;
    }

    public void sendDummyByte() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("minmin","dummy send");
                    dsocket.send(new DatagramPacket(dummyByte, dummyByte.length, inetSocketAddress));
                    Thread.sleep(1000);
                    dsocket.send(new DatagramPacket(dummyByte, dummyByte.length, inetSocketAddress));
                    isSocketConnected = true;
                    Log.i("minmin","dummy finish");
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
        }).start();

    }


    public void run() {

        Log.i("minmin","streamming start");
        while (true) {
            try {
               // Log.i("minmin","conn: "+isSocketConnected);
                if (isSocketConnected) {
                    Log.i("minmin","send in");
                    byte videoData[] = blockingQueue.take();
                    Log.i("kkkkkk!", blockingQueue.size() + " " + videoData[0] + " " + videoData[1] + " " + videoData[2] + " " + videoData[3] + " " + videoData[4] + " " + videoData[5] + " " + videoData[6] + " " + videoData[7] + " " + videoData[39] + " " + videoData[40]);
                    dsocket.send(new DatagramPacket(videoData, 0, videoData.length, inetSocketAddress));
                    Log.i("minmin","send finish");
                }
            } catch (Exception e) {
                if (blockingQueue != null)
                    Log.i("kkkkk", "Queue is empty " + blockingQueue.size());
                e.printStackTrace();
            }
        }
    }


    public void endSocket() {
        try {
            dsocket.close();
            if (sendThread != null && sendThread.isAlive())
                sendThread.interrupt();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}