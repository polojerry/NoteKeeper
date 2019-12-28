package com.jwhh.notekeeper.contentProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public class NoteKeeperProviderContract {
    private NoteKeeperProviderContract() {
    }

    public static final String AUTHORITY = "com.jwhh.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    private interface ColumnCourseId{
        public static final String COLUMN_COURSE_ID = "course_id";
    }

    private interface CourseColumns{
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }

    private interface NoteColumns{
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
    }

    public static final class Courses implements BaseColumns, ColumnCourseId, CourseColumns {
        public static final String PATH = "courses";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH);

    }

    public static final class Notes implements BaseColumns, ColumnCourseId, NoteColumns,CourseColumns {
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH);

        public static final String PATH_EXPANDED = "notes_expanded";
        public static final Uri CONTENT_URI_EXPANDED = Uri.withAppendedPath(AUTHORITY_URI,PATH_EXPANDED);



    }
}
