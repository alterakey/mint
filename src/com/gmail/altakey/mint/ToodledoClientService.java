package com.gmail.altakey.mint;

import android.app.Service;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class ToodledoClientService extends IntentService {
    public static final String ACTION_SYNC = "com.gmail.altakey.mint.SYNC";
    public static final String ACTION_SYNC_DONE = "com.gmail.altakey.mint.SYNC_DONE";

    public static final String EXTRA_TASKS = "tasks";
    public static final String EXTRA_TASK_FIELDS = "task_fields";

    private ToodledoClient mClient;

    public ToodledoClientService() {
        super("ToodledoClientService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClient = new ToodledoClient(Authenticator.create(this), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static String asListOfTasks(Task... tasks) {
        return getGson().toJson(tasks, Task[].class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        try {
            if (ACTION_SYNC.equals(action)) {
                sync();
                sync_done();
            }
        } catch (IOException e) {
            abort(e.getMessage());
        } catch (Authenticator.FailureException e) {
            fail();
        } catch (Authenticator.BogusException e) {
            require();
        } catch (Authenticator.Exception e) {
            abort(e.getMessage());
        }
    }

    private void abort(String message) {
        new Notifier(this).boo(String.format("sync failure: %s", message));
    }

    private void fail() {
        new Notifier(this).notify("Login failed", Notifier.NOTIFY_LOGIN_FAILED);
    }

    private void require() {
        new Notifier(this).notifyOnce("Setup synchronization", Notifier.NOTIFY_LOGIN_REQUIRED);
    }

    private void sync() throws IOException, Authenticator.Exception {
        final Notifier notifier = new Notifier(this);
        try {
            notifier.notify("Syncing...", "SYNC");
            final Synchronizer sync = new Synchronizer(this, mClient);
            sync.update();
            sync.commit();
        } finally {
            notifier.cancel();
        }
    }

    private void sync_done() {
        final Intent intent = new Intent(ACTION_SYNC_DONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private static Gson getGson() {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(TaskStatus.class, new TaskStatus.JsonAdapter());
        builder.registerTypeAdapter(TaskFolder.class, new TaskFolder.JsonAdapter());
        builder.registerTypeAdapter(Task.class, new Task.JsonAdapter());
        builder.registerTypeAdapter(TaskContext.class, new TaskContext.JsonAdapter());
        return builder.create();
    }

    public static class Synchronizer {
        private Context mmContext;
        private ToodledoClient mmClient;

        public Synchronizer(final Context context, final ToodledoClient client) {
            mmContext = context;
            mmClient = client;
        }

        private Map<String, Long> updatedSince(TaskStatus s) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mmContext);
            final Map<String, Long> out = new HashMap<String, Long>();

            final TaskStatus known = new TaskStatus();
            known.lastedit_task = pref.getLong("lastedit_task", 0);
            known.lastdelete_task = pref.getLong("lastdelete_task", 0);
            known.lastedit_context = pref.getLong("lastedit_context", 0);
            known.lastedit_folder = pref.getLong("lastedit_folder", 0);

            if (s.lastedit_folder > known.lastedit_folder) {
                out.put("folder", 1L);
            }
            if (s.lastedit_context > known.lastedit_context) {
                out.put("context", 1L);
            }
            if (s.lastedit_task > known.lastedit_task) {
                out.put("task", known.lastedit_task);
            }
            if (s.lastdelete_task > known.lastdelete_task) {
                out.put("task_delete", known.lastdelete_task);
            }
            return out;
        }

        public void update() throws IOException, Authenticator.BogusException, Authenticator.FailureException, Authenticator.ErrorException {
            final TaskStatus st = mmClient.getStatus();
            final Map<String, Long> flags = updatedSince(st);
            final Map<String, List<?>> data = new HashMap<String, List<?>>();
            final Set<String> notifyNeeded = new HashSet<String>();

            if (flags.containsKey("folder")) {
                data.put("folder", mmClient.getFolders());
            }

            if (flags.containsKey("context")) {
                data.put("context", mmClient.getContexts());
            }

            if (flags.containsKey("task_delete")) {
                data.put("task_delete", mmClient.getTasksDeletedAfter(flags.get("task_delete")));
            }

            if (flags.containsKey("task")) {
                data.put("task", mmClient.getTasksAfter(flags.get("task")));
            }

            final ContentResolver resolver = mmContext.getContentResolver();
            if (data.containsKey("folder")) {
                final List<ContentValues> rows = new LinkedList<ContentValues>();
                for (TaskFolder t : (List<TaskFolder>)data.get("folder")) {
                    final ContentValues row = new ContentValues();
                    row.put(TaskFolderProvider.COLUMN_FOLDER, t.id);
                    row.put(TaskFolderProvider.COLUMN_NAME, t.name);
                    row.put(TaskFolderProvider.COLUMN_PRIVATE, t.private_);
                    row.put(TaskFolderProvider.COLUMN_ARCHIVED, t.archived);
                    row.put(TaskFolderProvider.COLUMN_ORD, t.ord);
                    rows.add(row);
                }
                resolver.delete(TaskFolderProvider.CONTENT_URI, null, null);
                resolver.bulkInsert(TaskFolderProvider.CONTENT_URI, rows.toArray(new ContentValues[] {}));
                notifyNeeded.add("folder");
            }

            if (data.containsKey("context")) {
                final List<ContentValues> rows = new LinkedList<ContentValues>();
                for (TaskContext t : (List<TaskContext>)data.get("context")) {
                    final ContentValues row = new ContentValues();
                    row.put(TaskContextProvider.COLUMN_CONTEXT, t.id);
                    row.put(TaskContextProvider.COLUMN_NAME, t.name);
                    rows.add(row);
                }
                resolver.delete(TaskContextProvider.CONTENT_URI, null, null);
                resolver.bulkInsert(TaskContextProvider.CONTENT_URI, rows.toArray(new ContentValues[] {}));
                notifyNeeded.add("context");
            }

            if (data.containsKey("task_delete")) {
                final List<String> args = new LinkedList<String>();
                for (Task t : (List<Task>)data.get("task_delete")) {
                    args.add(String.valueOf(t.id));
                }
                if (0 < resolver.delete(TaskProvider.CONTENT_URI, TaskProvider.MULTIPLE_TASKS_FILTER, args.toArray(new String[] {}))) {
                    notifyNeeded.add("task");
                }
            }

            if (data.containsKey("task")) {
                final List<ContentValues> rows = new LinkedList<ContentValues>();
                for (Task t : (List<Task>)data.get("task")) {
                    final ContentValues row = new ContentValues();
                    row.put(TaskProvider.COLUMN_TASK, t.id);
                    row.put(TaskProvider.COLUMN_TITLE, t.title);
                    row.put(TaskProvider.COLUMN_NOTE, t.note);
                    row.put(TaskProvider.COLUMN_MODIFIED, t.modified);
                    row.put(TaskProvider.COLUMN_COMPLETED, t.completed);
                    row.put(TaskProvider.COLUMN_FOLDER, t.folder);
                    row.put(TaskProvider.COLUMN_CONTEXT, t.context);
                    row.put(TaskProvider.COLUMN_PRIORITY, t.priority);
                    row.put(TaskProvider.COLUMN_STAR, t.star);
                    row.put(TaskProvider.COLUMN_DUEDATE, t.duedate);
                    row.put(TaskProvider.COLUMN_DUETIME, t.duetime);
                    row.put(TaskProvider.COLUMN_STATUS, t.status);
                    rows.add(row);
                }
                if (0 < resolver.bulkInsert(TaskProvider.CONTENT_URI, rows.toArray(new ContentValues[] {}))) {
                    notifyNeeded.add("task");
                }
            }

            recordStatus(st);

            for (final String key: notifyNeeded) {
                if ("task".equals(key)) {
                    resolver.notifyChange(TaskProvider.CONTENT_URI, null);
                    resolver.notifyChange(TaskCountProvider.CONTENT_URI_BY_FOLDER, null);
                    resolver.notifyChange(TaskCountProvider.CONTENT_URI_BY_CONTEXT, null);
                    resolver.notifyChange(TaskCountProvider.CONTENT_URI_BY_STATUS, null);
                }
                if ("folder".equals(key)) {
                    resolver.notifyChange(TaskFolderProvider.CONTENT_URI, null);
                    resolver.notifyChange(TaskCountProvider.CONTENT_URI_BY_FOLDER, null);
                }
                if ("context".equals(key)) {
                    resolver.notifyChange(TaskContextProvider.CONTENT_URI, null);
                    resolver.notifyChange(TaskCountProvider.CONTENT_URI_BY_CONTEXT, null);
                }
            }
        }

        public void commit() throws IOException, Authenticator.BogusException, Authenticator.FailureException, Authenticator.ErrorException {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mmContext);
            final long lastedit_task = pref.getLong("lastedit_task", 0L);
            final Cursor c = mmContext.getContentResolver().query(TaskProvider.CONTENT_URI, TaskProvider.PROJECTION, TaskProvider.DIRTY_SINCE_FILTER, new String[] { String.valueOf(lastedit_task) }, null);
            if (c != null) {
                try {
                    if (c.getCount() > 0) {
                        final List<Task> tasks = new LinkedList<Task>();
                        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                            tasks.add(Task.fromCursor(c, 0));
                        }
                        mmClient.commitTasks(tasks, null);
                        recordStatus();
                    }
                } finally {
                    c.close();
                }
            }
        }

        private void recordStatus() throws IOException, Authenticator.BogusException, Authenticator.FailureException, Authenticator.ErrorException {
            recordStatus(mmClient.getStatus());
        }

        private void recordStatus(TaskStatus st) throws IOException, Authenticator.BogusException, Authenticator.FailureException, Authenticator.ErrorException {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mmContext);
            pref.edit()
                .putLong("lastedit_folder", st.lastedit_folder)
                .putLong("lastedit_context", st.lastedit_context)
                .putLong("lastedit_goal", st.lastedit_goal)
                .putLong("lastedit_location", st.lastedit_location)
                .putLong("lastedit_task", st.lastedit_task)
                .putLong("lastdelete_task", st.lastdelete_task)
                .putLong("lastedit_notebook", st.lastedit_notebook)
                .putLong("lastdelete_notebook", st.lastdelete_notebook)
                .commit();
        }
    }
}
