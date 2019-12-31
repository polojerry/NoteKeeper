package com.jwhh.notekeeper.utils;

import android.app.IntentService;
import android.content.Intent;

public class NoteBackupService extends IntentService {

    public static final String EXTRA_PARAM_COURSE_ID = "com.jwhh.notekeeper.utils.extra.COURSE_ID";

    public NoteBackupService() {
        super("NoteBackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            String courseId = intent.getStringExtra(EXTRA_PARAM_COURSE_ID);
            NoteBackup.doBackup(this,courseId);

        }
    }

}
