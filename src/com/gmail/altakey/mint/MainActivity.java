package com.gmail.altakey.mint;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

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

    public static class MainFragment extends ListFragment {
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
            setListAdapter(new AdapterBuilder(getActivity()).build());
        }

        @Override
        public void onListItemClick(ListView lv, View v, int pos, long id) {
            final String target = (String)getListAdapter().getItem(pos);
            Intent intent = new Intent(getActivity(), TaskListActivity.class);
            intent.putExtra(TaskListActivity.KEY_LIST_FILTER, AdapterBuilder.FILTERS[pos]);
            startActivity(intent);
        }

        private static class AdapterBuilder {
            public static final String[] TITLES = { "INBOX", "Hotlist", "Next Action", "Reference", "Waiting", "Someday" };
            public static final String[] FILTERS = { "inbox", "hotlist", "next_action", "reference", "waiting", "someday" };

            private Context mmmContext;

            public AdapterBuilder(Context ctx) {
                mmmContext = ctx;
            }

            public ListAdapter build() {
                return new ArrayAdapter<String>(mmmContext, android.R.layout.simple_list_item_1, TITLES);
            }
        }
    }
}
