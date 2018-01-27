/*
 * Author: Rio
 * Date: 2017/12/16
 */

package net.rio.wifi;

import android.util.Log;
import java.io.*; // IOException, DataInputStream, DataOutputStream
import java.net.*; // ServerSocket, Socket, BindException
import java.util.HashMap;

import net.rio.car.MainActivity;
import net.rio.car.AppEventListener;

public class RobotServer implements Runnable {

    private static final int LISTEN_PORT = 5438;

    private ServerSocket server;
    private Socket client;
    private OnReceiveListener recvListener;
    private AppEventListener eventListener;

    private Object sendLock = new Object();
    private byte[] data;

    private DataInputStream input;
    private DataOutputStream output;

    private Thread srvThread;
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

                    if(data.length > 0) {
                        output.writeInt(data.length);
                        output.write(data);
                        output.flush();
                    }
                }
            } catch(InterruptedException e) {
            } catch(IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            }
            Log.i(MainActivity.TAG, "Send thread stopped");
        }
    };

    public RobotServer(OnReceiveListener recvListener, AppEventListener eventListener) {
        this.recvListener = recvListener;
        this.eventListener = eventListener;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                Log.i(MainActivity.TAG, "Waiting connection");

                if(eventListener != null)
                    eventListener.onClientConnected(null);

                client = server.accept();
                Log.i(MainActivity.TAG, "Connection established");

                if(eventListener != null)
                    eventListener.onClientConnected(client.getRemoteSocketAddress().toString());

                input = new DataInputStream(client.getInputStream());
                output = new DataOutputStream(client.getOutputStream());

                sendThread = new Thread(sender);
                sendThread.start();

                byte[] buff = new byte[2];
                while(input.read(buff) > 0 && !Thread.interrupted()) {
                    recvListener.onReceive(buff);
                }

            } catch(BindException e) {
                Log.e(MainActivity.TAG, Log.getStackTraceString(e));
                return;
            } catch (IOException e) {
                Log.i(MainActivity.TAG, e.getMessage());
            } finally {
                if(sendThread != null) sendThread.interrupt();
                try {
                    if(input != null) input.close();
                    if(output != null) output.close();
                    if(client != null) client.close();

                    eventListener.onClientConnected(null);
                } catch(IOException e) {
                    Log.e(MainActivity.TAG, Log.getStackTraceString(e));
                }
            }
        }
    }

    public void startServer() {
        Log.i(MainActivity.TAG, "Starting server");

        try {
            server = new ServerSocket(LISTEN_PORT);
        } catch(IOException e) {
            Log.w(MainActivity.TAG, Log.getStackTraceString(e));
        }

        srvThread = new Thread(this);
        srvThread.start();
    }

    public void stopServer() {

        Log.i(MainActivity.TAG, "Stopping server");

        if(srvThread != null) {
            srvThread.interrupt();
            srvThread = null;
        }

        try {
            if(client != null) client.close();
            if(server != null) server.close();
        } catch(IOException e) {
            Log.e(MainActivity.TAG, Log.getStackTraceString(e));
        }
    }

    public void send(byte[] data) {
        this.data = data;
        synchronized(sendLock) {
            sendLock.notify();
        }
    }


    public interface OnReceiveListener {
        public void onReceive(byte[] data);
    }
}
