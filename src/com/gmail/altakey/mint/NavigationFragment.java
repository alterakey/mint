package com.gmail.altakey.mint;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.widget.ListAdapter;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.content.Loader;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Context;
import android.widget.TextView;

public class NavigationFragment extends ListFragment {
    private CursorAdapter mAdapter;
    private ContentLoaderManipulator mContentLoaderManip = new ContentLoaderManipulator();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new NavigationAdapter(getActivity(), null);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(1, null, mContentLoaderManip);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(1, null, mContentLoaderManip);
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

            final int col_title = TaskCountProvider.COL_COOKIE;

            if (cursor.getColumnIndex(TaskCountProvider.COLUMN_IS_SECTION_HEADER) != -1) {
                section_header.setText(cursor.getString(col_title));
                section_header.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
            } else {
                title.setText(cursor.getString(col_title));
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
                TaskCountProvider.PROJECTION_TOP,
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
