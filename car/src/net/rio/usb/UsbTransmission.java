/*
 * Author: Rio
 * Date: 2017/02/21
 */

package net.rio.usb;

import android.hardware.usb.*;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.Arrays;

import net.rio.car.MainActivity;

class UsbTransmission {

    private UsbManager manager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbInterface iface;

    private UsbEndpoint epOut;
    private UsbEndpoint epIn;
    private UsbRequest reqOut = new UsbRequest();

    private Object sendLock = new Object();

    private Thread recvThread;

    private Runnable recvLoop = new Runnable() {
        @Override
        public void run() {
            Log.i(MainActivity.TAG, "Receive thread started");
            while(!Thread.interrupted()) {
                byte[] buff = new byte[1];
                int read = connection.bulkTransfer(epIn, buff, buff.length, 20);
                if(read > 0) {
                    Log.d(MainActivity.TAG, "Received: " + buff[0]);
                }
            }
            Log.i(MainActivity.TAG, "Receive thread stopped");
        }
    };

    UsbTransmission(UsbManager manager, UsbDevice device) {
        this.manager = manager;
        this.device = device;

        connection = manager.openDevice(device);

        if(connection == null) {
            Log.e(MainActivity.TAG, "Failed to open device");
            return;
        }

        int ifaceCnt = device.getInterfaceCount();
        Log.d(MainActivity.TAG, "Found " + ifaceCnt + " interfaces");

        iface = device.getInterface(ifaceCnt - 1);

        if (!connection.claimInterface(iface, true)) {
            Log.e(MainActivity.TAG, "Failed to claim interface");
            return;
        }

        // Arduino serial usb converter setup
        connection.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
        connection.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
            0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0); // 9600, 8N1

        // For Rev. 1
        /*
        connection.controlTransfer(0x40, 0, 0, 0, null, 0, 0); // reset
        connection.controlTransfer(0x40, 0, 1, 0, null, 0, 0); // clear Rx
        connection.controlTransfer(0x40, 0, 2, 0, null, 0, 0); // clear Tx
        connection.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0); // 9600, 8n1
        */

        for (int i = 0; i < iface.getEndpointCount(); i++) {
            if (iface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (iface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    epIn = iface.getEndpoint(i);
                } else {
                    epOut = iface.getEndpoint(i);
                }
            }
        }

        reqOut.initialize(connection, epOut);

        recvThread = new Thread(recvLoop);
        recvThread.start();

    }

    void stop() {

        Log.i(MainActivity.TAG, "Stopping");

        // Stop threads
        recvThread.interrupt();

        // Cancel requests
        reqOut.cancel();

        // Close connection
        Log.i(MainActivity.TAG, "Closing connection");

        connection.releaseInterface(iface);
        connection.close();
    }

    void send(byte[] buff) {
        if(reqOut == null)
            return;

        Log.d(MainActivity.TAG, "Sending: " + Arrays.toString(buff));
        reqOut.queue(ByteBuffer.wrap(buff), buff.length);
    }
}
