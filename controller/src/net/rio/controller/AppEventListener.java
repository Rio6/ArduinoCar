package net.rio.controller;

import java.util.List;
import java.util.HashMap;

public interface AppEventListener {
    void onPeerChanged(List<String> peerNames);
    void onInfoChanged(HashMap<String, String> infoMap);
}
