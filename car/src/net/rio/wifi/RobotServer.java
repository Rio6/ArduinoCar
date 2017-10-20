/*
 * Author: Rio
 * Date: 2017/02/21
 */

package net.rio.wifi;

import android.util.Log;
import java.io.*; // IOException, DataInputStream
import java.net.*; // ServerSocket, Socket, BindException
import java.util.HashMap;

import net.rio.car.MainActivity;
import net.rio.car.AppEventListener;
import net.rio.usb.UsbController;

public class RobotServer implements Runnable {

    private static final int LISTEN_PORT = 5438;

    private Socket client;
    private UsbController usb;
    private AppEventListener eventListener;

    private DataInputStream input;

    private Thread srvThread;

    public RobotServer(UsbController usb, AppEventListener eventListener) {
        this.usb = usb;
        this.eventListener = eventListener;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try(ServerSocket server = new ServerSocket(LISTEN_PORT)) {
                Log.i(MainActivity.TAG, "Waiting connection");

                if(eventListener != null)
                    eventListener.onClientConnected(null);

                client = server.accept();
                Log.i(MainActivity.TAG, "Connection established");

                if(eventListener != null)
                    eventListener.onClientConnected(client.getRemoteSocketAddress().toString());

                input = new DataInputStream(client.getInputStream());

                byte[] buff = new byte[1];
                while(input.read(buff) > 0 && !Thread.interrupted()) {
                    usb.send(buff);
                }

            } catch(BindException e) {
                Log.e(MainActivity.TAG, Log.getStackTraceString(e));
                return;
            } catch (IOException e) {
                Log.i(MainActivity.TAG, e.getMessage());
            } finally {
                try {
                    if(input != null)
                        input.close();
                    if(client != null)
                        client.close();
                    eventListener.onClientConnected(null);
                } catch(IOException e) {
                    Log.e(MainActivity.TAG, Log.getStackTraceString(e));
                }
            }
        }
    }

    public void startServer() {
        Log.i(MainActivity.TAG, "Starting server");

        srvThread = new Thread(this);
        srvThread.start();
    }

    public void stopServer() {

        Log.i(MainActivity.TAG, "Stopping server");

        try {

            srvThread.interrupt();
            if(client != null)
                client.close();

        } catch(IOException e) {
            Log.e(MainActivity.TAG, Log.getStackTraceString(e));
        }
    }
}
