package com.gmail.altakey.mint;

import android.database.Cursor;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class Folder {
    public long id;
    public String name;
    public long private_;
    public long archived;
    public long ord;

    public static Folder fromCursor(Cursor c, int offset) {
        Folder folder = new Folder();
        folder.id = c.getLong(0 + offset);
        folder.name = c.getString(1 + offset);
        folder.private_ = c.getLong(2 + offset);
        folder.archived = c.getLong(3 + offset);
        folder.ord = c.getLong(4 + offset);
        return folder;
    }

    public class JsonAdapter extends TypeAdapter<Folder> {
        @Override
        public Folder read(JsonReader reader) throws IOException {
            final Folder folder = Folder.this;
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("id".equals(name)) {
                    folder.id = Long.valueOf(reader.nextString());
                } else if ("name".equals(name)) {
                    folder.name = reader.nextString();
                } else if ("private".equals(name)) {
                    folder.private_ = Long.valueOf(reader.nextString());
                } else if ("archived".equals(name)) {
                    folder.archived = Long.valueOf(reader.nextString());
                } else if ("ord".equals(name)) {
                    folder.ord = Long.valueOf(reader.nextString());
                }
            }
            reader.endObject();
            return folder;
        }

        @Override
        public void write(JsonWriter writer, Folder value) throws IOException {
            final Folder folder = Folder.this;
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
