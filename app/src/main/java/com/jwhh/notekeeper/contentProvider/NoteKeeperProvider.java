package com.jwhh.notekeeper.contentProvider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.jwhh.notekeeper.contentProvider.NoteKeeperProviderContract.Courses;
import com.jwhh.notekeeper.contentProvider.NoteKeeperProviderContract.Notes;
import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperOpenHelper;

public class NoteKeeperProvider extends ContentProvider {

    public static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";
    NoteKeeperOpenHelper mOpenHelper;
    public static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int COURSES = 0;

    private static final int NOTES = 1;

    private static final int NOTES_EXPANDED = 2;

    private static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        int deletedRow = 0;

        int match = sUriMatcher.match(uri);

        switch(match){
            case COURSES:

                break;
            case NOTES:

                deletedRow = database.delete(NoteInfoEntry.TABLE_NAME,null, null);


                break;
            case NOTES_ROW:
                long parsedId = ContentUris.parseId(uri);
                String notesRowSelection = Notes._ID + " = ?" ;
                String[] notesRowSelectionArgs = {
                        Long.toString(parsedId)
                };

                deletedRow = database.delete(NoteInfoEntry.TABLE_NAME,notesRowSelection, notesRowSelectionArgs);


                break;
        }

        return deletedRow;
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = "";

        int match = sUriMatcher.match(uri);

            switch (match) {

                case COURSES:

                    mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                            MIME_VENDOR_TYPE + Courses.PATH;

                    break;
                case NOTES:

                    mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                            MIME_VENDOR_TYPE + Notes.PATH;

                    break;
                case NOTES_EXPANDED:

                    mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                            MIME_VENDOR_TYPE + Notes.PATH_EXPANDED;

                    break;
                case NOTES_ROW:

                    mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                            MIME_VENDOR_TYPE + Notes.PATH;

                    break;

            }

        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        Uri insertUri = null;

        long rowId;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                rowId = database.insert(NoteInfoEntry.TABLE_NAME, null, values);

                insertUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = database.insert(CourseInfoEntry.TABLE_NAME, null, values);

                insertUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:
                break;


        }

        return insertUri;
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

        switch (match) {
            case COURSES:
                cursor = database.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case NOTES:
                cursor = database.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(database, projection, selection, selectionArgs, sortOrder);
                break;

            case NOTES_ROW:

                long parseId = ContentUris.parseId(uri);

                String rowSelection = Notes._ID + " = ? ";
                String[] rowSelectionArgs = {
                        Long.toString(parseId)
                };

                cursor = database.query(NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs,
                        null, null, null);
                break;
        }


        return cursor;

    }

    private Cursor notesExpandedQuery(SQLiteDatabase database, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {

        String[] columns = new String[projection.length];

        for (int index = 0; index < projection.length; index++) {

            columns[index] = projection[index].equals(BaseColumns._ID) || projection[index].equals(Notes.COLUMN_COURSE_ID) ?
                    NoteInfoEntry.getQName(projection[index]) : projection[index];
        }

        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " + CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

        return database.query(tablesWithJoin, columns, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rowsUpdated = 0;

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();

        long parsedId = ContentUris.parseId(uri);

        int match = sUriMatcher.match(uri);

        switch (match){

            case COURSES:
                String courseRowSelection = Courses._ID + " = ?";
                String[] courseRowSelectionArgs  = {
                        Long.toString(parsedId)
                };

                rowsUpdated = database.update(CourseInfoEntry.TABLE_NAME,values,courseRowSelection,courseRowSelectionArgs);


                break;
            case NOTES:
                break;
            case NOTES_ROW:
                String noteRowSelection = Notes._ID + " = ?";
                String[] noteRowSelectionArgs  = {
                        Long.toString(parsedId)
                };

                rowsUpdated = database.update(NoteInfoEntry.TABLE_NAME,values,noteRowSelection,noteRowSelectionArgs);
                break;

            case NOTES_EXPANDED:
                break;

        }


        return rowsUpdated;
    }
}
