package com.gmail.altakey.mint;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.io.UnsupportedEncodingException;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;
import java.net.URLEncoder;

public class ToodledoClient {
    private Authenticator mAuth;
    private android.content.Context mContext;

    public ToodledoClient(Authenticator auth, android.content.Context context) {
        mAuth = auth;
        mContext = context;
    }

    public List<Folder> getFolders() throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        return getFoldersAfter(0);
    }

    public List<Folder> getFoldersAfter(long time) throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/folders/get.php?"
                + "key=%s",
                mAuth.authenticate()
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Folder>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Folder> getFoldersDeletedAfter(long time) throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/folders/deleted.php?"
                + "key=%s",
                mAuth.authenticate()
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Folder>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Context> getContexts() throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        return getContextsAfter(0);
    }

    public List<Context> getContextsAfter(long time) throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/contexts/get.php?"
                + "key=%s",
                mAuth.authenticate()
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Context>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Task> getTasks() throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        return getTasksAfter(0);
    }

    public List<Task> getTasksAfter(long time) throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/tasks/get.php?"
                + "key=%s&modafter=%s&fields=folder,context,star,priority,duedate",
                mAuth.authenticate(),
                String.valueOf(time)
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();

        try {
            List<Task> tasks = getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Task>>(){}.getType());
            tasks.remove(0);
            return tasks;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Task> getTasksDeletedAfter(long time) throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/tasks/deleted.php?"
                + "key=%s&after=%s",
                mAuth.authenticate(),
                String.valueOf(time)
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();

        try {
            List<Task> tasks = getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Task>>(){}.getType());
            tasks.remove(0);
            return tasks;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public Status getStatus() throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/account/get.php?"
                + "key=%s",
                mAuth.authenticate()
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();

        try {
            return getGson().fromJson(os.toString("UTF-8"), Status.class);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Status.class, new Status.JsonAdapter());
        builder.registerTypeAdapter(Folder.class, new Folder.JsonAdapter());
        builder.registerTypeAdapter(Task.class, new Task.JsonAdapter());
        builder.registerTypeAdapter(Context.class, new Context.JsonAdapter());
        return builder.create();
    }

    public void updateDone(Task t) throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();
        final String taskDesc = getGson().toJson(new Task[] {t});
        final String url = String.format(
                "http://api.toodledo.com/2/tasks/edit.php?key=%s&tasks=%s",
                mAuth.authenticate(),
                URLEncoder.encode(taskDesc)
            );

        HttpPost req = new HttpPost(
            url
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();
    }
}
