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

import android.view.Window;
import java.util.Date;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.plate);

        final FilterType filter = new FilterType().makeHot();

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, TaskListFragment.newInstance(filter), TaskListFragment.TAG)
            .commit();
    }

}
