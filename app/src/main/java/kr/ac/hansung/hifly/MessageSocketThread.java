package kr.ac.hansung.hifly;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by hscom-014 on 2017-10-08.
 */

public class MessageSocketThread extends Thread {

    final String SERVER_ADDRESS = "192.168.0.10";
    final int SERVER_PORT = 10123;

    private Handler handler = null;
    private BlockingQueue queue = null;
    private Socket socket = null;
    private DataOutputStream dataoutputStream = null;
    private DataInputStream datainputStream = null;

    private HIFLYStreamingSocket streamingSocket = null;

    public MessageSocketThread(BlockingQueue queue, Handler handler) {
        try {
            this.handler = handler;
            this.queue = queue;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            dataoutputStream = new DataOutputStream(socket.getOutputStream());
            datainputStream = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                String dataString = datainputStream.readUTF();
                Log.i("minmin",dataString.toString());
                if (dataString.equals("FFMPEG_START!")) {

                    streamingSocket.start();
                    streamingSocket.sendDummyByte();

                } else if (dataString.equals("RoomClose")) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("stop", 1);
                    Message msg = new Message();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    streamingSocket.endSocket();
                } else if (dataString.contains("port:")) {

                        String str = dataString.substring(5);
                        int udpport = Integer.valueOf(str);
                        streamingSocket = new HIFLYStreamingSocket(queue, handler);
                        Log.i("kkkkk","port: "+udpport);
                        if (streamingSocket != null) {
                            streamingSocket.setUDPPort(udpport);
                    }
                }
            } catch (Exception e) {

                Log.i("kkkkk", "error");
                e.getStackTrace();
                e.printStackTrace();
                return;
            }
        }
    }

    public void sendData(String msg) {
        try {

            Log.e("jina","send data trying");
            if (dataoutputStream != null && socket != null && socket.isConnected())
                dataoutputStream.writeUTF(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
