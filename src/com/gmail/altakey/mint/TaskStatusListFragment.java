package com.gmail.altakey.mint;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import android.content.Loader;
import android.content.CursorLoader;
import android.app.LoaderManager;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import android.content.AsyncTaskLoader;
import java.io.IOException;

import android.database.CursorWrapper;

public class TaskStatusListFragment extends TaskGroupListFragment {
    @Override
    protected void setupItems(View v) {
        final ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setOnItemClickListener(new ItemClickAction());
    }

    @Override
    protected Loader<Cursor> createCursorLoader() {
        return new CursorLoader(
            getActivity(),
            TaskCountProvider.CONTENT_URI_BY_STATUS,
            TaskCountProvider.PROJECTION,
            null,
            null,
            null
            );
    }

    private class ItemClickAction implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> lv, View v, int pos, long id) {
            final CursorWrapper cw = (CursorWrapper)lv.getItemAtPosition(pos);
            final Intent intent = new Intent(getActivity(), TaskListActivity.class);
            final FilterType filter = new FilterType();
            filter.setTitle(cw.getString(TaskCountProvider.COL_COOKIE));

            // XXX: -1: hotlist
            if (id == -1) {
                filter.setSelection(
                    TaskProvider.HOTLIST_FILTER,
                    new String[] { String.valueOf(new Date(new Date().getTime() + 7 * 86400 * 1000).getTime()) }
                );
            } else {
                filter.setSimpleSelection(FilterType.TYPE_STATUS, cw.getInt(TaskCountProvider.COL_ID));
            }

            intent.putExtra(TaskListActivity.KEY_LIST_FILTER, filter);
            startActivity(intent);
        }
    }
}
