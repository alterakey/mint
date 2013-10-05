package com.gmail.altakey.mint;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class NavigationFragment extends ListFragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListAdapter adapter = new SimpleCursorAdapter(
            getActivity(),
            android.R.layout.simple_list_item_1,
            new ContentBuilder().build(),
            new String[] { ContentBuilder.COLUMN_TITLE },
            new int[] { android.R.id.text1 }
            );
        setListAdapter(adapter);
        setListShown(true);
    }

    private static class ContentBuilder {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";

        public Cursor build() {
            final MatrixCursor c = new MatrixCursor(new String[] { COLUMN_ID, COLUMN_TITLE });

            int id = 0;

            for (String title : new String[] { "Status", "Folder", "Context" }) {
                c.addRow(new Object[] { ++id, title });
            }
            return c;
        }
    }

}
