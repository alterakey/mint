package com.gmail.altakey.mint;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.Intent;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;

public class TaskListFragment extends ListFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle("Hotlist");
        setListAdapter(new TaskListAdapterBuilder().build());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
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

    public class TaskListAdapterBuilder {
        public ListAdapter build() {
            final List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
            SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                data,
                R.layout.list_item,
                new String[] { "title", "context_0", "context_1", "context_2", "due", "timer_flag" },
                new int[] { R.id.list_task_title, R.id.list_task_context_0, R.id.list_task_context_1, R.id.list_task_context_2, R.id.list_task_due, R.id.list_task_timer_flag } );
            new TaskListLoadTask(getActivity(), adapter, data).execute();
            return adapter;
        }
    }

    private static class TaskListLoadTask extends AsyncTask<Void, Void, Void> {
        private BaseAdapter mmAdapter;
        private List<Map<String, ?>> mmData;
        private Activity mmActivity;
        private Exception mmError;

        public TaskListLoadTask(Activity activity, BaseAdapter adapter, List<Map<String, ?>> data) {
            mmAdapter = adapter;
            mmData = data;
            mmActivity = activity;
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                for (Task t : new ToodledoClient(getAuthenticator(), mmActivity).getTasks()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("title", t.title);
                    map.put("context_0", "@here");
                    //map.put("context_1", "@here");
                    //map.put("context_2", "@here");
                    map.put("due", "2012-11-11 11:11:11");
                    map.put("timer_flag", "(on)");
                    mmData.add(map);
                }
                return null;
            } catch (IOException e) {
                mmError = e;
                return null;
            } catch (NoSuchAlgorithmException e) {
                mmError = e;
                return null;
            } catch (Authenticator.BogusException e) {
                mmError = e;
                return null;
            }
        }

        @Override
        public void onPostExecute(Void ret) {
            if (mmError != null) {
                Log.e("TLF", "fetch failure", mmError);
                Toast.makeText(mmActivity, String.format("fetch failure: %s", mmError.getMessage()), Toast.LENGTH_LONG).show();
            } else {
                mmAdapter.notifyDataSetChanged();
            }
        }

        private Authenticator getAuthenticator() {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mmActivity);
            String userId = pref.getString("user_id", null);
            String userPassword = pref.getString("user_password", null);
            return new Authenticator(mmActivity, userId, userPassword);
        }
    }

}
