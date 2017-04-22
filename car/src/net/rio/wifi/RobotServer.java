/*
 * Author: Rio
 * Date: 2017/02/21
 */

package net.rio.wifi;

import android.util.Log;
import java.io.*; // IOException, DataInputStream
import java.net.*; // ServerSocket, Socket, BindException

import net.rio.car.MainActivity;
import net.rio.usb.UsbController;

public class RobotServer implements Runnable {

    private static final int LISTEN_PORT = 5438;

    private ServerSocket server;
    private UsbController usb;

    private DataInputStream iStream;

    private Thread srvThread;

    public RobotServer(UsbController usb) {
        this.usb = usb;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try(ServerSocket server = new ServerSocket(LISTEN_PORT)) {
                this.server = server;
                Log.i(MainActivity.TAG, "Waiting connection");
                Socket client = server.accept();
                Log.i(MainActivity.TAG, "Connection established");

                iStream = new DataInputStream(client.getInputStream());

                byte[] buff = new byte[1];
                while(iStream.read(buff) > 0 && !Thread.interrupted()) {
                    usb.send(buff);
                }

            } catch(BindException e) {
                Log.e(MainActivity.TAG, Log.getStackTraceString(e));
                return;
            } catch (IOException e) {
                Log.i(MainActivity.TAG, e.getMessage());
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
            if(iStream != null)
                iStream.close();
            if(server != null)
                server.close();

            srvThread.interrupt();

        } catch(IOException e) {
            Log.e(MainActivity.TAG, Log.getStackTraceString(e));
        }
    }
}
