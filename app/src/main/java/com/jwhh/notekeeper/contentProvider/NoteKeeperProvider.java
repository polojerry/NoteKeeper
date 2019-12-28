package com.jwhh.notekeeper.contentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.jwhh.notekeeper.contentProvider.NoteKeeperProviderContract.Courses;
import com.jwhh.notekeeper.contentProvider.NoteKeeperProviderContract.Notes;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperOpenHelper;

public class NoteKeeperProvider extends ContentProvider {

    NoteKeeperOpenHelper mOpenHelper;
    public static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int COURSES = 0;

    private static final int NOTES = 1;

    private static final int NOTES_EXPANDED = 3 ;

    static{
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor = null;

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        switch(match){
            case COURSES:
                cursor = database.query(CourseInfoEntry.TABLE_NAME,projection, selection,selectionArgs,
                        null,null,sortOrder);
                break;

            case NOTES:
                cursor = database.query(NoteInfoEntry.TABLE_NAME,projection, selection,selectionArgs,
                        null,null,sortOrder);
                break;

            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(database, projection,selection,selectionArgs,sortOrder);
                break;
        }


        return cursor;

    }

    private Cursor notesExpandedQuery(SQLiteDatabase database, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {

        String[] columns = new String[projection.length];

        for (int index = 0; index<projection.length; index++){

            columns[index] = projection[index].equals(BaseColumns._ID) ||  projection[index].equals(Notes.COLUMN_COURSE_ID)?
                    NoteInfoEntry.getQName(projection[index]) : projection[index];
        }

        String tablesWithJoin = NoteInfoEntry.TABLE_NAME +" JOIN " + CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " + CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

        return database.query(tablesWithJoin,columns,selection,selectionArgs, null,null, sortOrder);
     }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
