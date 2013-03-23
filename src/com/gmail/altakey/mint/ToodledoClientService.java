package com.gmail.altakey.mint;

import android.app.Service;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

public class ToodledoClientService extends IntentService {
    public static final String ACTION_SYNC = "com.gmail.altakey.mint.SYNC";
    public static final String ACTION_SYNC_DONE = "com.gmail.altakey.mint.SYNC_DONE";

    public static final String EXTRA_TASKS = "tasks";
    public static final String EXTRA_TASK_FIELDS = "task_fields";

    private DB mDB;
    private ToodledoClient mClient;

    public ToodledoClientService() {
        super("ToodledoClientService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDB = new DB(this);
        mClient = new ToodledoClient(Authenticator.create(this), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDB.close();
    }

    public static String asListOfTasks(Task... tasks) {
        return getGson().toJson(tasks, Task[].class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        try {
            mDB.openForWriting();

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
        } finally {
            mDB.close();
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
            final Synchronizer sync = new Synchronizer(mDB, mClient);
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
        private DB mmDB;
        private ToodledoClient mmClient;

        public Synchronizer(final DB db, final ToodledoClient client) {
            mmDB = db;
            mmClient = client;
        }

        private Map<String, Long> updatedSince(TaskStatus s) {
            final SQLiteDatabase conn = mmDB.open();
            final Map<String, Long> out = new HashMap<String, Long>();
            final Cursor c = conn.rawQuery("select lastedit_folder, lastedit_context, lastedit_task, lastdelete_task from status limit 1", null);
            if (c.getCount() > 0) {
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    if (s.lastedit_folder > c.getLong(0))
                        out.put("folder", c.getLong(0));
                    if (s.lastedit_context > c.getLong(1))
                        out.put("context", c.getLong(1));
                    if (s.lastedit_task > c.getLong(2))
                        out.put("task", c.getLong(2));
                    if (s.lastdelete_task > c.getLong(3))
                        out.put("task_delete", c.getLong(3));
                }
            } else {
                out.put("folder", Long.valueOf(0));
                out.put("context", Long.valueOf(0));
                out.put("task", Long.valueOf(0));
                out.put("task_delete", Long.valueOf(0));
            }
            return out;
        }

        public void update() throws IOException, Authenticator.BogusException, Authenticator.FailureException, Authenticator.ErrorException {
            final SQLiteDatabase conn = mmDB.open();
            final TaskStatus st = mmClient.getStatus();
            final Map<String, Long> flags = updatedSince(st);
            final Map<String, List<?>> data = new HashMap<String, List<?>>();

            if (flags.containsKey("folder_delete")) {
                data.put("folder_delete", mmClient.getFoldersDeletedAfter(flags.get("folder_delete")));
            }

            if (flags.containsKey("task_delete")) {
                data.put("task_delete", mmClient.getTasksDeletedAfter(flags.get("task_delete")));
            }

            if (flags.containsKey("folder")) {
                data.put("folder", mmClient.getFoldersAfter(flags.get("folder")));
            }

            if (flags.containsKey("context")) {
                data.put("context", mmClient.getContextsAfter(flags.get("context")));
            }

            if (flags.containsKey("task")) {
                data.put("task", mmClient.getTasksAfter(flags.get("task")));
            }

            try {
                conn.beginTransaction();

                conn.execSQL("INSERT OR REPLACE INTO status (status, lastedit_folder, lastedit_context, lastedit_goal, lastedit_location, lastedit_task, lastdelete_task, lastedit_notebook, lastdelete_notebook) VALUES (?,?,?,?,?,?,?,?,?)",
                              new String[] {
                                  st.id,
                                  String.valueOf(st.lastedit_folder),
                                  String.valueOf(st.lastedit_context),
                                  String.valueOf(st.lastedit_goal),
                                  String.valueOf(st.lastedit_location),
                                  String.valueOf(st.lastedit_task),
                                  String.valueOf(st.lastdelete_task),
                                  String.valueOf(st.lastedit_notebook),
                                  String.valueOf(st.lastdelete_notebook)
                              });

                if (data.containsKey("folder_delete")) {
                    for (TaskFolder t : (List<TaskFolder>)data.get("folder_delete")) {
                        conn.execSQL(
                            "DELETE FROM folders WHERE folder=?",
                            new String[] {
                                String.valueOf(t.id)
                            });
                    }
                }

                if (data.containsKey("task_delete")) {
                    for (Task t : (List<Task>)data.get("task_delete")) {
                        conn.execSQL(
                            "DELETE FROM tasks WHERE task=?",
                            new String[] {
                                String.valueOf(t.id)
                            });
                    }
                }

                if (data.containsKey("folder")) {
                    for (TaskFolder t : (List<TaskFolder>)data.get("folder")) {
                        conn.execSQL(
                            "INSERT OR REPLACE INTO folders (folder, name, private, archived, ord) VALUES (?,?,?,?,?)",
                            new String[] {
                                String.valueOf(t.id),
                                t.name,
                                String.valueOf(t.private_),
                                String.valueOf(t.archived),
                                String.valueOf(t.ord)
                            });
                    }
                }

                if (data.containsKey("context")) {
                    for (TaskContext t : (List<TaskContext>)data.get("context")) {
                        conn.execSQL(
                            "INSERT OR REPLACE INTO contexts (context, name) VALUES (?,?)",
                            new String[] {
                                String.valueOf(t.id),
                                t.name
                            });
                    }
                }

                if (data.containsKey("task")) {
                    for (Task t : (List<Task>)data.get("task")) {
                        conn.execSQL(
                            "INSERT OR REPLACE INTO tasks (task, title, note, modified, completed, folder, context, priority, star, duedate, duetime, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                            new String[] {
                                String.valueOf(t.id),
                                t.title,
                                t.note,
                                String.valueOf(t.modified),
                                String.valueOf(t.completed),
                                String.valueOf(t.folder),
                                String.valueOf(t.context),
                                String.valueOf(t.priority),
                                String.valueOf(t.star),
                                String.valueOf(t.duedate),
                                String.valueOf(t.duetime),
                                String.valueOf(t.status)
                            });
                    }
                }

                conn.setTransactionSuccessful();
            } finally {
                conn.endTransaction();
            }
        }

        public void commit() throws IOException, Authenticator.BogusException, Authenticator.FailureException, Authenticator.ErrorException {
            final SQLiteDatabase conn = mmDB.open();
            try {
                conn.beginTransaction();

                final TaskStatus st = mmClient.getStatus();
                mmClient.commitTasks(mmDB.getTasks(String.format("tasks.id is null or tasks.modified > %d", st.lastedit_task), null), new String[] { "note", "duedate", "duetime" });
            } finally {
                conn.endTransaction();
            }
        }
    }
}
