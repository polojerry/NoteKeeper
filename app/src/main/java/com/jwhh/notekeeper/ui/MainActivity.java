package com.jwhh.notekeeper.ui;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String PRIMARY_NOTIFICATION_CHANNEL_ID = "primary_notification_channel";
    final int REQUEST_IMAGE_GET = 1;

    final int CAMERA = 10;
    final int GALLERY = 11;

    private final int PERMISSIONS_REQUEST_CAMERA = 1001;


    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerViewItems;
    private LinearLayoutManager mNotesLayoutManager;
    private GridLayoutManager mCoursesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private NoteKeeperOpenHelper mDbOpenHelper;

    private int LOADER_NOTES = 0;
    private int NOTE_UPLOADER_JOB_ID = 1;
    private AppCompatImageView mProfileImage;
    private AppCompatTextView mUserName;
    private AppCompatTextView mUserEmail;

    private boolean mPermissionGranted = false;

    private int PERMISION_REQUEST_ALL = 1000;

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

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePicture();
            }
        });
        openAndCloseDrawer();
    }

    private void updatePicture() {
        createImageLocationDialog();
    }


    private void createImageLocationDialog() {
        if(!mPermissionGranted) requestPermissionAll();

        AlertDialog.Builder imageSelectionDialog = new AlertDialog.Builder(this);
        imageSelectionDialog.setTitle("Select Image Location")
                .setItems(getResources().getStringArray(R.array.dialog_image_locations), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                selectFromGallery();
                                break;
                            case 1:
                                captureImage();
                                break;
                        }

                    }
                });
        imageSelectionDialog.show();
    }

    private void requestPermissionAll() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISION_REQUEST_ALL
                        );

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            mPermissionGranted = true;
        }

    }


    private void selectFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    private void captureImage() {
        Toast.makeText(this, "Not Yet Implemented", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            // Do work with photo saved at fullPhotoUri
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putString("profile_image_uri", fullPhotoUri.toString()).commit();



        }
    }

    private void enforceStrictModePolicy() {
        if (BuildConfig.DEBUG) {
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

        NavigationView navView = findViewById(R.id.nav_view);
        View navHeader = navView.getHeaderView(0);

        mUserName = navHeader.findViewById(R.id.nav_header_user_name);
        mUserEmail = navHeader.findViewById(R.id.nav_header_email_address);
        mProfileImage = navHeader.findViewById(R.id.imgProfilePhoto);

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

        final DrawerLayout navDrawer = findViewById(R.id.drawer_layout);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navDrawer.openDrawer(GravityCompat.START);


            }
        }, 1000);

    }

    private void updateNavHeader() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String user_name = preferences.getString("pref_display_name", "");
        String email_address = preferences.getString("pref_email_address", "");
        String profile_image = preferences.getString("profile_image_uri", "");

        mUserName.setText(user_name);
        mUserEmail.setText(email_address);

        if(!profile_image.equals("")){
            Glide.with(this)
                    .load(Uri.parse(profile_image))
                    .into(mProfileImage);
        }
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

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_delete_notes:
                deleteNotesFromDatabase();
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

        JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOADER_JOB_ID, componentName)
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
        int deletedRows = getContentResolver().delete(Notes.CONTENT_URI, null, null);

        if (deletedRows >= 1) {
            Toast.makeText(this, "Deleted All Notes", Toast.LENGTH_SHORT).show();
        } else {
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
