package com.gmail.altakey.mint;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

public class MainActivity extends FragmentActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plate);

        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.frag, new TaskListFragment())
            .commit();
    }
}
