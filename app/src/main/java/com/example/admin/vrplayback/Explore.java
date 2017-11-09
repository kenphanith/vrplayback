package com.example.admin.vrplayback;

import android.app.LauncherActivity;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitmovin.player.DrmLicenseKeyExpiredException;
import com.bitmovin.player.IllegalOperationException;
import com.bitmovin.player.NoConnectionException;
import com.bitmovin.player.api.event.data.ErrorEvent;
import com.bitmovin.player.config.drm.WidevineConfiguration;
import com.bitmovin.player.config.media.SourceItem;
import com.bitmovin.player.offline.OfflineContentManager;
import com.bitmovin.player.offline.OfflineContentManagerListener;
import com.bitmovin.player.offline.OfflineSourceItem;
import com.bitmovin.player.offline.options.OfflineContentOptions;
import com.bitmovin.player.offline.options.OfflineOptionEntry;
import com.bitmovin.player.offline.options.OfflineOptionEntryAction;
import com.bitmovin.player.offline.options.OfflineOptionEntryState;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Explore extends Fragment implements OfflineContentManagerListener, ListItemActionListener, SelectedListener {
    private static final String TAG = Explore.class.getSimpleName();

    private File rootFolder;
    private List<ListItem> listItems;
    private ListView listView;
    private ListAdapter listAdapter;
    private Gson gson;

    private boolean retryOfflinePlayback = true;
    private ListItem listItemForRetry = null;

    public static ListItemActionListener listItemActionListener;
    public static SelectedListener selectedListener;
    private static ListItem customItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.explore, container, false);
        findView(view);
        return view;
    }

    protected void findView(View view){
        this.listView = (ListView) view.findViewById(R.id.listview);
    }

    @Override
    public void onStart() {
        super.onStart();
        Explore.listItemActionListener = this;
        Explore.selectedListener = this;
        this.gson = new Gson();
        this.rootFolder = getContext().getDir("offline", ContextWrapper.MODE_PRIVATE);

        this.listItems = getItems();
        this.listAdapter = new ListAdapter(getContext(), 0, this.listItems);
        this.listView.setAdapter(this.listAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                onListItemClicked((ListItem) parent.getItemAtPosition(position));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requestOfflineContentOptions(this.listItems);
    }

    @Override
    public void onStop() {
        for (ListItem listItem : this.listItems)
        {
            listItem.getOfflineContentManager().release();
        }
        this.gson = null;
        this.listItems = null;
        this.listAdapter = null;
        this.listView.setOnItemClickListener(null);
        super.onStop();
    }

    private void requestOfflineContentOptions(List<ListItem> listItems) {
        for (ListItem listItem : listItems)
        {
            // Request OfflineContentOptions from the OfflineContentManager.
            // Note that the getOptions call is asynchronous, and that the result will be delivered to the according listener method onOptionsAvailable
            listItem.getOfflineContentManager().getOptions();
        }
    }

    private void onListItemClicked(ListItem listItem) {
        playSource(listItem);
    }

    private void playSource(ListItem listItem) {
        SourceItem sourceItem = null;
        try
        {
            // First we try to get an OfflineSourceItem from the OfflineContentManager, as we prefer offline content
            sourceItem = listItem.getOfflineContentManager().getOfflineSourceItem();
        }
        catch (IOException e)
        {
            // If it fails to load needed files
        }
        catch (DrmLicenseKeyExpiredException e)
        {
            try
            {
                this.listItemForRetry = listItem;
                this.retryOfflinePlayback = true;
                listItem.getOfflineContentManager().renewOfflineLicense();
            }
            catch (NoConnectionException e1)
            {
                Toast.makeText(getContext(), "The DRM license expired, but there is no network connection", Toast.LENGTH_LONG).show();
            }
        }

        // If no offline content is available, or it fails to get an OfflineSourceItem, we take the original SourceItem for online streaming
        if (sourceItem == null)
        {
            sourceItem = listItem.getSourceItem();
        }
        Explore.customItem = listItem;
        startPlayerActivity(sourceItem);
    }

    private void startPlayerActivity(SourceItem sourceItem) {
        Intent playerActivityIntent = new Intent(getContext(), PlayerActivity.class);
        String extraName = sourceItem instanceof OfflineSourceItem ? PlayerActivity.OFFLINE_SOURCE_ITEM : PlayerActivity.SOURCE_ITEM;
        playerActivityIntent.putExtra(extraName, gson.toJson(sourceItem));
        startActivity(playerActivityIntent);
    }

    private List<ListItem> getItems() {
        List<ListItem> listItems = new ArrayList<>();
        // Get all objectKey from AWS S3 and loop it here

        for (int i=0; i<10; i++){
            SourceItem vr4k = new SourceItem("https://qnet-vr-dev.s3.amazonaws.com/output_4k_1027/dash/stream.mpd");
//            SourceItem vr4k = new SourceItem("https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd");
            vr4k.setTitle("VR 4K Video");

            OfflineContentManager vr4kOfflineContentManager = OfflineContentManager.getOfflineContentManager(vr4k, this.rootFolder.getPath(), "VR4K", this, getContext());
            ListItem vr4kItem = new ListItem(vr4k, vr4kOfflineContentManager);
            listItems.add(vr4kItem);
        }

        return listItems;
    }

    @Override
    public void onCompleted(SourceItem sourceItem, OfflineContentOptions offlineContentOptions) {
//        Toast.makeText(getContext(), "onCompleted", Toast.LENGTH_LONG).show();
        Log.d(Explore.class.getSimpleName(), "############################ onCompleted");
        ListItem listItem = getListItemWithSourceItem(sourceItem);
        if (listItem != null)
        {
            // Update the OfflineContentOptions, reset progress and notify the ListAdapter to update the views
            listItem.setOfflineContentOptions(offlineContentOptions);
            listItem.setProgress(0);
            this.listAdapter.notifyDataSetChanged();
        }
    }

    private ListItem getListItemWithSourceItem(SourceItem sourceItem) {
        // Find the matching SourceItem in the List, containing all our SourceItems
        for (ListItem listItem : this.listItems){
            if (listItem.getSourceItem() == sourceItem){
                return listItem;
            }
        }
        return null;
    }

    @Override
    public void onError(SourceItem sourceItem, ErrorEvent errorEvent) {
//        Toast.makeText(getContext(), errorEvent.getMessage(), Toast.LENGTH_SHORT).show();
        Log.d(Explore.class.getSimpleName(), "############################ " + errorEvent.getMessage());
    }

    @Override
    public void onProgress(SourceItem sourceItem, float progress) {
        Log.d(Explore.class.getSimpleName(), "############################ onProgress");
        ListItem listItem = getListItemWithSourceItem(sourceItem);
        if (listItem != null){
            float oldProgress = listItem.getProgress();
            listItem.setProgress(progress);

            // Only show full progress changes
            if ((int) oldProgress != (int) progress){
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onOptionsAvailable(SourceItem sourceItem, OfflineContentOptions offlineContentOptions) {
        Log.d(Explore.class.getSimpleName(), "############################ onOptionAvailable");
        ListItem listItem = getListItemWithSourceItem(sourceItem);
        if (listItem != null){
            // Update the OfflineContentOptions and notify the ListAdapter to update the views
            listItem.setOfflineContentOptions(offlineContentOptions);
            this.listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDrmLicenseUpdated(SourceItem sourceItem) {
        Log.d(Explore.class.getSimpleName(), "############################ onDrmLicenseUpdated");
        if (this.retryOfflinePlayback){
            if (this.listItemForRetry.getSourceItem() == sourceItem)
            {
                // At the last try, the license was expired
                // so we try it now again
                ListItem listItem = this.listItemForRetry;
                this.retryOfflinePlayback = false;
                this.listItemForRetry = null;
                playSource(listItem);
            }
        }
    }

    @Override
    public void showSelectionDialog() {
        PlayerActivity.requestListener.popUpDialog(Explore.customItem);
    }

    public void download(ListItem listItem, OfflineContentManager offlineContentManager) {
        if (offlineContentManager != null){
            try {
                offlineContentManager.process(listItem.getOfflineContentOptions());
            } catch (NoConnectionException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(getContext(), "Start Downloading...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void delete(ListItem listItem) {
        listItem.getOfflineContentManager().deleteAll();
        Toast.makeText(getContext(), "Deleting " + listItem.getSourceItem().getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOptionSeletedListener(ListItem listItem, OfflineContentManager offlineContentManager) {
        download(listItem, offlineContentManager);
    }
}
