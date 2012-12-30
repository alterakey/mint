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
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;
import java.net.URLEncoder;

public class ToodledoClient {
    private Authenticator mAuth;
    private Context mContext;

    public ToodledoClient(Authenticator auth, Context context) {
        mContext = context;
        setAuthenticator(auth);
    }

    public void setAuthenticator(Authenticator auth) {
        mAuth = auth;
    }

    public List<TaskFolder> getFolders() throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        return getFoldersAfter(0);
    }

    public List<TaskFolder> getFoldersAfter(long time) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("folders/get", null)
            )
        );

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<TaskFolder>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TaskFolder> getFoldersDeletedAfter(long time) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("folders/deleted", null)
            )
        );

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<TaskFolder>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TaskContext> getContexts() throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        return getContextsAfter(0);
    }

    public List<TaskContext> getContextsAfter(long time) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("contexts/get", null)
            )
        );

        try {
            return getGson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<TaskContext>>(){}.getType());
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
                getServiceUrl("tasks/get", String.format("modafter=%s&fields=folder,context,star,priority,duedate,status", String.valueOf(time)))
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

    public void addTask(Task t, String[] additionalFields) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        addTasks(Arrays.asList(t), additionalFields);
    }

    public void addTasks(List<Task> tasks, String[] additionalFields) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final String fields = additionalFields == null ? "" : String.format("&fields=%s", Joiner.on(",").join(additionalFields));

        issueRequest(
            new HttpPost(
                getServiceUrl("tasks/add", String.format("tasks=%s%s", URLEncoder.encode(getGson().toJson(tasks.toArray(new Task[0])), "UTF-8"), fields))
            )
        );
    }

    public void commitTask(Task t, String[] additionalFields) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        commitTasks(Arrays.asList(t), additionalFields);
    }

    public void commitTasks(List<Task> tasks, String[] additionalFields) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final String fields = additionalFields == null ? "" : String.format("&fields=%s", Joiner.on(",").join(additionalFields));

        issueRequest(
            new HttpPost(
                getServiceUrl("tasks/edit", String.format("tasks=%s%s", URLEncoder.encode(getGson().toJson(tasks.toArray(new Task[0])), "UTF-8"), fields))
            )
        );
    }

    public TaskStatus getStatus() throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        final ByteArrayOutputStream os = issueRequest(
            new HttpGet(
                getServiceUrl("account/get", null)
            )
        );

        try {
            return getGson().fromJson(os.toString("UTF-8"), TaskStatus.class);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(TaskStatus.class, new TaskStatus.JsonAdapter());
        builder.registerTypeAdapter(TaskFolder.class, new TaskFolder.JsonAdapter());
        builder.registerTypeAdapter(Task.class, new Task.JsonAdapter());
        builder.registerTypeAdapter(TaskContext.class, new TaskContext.JsonAdapter());
        return builder.create();
    }

    public void updateDone(Task t) throws IOException, Authenticator.BogusException, Authenticator.ErrorException, Authenticator.FailureException {
        issueRequest(
            new HttpPost(
                getServiceUrl("tasks/edit", String.format("tasks=%s", URLEncoder.encode(getGson().toJson(new Task[] {t}), "UTF-8")))
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
