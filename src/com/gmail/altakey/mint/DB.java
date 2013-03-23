package com.gmail.altakey.mint;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;

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
        List<TaskFolder> ret = new LinkedList<TaskFolder>();
        Cursor c = sConn.rawQuery("SELECT folder,name,private,archived,ord FROM folders", null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                ret.add(TaskFolder.fromCursor(c, 0));
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public List<TaskContext> getContext() {
        List<TaskContext> ret = new LinkedList<TaskContext>();
        Cursor c = sConn.rawQuery("SELECT context,name FROM contexts", null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                ret.add(TaskContext.fromCursor(c, 0));
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    private final String TASK_QUERY = "SELECT tasks.id,tasks.cookie,task,title,note,modified,completed,folder,context,priority,star,duedate,duetime,status,folder as folder_id,folders.name as folder_name,folders.private as folder_private,folders.archived as folder_archived,folders.ord as folder_ord,context as context_id,contexts.name as context_name FROM tasks left join folders using (folder) left join contexts using (context) where %s order by %s";
    public static final String DEFAULT_ORDER = "duedate,priority desc";
    public static final String HOT_FILTER = "(priority=3 or (priority>=0 and duedate>0 and duedate<?)) and completed=0";
    public static final String ALL_FILTER = "1=1";

    public List<Task> getTasks(String filter, String order) {
        List<Task> ret = new LinkedList<Task>();
        Cursor c = sConn.rawQuery(
            String.format(TASK_QUERY, filter, DEFAULT_ORDER), null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Task task = Task.fromCursor(c, 0);
                task.resolved.folder = TaskFolder.fromCursor(c, 14);
                task.resolved.context = TaskContext.fromCursor(c, 19);
                ret.add(task);
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public List<Task> getHotTasks() {
        List<Task> ret = new LinkedList<Task>();
        String due = String.format("%d", (new Date().getTime() + (7 * 86400 * 1000)) / 1000);
        Cursor c = sConn.rawQuery(
            String.format(TASK_QUERY, HOT_FILTER, DEFAULT_ORDER), new String[] { due });
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Task task = Task.fromCursor(c, 0);
                task.resolved.folder = TaskFolder.fromCursor(c, 14);
                task.resolved.context = TaskContext.fromCursor(c, 19);
                ret.add(task);
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public Task getTaskById(long taskId) {
        Cursor c = sConn.rawQuery(
            String.format(TASK_QUERY, String.format("id=%d", taskId), DEFAULT_ORDER), null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Task task = Task.fromCursor(c, 0);
                task.resolved.folder = TaskFolder.fromCursor(c, 14);
                task.resolved.context = TaskContext.fromCursor(c, 19);
                return task;
            }
            return null;
        } finally {
            c.close();
        }
    }

    public void addTask(Task task) {
        try {
            sConn.beginTransaction();
            sConn.execSQL(
                "INSERT INTO tasks (cookie,title,note,modified,completed,folder,context,priority,star,duedate,duetime,status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                new String[] {
                    nextCookie(), task.title, task.note, String.valueOf(task.modified), String.valueOf(task.completed), String.valueOf(task.folder),
                    String.valueOf(task.context), String.valueOf(task.priority), String.valueOf(task.star), String.valueOf(task.duedate), String.valueOf(task.duetime), task.status
                }
            );
            sConn.setTransactionSuccessful();
        } finally {
            sConn.endTransaction();
        }
    }

    public void commitTask(Task task) {
        try {
            sConn.beginTransaction();
            sConn.execSQL(
                "UPDATE tasks SET title=?,note=?,modified=?,completed=?,folder=?,context=?,priority=?,star=?,duedate=?,duetime=?,status=? WHERE id=?",
                new String[] {
                    task.title, task.note, String.valueOf(task.modified), String.valueOf(task.completed), String.valueOf(task.folder),
                    String.valueOf(task.context), String.valueOf(task.priority), String.valueOf(task.star), String.valueOf(task.duedate), String.valueOf(task.duetime), task.status, String.valueOf(task._id)
                }
            );
            sConn.setTransactionSuccessful();
        } finally {
            sConn.endTransaction();
        }
    }

    public static String nextCookie() {
        return UUID.randomUUID().toString();
    }
}
