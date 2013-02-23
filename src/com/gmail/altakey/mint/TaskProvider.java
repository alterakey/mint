package com.gmail.altakey.mint;

import android.content.Context;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.UriMatcher;

public class TaskProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/tasks", TaskProvider.class.getCanonicalName()));
    private SQLiteOpenHelper mHelper;

    private static final String[] TASK_LIST_PROJECTION = new String[] {
        "id", "cookie", "task", "title", "note", "modified", "completed", "folder", "context", "priority", "star", "duedate", "duetime", "status"
    };
    public static final String DEFAULT_ORDER = "duedate,priority desc";
    public static final String HOTLIST_FILTER = "(priority=3 or (priority>=0 and duedate>0 and duedate<?)) and completed=0";

    @Override
    public String getType(Uri uri) {
        return new ProviderMap(uri).getType();
    }

    @Override
    public boolean onCreate() {
        mHelper = new Schema.OpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
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
