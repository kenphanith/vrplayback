package com.example.admin.vrplayback;

import com.bitmovin.player.config.media.SourceItem;
import com.bitmovin.player.offline.OfflineContentManager;
import com.bitmovin.player.offline.options.OfflineContentOptions;

/**
 * A class representing a ListItem
 */
class ListItem
{
    private SourceItem sourceItem;
    private OfflineContentManager offlineContentManager;
    private OfflineContentOptions offlineContentOptions;
    private float progress;

    public ListItem(SourceItem sourceItem, OfflineContentManager offlineContentManager)
    {
        this.sourceItem = sourceItem;
        this.offlineContentManager = offlineContentManager;
    }

    public SourceItem getSourceItem()
    {
        return this.sourceItem;
    }

    public OfflineContentManager getOfflineContentManager()
    {
        return this.offlineContentManager;
    }

    public OfflineContentOptions getOfflineContentOptions()
    {
        return this.offlineContentOptions;
    }

    public float getProgress()
    {
        return this.progress;
    }

    public void setOfflineContentOptions(OfflineContentOptions offlineContentOptions)
    {
        this.offlineContentOptions = offlineContentOptions;
    }

    public void setProgress(float progress)
    {
        this.progress = progress;
    }
}
