package com.gmail.altakey.mint;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class Task {
    public String id;
    public String title;
    public long modified;
    public String completed;
    public String folder;
    public String context;
    public String priority;
    public String star;

    public class JsonAdapter extends TypeAdapter<Task> {
        @Override
        public Task read(JsonReader reader) throws IOException {
            final Task task = Task.this;
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("id".equals(name)) {
                    task.id = reader.nextString();
                } else if ("title".equals(name)) {
                    task.title = reader.nextString();
                } else if ("modified".equals(name)) {
                    task.modified = reader.nextLong();
                } else if ("completed".equals(name)) {
                    task.completed = reader.nextString();
                } else if ("folder".equals(name)) {
                    task.folder = reader.nextString();
                } else if ("context".equals(name)) {
                    task.context = reader.nextString();
                } else if ("priority".equals(name)) {
                    task.priority = reader.nextString();
                } else if ("star".equals(name)) {
                    task.star = reader.nextString();
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
                .name("id").value(task.id)
                .name("title").value(task.title)
                .name("modified").value(task.modified)
                .name("completed").value(task.completed)
                .name("folder").value(task.folder)
                .name("context").value(task.context)
                .name("priority").value(task.priority)
                .name("star").value(task.star)
                .endObject();
        }
    }
}
