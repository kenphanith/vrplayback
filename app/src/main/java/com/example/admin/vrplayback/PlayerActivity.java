package com.example.admin.vrplayback;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bitmovin.player.BitmovinPlayer;
import com.bitmovin.player.BitmovinPlayerView;
import com.bitmovin.player.IllegalOperationException;
import com.bitmovin.player.NoConnectionException;
import com.bitmovin.player.config.media.SourceConfiguration;
import com.bitmovin.player.config.media.SourceItem;
import com.bitmovin.player.offline.OfflineContentManager;
import com.bitmovin.player.offline.OfflineSourceItem;
import com.bitmovin.player.offline.options.OfflineContentOptions;
import com.bitmovin.player.offline.options.OfflineOptionEntry;
import com.bitmovin.player.offline.options.OfflineOptionEntryAction;
import com.bitmovin.player.offline.options.OfflineOptionEntryState;
import com.google.gson.Gson;

import java.util.List;

public class PlayerActivity extends AppCompatActivity implements RequestListener {

    public static final String SOURCE_ITEM = "SOURCE_ITEM";
    public static final String OFFLINE_SOURCE_ITEM = "OFFLINE_SOURCE_ITEM";

    private BitmovinPlayerView bitmovinPlayerView;
    private BitmovinPlayer bitmovinPlayer;
    private Gson gson = new Gson();
    private ListItem listItem;

    public static RequestListener requestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if(Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor));
        }

        PlayerActivity.requestListener = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Player");
        setSupportActionBar(toolbar);

        SourceItem sourceItem;
        if (getIntent().hasExtra(SOURCE_ITEM)) {
            sourceItem = this.gson.fromJson(getIntent().getStringExtra(SOURCE_ITEM), SourceItem.class);
        }else if (getIntent().hasExtra(OFFLINE_SOURCE_ITEM)) {
            sourceItem = this.gson.fromJson(getIntent().getStringExtra(OFFLINE_SOURCE_ITEM), OfflineSourceItem.class);
        }else {
            finish();
            return;
        }

        this.bitmovinPlayerView = (BitmovinPlayerView) this.findViewById(R.id.bitmovinPlayerView);
        this.bitmovinPlayer = this.bitmovinPlayerView.getPlayer();

        this.initializePlayer(sourceItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_download){
//            Toast.makeText(this, "Start Downloading...", Toast.LENGTH_LONG).show();
            Explore.listItemActionListener.showSelectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.bitmovinPlayerView.onResume();
    }

    @Override
    protected void onPause()
    {
        this.bitmovinPlayerView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        this.bitmovinPlayerView.onDestroy();
        super.onDestroy();
    }

    protected void initializePlayer(SourceItem sourceItem)
    {
        // Create a new source configuration
        SourceConfiguration sourceConfiguration = new SourceConfiguration();

        // Add source item to source configuration
        sourceConfiguration.addSourceItem(sourceItem);

        // load source using the created source configuration
        this.bitmovinPlayer.load(sourceConfiguration);
    }

    @Override
    public void popUpDialog(final ListItem listItem) {
        OfflineContentOptions offlineContentOptions = listItem.getOfflineContentOptions();
        // Generating the needed lists, to create an AlertDialog, listing all options
        final List<OfflineOptionEntry> entries = Util.getAsOneList(offlineContentOptions);
        String[] entriesAsText = new String[entries.size()];
        boolean[] entriesCheckList = new boolean[entries.size()];
        for (int i = 0; i < entriesAsText.length; i++)
        {
            OfflineOptionEntry oh = entries.get(i);
            try
            {
                // Resetting the Action if set
                oh.setAction(null);
            }
            catch (IllegalOperationException e)
            {
                // Won't happen
            }
            entriesAsText[i] = oh.getId() + "-" + oh.getMimeType();
            entriesCheckList[i] = oh.getState() == OfflineOptionEntryState.DOWNLOADED || oh.getAction() == OfflineOptionEntryAction.DOWNLOAD;
        }

        // Building and showing the AlertDialog
        new AlertDialog.Builder(this).setMultiChoiceItems(entriesAsText, entriesCheckList, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                try
                {
                    // Set an Download/Delete action, if the user changes the checked state
                    OfflineOptionEntry offlineOptionEntry = entries.get(which);
                    offlineOptionEntry.setAction(isChecked ? OfflineOptionEntryAction.DOWNLOAD : OfflineOptionEntryAction.DELETE);
                }
                catch (IllegalOperationException e)
                {
                }
            }
        }).setPositiveButton(
                "Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OfflineContentManager offlineContentManager = listItem.getOfflineContentManager();
                        Explore.selectedListener.onOptionSeletedListener(listItem, offlineContentManager);
                    }
        }).setNegativeButton("Cancel", null).show();
    }
}
