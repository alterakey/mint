package com.gmail.altakey.mint;

import android.content.Context;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.content.ContentUris;

import java.util.Arrays;
import java.util.List;

public class TaskContextProvider extends BaseProvider {
    public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/contexts", ProviderMap.AUTHORITY_CONTEXT));

    public static final String[] PROJECTION = new String[] {
        "_id", "context", "name"
    };

    public static final String DEFAULT_ORDER = "order by name";
    public static final String NO_ORDER = "";
    public static final String ID_FILTER = "_id=?";
    public static final String ALL_FILTER = "1=1";
    public static final String MULTIPLE_CONTEXTS_FILTER = "context in (%s)";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONTEXT = "context";
    public static final String COLUMN_NAME = "name";

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;

    private static final String CONTEXT_QUERY = "SELECT _id,context,name FROM contexts WHERE %s %s";

    private static final String CONTEXT_INSERT_QUERY = "INSERT OR IGNORE INTO contexts (context,name) VALUES (?,?)";

    private static final String CONTEXT_REPLACE_QUERY = "REPLACE INTO contexts (_id,context,name) VALUES (?,?,?)";

    private static final String CONTEXT_UPDATE_QUERY = "UPDATE contexts set context=?,name=? %s";

    private static final String CONTEXT_DELETE_QUERY = "DELETE FROM contexts %s";

    @Override
    public Cursor doQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mHelper.getReadableDatabase();

        switch (new ProviderMap(uri).getResourceType()) {
        case ProviderMap.CONTEXTS:
            return db.rawQuery(String.format(CONTEXT_QUERY, selection == null ? ALL_FILTER : selection, sortOrder == null ? DEFAULT_ORDER : sortOrder), selectionArgs);
        case ProviderMap.CONTEXTS_ID:
            return db.rawQuery(String.format(CONTEXT_QUERY, ID_FILTER, NO_ORDER), new String[] { String.valueOf(ContentUris.parseId(uri)) });
        default:
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int resourceType = new ProviderMap(uri).getResourceType();

        if (resourceType == ProviderMap.CONTEXTS) {
            final SQLiteStatement stmt = db.compileStatement(CONTEXT_INSERT_QUERY);
            ProviderUtils.bind(stmt, values, new String[] {
                    "context", "name"
            });
            try {
                final long id = stmt.executeInsert();
                if (id >= 0) {
                    return ContentUris.withAppendedId(uri, id);
                } else {
                    return null;
                }
            } finally {
                stmt.close();
            }
        } else if (resourceType == ProviderMap.CONTEXTS_ID) {
            final SQLiteStatement stmt = db.compileStatement(CONTEXT_REPLACE_QUERY);
            ProviderUtils.bind(stmt, values, new String[] {
                    "_id", "context", "name"
            });
            try {
                final long id = stmt.executeInsert();
                if (id >= 0) {
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

        if (resourceType == ProviderMap.CONTEXTS) {
            if (MULTIPLE_CONTEXTS_FILTER.equals(selection)) {
                selection = ProviderUtils.expandFilter(selection, selectionArgs);
            }

            final SQLiteStatement stmt = db.compileStatement(String.format(CONTEXT_UPDATE_QUERY, selection == null ? "" : String.format("WHERE %s", selection)));
            int offset = ProviderUtils.bind(stmt, values, new String[] {
                    "context", "name"
            });

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
                return stmt.executeUpdateDelete();
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
        case ProviderMap.CONTEXTS:
            if (MULTIPLE_CONTEXTS_FILTER.equals(selection)) {
                selection = ProviderUtils.expandFilter(selection, selectionArgs);
            }

            final SQLiteStatement stmt =
                db.compileStatement(String.format(CONTEXT_DELETE_QUERY, selection == null ? "" : String.format("WHERE %s", selection)));

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
                return stmt.executeUpdateDelete();
            } finally {
                stmt.close();
            }
        default:
            return 0;
        }
    }
}
