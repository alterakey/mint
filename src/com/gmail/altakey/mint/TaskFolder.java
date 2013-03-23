package com.gmail.altakey.mint;

import android.database.Cursor;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class TaskFolder {
    public long id;
    public String name;
    public long private_;
    public long archived;
    public long ord;

    public static TaskFolder fromCursor(Cursor c, int offset) {
        final TaskFolder folder = new TaskFolder();
        folder.id = c.getLong(offset++);
        folder.name = c.getString(offset++);
        folder.private_ = c.getLong(offset++);
        folder.archived = c.getLong(offset++);
        folder.ord = c.getLong(offset++);
        return folder;
    }

    public boolean isNull() {
        return id == 0;
    }

    public static class JsonAdapter extends TypeAdapter<TaskFolder> {
        @Override
        public TaskFolder read(JsonReader reader) throws IOException {
            final TaskFolder folder = new TaskFolder();
            reader.beginObject();
            while (reader.hasNext()) {
                final String name = reader.nextName();
                final String value = reader.nextString();
                if ("id".equals(name)) {
                    folder.id = Long.valueOf(value);
                } else if ("name".equals(name)) {
                    folder.name = value;
                } else if ("private".equals(name)) {
                    folder.private_ = Long.valueOf(value);
                } else if ("archived".equals(name)) {
                    folder.archived = Long.valueOf(value);
                } else if ("ord".equals(name)) {
                    folder.ord = Long.valueOf(value);
                }
            }
            reader.endObject();
            return folder;
        }

        @Override
        public void write(JsonWriter writer, TaskFolder value) throws IOException {
            final TaskFolder folder = value;
            writer
                .beginObject()
                .name("id").value(String.valueOf(folder.id))
                .name("name").value(folder.name)
                .name("private").value(String.valueOf(folder.private_))
                .name("archived").value(String.valueOf(folder.archived))
                .name("ord").value(String.valueOf(folder.ord))
                .endObject();
        }
    }
}
