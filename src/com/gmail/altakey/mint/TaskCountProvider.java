package com.gmail.altakey.mint;

import android.content.Context;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteStatement;
import android.content.UriMatcher;
import android.content.ContentUris;
import android.database.Cursor;
import android.database.MatrixCursor;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class TaskCountProvider extends BaseProvider {
    public static final Uri CONTENT_URI_BY_STATUS = Uri.parse(String.format("content://%s/count/by-status", ProviderMap.AUTHORITY_TASK_COUNT));
    public static final Uri CONTENT_URI_BY_FOLDER = Uri.parse(String.format("content://%s/count/by-folder", ProviderMap.AUTHORITY_TASK_COUNT));
    public static final Uri CONTENT_URI_BY_CONTEXT = Uri.parse(String.format("content://%s/count/by-context", ProviderMap.AUTHORITY_TASK_COUNT));

    public static final String[] PROJECTION = new String[] {
        "_id", "title", "count"
    };

    public static final String DEFAULT_ORDER = "";
    public static final String ALL_FILTER = "1=1";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_COUNT = "count";

    public static final int COL_ID = 0;
    public static final int COL_COOKIE = 1;
    public static final int COL_COUNT = 2;

    private static final String QUERY_BY_STATUS = "SELECT statuses.status AS _id,statuses.name AS title,(SELECT COUNT(1) FROM tasks WHERE tasks.status=status) AS count FROM statuses WHERE %s";
    private static final String QUERY_BY_FOLDER = "SELECT folders.folder AS _id,folders.name AS title,(SELECT COUNT(1) FROM tasks WHERE tasks.folder=folder) AS count FROM folders WHERE %s";
    private static final String QUERY_BY_CONTEXT = "SELECT contexts.context AS _id,contexts.name AS title,(SELECT COUNT(1) FROM tasks WHERE tasks.context=context) AS count FROM contexts WHERE %s";

    @Override
    public Cursor doQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mHelper.getReadableDatabase();

        switch (new ProviderMap(uri).getResourceType()) {
        case ProviderMap.TASK_COUNT_BY_STATUS:
            return db.rawQuery(String.format(QUERY_BY_STATUS, selection == null ? ALL_FILTER : selection, sortOrder == null ? DEFAULT_ORDER : sortOrder), selectionArgs);
        case ProviderMap.TASK_COUNT_BY_FOLDER:
            return db.rawQuery(String.format(QUERY_BY_FOLDER, selection == null ? ALL_FILTER : selection, sortOrder == null ? DEFAULT_ORDER : sortOrder), selectionArgs);
        case ProviderMap.TASK_COUNT_BY_CONTEXT:
            return db.rawQuery(String.format(QUERY_BY_CONTEXT, selection == null ? ALL_FILTER : selection, sortOrder == null ? DEFAULT_ORDER : sortOrder), selectionArgs);
        default:
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
