package com.example.admin.vrplayback;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bitmovin.player.config.media.SourceItem;
import com.bitmovin.player.offline.OfflineSourceItem;
import com.bitmovin.player.offline.options.OfflineContentOptions;
import com.bitmovin.player.offline.options.OfflineOptionEntry;
import com.bitmovin.player.offline.options.OfflineOptionEntryState;

import java.util.List;

import android.app.LauncherActivity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bitmovin.player.config.media.SourceItem;
import com.bitmovin.player.offline.options.OfflineContentOptions;
import com.bitmovin.player.offline.options.OfflineOptionEntry;
import com.bitmovin.player.offline.options.OfflineOptionEntryState;

import java.util.List;

class ListAdapter extends ArrayAdapter<ListItem>
{
    private ListItemActionListener listItemActionListener;

    public ListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ListItem> objects, ListItemActionListener listItemActionListener)
    {
        super(context, resource, objects);
        this.listItemActionListener = listItemActionListener;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View view = convertView;

        if (view == null)
        {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        final ListItem listItem = getItem(position);

        TextView title = (TextView) view.findViewById(R.id.title);

        SourceItem sourceItem = listItem.getSourceItem();
        OfflineContentOptions offlineContentOptions = listItem.getOfflineContentOptions();

        title.setText(sourceItem.getTitle());
        if (offlineContentOptions != null)
        {
            Toast.makeText(getContext(), "OfflineContentOption is not null", Toast.LENGTH_LONG).show();
        }
        else
        {
            // If no options are available, we hide the download and the delete button
            Toast.makeText(getContext(), "OfflineContentOption is null", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    /**
     * Returns true, if one {@link OfflineOptionEntry}s state is {@link OfflineOptionEntryState#DOWNLOADING}
     *
     * @param offlineContentOptions
     * @return true, if one {@link OfflineOptionEntry}s state is {@link OfflineOptionEntryState#DOWNLOADING}
     */
    private boolean isDownloading(OfflineContentOptions offlineContentOptions)
    {
        List<OfflineOptionEntry> allOfflineOptionEntries = Util.getAsOneList(offlineContentOptions);
        for (OfflineOptionEntry entry : allOfflineOptionEntries)
        {
            if (entry.getState() == OfflineOptionEntryState.DOWNLOADING)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true, if one {@link OfflineOptionEntry}s state is {@link OfflineOptionEntryState#DOWNLOADED}
     *
     * @param offlineContentOptions
     * @return true, if one {@link OfflineOptionEntry}s state is {@link OfflineOptionEntryState#DOWNLOADED}
     */
    private boolean hasDownloaded(OfflineContentOptions offlineContentOptions)
    {
        List<OfflineOptionEntry> allOfflineOptionEntries = Util.getAsOneList(offlineContentOptions);
        for (OfflineOptionEntry entry : allOfflineOptionEntries)
        {
            if (entry.getState() == OfflineOptionEntryState.DOWNLOADED)
            {
                return true;
            }
        }
        return false;
    }
}
