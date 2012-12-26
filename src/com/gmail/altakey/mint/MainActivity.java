package com.gmail.altakey.mint;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.app.ActionBar;
import android.util.Log;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plate);
        setupActionBar();

        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.frag, new TaskListFragment(), TaskListFragment.TAG)
            .commit();
    }

    public void setupActionBar() {
        final ActionBar bar = getActionBar();
        final TaskListFragment.Mode mode = new TaskListFragment.Mode(this);
        bar.setTitle("");
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        SpinnerAdapter adapter = new SimpleAdapter(
            this, mode.getData(),
            android.R.layout.simple_spinner_dropdown_item,
            new String[] {
                "title"
            },
            new int[] {
                android.R.id.text1
            }
        );

        bar.setListNavigationCallbacks(adapter, mode);
    }
}
