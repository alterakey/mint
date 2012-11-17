package com.gmail.altakey.mint;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
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

public class ToodledoClient {
    private Authenticator mAuth;
    private android.content.Context mContext;
    private Resolver mResolver = new Resolver();

    public ToodledoClient(Authenticator auth, android.content.Context context) {
        mAuth = auth;
        mContext = context;
    }

    public Resolver getResolver() {
        return mResolver;
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
            List<Folder> ret = new Gson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Folder>>(){}.getType());
            mResolver.feedFolders(ret);
            return ret;
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
            List<Context> ret = new Gson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Context>>(){}.getType());
            mResolver.feedContexts(ret);
            return ret;
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
            List<Task> tasks = new Gson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Task>>(){}.getType());
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
            return new Gson().fromJson(os.toString("UTF-8"), Status.class);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }



    public class Resolver {
        public Map<Long, Context> contextMap = new HashMap<Long, Context>();
        public Map<Long, Folder> folderMap = new HashMap<Long, Folder>();

        public void feedContexts(List<Context> contexts) {
            for (Context c : contexts) {
                contextMap.put(c.id, c);
            }
        }

        public void feedFolders(List<Folder> folders) {
            for (Folder f : folders) {
                folderMap.put(f.id, f);
            }
        }
    }
}
