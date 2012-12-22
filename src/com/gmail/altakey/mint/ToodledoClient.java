package com.gmail.altakey.mint;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
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

    public List<Folder> getFolders() throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        return getFoldersAfter(0);
    }

    public List<Folder> getFoldersAfter(long time) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("folders/get", null)
            )
        );

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Folder>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Folder> getFoldersDeletedAfter(long time) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("folders/deleted", null)
            )
        );

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Folder>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Context> getContexts() throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        return getContextsAfter(0);
    }

    public List<Context> getContextsAfter(long time) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("contexts/get", null)
            )
        );

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Context>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Task> getTasks() throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        return getTasksAfter(0);
    }

    public List<Task> getTasksAfter(long time) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("tasks/get", String.format("modafter=%s&fields=folder,context,star,priority,duedate", String.valueOf(time)))
            )
        );

        try {
            List<Task> tasks = getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Task>>(){}.getType());
            tasks.remove(0);
            return tasks;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Task> getTasksDeletedAfter(long time) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("tasks/deleted", String.format("after=%s", String.valueOf(time)))
            )
        );

        try {
            List<Task> tasks = getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Task>>(){}.getType());
            tasks.remove(0);
            return tasks;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public Status getStatus() throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("account/get", null)
            )
        );

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

    public void updateDone(Task t) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        issueRequest(
            new HttpPost(
                getServiceUrl("tasks/edit", String.format("tasks=%s", URLEncoder.encode(getGson().toJson(new Task[] {t}))))
            )
        );
    }

    private ByteArrayOutputStream issueRequest(HttpRequestBase req) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final HttpClient client = new DefaultHttpClient();
        final HttpResponse response = client.execute(req);
        final HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();
        Log.d("TC.iR", String.format("got: %s", os.toString()));
        return os;
    }

    private String getServiceUrl(String service, String params) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        return String.format(
            "http://api.toodledo.com/2/%s.php?key=%s%s",
            service,
            mAuth.authenticate(),
            params == null ? "" : String.format("&%s", params)
        );
    }
}
