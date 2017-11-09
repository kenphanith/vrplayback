package com.example.admin.vrplayback;

import com.bitmovin.player.offline.OfflineContentManager;

public interface SelectedListener {
    void onOptionSeletedListener(ListItem listItem, OfflineContentManager offlineContentManager);
}
