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

import com.bitmovin.player.config.media.SourceItem;
import com.bitmovin.player.offline.options.OfflineContentOptions;
import com.bitmovin.player.offline.options.OfflineOptionEntry;
import com.bitmovin.player.offline.options.OfflineOptionEntryState;

import java.util.List;

class ListAdapter extends ArrayAdapter<ListItem>
{
    private ListItemActionListener listItemActionListener;
    public ListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ListItem> objects)
    {
        super(context, resource, objects);
        this.listItemActionListener = listItemActionListener;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        final ListItem listItem = getItem(position);
        TextView title = (TextView) view.findViewById(R.id.title);
        OfflineContentOptions offlineContentOptions = listItem.getOfflineContentOptions();
        if (offlineContentOptions != null){
            Log.d(ListAdapter.class.getSimpleName(), "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        }else {
            Log.d(ListAdapter.class.getSimpleName(), "*************************************");
        }
        SourceItem sourceItem = listItem.getSourceItem();
        title.setText(sourceItem.getTitle());

        return view;
    }
}
