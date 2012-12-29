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

    private static class MainFragment extends ListFragment {
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(new AdapterBuilder(getActivity()).build());
        }

        @Override
        public void onListItemClick(ListView lv, View v, int pos, long id) {
            final String target = (String)getListAdapter().getItem(pos);
            Intent intent = new Intent(getActivity(), TaskListActivity.class);
            startActivity(intent);
        }

        private static class AdapterBuilder {
            public static final String[] TITLES = { "INBOX", "Hotlist", "Next Action", "Reference", "Waiting", "Someday" };
            public static final String[] FILTERS = { "inbox", "hotlist", "next_action", "reference", "waiting", "someday" };

            private android.content.Context mmmContext;

            public AdapterBuilder(android.content.Context ctx) {
                mmmContext = ctx;
            }

            public ListAdapter build() {
                return new ArrayAdapter<String>(mmmContext, android.R.layout.simple_list_item_1, TITLES);
            }
        }
    }
}
