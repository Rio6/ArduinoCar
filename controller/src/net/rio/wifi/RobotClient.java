/*
 * Author: Rio
 * Date: 2017/04/23
 */

package net.rio.wifi;

import android.util.Log;
import java.io.*; // IOException, DataOutputStream
import java.net.*; // Socket
import java.util.ArrayList;
import java.util.List;

import net.rio.controller.MainActivity;
import net.rio.controller.AppEventListener;

public class RobotClient implements Runnable {

    private Thread sendThread;
    private Object sendLock = new Object();
    private List<byte[]> cmdList = new ArrayList<>();

    private String host;
    private final int port = 5438;

    private Socket socket;
    private DataOutputStream output;

    private AppEventListener eventListener;

    public RobotClient(AppEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void run() {

        if(host == null) return;

        Log.i(MainActivity.TAG, "Connecting to " + host);

        try {
            socket = new Socket(host, port);

            Log.i(MainActivity.TAG, "Connected");
            if(eventListener != null)
                eventListener.onConnectionChanged("Connected");

            output = new DataOutputStream(socket.getOutputStream());

            while(!Thread.interrupted()) {
                synchronized(sendLock) {
                    sendLock.wait();
                }

                while(cmdList.size() > 0) {
                    byte[] cmd = cmdList.remove(0);

                    if(cmd != null) {
                        for(byte c : cmd) {
                            output.write(c);
                        }
                    }
                }
                output.flush();
            }

        } catch(InterruptedException e) {
            Log.i(MainActivity.TAG, "Send thread interrupted");
        } catch(IOException e) {
            Log.e(MainActivity.TAG, e.getMessage());
        } finally {
            try {
                if(output != null)
                    output.close();
                if(socket != null) {
                    socket.close();
                    Log.i(MainActivity.TAG, "Disconnected");
                    if(eventListener != null)
                        eventListener.onConnectionChanged("Not connected");
                }
            } catch(IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
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
