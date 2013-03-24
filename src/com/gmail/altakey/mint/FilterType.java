package com.gmail.altakey.mint;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class FilterType {
    public static final int UNKNOWN = -1;
    private static final String[] ALL = { "hotlist", "inbox", "next_action", "reference", "waiting", "someday" };
    private static final String[] TITLES = { "Hotlist", "Inbox", "Next Action", "Reference", "Waiting", "Someday" };

    private static final Map<String, Integer> FOLDER_MAP;
    private String mmFilter;

    static {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("inbox", 0);
        map.put("next_action", 1);
        map.put("reference", 10);
        map.put("waiting", 5);
        map.put("someday", 8);
        FOLDER_MAP = Collections.unmodifiableMap(map);
    }

    public FilterType(String filter) {
        mmFilter = filter;
    }

    public static List<String> getTitles() {
        return Arrays.asList(TITLES);
    }

    public String getTitle() {
        try {
            return TITLES[Arrays.asList(ALL).indexOf(mmFilter)];
        } catch (IndexOutOfBoundsException e) {
            return "?";
        }
    }

    public String getSelection() {
        if ("hotlist".equals(mmFilter)) {
            return TaskProvider.HOTLIST_FILTER;
        } else {
            return null;
        }
    }

    public String[] getSelectionArgs() {
        if ("hotlist".equals(mmFilter)) {
            return new String[] { String.valueOf(new Date(new Date().getTime() + 7 * 86400 * 1000).getTime()) };
        } else {
            return null;
        }
    }

    public int getToodledoStatus() {
        final Integer ret = FOLDER_MAP.get(mmFilter);
        return ret == null ? UNKNOWN : ret.intValue();
    }
}
