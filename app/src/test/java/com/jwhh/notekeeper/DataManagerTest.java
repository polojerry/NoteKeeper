package com.jwhh.notekeeper;

import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.DataManager;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {


    @Test
    public void createNewNote() {

        DataManager dataManager = DataManager.getInstance();

        final CourseInfo course = dataManager.getCourse("android_intents");
        final String noteTittle = "Test Note Tittle";
        final String noteText = "Test Note Text";


        int newNoteIndex = dataManager.createNewNote();
        NoteInfo createdNote = dataManager.getNotes().get(newNoteIndex);
        createdNote.setCourse(course);
        createdNote.setTitle(noteTittle);
        createdNote.setText(noteText);


        NoteInfo compareNote = dataManager.getNotes().get(newNoteIndex);

        assertEquals(course, compareNote.getCourse() );
        assertEquals(noteTittle, compareNote.getTitle() );
        assertEquals(noteText, compareNote.getText() );
    }
}