package com.gmail.altakey.mint;

import android.database.sqlite.SQLiteStatement;
import android.content.ContentValues;
import android.text.TextUtils;
import java.util.List;
import java.util.LinkedList;

public class ProviderUtils {
    public static int bindNullableStrings(final SQLiteStatement stmt, final ContentValues values, final String[] keys) {
        int offset = 1;
        for (final String key: keys) {
            if (key != null) {
                stmt.bindString(offset++, (String)values.get(key));
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
