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

public class ToodledoClient {
    private Authenticator mAuth;

    public ToodledoClient(Authenticator auth) {
        mAuth = auth;
    }

    public List<Folder> getFolders() throws IOException, NoSuchAlgorithmException {
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
            return new Gson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Folder>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Context> getContexts() throws IOException, NoSuchAlgorithmException {
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
            return new Gson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Context>>(){}.getType());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Task> getTasks() throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/tasks/get.php?"
                + "key=%s&fields=folder,context,star,priority",
                mAuth.authenticate()
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();

        try {
            List<Task> tasks = new Gson().fromJson(os.toString("UTF-8"), new TypeToken<LinkedList<Task>>(){}.getType());
            tasks.remove(0);
            return resolve(tasks);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Task> resolve(List<Task> tasks) throws IOException, NoSuchAlgorithmException {
        Map<String, String> folderMap = new HashMap<String, String>();
        Map<String, String> contextMap = new HashMap<String, String>();

        for (Folder f : getFolders()) {
            folderMap.put(f.id, f.name);
        }
        for (Context c : getContexts()) {
            contextMap.put(c.id, c.name);
        }
        folderMap.put("0", "なし");
        contextMap.put("0", "なし");

        for (Task t : tasks) {
            t.folder = folderMap.get(t.folder);
            t.context = contextMap.get(t.context);
        }
        return tasks;
    }
}
