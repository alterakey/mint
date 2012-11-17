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

    public void update() throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        DB db = null;
        try {
            db = new DB(mContext);
            SQLiteDatabase conn = db.open();
            try {
                conn.beginTransaction();

                Status st = getStatus();
                Map<String, Long> flags = db.updatedSince(st);

                conn.execSQL("INSERT OR REPLACE INTO status (id, lastedit_folder, lastedit_context, lastedit_goal, lastedit_location, lastedit_task, lastdelete_task, lastedit_notebook, lastdelete_notebook) VALUES (?,?,?,?,?,?,?,?,?)",
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

                if (flags.containsKey("folder_delete")) {
                }

                if (flags.containsKey("task_delete")) {
                }

                if (flags.containsKey("folder")) {
                    for (Folder t : getFoldersAfter(flags.get("folder"))) {
                        conn.execSQL(
                            "INSERT OR REPLACE INTO folders (id, name, private, archived, ord) VALUES (?,?,?,?,?)",
                            new String[] {
                                String.valueOf(t.id),
                                t.name,
                                String.valueOf(t.private_),
                                String.valueOf(t.archived),
                                String.valueOf(t.ord)
                            });
                    }
                }

                if (flags.containsKey("context")) {
                    for (Context t : getContextsAfter(flags.get("context"))) {
                        conn.execSQL(
                            "INSERT OR REPLACE INTO contexts (id, name) VALUES (?,?)",
                            new String[] {
                                String.valueOf(t.id),
                                t.name
                            });
                    }
                }

                if (flags.containsKey("task")) {
                    for (Task t : getTasksAfter(flags.get("task"))) {
                        conn.execSQL(
                            "INSERT OR REPLACE INTO tasks (id, title, modified, completed, folder, context, priority, star) VALUES (?,?,?,?,?,?,?,?)",
                            new String[] {
                                String.valueOf(t.id),
                                t.title,
                                String.valueOf(t.modified),
                                String.valueOf(t.completed),
                                String.valueOf(t.folder),
                                String.valueOf(t.context),
                                String.valueOf(t.priority),
                                String.valueOf(t.star)
                            });
                    }
                }

                conn.setTransactionSuccessful();
            } finally {
                conn.endTransaction();
            }
        } finally {
            db.close();
        }
    }

    // Primarily cares synchronizers
    public static class DB {
        public static SQLiteDatabase conn = null;
        private static int ssRefs = 0;
        private android.content.Context mmContext;

        public DB(android.content.Context c) {
            mmContext = c;
        }

        public SQLiteDatabase open() {
            if (this.conn == null) {
                this.conn = new SQLiteOpenHelper(mmContext, "toodledo", null, 1) {
                    @Override
                    public void onCreate(SQLiteDatabase db) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS tasks (id BIGINT PRIMARY KEY, title TEXT, modified BIGINT, completed TEXT, folder BIGINT, context BIGINT, priority INTEGER, star INTEGER)");
                        db.execSQL("CREATE TABLE IF NOT EXISTS folders (id BIGINT PRIMARY KEY, name TEXT, private TEXT, archived TEXT, ord TEXT)");
                        db.execSQL("CREATE TABLE IF NOT EXISTS contexts (id BIGINT PRIMARY KEY, name TEXT)");
                        db.execSQL("CREATE TABLE IF NOT EXISTS status (id TEXT PRIMARY KEY, lastedit_folder BIGINT, lastedit_context BIGINT, lastedit_goal BIGINT, lastedit_location BIGINT, lastedit_task BIGINT, lastdelete_task BIGINT, lastedit_notebook BIGINT, lastdelete_notebook BIGINT)");
                    }

                    @Override
                    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    }
                }.getWritableDatabase();
            }
            ++ssRefs;
            return this.conn;
        }

        public void close() {
            if (--ssRefs <= 0) {
                if (this.conn != null) {
                    this.conn.close();
                    this.conn = null;
                }
                ssRefs = 0;
            }
        }

        public Map<String, Long> updatedSince(Status s) {
            Map<String, Long> out = new HashMap<String, Long>();
            Cursor c = null;
            try {
                SQLiteDatabase conn = open();
                c = conn.rawQuery("select lastedit_folder, lastedit_context, lastedit_task, lastdelete_task from status limit 1", null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    if (s.lastedit_folder > c.getLong(1))
                        out.put("folder", c.getLong(1));
                    if (s.lastedit_context > c.getLong(2))
                        out.put("context", c.getLong(2));
                    if (s.lastedit_task > c.getLong(3))
                        out.put("task", c.getLong(3));
                    if (s.lastdelete_task > c.getLong(4))
                        out.put("task_delete", c.getLong(4));
                } else {
                    out.put("folder", Long.valueOf(0));
                    out.put("context", Long.valueOf(0));
                    out.put("task", Long.valueOf(0));
                    out.put("task_delete", Long.valueOf(0));
                }
                return out;
            } finally {
                if (c != null)
                    c.close();
                close();
            }
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
