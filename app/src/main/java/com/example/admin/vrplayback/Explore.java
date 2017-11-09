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

public class Explore extends Fragment implements OfflineContentManagerListener, ListItemActionListener {
    private static final String TAG = Explore.class.getSimpleName();

    private File rootFolder;
    private List<ListItem> listItems;
    private ListView listView;
    private ListAdapter listAdapter;
    private Gson gson;

    private boolean retryOfflinePlayback = true;
    private ListItem listItemForRetry = null;

    public static ListItemActionListener listItemActionListener;
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
        Explore.listItemActionListener = this;
        this.gson = new Gson();
        this.rootFolder = getContext().getDir("offline", ContextWrapper.MODE_PRIVATE);

        this.listItems = getListItems();
        this.listAdapter = new ListAdapter(getContext(), 0, this.listItems, this);
        this.listView.setAdapter(this.listAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                onListItemClicked((ListItem) parent.getItemAtPosition(position));
            }
        });
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
        startPlayerActivity(sourceItem);
//        this.showSelectionDialog(listItem);
    }

    private void startPlayerActivity(SourceItem sourceItem) {
        Intent playerActivityIntent = new Intent(getContext(), PlayerActivity.class);
        String extraName = sourceItem instanceof OfflineSourceItem ? PlayerActivity.OFFLINE_SOURCE_ITEM : PlayerActivity.SOURCE_ITEM;
        playerActivityIntent.putExtra(extraName, gson.toJson(sourceItem));
//        startActivity(playerActivityIntent);
    }

    private List<ListItem> getListItems() {
//        List<ListItem> listItems = new ArrayList<>();
//        // Get all objectKey from AWS S3 and loop it here
//
//        for (int i=0; i<10; i++){
//            SourceItem vr4k = new SourceItem("https://qnet-vr-dev.s3.amazonaws.com/output_4k_1027/dash/stream.mpd");
//            vr4k.setTitle("VR 4K Video");
//
//            OfflineContentManager vr4kOfflineContentManager = OfflineContentManager.getOfflineContentManager(vr4k, this.rootFolder.getPath(), "VR4K", this, getContext());
//            ListItem vr4kItem = new ListItem(vr4k, vr4kOfflineContentManager);
//            listItems.add(vr4kItem);
//        }
//
//        return listItems;
        List<ListItem> listItems = new ArrayList<>();

        // Initialize a SourceItem
        SourceItem artOfMotion = new SourceItem("https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd");
        artOfMotion.setTitle("Art of Motion");

        // Initialize an OfflineContentManager in the rootFolder with the id "artOfMotion"
        OfflineContentManager artOfMotionOfflineContentManager = OfflineContentManager.getOfflineContentManager(artOfMotion, this.rootFolder.getPath(), "artOfMotion", this, getContext());

        // Create a ListItem from the SourceItem and the OfflienContentManager
        ListItem artOfMotionListItem = new ListItem(artOfMotion, artOfMotionOfflineContentManager);

        // Add the ListItem to the List
        listItems.add(artOfMotionListItem);

        // Initialize a SourceItem with a DRM configuration
        SourceItem artOfMotionDrm = new SourceItem("https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd");
        artOfMotionDrm.addDRMConfiguration(new WidevineConfiguration("http://widevine-proxy.appspot.com/proxy"));
        artOfMotionDrm.setTitle("Art of Motion with DRM");

        // Initialize an OfflineContentManager in the rootFolder with the id "artOfMotionDrm"
        OfflineContentManager artOfMotionDrmOfflineContentManager = OfflineContentManager.getOfflineContentManager(artOfMotionDrm, this.rootFolder.getPath(), "artOfMotionDrm", this, getContext());

        // Create a ListItem from the SourceItem and the OfflienContentManager
        ListItem artOfMotionDrmListItem = new ListItem(artOfMotionDrm, artOfMotionDrmOfflineContentManager);

        // Add the ListItem to the List
        listItems.add(artOfMotionDrmListItem);


        return listItems;
    }

    @Override
    public void onCompleted(SourceItem sourceItem, OfflineContentOptions offlineContentOptions) {
        Toast.makeText(getContext(), "onCompleted", Toast.LENGTH_LONG).show();
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
        Toast.makeText(getContext(), errorEvent.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgress(SourceItem sourceItem, float progress) {
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
        ListItem listItem = getListItemWithSourceItem(sourceItem);
        if (listItem != null){
            // Update the OfflineContentOptions and notify the ListAdapter to update the views
            listItem.setOfflineContentOptions(offlineContentOptions);
            this.listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDrmLicenseUpdated(SourceItem sourceItem) {
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
    public void showSelectionDialog(ListItem listItem) {
        OfflineContentOptions offlineContentOptions = listItem.getOfflineContentOptions();
        if (listItem.getOfflineContentOptions() != null){
            // Generating the needed lists, to create an AlertDialog, listing all options
            List<OfflineOptionEntry> entries = Util.getAsOneList(offlineContentOptions);
            String[] entriesAsText = new String[entries.size()];
            boolean[] entriesCheckList = new boolean[entries.size()];
            for (int i = 0; i < entriesAsText.length; i++){
                OfflineOptionEntry oh = entries.get(i);
                try{
                    // Resetting the Action if set
                    oh.setAction(null);
                }
                catch (IllegalOperationException e){
                    // Won't happen
                }
                entriesAsText[i] = oh.getId() + "-" + oh.getMimeType();
                entriesCheckList[i] = oh.getState() == OfflineOptionEntryState.DOWNLOADED || oh.getAction() == OfflineOptionEntryAction.DOWNLOAD;
            }

            // Building and showing the AlertDialog
            AlertDialog.Builder dialogBuilder = generateAlertDialogBuilder(listItem, entries, entriesAsText, entriesCheckList);
            dialogBuilder.show();
        }else {
            Toast.makeText(getContext(), "getOfflineContentOption null", Toast.LENGTH_LONG).show();
        }
    }

    private AlertDialog.Builder generateAlertDialogBuilder(final ListItem listItem, final List<OfflineOptionEntry> entries, String[] entriesAsText, boolean[] entriesCheckList) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext()).setMultiChoiceItems(entriesAsText, entriesCheckList, new DialogInterface.OnMultiChoiceClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked)
            {
                try{
                    // Set an Download/Delete action, if the user changes the checked state
                    OfflineOptionEntry offlineOptionEntry = entries.get(which);
                    offlineOptionEntry.setAction(isChecked ? OfflineOptionEntryAction.DOWNLOAD : OfflineOptionEntryAction.DELETE);
                }
                catch (IllegalOperationException e){
                    e.printStackTrace();
                }

            }
        });
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                download(listItem);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        return dialogBuilder;
    }

    private void download(ListItem listItem) {
        OfflineContentManager offlineContentManager = listItem.getOfflineContentManager();
        if (offlineContentManager == null){
            return;
        }

        try{
            // Passing the OfflineContentOptions with set OfflineOptionEntryActions to the OfflineContentManager
            offlineContentManager.process(listItem.getOfflineContentOptions());
        }
        catch (NoConnectionException e){
            e.printStackTrace();
        }
    }

    @Override
    public void delete(ListItem listItem) {
        Toast.makeText(getContext(), "delete", Toast.LENGTH_LONG).show();
    }
}
