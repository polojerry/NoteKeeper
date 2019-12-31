package com.jwhh.notekeeper.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jwhh.notekeeper.ui.NoteReminderNotification;

public class NoteReminderReceiver extends BroadcastReceiver {
    public static String EXTRA_NOTE_ID = "com.jwhh.notekeeper.utils.extra.NOTE_ID";
    public static String EXTRA_NOTE_TITLE = "com.jwhh.notekeeper.utils.extra.NOTE_TITLE";
    public static String EXTRA_NOTE_TEXT = "com.jwhh.notekeeper.utils.extra.NOTE_TEXT";


    @Override
    public void onReceive(Context context, Intent intent) {
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID,0);

        NoteReminderNotification.notify(context,noteTitle,noteText,noteId);
    }
}
