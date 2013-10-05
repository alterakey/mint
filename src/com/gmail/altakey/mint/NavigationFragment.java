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

public class NavigationFragment extends ListFragment {
    private CursorAdapter mAdapter;
    private ContentLoaderManipulator mContentLoaderManip = new ContentLoaderManipulator();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new SimpleCursorAdapter(
            getActivity(),
            android.R.layout.simple_list_item_1,
            null,
            new String[] { TaskCountProvider.COLUMN_TITLE },
            new int[] { android.R.id.text1 }
        );
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(1, null, mContentLoaderManip);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(1, null, mContentLoaderManip);
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
