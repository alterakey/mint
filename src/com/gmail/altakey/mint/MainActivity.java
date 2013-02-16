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

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plate);
        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, new MainFragment())
            .commit();
    }

    public static class MainFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.main, container, false);
            final ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setAdapter(new AdapterBuilder(getActivity()).build());
            lv.setOnItemClickListener(new ItemClickAction());
            return v;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.main_preferences:
                startActivity(new Intent(getActivity(), ConfigActivity.class));
                return false;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.main, menu);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
        }

        private class ItemClickAction implements AdapterView.OnItemClickListener {
            @Override
            public void onItemClick(AdapterView<?> lv, View v, int pos, long id) {
                Intent intent = new Intent(getActivity(), TaskListActivity.class);
                intent.putExtra(TaskListActivity.KEY_LIST_FILTER, AdapterBuilder.FILTERS[pos]);
                startActivity(intent);
            }
        }

        private static class AdapterBuilder {
            public static final String[] TITLES = { "INBOX", "Hotlist", "Next Action", "Reference", "Waiting", "Someday" };
            public static final String[] FILTERS = { "inbox", "hotlist", "next_action", "reference", "waiting", "someday" };

            private Context mmmContext;

            public AdapterBuilder(Context ctx) {
                mmmContext = ctx;
            }

            public ListAdapter build() {
                final List<Map<String, ?>> data = new LinkedList<Map<String, ?>>();
                for (String title: TITLES) {
                    final Map<String, Object> column = new HashMap<String, Object>();
                    column.put("title", title);
                    data.add(column);
                }
                return new SimpleAdapter(
                    mmmContext, data, R.layout.main_list_item,
                    new String[] { "title" },
                    new int[] { android.R.id.text1 }
                );
            }
        }
    }
}
