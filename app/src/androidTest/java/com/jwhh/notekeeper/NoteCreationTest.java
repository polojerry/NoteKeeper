package com.jwhh.notekeeper;

import androidx.test.rule.ActivityTestRule;

import com.jwhh.notekeeper.ui.NoteListActivity;

import org.junit.Rule;
import org.junit.Test;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class NoteCreationTest {

    @Rule
    public ActivityTestRule<NoteListActivity> mNoteListActivityRule = new
            ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void CreteNoteTest(){
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.text_note_title)).perform(typeText("This is the typed note Tittle"));
        onView(withId(R.id.text_note_text)).perform(typeText("This is the typed note text"),
                closeSoftKeyboard());

    }

}