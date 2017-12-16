/*
 * Author: Rio
 * Date: 2017/12/16
 */

package net.rio.controller;

import net.rio.wifi.RobotClient;

public class RobotMovement implements ControlView.OnMoveListener {

    private final static int THRESHOLD = 30;
    private RobotClient robotClient;

    private int lastL, lastR;

    public RobotMovement(RobotClient robotClient) {
        this.robotClient = robotClient;
    }

    @Override
    public void onMove(float x, float y) {
        int left = (int) ((y + x) * 200);
        int right = (int) ((y - x) * 200);

        if(left == 0 || Math.abs(lastL - left) > THRESHOLD) {
            if(left > 0) {
                robotClient.send(new byte[]{'>', 'M', 0, (byte) left, 2, '<'});
                robotClient.send(new byte[]{'>', 'M', 1, (byte) left, 2, '<'});
            } else if(left < 0) {
                robotClient.send(new byte[]{'>', 'M', 0, (byte) -left, 1, '<'});
                robotClient.send(new byte[]{'>', 'M', 1, (byte) -left, 1, '<'});
            } else {
                robotClient.send(new byte[]{'>', 'M', 0, 0, 4, '<'});
                robotClient.send(new byte[]{'>', 'M', 1, 0, 4, '<'});
            }
            lastL = left;
        }
        if(right == 0 || Math.abs(lastR - right) > THRESHOLD) {
            if(right > 0) {
                robotClient.send(new byte[]{'>', 'M', 2, (byte) right, 2, '<'});
                robotClient.send(new byte[]{'>', 'M', 3, (byte) right, 2, '<'});
            } else if(right < 0) {
                robotClient.send(new byte[]{'>', 'M', 2, (byte) -right, 1, '<'});
                robotClient.send(new byte[]{'>', 'M', 3, (byte) -right, 1, '<'});
            } else {
                robotClient.send(new byte[]{'>', 'M', 2, 0, 4, '<'});
                robotClient.send(new byte[]{'>', 'M', 3, 0, 4, '<'});
            }
            lastR = right;
        }
    }
}
