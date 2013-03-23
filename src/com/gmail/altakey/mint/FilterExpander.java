package com.gmail.altakey.mint;

import android.text.TextUtils;
import java.util.List;
import java.util.LinkedList;

public class FilterExpander {
    private final String mFilter;
    private final String[] mArgs;

    public FilterExpander(final String filter, final String[] args) {
        mFilter = filter;
        mArgs = args;
    }

    public String expand() {
        final List<String> list = new LinkedList<String>();
        for (final String t : mArgs) {
            list.add("?");
        }
        return String.format(mFilter, TextUtils.join(",", list.toArray(new String[] {})));
    }
}
