package com.jwhh.notekeeper.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NoteKeeper.db";
    private static final int DATABASE_VERSION = 2;

    public NoteKeeperOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE);

        db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX_1);
        db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX_1);

        DatabaseDataWorker worker = new DatabaseDataWorker(db);
        worker.insertCourses();
        worker.insertSampleNotes();


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(oldVersion<2){
            db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX_1);
            db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX_1);
        }
    }
}
