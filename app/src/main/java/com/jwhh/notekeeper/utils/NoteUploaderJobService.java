package com.jwhh.notekeeper.utils;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class NoteUploaderJobService extends JobService {

    public static String EXTRA_JOB_DATA = "com.jwhh.notekeeper.utils.extra.NOTE_URI";
    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
