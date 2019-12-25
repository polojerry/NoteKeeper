package com.jwhh.notekeeper.ui;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.database.DataManager;
import com.jwhh.notekeeper.ui.MainActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class NoteCreationTest {

    private static DataManager sDataManager;

    @BeforeClass
    public static void setUpClass(){
        sDataManager = DataManager.getInstance();

    }
    @Rule
    public ActivityTestRule<MainActivity> mNoteListActivityRule = new
            ActivityTestRule<>(MainActivity.class);

    @Test
    public void CreteNoteTest(){
        CourseInfo courseInfo = sDataManager.getCourse("java_lang");
        String noteTittle = "This is the typed note Tittle";
        String noteText = "This is the typed note text";


        onView(ViewMatchers.withId(R.id.fab)).perform(click());

        onView(withId(R.id.spinner_courses)).perform(click());
        onData(allOf(instanceOf(CourseInfo.class), equalTo(courseInfo))).perform(click());

        onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(containsString(courseInfo.getTitle()))));
        onView(withId(R.id.text_note_title)).perform(typeText(noteTittle));
        onView(withId(R.id.text_note_text)).perform(typeText(noteText),
                closeSoftKeyboard());
        pressBack();

    }

}