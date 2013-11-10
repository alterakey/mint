package com.gmail.altakey.mint;

import android.content.Context;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.content.ContentUris;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;

public class TaskContextProvider extends BaseProvider {
    public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/contexts", ProviderMap.AUTHORITY_CONTEXT));

    public static final String[] PROJECTION = new String[] {
        "_id", "context", "name"
    };

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONTEXT = "context";
    public static final String COLUMN_NAME = "name";

    public static final int COL_ID = 0;
    public static final int COL_NAME = 2;

    private ToodledoClient getClient() {
        final Context context = getContext();
        return new ToodledoClient(Authenticator.create(context), context);
    }

    @Override
    public Cursor doQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mHelper.getReadableDatabase();

        try {
            switch (new ProviderMap(uri).getResourceType()) {
            case ProviderMap.CONTEXTS:
                return asTable(getClient().getContexts());
            case ProviderMap.CONTEXTS_ID:
                return asTable(filterWithId(ContentUris.parseId(uri), getClient().getContexts()));
            default:
                return null;
            }
        } catch (IOException e) {
            return null;
        } catch (Authenticator.BogusException e) {
            return null;
        } catch (Authenticator.FailureException e) {
            return null;
        } catch (Authenticator.ErrorException e) {
            return null;
        } finally {
            getContext().getContentResolver().notifyChange(TaskContextProvider.CONTENT_URI, null);
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

    private static Cursor asTable(final List<TaskContext> contexts) {
        final String[] cols = { "_id", "context", "name" };
        final MatrixCursor c = new MatrixCursor(cols);
        for (TaskContext context : contexts) {
            c.addRow(new Object[] { context.id, context.id, context.name });
        }
        return c;
    }

    private static List<TaskContext> filterWithId(final long id, final List<TaskContext> contexts) {
        for (TaskContext context : contexts) {
            if (id == context.id) {
                return Arrays.asList(new TaskContext[] { context });
            }
        }
        return Arrays.asList(new TaskContext[] {});
    }
}
