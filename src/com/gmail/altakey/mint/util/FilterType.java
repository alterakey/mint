package com.gmail.altakey.mint.util;

import android.os.Parcelable;
import android.os.Parcel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import com.gmail.altakey.mint.provider.TaskProvider;

public class FilterType implements Parcelable {
    public static final int UNKNOWN = -1;
    public static final String TYPE_STATUS = TaskProvider.COLUMN_STATUS;
    public static final String TYPE_FOLDER = TaskProvider.COLUMN_FOLDER;
    public static final String TYPE_CONTEXT = TaskProvider.COLUMN_CONTEXT;

    private String mTitle;
    private String mSelection;
    private String[] mSelectionArgs;

    public static final Parcelable.Creator<FilterType> CREATOR = new Parcelable.Creator<FilterType>() {
        public FilterType createFromParcel(Parcel in) {
            return new FilterType(in);
        }

        public FilterType[] newArray(int size) {
            return new FilterType[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeString(mSelection);
        out.writeStringArray(mSelectionArgs);
    }

    public FilterType() {
    }

    private FilterType(Parcel in) {
        mTitle = in.readString();
        mSelection = in.readString();
        mSelectionArgs = in.createStringArray();
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSelection() {
        return mSelection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public int getToodledoStatus() {
        if (mSelection.startsWith(String.format("%s=?", TYPE_STATUS))) {
            return Integer.parseInt(mSelectionArgs[0]);
        } else {
            return UNKNOWN;
        }
    }

    public FilterType setTitle(String title) {
        mTitle = title;
        return this;
    }

    public FilterType makeHot() {
        setTitle("Hotlist");
        setSelection(
            TaskProvider.HOTLIST_FILTER,
            new String[] { String.valueOf(new Date(new Date().getTime() + 7 * 86400 * 1000).getTime()) }
        );
        return this;
    }

    public FilterType setSimpleSelection(String type, int value) {
        return setSimpleSelection(type, value, false);
    }

    public FilterType setSimpleSelection(String type, int value, boolean includeCompleted) {
        if (includeCompleted) {
            mSelection = String.format("%s=?", type);
        } else {
            mSelection = String.format("%s=? and completed=0", type);
        }
        mSelectionArgs = new String[] { String.valueOf(value) };
        return this;
    }

    public FilterType setSelection(String selection, String[] selectionArgs) {
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        return this;
    }
}
