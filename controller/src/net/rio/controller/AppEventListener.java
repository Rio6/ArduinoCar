package net.rio.controller;

import java.util.List;

public interface AppEventListener {
    void onPeerChanged(List<String> peerNames);
}
