package com.gmail.altakey.mint;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;

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
            setListAdapter(new AdapterBuilder().build());
        }

        private class AdapterBuilder {
            private final String[] mmmTitles = { "INBOX", "Hotlist", "Next Action", "Reference", "Waiting", "Someday" };

            public ListAdapter build() {
                return new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mmmTitles);
            }
        }
    }
}
