/*
 * Author: Rio
 * Date: 2017/02/21
 */

package net.rio.car;

import java.util.HashMap;
import java.util.List;

public interface AppEventListener {
    void onInfoChanged(HashMap<String, String> infoMap);
    void onPeerChanged(List<String> peerNames);
    void onClientConnected(String addr);
}
