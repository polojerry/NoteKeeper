package com.jwhh.notekeeper.utils;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;

public class NoteUploaderJobService extends JobService {

    public static String EXTRA_JOB_DATA = "com.jwhh.notekeeper.utils.extra.NOTE_URI";
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        @SuppressLint("StaticFieldLeak") AsyncTask<JobParameters, Void,Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... jobParameters) {
                JobParameters backgroundParameters = jobParameters[0];

                String jobStringUri = backgroundParameters.getExtras().getString(EXTRA_JOB_DATA);
                Uri jobUri = Uri.parse(jobStringUri);
                mNoteUploader.doUpload(jobUri);


                jobFinished(backgroundParameters,false);
                return null;
            }
        };

        mNoteUploader = new NoteUploader(this);
        task.execute(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
