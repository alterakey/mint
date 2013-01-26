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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.io.IOException;

public class ToodledoClientService extends IntentService {
    public static final String ACTION_SYNC = "com.gmail.altakey.mint.SYNC";
    public static final String ACTION_SYNC_DONE = "com.gmail.altakey.mint.SYNC_DONE";

    public static final String EXTRA_TASKS = "tasks";
    public static final String EXTRA_TASK_FIELDS = "task_fields";

    private NetworkTask mNetworkTask;
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
            mDB.update(mClient);
            mDB.commit(mClient);
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
}
