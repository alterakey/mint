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

import android.content.Loader;
import android.content.CursorLoader;
import android.app.LoaderManager;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import android.content.AsyncTaskLoader;
import java.io.IOException;

public abstract class TaskGroupListFragment extends Fragment {
    private TaskCountLoader mTaskCountLoaderManip = new TaskCountLoader();
    private SyncPoker mSyncPokerManip = new SyncPoker(this);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.main, container, false);
        final ListView lv = (ListView) v.findViewById(android.R.id.list);
        setupItems(v);
        setListShown(v, false);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        LoaderUtil.initLoader(0, null, mTaskCountLoaderManip, getLoaderManager());
        LoaderUtil.initLoader(1, null, mSyncPokerManip, getLoaderManager());
    }

    private void setListShown(final boolean shown) {
        setListShown(getView(), shown);
    }

    private void setListShown(final View root, final boolean shown) {
        final View progress = root.findViewById(android.R.id.progress);
        final View container = root.findViewById(R.id.container);
        if (shown) {
            container.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.main_preferences:
            startActivity(new Intent(getActivity(), ConfigActivity.class));
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public class TaskCountLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            setListShown(false);
            return createCursorLoader();
        }

        @Override
        public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
            final ListView lv = (ListView)getView().findViewById(android.R.id.list);
            final ListAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.main_list_item,
                data,
                new String[] { TaskCountProvider.COLUMN_TITLE, TaskCountProvider.COLUMN_COUNT },
                new int[] { android.R.id.text1, R.id.count }
                );
            lv.setAdapter(adapter);
            setListShown(true);
        }

        @Override
        public void onLoaderReset(final Loader<Cursor> loader) {
        }
    }

    protected abstract void setupItems(View root);
    protected abstract Loader<Cursor> createCursorLoader();
}
