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
import com.bitmovin.player.config.media.SourceConfiguration;
import com.bitmovin.player.config.media.SourceItem;
import com.bitmovin.player.offline.OfflineSourceItem;
import com.bitmovin.player.offline.options.OfflineContentOptions;
import com.bitmovin.player.offline.options.OfflineOptionEntry;
import com.bitmovin.player.offline.options.OfflineOptionEntryAction;
import com.bitmovin.player.offline.options.OfflineOptionEntryState;
import com.google.gson.Gson;

import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    public static final String SOURCE_ITEM = "SOURCE_ITEM";
    public static final String OFFLINE_SOURCE_ITEM = "OFFLINE_SOURCE_ITEM";

    private BitmovinPlayerView bitmovinPlayerView;
    private BitmovinPlayer bitmovinPlayer;
    private Gson gson = new Gson();
    private ListItem listItem;

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
            Toast.makeText(this, "Start Downloading...", Toast.LENGTH_LONG).show();
//            Explore.listItemActionListener.showSelectionDialog();
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

    private AlertDialog.Builder generateAlertDialogBuilder(ListItem listItem, final List<OfflineOptionEntry> entries, String[] entriesAsText, boolean[] entriesCheckList) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this).setMultiChoiceItems(entriesAsText, entriesCheckList, new DialogInterface.OnMultiChoiceClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked)
            {
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
        });
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        return dialogBuilder;
    }
}
