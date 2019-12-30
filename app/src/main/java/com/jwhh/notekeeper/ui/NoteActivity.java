package com.jwhh.notekeeper.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.contentProvider.NoteKeeperProviderContract.Courses;
import com.jwhh.notekeeper.contentProvider.NoteKeeperProviderContract.Notes;
import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.DataManager;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperOpenHelper;
import com.jwhh.notekeeper.viewModels.NoteActivityViewModel;


public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String NOTE_ID = "com.jwhh.notekeeper.dataModels.NOTE_ID";
    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;

    private AppCompatSpinner mSpinnerCourses;
    private AppCompatEditText mNoteTittle;
    private AppCompatEditText mNoteText;


    private int mNoteId;
    private boolean mIsCancelling;

    private NoteActivityViewModel mViewModel;
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;
    private Cursor mNoteCursor;
    private SimpleCursorAdapter mAdapterSpinnerCourses;

    private int LOADER_NOTES = 0;
    private int LOADER_COURSES = 1;
    private boolean mLoadCourseFinished;
    private boolean mNoteLoadFinished;
    private Uri mNoteUri;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mViewModel.saveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNoteKeeperOpenHelper.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNoteKeeperOpenHelper = new NoteKeeperOpenHelper(this);

        mSpinnerCourses = findViewById(R.id.spinner_courses);
        mNoteTittle = findViewById(R.id.text_note_title);
        mNoteText = findViewById(R.id.text_note_text);

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));

        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if (!mViewModel.isNewlyCreated && savedInstanceState != null)
            mViewModel.restoreSavedState(savedInstanceState);


        mViewModel.isNewlyCreated = false;


        mAdapterSpinnerCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE}, new int[]{android.R.id.text1}, 0);

        mAdapterSpinnerCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterSpinnerCourses);

        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this);
        initializeDisplayValues();

        if (mIsNewNote) {
            createNewNote();
        } else {
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this);

        }


    }

    private void saveOriginalDisplayValues() {

    }

    public void saveNoteToDatabase(String course_id, String note_title, String note_text) {

        final ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, course_id);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, note_title);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, note_text);

        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);

        getContentResolver().update(mNoteUri,values,null,null);

    }

    private void displayNote() {
        mNoteCursor.moveToNext();
        int courseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        int noteTittlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        int noteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        int noteIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry._ID);

        String courseId = mNoteCursor.getString(courseIdPos);
        String noteTittle = mNoteCursor.getString(noteTittlePos);
        String noteText = mNoteCursor.getString(noteTextPos);
        int noteId = mNoteCursor.getInt(noteIdPos);


        final int courseIndex = getIndexOfCourse(courseId);

        mSpinnerCourses.post(new Runnable() {
            @Override
            public void run() {
                mSpinnerCourses.setSelection(courseIndex);
            }
        });

        mNoteTittle.setText(noteTittle);
        mNoteText.setText(noteText);
    }

    private int getIndexOfCourse(String courseId) {
        Cursor cursor = mAdapterSpinnerCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;


        boolean more = cursor.moveToFirst();

        while (more) {

            String currentCourseId = cursor.getString(courseIdPos);

            if (courseId.equals(currentCourseId)) {
                break;
            }

            courseRowIndex++;
            more = cursor.moveToNext();
        }

        return courseRowIndex;
    }

    private void initializeDisplayValues() {

        Intent intent = getIntent();

        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);

        mIsNewNote = mNoteId == ID_NOT_SET;
    }

    private void createNewNote() {
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");

        mNoteUri = getContentResolver().insert(Notes.CONTENT_URI, values);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsCancelling) {
            if (mIsNewNote) {
                deleteNoteFromDatabase();
            } else {
                restoreOriginalNoteState();
            }

        } else {
            saveNote();
        }

    }

    private void deleteNoteFromDatabase() {

        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI,mNoteId);

       int rowsDeleted  = getContentResolver().delete(mNoteUri,null,null);

        if(rowsDeleted>=1){
            Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed to delete Note...", Toast.LENGTH_SHORT).show();
        }


    }

    private void restoreOriginalNoteState() {
        mNote.setCourse(DataManager.getInstance().getCourse(mViewModel.mOriginalCourseId));
        mNote.setTitle(mViewModel.mOriginalNoteTittle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {

        String courseId = selectedCourseId();
        String noteTitle = mNoteTittle.getText().toString();
        String noteText = mNoteText.getText().toString();

        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();

        Cursor cursor = mAdapterSpinnerCourses.getCursor();
        cursor.moveToPosition(selectedPosition);

        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);

        return courseId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_send_mail:
                sendMail();
                break;
            case R.id.action_cancel:
                cancel();
                break;
            case R.id.action_delete:
                deleteNoteFromDatabase();
                finish();
                break;
            case R.id.action_next:
                nextNote();
                break;
            case R.id.action_set_reminder:
                setReminderNotification();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setReminderNotification() {
        String noteTitle = mNoteTittle.getText().toString().trim();
        String noteText = mNoteText.getText().toString().trim();
        int noteId = (int) ContentUris.parseId(mNoteUri);


        NoteReminderNotification.notify(this,noteTitle,noteText,noteId);
    }



    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);

        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;


        item.setEnabled(mNoteId < lastNoteIndex);

        return super.onPrepareOptionsMenu(menu);

    }

    private void nextNote() {
        mNoteId += 1;
        saveNote();

        mNote = DataManager.getInstance().getNotes().get(mNoteId);
        saveOriginalDisplayValues();
        invalidateOptionsMenu();
    }

    private void cancel() {
        mIsCancelling = true;
        finish();
    }

    private void sendMail() {

        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mNoteTittle.getText().toString();
        String textBody = "Check out what i learnt from Pluralsite Course \"" + "" +
                course.getTitle() + " \" \n" + mNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, textBody);

        startActivity(intent);

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;

        if (id == LOADER_NOTES) {
            loader = createNotesLoader();
        } else if (id == LOADER_COURSES) {
            loader = createCoursesLoader();
        }
        return loader;
    }

    private CursorLoader createCoursesLoader() {
        mLoadCourseFinished = false;

        Uri coursesUri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };

        return new CursorLoader(this, coursesUri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);
    }

    private CursorLoader createNotesLoader() {
        mNoteLoadFinished = false;

        String[] columnsNotes = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
                NoteInfoEntry._ID

        };

        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);

        return new CursorLoader(this, mNoteUri, columnsNotes,null,null,null);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == LOADER_NOTES) {
            mNoteLoadFinished = true;
            loadFinishedNotes(data);
        } else if (loader.getId() == LOADER_COURSES) {
            mLoadCourseFinished = true;
            loadFinishedCourses(data);

        }

    }

    private void loadFinishedCourses(Cursor data) {
        mAdapterSpinnerCourses.swapCursor(data);
        displayNoteWhenLoadFinished();
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        displayNoteWhenLoadFinished();
    }

    private void displayNoteWhenLoadFinished() {
        if (mNoteLoadFinished && mLoadCourseFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        if (loader.getId() == LOADER_NOTES) {
            if (mNoteCursor != null) {
                mNoteCursor.close();
            }
        } else if (loader.getId() == LOADER_COURSES) {
            mAdapterSpinnerCourses.swapCursor(null);
        }

    }

}
