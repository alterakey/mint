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
import java.util.Date;

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
                    db.execSQL("CREATE TABLE IF NOT EXISTS tasks (task BIGINT PRIMARY KEY, title TEXT, modified BIGINT, completed TEXT, folder BIGINT, context BIGINT, priority INTEGER, star INTEGER, duedate BIGINT)");
                    db.execSQL("CREATE TABLE IF NOT EXISTS folders (folder BIGINT PRIMARY KEY, name TEXT, private TEXT, archived TEXT, ord TEXT)");
                    db.execSQL("CREATE TABLE IF NOT EXISTS contexts (context BIGINT PRIMARY KEY, name TEXT)");
                    db.execSQL("CREATE TABLE IF NOT EXISTS status (status TEXT PRIMARY KEY, lastedit_folder BIGINT, lastedit_context BIGINT, lastedit_goal BIGINT, lastedit_location BIGINT, lastedit_task BIGINT, lastdelete_task BIGINT, lastedit_notebook BIGINT, lastdelete_notebook BIGINT)");
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

            if (flags.containsKey("folder_delete")) {
                for (Folder t : client.getFoldersDeletedAfter(flags.get("folder_delete"))) {
                    conn.execSQL(
                        "DELETE FROM folders WHERE folder=?",
                        new String[] {
                            String.valueOf(t.id)
                        });
                }
            }

            if (flags.containsKey("task_delete")) {
                for (Task t : client.getTasksDeletedAfter(flags.get("task_delete"))) {
                    conn.execSQL(
                        "DELETE FROM tasks WHERE task=?",
                        new String[] {
                            String.valueOf(t.id)
                        });
                }
            }

            if (flags.containsKey("folder")) {
                for (Folder t : client.getFoldersAfter(flags.get("folder"))) {
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

            if (flags.containsKey("context")) {
                for (Context t : client.getContextsAfter(flags.get("context"))) {
                    conn.execSQL(
                        "INSERT OR REPLACE INTO contexts (context, name) VALUES (?,?)",
                        new String[] {
                            String.valueOf(t.id),
                            t.name
                        });
                }
            }

            if (flags.containsKey("task")) {
                for (Task t : client.getTasksAfter(flags.get("task"))) {
                    conn.execSQL(
                        "INSERT OR REPLACE INTO tasks (task, title, modified, completed, folder, context, priority, star, duedate) VALUES (?,?,?,?,?,?,?,?,?)",
                        new String[] {
                            String.valueOf(t.id),
                            t.title,
                            String.valueOf(t.modified),
                            String.valueOf(t.completed),
                            String.valueOf(t.folder),
                            String.valueOf(t.context),
                            String.valueOf(t.priority),
                            String.valueOf(t.star),
                            String.valueOf(t.duedate)
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
        Cursor c = conn.rawQuery("SELECT folder,name,private,archived,ord FROM folders", null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                ret.add(Folder.fromCursor(c, 0));
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public List<Context> getContext() {
        List<Context> ret = new LinkedList<Context>();
        Cursor c = conn.rawQuery("SELECT context,name FROM contexts", null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                ret.add(Context.fromCursor(c, 0));
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    private final String TASK_QUERY = "SELECT task,title,modified,completed,folder,context,priority,star,duedate,folder as folder_id,folders.name as folder_name,folders.private as folder_private,folders.archived as folder_archived,folders.ord as folder_ord,context as context_id,contexts.name as context_name FROM tasks left join folders using (folder) left join contexts using (context) where %s order by %s";
    private final String DEFAULT_ORDER = "duedate,priority desc";
    private final String HOT_FILTER = "(priority=3 or (priority>=0 and duedate>0 and duedate<?)) and completed=0";
    private final String ALL_FILTER = "1=1";

    public List<Task> getTasks(String filter, String order) {
        List<Task> ret = new LinkedList<Task>();
        Cursor c = conn.rawQuery(
            String.format(TASK_QUERY, ALL_FILTER, DEFAULT_ORDER), null);
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Task task = Task.fromCursor(c, 0);
                task.resolved.folder = Folder.fromCursor(c, 9);
                task.resolved.context = Context.fromCursor(c, 14);
                ret.add(task);
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }

    public List<Task> getHotTasks() {
        List<Task> ret = new LinkedList<Task>();
        String due = String.format("%d", (new Date().getTime() + (7 * 86400 * 1000)) / 1000);
        Cursor c = conn.rawQuery(
            String.format(TASK_QUERY, HOT_FILTER, DEFAULT_ORDER), new String[] { due });
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Task task = Task.fromCursor(c, 0);
                task.resolved.folder = Folder.fromCursor(c, 9);
                task.resolved.context = Context.fromCursor(c, 14);
                ret.add(task);
                c.moveToNext();
            }
            return ret;
        } finally {
            c.close();
        }
    }
}
