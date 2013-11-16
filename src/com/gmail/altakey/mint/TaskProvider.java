package com.gmail.altakey.mint;

import android.content.Context;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.content.ContentUris;
import android.util.Log;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

public class TaskProvider extends BaseProvider {
    public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/tasks", ProviderMap.AUTHORITY_TASK));

    public static final String[] PROJECTION = new String[] {
        "_id", "cookie", "task", "title", "note", "modified", "completed", "folder", "context", "priority", "star", "duedate", "duetime", "status", "folder_id", "folder_name", "folder_private", "folder_archived", "folder_ord", "context_id", "context_name"
    };

    public static final String DEFAULT_ORDER = "order by _duedate_sort_key,priority desc";
    public static final String NO_ORDER = "";
    public static final String HOTLIST_FILTER = "(priority=3 or (priority>=0 and duedate>0 and duedate<?)) and completed=0";
    public static final String ID_FILTER = "tasks._id=?";
    public static final String ALL_FILTER = "1=1";
    public static final String MULTIPLE_TASKS_FILTER = "task in (%s)";
    public static final String DIRTY_SINCE_FILTER = "tasks.task is null or tasks.modified > ?";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COOKIE = "cookie";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_MODIFIED = "modified";
    public static final String COLUMN_COMPLETED = "completed";
    public static final String COLUMN_FOLDER = "folder";
    public static final String COLUMN_CONTEXT = "context";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_STAR = "star";
    public static final String COLUMN_DUEDATE = "duedate";
    public static final String COLUMN_DUETIME = "duetime";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_FOLDER_ID = "folder_id";
    public static final String COLUMN_FOLDER_NAME = "folder_name";
    public static final String COLUMN_FOLDER_PRIVATE = "folder_private";
    public static final String COLUMN_FOLDER_ARCHIVED = "folder_archived";
    public static final String COLUMN_FOLDER_ORD = "folder_ord";
    public static final String COLUMN_CONTEXT_ID = "context_id";
    public static final String COLUMN_CONTEXT_NAME = "context_name";

    public static final int COL_ID = 0;
    public static final int COL_COOKIE = 1;
    public static final int COL_TASK = 2;
    public static final int COL_TITLE = 3;
    public static final int COL_NOTE = 4;
    public static final int COL_MODIFIED = 5;
    public static final int COL_COMPLETED = 6;
    public static final int COL_FOLDER = 7;
    public static final int COL_CONTEXT = 8;
    public static final int COL_PRIORITY = 9;
    public static final int COL_STAR = 10;
    public static final int COL_DUEDATE = 11;
    public static final int COL_DUETIME = 12;
    public static final int COL_STATUS = 13;
    public static final int COL_FOLDER_ID = 14;
    public static final int COL_FOLDER_NAME = 15;
    public static final int COL_FOLDER_PRIVATE = 16;
    public static final int COL_FOLDER_ARCHIVED = 17;
    public static final int COL_FOLDER_ORD = 18;
    public static final int COL_CONTEXT_ID = 19;
    public static final int COL_CONTEXT_NAME = 20;

    private static final String TASK_QUERY = "SELECT tasks._id,tasks.cookie,task,title,tasks.note,tasks.modified,tasks.completed,tasks.priority,tasks.star,tasks.duedate,tasks.duetime,status,folder AS folder_id,folders.name AS folder_name,folders.private AS folder_private,folders.archived AS folder_archived,folders.ord AS folder_ord,context AS context_id,contexts.name AS context_name,(case when tasks.duedate=0 then 9223372036854775807 else tasks.duedate end) as _duedate_sort_key FROM tasks LEFT JOIN folders USING (folder) LEFT JOIN contexts USING (context) WHERE %s %s";

