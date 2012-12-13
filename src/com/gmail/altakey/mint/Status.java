package com.gmail.altakey.mint;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class Status {
    public String id;
    public long lastedit_folder;
    public long lastedit_context;
    public long lastedit_goal;
    public long lastedit_location;
    public long lastedit_task;
    public long lastdelete_task;
    public long lastedit_notebook;
    public long lastdelete_notebook;

    public static class JsonAdapter extends TypeAdapter<Status> {
        @Override
        public Status read(JsonReader reader) throws IOException {
            final Status status = new Status();
            reader.beginObject();
            while (reader.hasNext()) {
                final String name = reader.nextName();
                final String value = reader.nextString();
                if ("userid".equals(name)) {
                    status.id = value;
                } else if ("lastedit_folder".equals(name)) {
                    status.lastedit_folder = Long.valueOf(value);
                } else if ("lastedit_context".equals(name)) {
                    status.lastedit_context = Long.valueOf(value);
                } else if ("lastedit_goal".equals(name)) {
                    status.lastedit_goal = Long.valueOf(value);
                } else if ("lastedit_task".equals(name)) {
                    status.lastedit_task = Long.valueOf(value);
                } else if ("lastdelete_task".equals(name)) {
                    status.lastdelete_task = Long.valueOf(value);
                } else if ("lastedit_notebook".equals(name)) {
                    status.lastedit_notebook = Long.valueOf(value);
                } else if ("lastdelete_notebook".equals(name)) {
                    status.lastdelete_notebook = Long.valueOf(value);
                }
            }
            reader.endObject();
            return status;
        }

        @Override
        public void write(JsonWriter writer, Status value) throws IOException {
            final Status status = value;
            writer
                .beginObject()
                .name("userid").value(status.id)
                .name("lastedit_folder").value(String.valueOf(status.lastedit_folder))
                .name("lastedit_context").value(String.valueOf(status.lastedit_context))
                .name("lastedit_goal").value(String.valueOf(status.lastedit_goal))
                .name("lastedit_task").value(String.valueOf(status.lastedit_task))
                .name("lastdelete_task").value(String.valueOf(status.lastdelete_task))
                .name("lastedit_notebook").value(String.valueOf(status.lastedit_notebook))
                .name("lastdelete_notebook").value(String.valueOf(status.lastdelete_notebook))
                .endObject();
        }
    }
}
