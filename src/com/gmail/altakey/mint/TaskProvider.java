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

    public static class Schema {
        public static final String DATABASE = "toodledo";
        public static final int VERSION = 1;

        public static class OpenHelper extends SQLiteOpenHelper {
            public OpenHelper(final Context ctx) {
                super(ctx, DATABASE, null, VERSION);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, cookie VARCHAR UNIQUE, task BIGINT UNIQUE, title TEXT, note TEXT, modified BIGINT, completed TEXT, folder BIGINT, context BIGINT, priority INTEGER, star INTEGER, duedate BIGINT, duetime BIGINT, status BIGINT)");
                db.execSQL("CREATE TABLE IF NOT EXISTS folders (folder BIGINT PRIMARY KEY, name TEXT, private TEXT, archived TEXT, ord TEXT)");
                db.execSQL("CREATE TABLE IF NOT EXISTS contexts (context BIGINT PRIMARY KEY, name TEXT)");
                db.execSQL("CREATE TABLE IF NOT EXISTS status (status TEXT PRIMARY KEY, lastedit_folder BIGINT, lastedit_context BIGINT, lastedit_goal BIGINT, lastedit_location BIGINT, lastedit_task BIGINT, lastdelete_task BIGINT, lastedit_notebook BIGINT, lastdelete_notebook BIGINT)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }
        };
    }

    private static class ContentUriMap {
        private static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        private static final int TASKS = 1;
        private static final int TASKS_ID = 2;
        private static final int FOLDERS = 3;
        private static final int FOLDERS_ID = 4;
        private static final int CONTEXTS = 5;
        private static final int CONTEXTS_ID = 6;
        private static final int STATUSES = 7;
        private static final int STATUSES_ID = 8;

        private static final String SINGLE_ITEM_TYPE = "vnd.android.cursor.item/%s";
        private static final String MULTIPLE_ITEM_TYPE = "vnd.android.cursor.dir/%s";

        static {
            final String taskProvider = TaskProvider.class.getCanonicalName();
            sMatcher.addURI(taskProvider, "tasks", TASKS);
            sMatcher.addURI(taskProvider, "tasks/#", TASKS);
            sMatcher.addURI(taskProvider, "folders", FOLDERS);
            sMatcher.addURI(taskProvider, "folders/#", FOLDERS_ID);
            sMatcher.addURI(taskProvider, "contexts", CONTEXTS);
            sMatcher.addURI(taskProvider, "contexts/#", CONTEXTS_ID);
            sMatcher.addURI(taskProvider, "statuses", STATUSES);
            sMatcher.addURI(taskProvider, "statuses/#", STATUSES_ID);
        }

        private final Uri mUri;

        public ContentUriMap(final Uri uri) {
            mUri = uri;
        }

        public String getType() {
            switch (sMatcher.match(mUri)) {
            case TASKS:
                return String.format(MULTIPLE_ITEM_TYPE, "task");
            case TASKS_ID:
                return String.format(SINGLE_ITEM_TYPE, "task");
            case FOLDERS:
                return String.format(MULTIPLE_ITEM_TYPE, "folder");
            case FOLDERS_ID:
                return String.format(SINGLE_ITEM_TYPE, "folder");
            case CONTEXTS:
                return String.format(MULTIPLE_ITEM_TYPE, "context");
            case CONTEXTS_ID:
                return String.format(SINGLE_ITEM_TYPE, "context");
            case STATUSES:
                return String.format(MULTIPLE_ITEM_TYPE, "status");
            case STATUSES_ID:
                return String.format(SINGLE_ITEM_TYPE, "status");
            default:
                return null;
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        return new ContentUriMap(uri).getType();
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
