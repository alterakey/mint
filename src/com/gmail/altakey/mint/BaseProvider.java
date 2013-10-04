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

import java.util.Arrays;
import java.util.List;

public abstract class BaseProvider extends ContentProvider {
    protected SQLiteOpenHelper mHelper;

    @Override
    public String getType(Uri uri) {
        return new ProviderMap(uri).getContentType();
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int affected = 0;
        for (final ContentValues value : values) {
            if (insert(uri, value) != null) {
                ++affected;
            }
        }
        return affected;
    }

    @Override
    public boolean onCreate() {
        mHelper = new Schema.OpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Cursor c = doQuery(uri, projection, selection, selectionArgs, sortOrder);

        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return c;
    }

    protected abstract Cursor doQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);
}
