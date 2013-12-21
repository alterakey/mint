package com.gmail.altakey.mint.util;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

public class Joiner {
    private String mSeparator;

    private Joiner(String sep) {
        mSeparator = sep;
    }

    public static Joiner on(String sep) {
        return new Joiner(sep);
    }

    public String join(List<?> list) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iter = list.iterator();
        if (iter.hasNext()) {
            builder.append(toString(iter.next()));
            while (iter.hasNext()) {
                builder.append(mSeparator);
                builder.append(toString(iter.next()));
            }
        }
        return builder.toString();
    }

    public String join(Object[] array) {
        return join(Arrays.asList(array));
    }

    private String toString(Object o) {
        return o == null ? "" : o.toString();
    }
}
