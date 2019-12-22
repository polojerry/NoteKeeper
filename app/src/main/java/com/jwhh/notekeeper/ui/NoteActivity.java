package com.jwhh.notekeeper.ui;

import android.content.Intent;
import android.os.Bundle;

import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.DataManager;
import com.jwhh.notekeeper.viewModels.NoteActivityViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

    public static final String NOTE_POSITION = "com.jwhh.notekeeper.dataModels.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mNoteTittle;
    private EditText mNoteText;
    private int mNewNotePosition;
    private boolean mIsCancelling;

    private NoteActivityViewModel mViewModel;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mViewModel.saveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinnerCourses = findViewById(R.id.spinner_courses);
        mNoteTittle = findViewById(R.id.text_note_title);
        mNoteText = findViewById(R.id.text_note_text);

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));

        mViewModel  = viewModelProvider.get(NoteActivityViewModel.class);

        if(!mViewModel.isNewlyCreated && savedInstanceState!=null)
            mViewModel.restoreSavedState(savedInstanceState);


        mViewModel.isNewlyCreated = false;

        initializeDisplayValues();
        saveOriginalDisplayValues();

        if(!mIsNewNote)
            displayValues(mSpinnerCourses, mNoteTittle, mNoteText);


        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

    }

    private void saveOriginalDisplayValues() {

        if(mIsNewNote)
            return;

        mViewModel.mOriginalCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTittle = mNote.getTitle();
        mViewModel. mOriginalNoteText = mNote.getText();
    }

    private void displayValues(Spinner spinnerCourses, EditText noteTittle, EditText noteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());

        spinnerCourses.setSelection(courseIndex);
        noteTittle.setText(mNote.getTitle());
        noteText.setText(mNote.getText());
    }

    private void initializeDisplayValues() {

        Intent intent = getIntent();

        int notePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);

        mIsNewNote = notePosition == POSITION_NOT_SET;

        if(mIsNewNote){
            createNewNote();
        }else{
            mNote = DataManager.getInstance().getNotes().get(notePosition);
        }

    }

    private void createNewNote() {
        DataManager dataManager = DataManager.getInstance();
        mNewNotePosition = dataManager.createNewNote();

        mNote = dataManager.getNotes().get(mNewNotePosition);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mIsCancelling){
            if(mIsNewNote){
                DataManager.getInstance().removeNote(mNewNotePosition);
            }else{
                restoreOriginalNoteState();
            }

        }else{
            saveNote();
        }

    }

    private void restoreOriginalNoteState() {
        mNote.setCourse(DataManager.getInstance().getCourse(mViewModel.mOriginalCourseId));
        mNote.setTitle(mViewModel.mOriginalNoteTittle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo)mSpinnerCourses.getSelectedItem());
        mNote.setText(mNoteText.getText().toString());
        mNote.setTitle(mNoteTittle.getText().toString());
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

        switch (id){

            case R.id.action_send_mail:
                sendMail();
                break;
            case R.id.action_cancel:
                cancel();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cancel() {
        mIsCancelling = true;
        finish();
    }

    private void sendMail() {

        CourseInfo course =(CourseInfo) mSpinnerCourses.getSelectedItem();
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
