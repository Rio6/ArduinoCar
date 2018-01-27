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

    private Object sendLock = new Object();
    private List<byte[]> cmdList = new ArrayList<>();

    private String host;
    private final int port = 5438;

    private Socket socket;

    private DataInputStream input;
    private DataOutputStream output;

    private AppEventListener eventListener;
    private OnReceiveListener recvListener;

    private Thread socketThread;
    private Thread sendThread;

    private Runnable sender = new Runnable() {
        @Override
        public void run() {
            Log.i(MainActivity.TAG, "Starting send thread");
            try {
                while(!Thread.interrupted()) {
                    synchronized(sendLock) {
                        sendLock.wait();
                    }

                    while(cmdList.size() > 0) {
                        byte[] cmd = cmdList.remove(0);

                        if(cmd != null) {
                            output.write(cmd);
                        }
                    }
                    output.flush();
                }
            } catch(InterruptedException e) {
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            }
            Log.i(MainActivity.TAG, "Send thread stopped");
        }
    };

    public RobotClient(OnReceiveListener recvListener, AppEventListener eventListener) {
        this.recvListener = recvListener;
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

            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            sendThread = new Thread(sender);
            sendThread.start();

recvLoop:
            while(!Thread.interrupted()) {
                int length = input.readInt();

                byte[] rst = new byte[length];
                byte[] buff = new byte[512];

                int read = 0;
                while(read < length) {
                    int toRead = length - read > 512 ? 512 : length - read;

                    int r = input.read(buff, 0, toRead);
                    if(r < 0) break recvLoop;

                    System.arraycopy(buff, 0, rst, read, r);

                    read += r;
                }

                recvListener.onReceive(rst);
            }
        } catch(IOException e) {
            Log.e(MainActivity.TAG, e.getMessage());
        } finally {
            sendThread.interrupt();
            try {
                if(input != null) input.close();
                if(output != null) output.close();
                if(socket != null) {
                    socket.close();
                    Log.i(MainActivity.TAG, "Disconnected");
                    if(eventListener != null)
                        eventListener.onConnectionChanged("Not connected");
                }
            } catch(IOException e) {
                Log.e(MainActivity.TAG, Log.getStackTraceString(e));
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
        try {
            if(input != null) input.close();
        } catch(IOException e) {
            Log.e(MainActivity.TAG, Log.getStackTraceString(e));
        }

        if(socketThread != null) {
            socketThread.interrupt();
            socketThread = null;
        }
    }

    public void send(byte[] cmd) {
        cmdList.add(cmd);
        synchronized(sendLock) {
            sendLock.notify();
        }
    }


    public interface OnReceiveListener {
        public void onReceive(byte[] data);
    }
}
