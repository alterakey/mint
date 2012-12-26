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
        bar.setTitle("");
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        final List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
        Map<String, Object> entry = null;

        entry = new HashMap<String, Object>();
        entry.put("title", "Hotlist");
        entry.put("filter", "hotlist");
        data.add(entry);

        entry = new HashMap<String, Object>();
        entry.put("title", "Inbox");
        entry.put("filter", "inbox");
        data.add(entry);

        entry = new HashMap<String, Object>();
        entry.put("title", "Next Action");
        entry.put("filter", "next_action");
        data.add(entry);

        entry = new HashMap<String, Object>();
        entry.put("title", "Reference");
        entry.put("filter", "reference");
        data.add(entry);

        entry = new HashMap<String, Object>();
        entry.put("title", "Delegated");
        entry.put("filter", "delegated");
        data.add(entry);

        entry = new HashMap<String, Object>();
        entry.put("title", "Someday");
        entry.put("filter", "someday");
        data.add(entry);

        SpinnerAdapter adapter = new SimpleAdapter(
            this, data,
            android.R.layout.simple_spinner_dropdown_item,
            new String[] {
                "title"
            },
            new int[] {
                android.R.id.text1
            }
        );

        bar.setListNavigationCallbacks(
            adapter,
            new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int pos, long id) {
                    Map<String, Object> map = data.get(pos);
                    final String filter = (String)map.get("filter");
                    final TaskListFragment tlf = (TaskListFragment)getSupportFragmentManager().findFragmentByTag(TaskListFragment.TAG);
                    if (tlf != null) {
                        Log.d("MA", String.format("Would set filter to %s", filter));
                    }
                    return true;
                }
            }
        );
    }
}
