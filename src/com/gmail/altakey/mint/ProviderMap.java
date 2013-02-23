package com.gmail.altakey.mint;

import android.content.UriMatcher;
import android.net.Uri;

public class ProviderMap {
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

    public ProviderMap(final Uri uri) {
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
