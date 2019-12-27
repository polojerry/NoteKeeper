package com.jwhh.notekeeper.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.DataManager;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperOpenHelper;
import com.jwhh.notekeeper.recyclerView.CourseRecyclerAdapter;
import com.jwhh.notekeeper.recyclerView.NoteRecyclerAdapter;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;

import java.time.Instant;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerViewItems;
    private LinearLayoutManager mNotesLayoutManager;
    private GridLayoutManager mCoursesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private NoteKeeperOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences,false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayNotes();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbOpenHelper.close();
    }

    private void initializeDisplayNotes() {
        DataManager.getInstance().loadFromDatabase(mDbOpenHelper);

        mRecyclerViewItems = findViewById(R.id.list_item);

        mNotesLayoutManager = new LinearLayoutManager(this);
        mCoursesLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.course_grid_span));

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        List<CourseInfo> course = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, course);

        displayNotes();


    }

    private void displayNotes() {
        mRecyclerViewItems.setAdapter(mNoteRecyclerAdapter);
        mRecyclerViewItems.setLayoutManager(mNotesLayoutManager);

        setChecked(R.id.nav_notes);
    }

    private void displayCourses() {
        mRecyclerViewItems.setAdapter(mCourseRecyclerAdapter);
        mRecyclerViewItems.setLayoutManager(mCoursesLayoutManager);

        setChecked(R.id.nav_courses);
    }



    private void setChecked(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        updateNavHeader();
    }

    private void loadNotes() {

        SQLiteDatabase database = mDbOpenHelper.getReadableDatabase()
                ;
        final String[] columnNotes = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID
        };
        String notesSortOrder = NoteInfoEntry.COLUMN_NOTE_TITLE + ", " + NoteInfoEntry.COLUMN_COURSE_ID  +" ASC" ;


        Cursor notesCursor = database.query(NoteInfoEntry.TABLE_NAME, columnNotes,
                null, null, null, null, notesSortOrder);

        mNoteRecyclerAdapter.swapCursor(notesCursor);

    }

    private void updateNavHeader() {
        NavigationView navView = findViewById(R.id.nav_view);
        View navHeader = navView.getHeaderView(0);

        AppCompatTextView userName = navHeader.findViewById(R.id.nav_header_user_name);
        AppCompatTextView userEmail = navHeader.findViewById(R.id.nav_header_email_address);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String user_name = preferences.getString("pref_display_name", "");
        String email_address = preferences.getString("pref_email_address", "");

        userName.setText(user_name);
        userEmail.setText(email_address);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            startActivity(new Intent(MainActivity.this,SettingsActivity.class ));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int itemId = menuItem.getItemId();

        switch(itemId){
            case R.id.nav_notes:
                displayNotes();
                break;
            case R.id.nav_courses:
                displayCourses();
                break;
            case R.id.nav_share:
                handleMessage(R.string.share);
                break;
            case R.id.nav_send:
                handleMessage(R.string.send);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void handleMessage(int message) {
        Snackbar.make(mRecyclerViewItems,message, Snackbar.LENGTH_LONG).show();
    }


}
