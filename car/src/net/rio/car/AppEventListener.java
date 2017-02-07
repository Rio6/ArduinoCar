package net.rio.car;

import java.util.HashMap;

public interface AppEventListener {
    void onInfoChanged(HashMap<String, String> infoMap);
}