    private static final String TASK_INSERT_QUERY = "INSERT OR REPLACE INTO tasks (cookie,task,title,note,modified,completed,folder,context,priority,star,duedate,duetime,status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String TASK_REPLACE_QUERY = "REPLACE INTO tasks (_id,cookie,task,title,note,modified,completed,folder,context,priority,star,duedate,duetime,status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String TASK_UPDATE_QUERY = "UPDATE tasks SET %s %s";

    private static final String TASK_DELETE_QUERY = "DELETE FROM tasks %s";

    @Override
    public Cursor doQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mHelper.getReadableDatabase();

        switch (new ProviderMap(uri).getResourceType()) {
        case ProviderMap.TASKS:
            return db.rawQuery(String.format(TASK_QUERY, selection == null ? ALL_FILTER : selection, sortOrder == null ? DEFAULT_ORDER : sortOrder), selectionArgs);
        case ProviderMap.TASKS_ID:
            return db.rawQuery(String.format(TASK_QUERY, ID_FILTER, NO_ORDER), new String[] { String.valueOf(ContentUris.parseId(uri)) });
        default:
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int resourceType = new ProviderMap(uri).getResourceType();

        if (resourceType == ProviderMap.TASKS) {
            final SQLiteStatement stmt = db.compileStatement(TASK_INSERT_QUERY);
            ProviderUtils.bind(stmt, values, new String[] {
                    "cookie", "task", "title", "note", "modified",
                    "completed", "folder", "context", "priority", "star",
                    "duedate", "duetime", "status"
            });
            try {
                final long id = stmt.executeInsert();
                if (id >= 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(uri, id);
                } else {
                    return null;
                }
            } finally {
                stmt.close();
            }
        } else if (resourceType == ProviderMap.TASKS_ID) {
            final SQLiteStatement stmt = db.compileStatement(TASK_REPLACE_QUERY);
            ProviderUtils.bind(stmt, values, new String[] {
                    "_id", "cookie", "task", "title", "note",
                    "modified", "completed", "folder", "context", "priority",
                    "star", "duedate", "duetime", "status"
            });
            try {
                final long id = stmt.executeInsert();
                if (id >= 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return uri;
                } else {
                    return null;
                }
            } finally {
                stmt.close();
            }
        } else {
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int resourceType = new ProviderMap(uri).getResourceType();

        if (resourceType == ProviderMap.TASKS) {
            if (MULTIPLE_TASKS_FILTER.equals(selection)) {
                selection = ProviderUtils.expandFilter(selection, selectionArgs);
            }

            final List<String> holders = new LinkedList<String>();
            for (String k: values.keySet()) {
                holders.add(String.format("%s=?", k));
            }

            final SQLiteStatement stmt = db.compileStatement(String.format(TASK_UPDATE_QUERY, TextUtils.join(",", holders), selection == null ? "" : String.format("WHERE %s", selection)));
            int offset = ProviderUtils.bind(stmt, values, values.keySet());

            if (selectionArgs != null) {
                for (final String arg: selectionArgs) {
                    if (arg != null) {
                        stmt.bindString(offset++, arg);
                    } else {
                        stmt.bindNull(offset++);
                    }
                }
            }

            try {
                final int affected = stmt.executeUpdateDelete();
                if (affected > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return affected;
            } finally {
                stmt.close();
            }
        } else {
            return 0;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();

        switch (new ProviderMap(uri).getResourceType()) {
        case ProviderMap.TASKS:
            if (MULTIPLE_TASKS_FILTER.equals(selection)) {
                selection = ProviderUtils.expandFilter(selection, selectionArgs);
            }

            final SQLiteStatement stmt =
                db.compileStatement(String.format(TASK_DELETE_QUERY, selection == null ? "" : String.format("WHERE %s", selection)));

            if (selectionArgs != null) {
                int offset = 1;
                for (final String arg: selectionArgs) {
                    if (arg != null) {
                        stmt.bindString(offset++, arg);
                    } else {
                        stmt.bindNull(offset++);
                    }
                }
            }

            try {
                final int affected = stmt.executeUpdateDelete();
                if (affected > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return affected;
            } finally {
                stmt.close();
            }
        default:
            return 0;
        }
    }
}
