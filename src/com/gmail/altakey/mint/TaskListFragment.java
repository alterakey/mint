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
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, R.id.list_item_title, new ArrayList<String>());
            new TaskListLoadTask(getActivity(), adapter).execute();
            return adapter;
        }
    }

    private static class TaskListLoadTask extends AsyncTask<Void, Void, List<Task>> {
        private ArrayAdapter<String> mmAdapter;
        private Activity mmActivity;
        private Exception mmError;

        public TaskListLoadTask(Activity activity, ArrayAdapter<String> adapter) {
            mmAdapter = adapter;
            mmActivity = activity;
        }

        @Override
        public List<Task> doInBackground(Void... params) {
            try {
                return new ToodledoClient(getAuthenticator(), mmActivity).getTasks();
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
        public void onPostExecute(List<Task> tasks) {
            if (mmError != null) {
                Log.e("TLF", "fetch failure", mmError);
                Toast.makeText(mmActivity, String.format("fetch failure: %s", mmError.getMessage()), Toast.LENGTH_LONG).show();
            } else {
                for (Task t : tasks) {
                    mmAdapter.add(t.title);
                }
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
