package com.jwhh.notekeeper.utils;

import android.content.Context;
import android.content.Intent;

public class CourseEventBroadcastHelper {

    public static String ACTION_COURSE_EVENT = "com.jwhh.notekeeper.utils.extra.COURSE_EVENT";


    public static String EXTRA_COURSE_ID = "com.jwhh.notekeeper.utils.extra.COURSE_ID";
    public static String EXTRA_COURSE_MESSAGE = "com.jwhh.notekeeper.utils.extra.COURSE_MESSAGE";

    public static void sendEventBroadcast(Context context, String courseId, String message){
        Intent intent = new Intent(ACTION_COURSE_EVENT);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_COURSE_MESSAGE, message);

        context.sendBroadcast(intent);

    }
}
