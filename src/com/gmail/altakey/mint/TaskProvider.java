package com.gmail.altakey.mint;

import android.content.Context;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.UriMatcher;
import android.content.ContentUris;

public class TaskProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/tasks", ProviderMap.AUTHORITY_TASK));

    public static final String[] PROJECTION = new String[] {
        "id", "cookie", "task", "title", "note", "modified", "completed", "folder", "context", "priority", "star", "duedate", "duetime", "status", "folder_id", "folder_name", "folder_private", "folder_archived", "folder_ord", "context_id", "context_name"
    };

    public static final String DEFAULT_ORDER = "order by duedate,priority desc";
    public static final String NO_ORDER = "";
    public static final String HOTLIST_FILTER = "(priority=3 or (priority>=0 and duedate>0 and duedate<?)) and completed=0";
    public static final String ID_FILTER = "tasks.id=?";

    public static final String COLUMN_ID = "id";
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

    private static final String TASK_QUERY = "SELECT tasks.id,tasks.cookie,task,title,note,modified,completed,priority,star,duedate,duetime,status,folder as folder_id,folders.name as folder_name,folders.private as folder_private,folders.archived as folder_archived,folders.ord as folder_ord,context as context_id,contexts.name as context_name FROM tasks left join folders using (folder) left join contexts using (context) where %s %s";

    private static final String TASK_INSERT_QUERY = "insert into tasks (cookie,task,title,note,modified,completed,folder,context,priority,star,duedate,duetime,status) values (?,?,?,?,?,?,(select id from folders where name=?),(select id from contexts from name=?),?,?,?,?,?,?)";

    private static final String TASK_REPLACE_QUERY = "replace into tasks (id,cookie,task,title,note,modified,completed,folder,context,priority,star,duedate,duetime,status) values (?,?,?,?,?,?,?,(select id from folders where name=?),(select id from contexts from name=?),?,?,?,?,?,?)";

    private SQLiteOpenHelper mHelper;

    @Override
    public String getType(Uri uri) {
        return new ProviderMap(uri).getContentType();
    }

    @Override
    public boolean onCreate() {
        mHelper = new Schema.OpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mHelper.getReadableDatabase();

        switch (new ProviderMap(uri).getResourceType()) {
        case ProviderMap.TASKS:
            return db.rawQuery(String.format(TASK_QUERY, selection, sortOrder), selectionArgs);
        case ProviderMap.TASKS_ID:
            return db.rawQuery(String.format(TASK_QUERY, ID_FILTER, NO_ORDER), new String[] { String.valueOf(ContentUris.parseId(uri)) });
        default:
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            switch (new ProviderMap(uri).getResourceType()) {
            case ProviderMap.TASKS:
                final SQLiteStatement stmt =
                    new SQLiteStatement(
                        db,
                        String.format(TASK_INSERT_QUERY),
                        new Object[] {
                            newCookie(),
                            null,
                            values.get("title"),
                            values.get("note"),
                            values.get("modified"),
                            values.get("completed"),
                            values.get("folder_name"),
                            values.get("context_name"),
                            values.get("priority"),
                            values.get("star"),
                            values.get("duedate"),
                            values.get("duetime"),
                            values.get("status")
                        }
                );
                try {
                    return ContentUris.withAppendedId(uri, stmt.executeInsert());
                } finally {
                    stmt.close();
                }
            default:
                return null;
            }
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
