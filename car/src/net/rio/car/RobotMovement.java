/*
 * Author: Rio
 * Date: 2017/12/16
 */

package net.rio.car;

import net.rio.usb.UsbController;
import net.rio.wifi.RobotServer;

public class RobotMovement implements RobotServer.OnReceiveListener {

    private final static int THRESHOLD = 40;
    private UsbController usb;

    private MotorCommand lastL, lastR;

    public RobotMovement(UsbController usb) {
        this.usb = usb;
    }

    @Override
    public void onReceive(byte[] data) {
        if(data.length > 1) {

            MotorCommand left, right;

            if(data[0] > -THRESHOLD && data[0] < THRESHOLD) {
                if(data[1] > -THRESHOLD && data[1] < THRESHOLD) {
                    left = right = MotorCommand.RELEASE;
                } else {
                    left = right = data[1] < 0 ? MotorCommand.FORWARD : MotorCommand.BACKWARD;
                }
            } else {
                if(data[1] > -THRESHOLD && data[1] < THRESHOLD) {
                    left = data[0] > 0 ? MotorCommand.FORWARD : MotorCommand.BACKWARD;
                    right = data[0] < 0 ? MotorCommand.FORWARD : MotorCommand.BACKWARD;
                } else {
                    if(data[0] > 0) {
                        left = data[1] < 0 ? MotorCommand.FORWARD : MotorCommand.BACKWARD;
                        right = MotorCommand.RELEASE;
                    } else {
                        left = MotorCommand.RELEASE;
                        right = data[1] < 0 ? MotorCommand.FORWARD : MotorCommand.BACKWARD;
                    }
                }
            }

            if(left != lastL) {
                usb.send(new byte[]{'>', 'M', 0, (byte) 255, left.cmd, '<'});
                usb.send(new byte[]{'>', 'M', 1, (byte) 255, left.cmd, '<'});
                lastL = left;
            }
            if(right != lastR) {
                usb.send(new byte[]{'>', 'M', 2, (byte) 255, right.cmd, '<'});
                usb.send(new byte[]{'>', 'M', 3, (byte) 255, right.cmd, '<'});
                lastR = right;
            }
        }
    }


    public enum MotorCommand {
        FORWARD(1),
        BACKWARD(2),
        BRAKE(3),
        RELEASE(4);

        public final byte cmd;

        private MotorCommand(int cmd) {
            this.cmd = (byte) cmd;
        }
    }
}
