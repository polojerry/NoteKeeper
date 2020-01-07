package com.jwhh.notekeeper.viewModels;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {

    private final String ORIGINAL_COURSE_ID = "com.jwhh.notekeeper.ORIGINAL_COURSE_ID";
    private final String ORIGINAL_NOTE_TITLE = "com.jwhh.notekeeper.ORIGINAL_NOTE_TITLE";
    private final String ORIGINAL_NOTE_TEXT = "com.jwhh.notekeeper.ORIGINAL_NOTE_TEXT";

    public boolean isNewlyCreated = true;

    public String mOriginalNoteId;
    public String mOriginalCourseId;
    public String mOriginalNoteTittle;
    public String mOriginalNoteText;

    public void saveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_COURSE_ID, mOriginalCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTittle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    public void restoreSavedState(Bundle savedInstanceState) {
        mOriginalCourseId = savedInstanceState.getString(ORIGINAL_COURSE_ID);
        mOriginalNoteTittle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);

    }
}
