package com.jwhh.notekeeper.ui;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.DataManager;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperOpenHelper;
import com.jwhh.notekeeper.viewModels.NoteActivityViewModel;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

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

        initializeDisplayValues();
        saveOriginalDisplayValues();

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

    }

    private void loadNoteData() {
        SQLiteDatabase database = mNoteKeeperOpenHelper.getReadableDatabase();

        String[] columnsNotes = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
                NoteInfoEntry._ID

        };

        String selection = NoteInfoEntry._ID + "= ?";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        mNoteCursor = database.query(NoteInfoEntry.TABLE_NAME,
                columnsNotes,selection,selectionArgs,
                null,null,null,null);
        mNoteCursor.moveToNext();

        displayNote();

    }

    private void saveOriginalDisplayValues() {

        /*if (mIsNewNote)
            return;

        mViewModel.mOriginalCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTittle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();*/
    }


    private void displayNote() {
        int courseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        int noteTittlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        int noteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        int noteIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry._ID);

        String courseId = mNoteCursor.getString(courseIdPos);
        String noteTittle = mNoteCursor.getString(noteTittlePos);
        String noteText = mNoteCursor.getString(noteTextPos);
        int noteId = mNoteCursor.getInt(noteIdPos);


        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        int courseIndex = courses.indexOf(course);

        mSpinnerCourses.setSelection(courseIndex);
        mNoteTittle.setText(noteTittle);
        mNoteText.setText(noteText);
    }

    private void initializeDisplayValues() {

        Intent intent = getIntent();

        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);

        mIsNewNote = mNoteId == ID_NOT_SET;

        if (mIsNewNote)
            createNewNote();

        if (!mIsNewNote)
            loadNoteData();


    }

    private void createNewNote() {
        DataManager dataManager = DataManager.getInstance();
        mNoteId = dataManager.createNewNote();

        mNote = dataManager.getNotes().get(mNoteId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsCancelling) {
            if (mIsNewNote) {
                DataManager.getInstance().removeNote(mNoteId);
            } else {
                restoreOriginalNoteState();
            }

        } else {
            saveNote();
        }

    }

    private void restoreOriginalNoteState() {
        mNote.setCourse(DataManager.getInstance().getCourse(mViewModel.mOriginalCourseId));
        mNote.setTitle(mViewModel.mOriginalNoteTittle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        /*mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setText(mNoteText.getText().toString());
        mNote.setTitle(mNoteTittle.getText().toString());*/
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
            case R.id.action_next:
                nextNote();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);

        int lastNoteIndex = DataManager.getInstance().getNotes().size()-1;


        item.setEnabled(mNoteId < lastNoteIndex);

        return super.onPrepareOptionsMenu(menu);

    }

    private void nextNote() {
        mNoteId +=1;
        saveNote();

        mNote = DataManager.getInstance().getNotes().get(mNoteId);
        saveOriginalDisplayValues();

        //displayNotes();
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
}
