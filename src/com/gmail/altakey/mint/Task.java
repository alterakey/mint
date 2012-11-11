package com.gmail.altakey.mint;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class Task {
    public long id;
    public String title;
    public long modified;
    public long completed;
    public long folder;
    public long context;
    public long priority;
    public long star;
    public long duedate;

    public class JsonAdapter extends TypeAdapter<Task> {
        @Override
        public Task read(JsonReader reader) throws IOException {
            final Task task = Task.this;
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("id".equals(name)) {
                    task.id = Long.valueOf(reader.nextString());
                } else if ("title".equals(name)) {
                    task.title = reader.nextString();
                } else if ("modified".equals(name)) {
                    task.modified = Long.valueOf(reader.nextLong());
                } else if ("completed".equals(name)) {
                    task.completed = Long.valueOf(reader.nextString());
                } else if ("folder".equals(name)) {
                    task.folder = Long.valueOf(reader.nextString());
                } else if ("context".equals(name)) {
                    task.context = Long.valueOf(reader.nextString());
                } else if ("priority".equals(name)) {
                    task.priority = Long.valueOf(reader.nextString());
                } else if ("star".equals(name)) {
                    task.star = Long.valueOf(reader.nextString());
                } else if ("duedate".equals(name)) {
                    task.duedate = Long.valueOf(reader.nextString());
                }
            }
            reader.endObject();
            return task;
        }

        @Override
        public void write(JsonWriter writer, Task value) throws IOException {
            final Task task = Task.this;
            writer
                .beginObject()
                .name("id").value(String.valueOf(task.id))
                .name("title").value(task.title)
                .name("modified").value(String.valueOf(task.modified))
                .name("completed").value(String.valueOf(task.completed))
                .name("folder").value(String.valueOf(task.folder))
                .name("context").value(String.valueOf(task.context))
                .name("priority").value(String.valueOf(task.priority))
                .name("star").value(String.valueOf(task.star))
                .name("duedate").value(String.valueOf(task.duedate))
                .endObject();
        }
    }
}
