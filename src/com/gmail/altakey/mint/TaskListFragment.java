package com.gmail.altakey.mint;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

public class TaskListFragment extends ListFragment
{
    private final String[] items = {"やること #1", "やること＃２", "Item #3", "Important!!! item #4"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle("Hotlist");
        setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item, R.id.list_item_title, items));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }
}
