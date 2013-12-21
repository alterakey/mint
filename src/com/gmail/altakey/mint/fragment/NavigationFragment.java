package com.gmail.altakey.mint.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.CursorWrapper;
import android.widget.ListAdapter;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.content.Loader;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Context;
import android.widget.TextView;
import android.widget.ListView;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;

import com.gmail.altakey.mint.R;
import com.gmail.altakey.mint.provider.TaskCountProvider;
import com.gmail.altakey.mint.util.FilterType;
import com.gmail.altakey.mint.util.LoaderUtil;

public class NavigationFragment extends ListFragment {
    private CursorAdapter mAdapter;
    private ContentLoaderManipulator mContentLoaderManip = new ContentLoaderManipulator();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new NavigationAdapter(getActivity(), null);
        setListAdapter(mAdapter);
        LoaderUtil.initLoader(1, null, mContentLoaderManip, getLoaderManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        LoaderUtil.initLoader(1, null, mContentLoaderManip, getLoaderManager());
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        super.onListItemClick(lv, v, position, id);

        final CursorWrapper cw = (CursorWrapper)lv.getItemAtPosition(position);

        final int type = cw.getInt(TaskCountProvider.COL_TYPE);
        if (type != TaskCountProvider.TYPE_SECTION) {
            final String title = cw.getString(TaskCountProvider.COL_COOKIE);
            final int id_ = cw.getInt(TaskCountProvider.COL_ID);

            final FilterType filter = new FilterType();
            switch (type) {
            case TaskCountProvider.TYPE_STATUS:
                if (id_ == -1) {
                    filter.makeHot();
                } else {
                    filter.setTitle(title);
                    filter.setSimpleSelection(FilterType.TYPE_STATUS, id_);
                }
                break;
            case TaskCountProvider.TYPE_FOLDER:
                filter.setTitle(title);
                filter.setSimpleSelection(FilterType.TYPE_FOLDER, id_);
                break;
            case TaskCountProvider.TYPE_CONTEXT:
                filter.setTitle(title);
                filter.setSimpleSelection(FilterType.TYPE_CONTEXT, id_);
                break;
            }

            try {
                final SlidingMenu menu = ((SlidingActivity)getActivity()).getSlidingMenu();
                menu.toggle();
            } catch (ClassCastException e) {
            }

            getFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, TaskListFragment.newInstance(filter), TaskListFragment.TAG)
                .commit();
        }
    }

    private static class NavigationAdapter extends CursorAdapter {
        public NavigationAdapter(Context context, Cursor c) {
            super(context, c);
        }

        public NavigationAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        public NavigationAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = inflater.inflate(R.layout.nav_item, parent, false);
            bindView(v, context, cursor);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView title = (TextView)view.findViewById(R.id.title);
            final TextView section_header = (TextView)view.findViewById(R.id.section_header);

            final String itemTitle = cursor.getString(TaskCountProvider.COL_COOKIE);
            final int itemType = cursor.getInt(TaskCountProvider.COL_TYPE);

            if (itemType == TaskCountProvider.TYPE_SECTION) {
                section_header.setText(itemTitle);
                section_header.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
            } else {
                title.setText(itemTitle);
                title.setVisibility(View.VISIBLE);
                section_header.setVisibility(View.GONE);
            }
        }
    }

    private class ContentLoaderManipulator implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            setListShown(false);
            return new CursorLoader(
                getActivity(),
                TaskCountProvider.CONTENT_URI_TOP,
                TaskCountProvider.PROJECTION,
                null,
                null,
                null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            mAdapter.changeCursor(data);
            setListShown(true);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.changeCursor(null);
        }
    }
}
