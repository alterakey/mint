package com.gmail.altakey.mint;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

public class MainActivity extends ListActivity
{
    private final String[] items = {"やること #1", "やること＃２", "Item #3", "Important!!! item #4"};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle("Hotlist");
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.list_item_title, items));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
}
