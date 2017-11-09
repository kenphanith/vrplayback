package com.example.admin.vrplayback;

import android.app.LauncherActivity;

public interface ListItemActionListener {
    void showSelectionDialog(ListItem listItem);
    void delete(ListItem listItem);
}
