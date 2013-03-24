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

    private List<Task> getTasks(String filter, String[] args, String order) {
        final List<Task> ret = new LinkedList<Task>();
        final Cursor c = mContext.getContentResolver().query(TaskProvider.CONTENT_URI, TaskProvider.PROJECTION, filter, args, order);
        try {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                final Task task = Task.fromCursor(c, 0);
                task.resolved.folder = TaskFolder.fromCursor(c, 14);
                task.resolved.context = TaskContext.fromCursor(c, 19);
                ret.add(task);
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public List<Task> getTasks(String filter, String order) {
        return getTasks(filter, null, order);
    }

    public List<Task> getHotTasks() {
        final String due = new SimpleDateFormat("yyyy-MM-dd").format(new Date(new Date().getTime() + 7 * 86400 * 1000));
        return getTasks(TaskProvider.HOTLIST_FILTER, new String[] { due }, TaskProvider.DEFAULT_ORDER);
    }

    public Task getTaskById(long taskId) {
        return getTasks(TaskProvider.ID_FILTER, new String[] { String.valueOf(taskId) }, TaskProvider.DEFAULT_ORDER).get(0);
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
