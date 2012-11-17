package com.gmail.altakey.mint;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

// Primarily cares synchronizers
public class DB {
    public static SQLiteDatabase conn = null;
    private static int ssRefs = 0;
    private android.content.Context mContext;

    public DB(android.content.Context c) {
        mContext = c;
    }

    public SQLiteDatabase open() {
        if (this.conn == null) {
            this.conn = new SQLiteOpenHelper(mContext, "toodledo", null, 1) {
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
        Cursor c = conn.rawQuery("select lastedit_folder, lastedit_context, lastedit_task, lastdelete_task from status limit 1", null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            if (s.lastedit_folder > c.getLong(0))
                out.put("folder", c.getLong(0));
            if (s.lastedit_context > c.getLong(1))
                out.put("context", c.getLong(1));
            if (s.lastedit_task > c.getLong(2))
                out.put("task", c.getLong(2));
            if (s.lastdelete_task > c.getLong(3))
                out.put("task_delete", c.getLong(3));
        } else {
            out.put("folder", Long.valueOf(0));
            out.put("context", Long.valueOf(0));
            out.put("task", Long.valueOf(0));
            out.put("task_delete", Long.valueOf(0));
        }
        return out;
    }

    public void update(ToodledoClient client) throws IOException, NoSuchAlgorithmException, Authenticator.BogusException {
        try {
            conn.beginTransaction();

            Status st = client.getStatus();
            Map<String, Long> flags = updatedSince(st);

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
                for (Folder t : client.getFoldersAfter(flags.get("folder"))) {
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
                for (Context t : client.getContextsAfter(flags.get("context"))) {
                    conn.execSQL(
                        "INSERT OR REPLACE INTO contexts (id, name) VALUES (?,?)",
                        new String[] {
                            String.valueOf(t.id),
                            t.name
                        });
                }
            }

            if (flags.containsKey("task")) {
                for (Task t : client.getTasksAfter(flags.get("task"))) {
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
    }

    public List<Folder> getFolders() {
        List<Folder> ret = new LinkedList<Folder>();
        Cursor c = conn.rawQuery("SELECT id,name,private,archived,ord FROM folders", null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Folder folder = new Folder();
                folder.id = c.getLong(0);
                folder.name = c.getString(1);
                folder.private_ = c.getLong(2);
                folder.archived = c.getLong(3);
                folder.ord = c.getLong(4);
                ret.add(folder);
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public List<Context> getContext() {
        List<Context> ret = new LinkedList<Context>();
        Cursor c = conn.rawQuery("SELECT id,name FROM contexts", null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Context context = new Context();
                context.id = c.getLong(0);
                context.name = c.getString(1);
                ret.add(context);
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public List<Task> getTasks() {
        List<Task> ret = new LinkedList<Task>();
        Cursor c = conn.rawQuery("SELECT id,title,modified,completed,folder,context,priority,star FROM tasks", null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Task task = new Task();
                task.id = c.getLong(0);
                task.title = c.getString(1);
                task.modified = c.getLong(2);
                task.completed = c.getLong(3);
                task.folder = c.getLong(4);
                task.context = c.getLong(5);
                task.priority = c.getLong(6);
                task.star = c.getLong(7);
                task.duedate = c.getLong(8);
                ret.add(task);
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public Folder getFolderById(long id) {
        Cursor c = conn.rawQuery("SELECT id,name,private,archived,ord FROM folders WHERE id=?", new String[] { String.valueOf(id) });
        try {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                Folder folder = new Folder();
                folder.id = c.getLong(0);
                folder.name = c.getString(1);
                folder.private_ = c.getLong(2);
                folder.archived = c.getLong(3);
                folder.ord = c.getLong(4);
                return folder;
            }
            return null;
        } finally {
            c.close();
        }
    }

    public Context getContextById(long id) {
        Cursor c = conn.rawQuery("SELECT id,name FROM contexts WHERE id=?", new String[] { String.valueOf(id) } );
        try {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                Context context = new Context();
                context.id = c.getLong(0);
                context.name = c.getString(1);
                return context;
            }
            return null;
        } finally {
            c.close();
        }
    }

}
