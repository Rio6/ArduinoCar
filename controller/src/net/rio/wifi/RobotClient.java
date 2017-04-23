/*
 * Author: Rio
 * Date: 2017/04/21
 */

package net.rio.wifi;

import android.util.Log;
import java.io.*; // IOException, DataOutputStream
import java.net.*; // Socket
import java.util.ArrayList;
import java.util.List;

import net.rio.controller.MainActivity;

public class RobotClient implements Runnable {

    private Thread sendThread;
    private Object sendLock = new Object();
    private List<byte[]> cmdList = new ArrayList<>();

    private String host;
    private final int port = 5438;

    private Socket socket;
    private DataOutputStream oStream;

    @Override
    public void run() {

        if(host == null) return;

        Log.i(MainActivity.TAG, "Connecting to " + host);

        try {
            socket = new Socket();
            socket.bind(null);
            socket.connect(new InetSocketAddress(host, port), 500);

            Log.i(MainActivity.TAG, "Connected");

            oStream = new DataOutputStream(socket.getOutputStream());

            while(!Thread.interrupted()) {
                synchronized(sendLock) {
                    sendLock.wait();
                }

                while(cmdList.size() > 0) {
                    byte[] cmd = cmdList.remove(0);

                    for(byte c : cmd) {
                        oStream.write(c);
                    }
                }
                oStream.flush();
            }

        } catch(InterruptedException e) {
            Log.i(MainActivity.TAG, "Send thread interrupted");
        } catch(IOException e) {
            Log.e(MainActivity.TAG, e.getMessage());
        } finally {
            if(socket != null) {
                Log.i(MainActivity.TAG, "Disconnecting");
                try {
                    socket.close();
                } catch(IOException e) {
                    Log.e(MainActivity.TAG, e.getMessage());
                }
                Log.i(MainActivity.TAG, "Disconnected");
            }
        }
    }

    public void connect(String host) {
        disconnect();
        this.host = host;
        sendThread = new Thread(this);
        sendThread.start();
    }

    public void disconnect() {
        if(sendThread != null)
            sendThread.interrupt();
    }

    public void send(byte[] cmd) {
        cmdList.add(cmd);
        synchronized(sendLock) {
            sendLock.notify();
        }
    }
}
