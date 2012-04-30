package com.gmail.altakey.mint;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

public class MainActivity extends ListActivity
{
    private final String[] items = {"item 1", "item 2"};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.list_item_title, items));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
}
