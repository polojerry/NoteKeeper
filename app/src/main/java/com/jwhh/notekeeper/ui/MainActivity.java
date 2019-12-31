package com.jwhh.notekeeper.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jwhh.notekeeper.BuildConfig;
import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.contentProvider.NoteKeeperProviderContract.Notes;
import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.database.DataManager;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperOpenHelper;
import com.jwhh.notekeeper.recyclerView.CourseRecyclerAdapter;
import com.jwhh.notekeeper.recyclerView.NoteRecyclerAdapter;
import com.jwhh.notekeeper.utils.NoteBackup;
import com.jwhh.notekeeper.utils.NoteBackupService;
import com.jwhh.notekeeper.utils.NoteUploaderJobService;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String PRIMARY_NOTIFICATION_CHANNEL_ID = "primary_notification_channel";


    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerViewItems;
    private LinearLayoutManager mNotesLayoutManager;
    private GridLayoutManager mCoursesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private NoteKeeperOpenHelper mDbOpenHelper;

    private int LOADER_NOTES = 0;
    private int NOTE_UPLOADER_JOB_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        enforceStrictModePolicy();

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


        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayNotes();

        openAndCloseDrawer();
    }

    private void enforceStrictModePolicy() {
        if(BuildConfig.DEBUG){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();

            StrictMode.setThreadPolicy(policy);
        }
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
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this);
        updateNavHeader();
    }

    private void openAndCloseDrawer() {

        final DrawerLayout navDrawer= findViewById(R.id.drawer_layout);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navDrawer.openDrawer(GravityCompat.START);


            }
        },1000);
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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

        int id = item.getItemId();

        switch(id){
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_delete_notes:
                deleteNotesFromDatabase();;
                break;
            case R.id.action_back_up_notes:
                backUpNotes();
                break;
            case R.id.action_upload_notes:
                scheduleNotesUpload();
                break;
            default:
                return super.onOptionsItemSelected(item);


        }
        return super.onOptionsItemSelected(item);

    }

    private void scheduleNotesUpload() {

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(NoteUploaderJobService.EXTRA_JOB_DATA, Notes.CONTENT_URI.toString());


        ComponentName componentName = new ComponentName(this, NoteUploaderJobService.class);

        JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOADER_JOB_ID,componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(bundle)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);

        
    }

    private void backUpNotes() {

        Intent backUpNotesIntent = new Intent(this, NoteBackupService.class);
        backUpNotesIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(backUpNotesIntent);
    }

    private void deleteNotesFromDatabase() {
        int deletedRows = getContentResolver().delete(Notes.CONTENT_URI,null,null);

        if(deletedRows>=1){
            Toast.makeText(this, "Deleted All Notes", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed to delete Notes...", Toast.LENGTH_SHORT).show();
        }

        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int itemId = menuItem.getItemId();

        switch (itemId) {
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
        Snackbar.make(mRecyclerViewItems, message, Snackbar.LENGTH_LONG).show();
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES) {
            loader = createNotesLoader();
        }
        return loader;
    }

    private CursorLoader createNotesLoader() {

        final String[] columnNotes = {
                Notes.COLUMN_NOTE_TITLE,
                Notes._ID,
                Notes.COLUMN_COURSE_TITLE
        };

        String notesSortOrder = CourseInfoEntry.COLUMN_COURSE_TITLE + ", " + NoteInfoEntry.COLUMN_NOTE_TITLE + " ASC";

        return new CursorLoader(this, Notes.CONTENT_URI_EXPANDED,
                columnNotes, null, null, notesSortOrder);


    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES) {
            mNoteRecyclerAdapter.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES) {
            mNoteRecyclerAdapter.swapCursor(null);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "NoteKeeper Notification";
            String description = "Notifications for NoteKeeper";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(PRIMARY_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
