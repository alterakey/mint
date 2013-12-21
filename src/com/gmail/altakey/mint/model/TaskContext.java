package com.gmail.altakey.mint.model;

import android.database.Cursor;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class TaskContext {
    public static final int COLUMNS = 2;

    public long id;
    public String name;

    public static TaskContext fromCursor(Cursor c, int offset) {
        final TaskContext context = new TaskContext();
        context.id = c.getLong(offset++);
        context.name = c.getString(offset++);
        return context;
    }

    public boolean isNull() {
        return id == 0;
    }

    public static class JsonAdapter extends TypeAdapter<TaskContext> {
        @Override
        public TaskContext read(JsonReader reader) throws IOException {
            final TaskContext context = new TaskContext();
            reader.beginObject();
            while (reader.hasNext()) {
                final String name = reader.nextName();
                final String value = reader.nextString();
                if ("id".equals(name)) {
                    context.id = Long.valueOf(value);
                } else if ("name".equals(name)) {
                    context.name = value;
                }
            }
            reader.endObject();
            return context;
        }

        @Override
        public void write(JsonWriter writer, TaskContext value) throws IOException {
            final TaskContext context = value;
            writer
                .beginObject()
                .name("id").value(String.valueOf(context.id))
                .name("name").value(context.name)
                .endObject();
        }
    }
}
