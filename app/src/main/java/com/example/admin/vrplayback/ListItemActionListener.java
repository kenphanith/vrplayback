package com.example.admin.vrplayback;

import android.app.LauncherActivity;

public interface ListItemActionListener {
    void showSelectionDialog();
    void delete(ListItem listItem);
}
