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
    private static SQLiteDatabase sConn;

    private Context mContext;

    public static class Filter {
        public static final int UNKNOWN = -1;

        private static final Map<String, Integer> FOLDER_MAP = new HashMap<String, Integer>();
        private String mmFilter;

        public Filter(String filter) {
            mmFilter = filter;
            if (FOLDER_MAP.isEmpty()) {
                FOLDER_MAP.put("inbox", 0);
                FOLDER_MAP.put("next_action", 1);
                FOLDER_MAP.put("reference", 10);
                FOLDER_MAP.put("waiting", 5);
                FOLDER_MAP.put("someday", 8);
            }
        }

        public int getStatus() {
            Integer ret = FOLDER_MAP.get(mmFilter);
            return ret == null ? UNKNOWN : ret.intValue();
        }
    }

    public DB(Context c) {
        mContext = c;
    }

    public SQLiteDatabase open() {
        return open(false);
    }

    public SQLiteDatabase openForWriting() {
        return open(true);
    }

    private SQLiteDatabase open(boolean writable) {
        if (sConn == null) {
            final SQLiteOpenHelper helper = new Schema.OpenHelper(mContext);
            if (writable == false) {
                sConn = helper.getReadableDatabase();
            } else {
                sConn = helper.getWritableDatabase();
            }
        }
        return sConn;
    }

    public void close() {
    }

    public List<TaskFolder> getFolders() {
        final List<TaskFolder> ret = new LinkedList<TaskFolder>();
        final Cursor c = mContext.getContentResolver().query(TaskFolderProvider.CONTENT_URI, TaskFolderProvider.PROJECTION, null, null, null);
        try {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ret.add(TaskFolder.fromCursor(c, 0));
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public List<TaskContext> getContext() {
        final List<TaskContext> ret = new LinkedList<TaskContext>();
        final Cursor c = mContext.getContentResolver().query(TaskContextProvider.CONTENT_URI, TaskContextProvider.PROJECTION, null, null, null);
        try {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ret.add(TaskContext.fromCursor(c, 0));
            }
            return ret;
        } finally {
            c.close();
        }
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
