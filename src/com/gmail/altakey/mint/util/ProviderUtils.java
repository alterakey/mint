package com.gmail.altakey.mint.util;

import android.database.sqlite.SQLiteStatement;
import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

public class ProviderUtils {
    public static int bind(final SQLiteStatement stmt, final ContentValues values, final Collection<String> keys) {
        return bind(stmt, values, keys.toArray(new String[0]));
    }

    public static int bind(final SQLiteStatement stmt, final ContentValues values, final String[] keys) {
        int offset = 1;
        for (final String key: keys) {
            final Object value = key != null ? values.get(key) : null;
            if (value != null) {
                final Class<?> type = value.getClass();
                if (type.isAssignableFrom(Long.class)) {
                    stmt.bindLong(offset++, (Long)value);
                } else if (type.isAssignableFrom(String.class)) {
                    stmt.bindString(offset++, (String)value);
                }
            } else {
                stmt.bindNull(offset++);
            }
        }
        return offset;
    }

    public static String expandFilter(final String filter, final String[] args) {
        final List<String> list = new LinkedList<String>();
        for (final String t : args) {
            list.add("?");
        }
        return String.format(filter, TextUtils.join(",", list.toArray(new String[] {})));
    }
}
