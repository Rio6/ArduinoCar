/*
 * Author: Rio
 * Date: 2017/04/23
 */

package net.rio.controller;

import java.util.List;
import java.util.HashMap;

public interface AppEventListener {
    void onConnectionChanged(String status);
    void onPeerChanged(List<String> peerNames);
    void onInfoChanged(HashMap<String, String> infoMap);
}
