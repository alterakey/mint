package com.gmail.altakey.mint;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;
import android.content.ContentValues;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.util.UUID;

public class DB {
    private Context mContext;

    public DB(Context c) {
        mContext = c;
    }

    public void addTask(Task task) {
        final ContentValues values = new ContentValues();
        values.put(TaskProvider.COLUMN_COOKIE, nextCookie());
        values.put(TaskProvider.COLUMN_TITLE, task.title);
        values.put(TaskProvider.COLUMN_NOTE, task.note);
        values.put(TaskProvider.COLUMN_MODIFIED, task.modified);
        values.put(TaskProvider.COLUMN_COMPLETED, task.completed);
        values.put(TaskProvider.COLUMN_FOLDER, task.folder);
        values.put(TaskProvider.COLUMN_CONTEXT, task.context);
        values.put(TaskProvider.COLUMN_PRIORITY, task.priority);
        values.put(TaskProvider.COLUMN_STAR, task.star);
        values.put(TaskProvider.COLUMN_DUEDATE, task.duedate);
        values.put(TaskProvider.COLUMN_DUETIME, task.duetime);
        values.put(TaskProvider.COLUMN_STATUS, task.status);
        mContext.getContentResolver().insert(TaskProvider.CONTENT_URI, values);
    }

    public void commitTask(Task task) {
        final ContentValues values = new ContentValues();
        values.put(TaskProvider.COLUMN_COOKIE, nextCookie());
        values.put(TaskProvider.COLUMN_TITLE, task.title);
        values.put(TaskProvider.COLUMN_NOTE, task.note);
        values.put(TaskProvider.COLUMN_MODIFIED, task.modified);
        values.put(TaskProvider.COLUMN_COMPLETED, task.completed);
        values.put(TaskProvider.COLUMN_FOLDER, task.folder);
        values.put(TaskProvider.COLUMN_CONTEXT, task.context);
        values.put(TaskProvider.COLUMN_PRIORITY, task.priority);
        values.put(TaskProvider.COLUMN_STAR, task.star);
        values.put(TaskProvider.COLUMN_DUEDATE, task.duedate);
        values.put(TaskProvider.COLUMN_DUETIME, task.duetime);
        values.put(TaskProvider.COLUMN_STATUS, task.status);
        mContext.getContentResolver().update(TaskProvider.CONTENT_URI, values, TaskProvider.ID_FILTER, new String[] { String.valueOf(task.id) });
    }

    public static String nextCookie() {
        return UUID.randomUUID().toString();
    }
}
